package cn.lunzn.service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.util.DateUtil;
import cn.lunzn.xiaoyu.dao.MvReleaseCompanyDao;
import cn.lunzn.xiaoyu.model.MvReleaseCompany;

/**
 * 渠道数据报表
 * 
 * @author  yi.li
 * @version  [版本号, 2017年10月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class VersionOperService
{
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /**
     * 小鱼渠道数据查询
     */
    @Autowired
    private MvReleaseCompanyDao mvReleaseCompanyDao;
    
    /** 
     * 查询所有能兼容小鱼的渠道
     * @return List<MvReleaseCompany>
     * @see [类、类#方法、类#成员]
     */
    public List<MvReleaseCompany> queryCoversions(String date)
    {
        Client client = esTemplate.getClient();
        
        // 分组
        TermsBuilder coversionAgg = AggregationBuilders.terms("coversion");
        // 将coversion字段分组，并查询所有
        coversionAgg.field(XiaoyuField.COVERSION).size(Integer.MAX_VALUE);
        
        TermsBuilder companyAgg = AggregationBuilders.terms("company");
        // 将coversion字段分组，并查询所有
        companyAgg.field(XiaoyuField.COMPANY).size(Integer.MAX_VALUE);
        // 根据company+coversion分组
        companyAgg.subAggregation(coversionAgg);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        queryBuilder.must(
            QueryBuilders.rangeQuery("logdate").format(DateUtil.DATE_FORMAT_SECOND_BAR).lte(DateUtil.endDate(date)));
        
        search.setQuery(queryBuilder)
            .addAggregation(companyAgg)
            .addSort(XiaoyuField.COMPANY, SortOrder.DESC)
            .addSort(XiaoyuField.COVERSION, SortOrder.DESC)
            .setSize(0);
        
        SearchResponse response = search.get();
        
        //  获取聚合数据 
        List<MvReleaseCompany> coversions = new ArrayList<MvReleaseCompany>();
        
        StringTerms companySt = response.getAggregations().get("company");
        for (Terms.Bucket companyTb : companySt.getBuckets())
        {
            StringTerms coversionSt = companyTb.getAggregations().get("coversion");
            for (Terms.Bucket coversionTb : coversionSt.getBuckets())
            {
                MvReleaseCompany bean = new MvReleaseCompany();
                bean.setCompany(companyTb.getKeyAsString());
                bean.setCoversion(coversionTb.getKeyAsString());
                coversions.add(bean);
            }
        }
        
        return coversions;
    }
    
    /** 
     * 根据渠道编号查询渠道名称
     * <功能详细描述>
     * @param coversionId 渠道编号
     * @return String 渠道名称
     * @see [类、类#方法、类#成员]
     */
    public List<MvReleaseCompany> getCoversionName(String date)
    {
        // 根据company+coversion查询渠道名称
        List<MvReleaseCompany> coversionsNames = new ArrayList<MvReleaseCompany>();
        
        // 有使用小鱼的渠道
        List<MvReleaseCompany> coversions = queryCoversions(date);
        
        // 查询所有的渠道
        List<MvReleaseCompany> companys = mvReleaseCompanyDao.find();
        
        if (!companys.isEmpty())
        {
            for (MvReleaseCompany company : coversions)
            {
                for (MvReleaseCompany com : companys)
                {
                    if (company.getCompany().equals(com.getCompany())
                        && company.getCoversion().equals(com.getCoversion()))
                    {
                        // 渠道名称
                        coversionsNames.add(com);
                        break;
                    }
                }
            }
        }
        return coversionsNames;
    }
    
    /** 
     * 统计用户总量
     * <功能详细描述>
     * @param date 统计日期
     * @param param 查询参数
     * @return long
     * @see [类、类#方法、类#成员]
     */
    public long findRegCusumers(String date, Map<String, Object> param)
    {
        Client client = esTemplate.getClient();
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(
            QueryBuilders.rangeQuery("logdate").format(DateUtil.DATE_FORMAT_SECOND_BAR).lte(DateUtil.endDate(date)));
        
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, param.get("company")).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, param.get("coversion")).slop(0));
        
        // uuid去重后统计总注册量
        CardinalityBuilder cardinality =
            AggregationBuilders.cardinality("distinct_uuids").field("uuid").precisionThreshold(Integer.MAX_VALUE);
        search.setQuery(queryBuilder).addAggregation(cardinality).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        // 获取聚合数据 
        //Cardinality agg = response.getAggregations().get("distinct_uuids");
        //Integer value = (int)agg.getValue();
        
        return response.getHits().getTotalHits();
    }
    
    /** 
     * 统计当日激活数量
     * <功能详细描述>
     * @param date 统计日期
     * @param param 查询参数
     * @return Integer
     * @see [类、类#方法、类#成员]
     */
    public Long findDayRegCusumers(String date, Map<String, Object> param)
    {
        // 创建查询对象
        param.put("date", date);
        // 统计日期之前的用户总量
        Long consumerCount = getCusumerByDate(param);
        
        // 统计日期前一天的所有用户注册量
        // 前一天日期
        param.put("date", DateUtil.strDiffDate(date, -1));
        Long beforeCusTotal = getCusumerByDate(param);
        
        // 计算统计日期当天的用户注册量
        Long consumers = consumerCount - beforeCusTotal;
        
        return consumers;
    }
    
    /** 
     * 统计日期前的所有用户注册量
     * <功能详细描述>
     * @param date
     * @param search
     * @return Integer
     * @see [类、类#方法、类#成员]
     */
    private long getCusumerByDate(Map<String, Object> param)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        String date = String.valueOf(param.get("date"));
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(
            QueryBuilders.rangeQuery("logdate").format(DateUtil.DATE_FORMAT_SECOND_BAR).lte(DateUtil.endDate(date)));
        // coversion
        if (param.containsKey("company"))
        {
            queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, param.get("company")).slop(0));
        }
        
        // coversion
        if (param.containsKey("coversion"))
        {
            queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, param.get("coversion")).slop(0));
        }
        
        // uuid去重后统计总注册量
        CardinalityBuilder cardinality =
            AggregationBuilders.cardinality("distinct_uuids").field("uuid").precisionThreshold(Integer.MAX_VALUE);
        search.setQuery(queryBuilder).addAggregation(cardinality).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        // 获取聚合数据 
        //Cardinality agg = response.getAggregations().get("distinct_uuids");
        
        //Integer cusTotal = (int)agg.getValue();
        return response.getHits().getTotalHits();
    }
    
    /** 
     * 统计各个渠道的日活
     * @param param 查询参数
     * @return Integer 统计结果
     * @see [类、类#方法、类#成员]
     */
    public Integer queryDayUserByCoversion(Map<String, Object> param)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        String startDate = String.valueOf(param.get("endDate"));
        String endDate = String.valueOf(param.get("endDate"));
        String company = String.valueOf(param.get("company"));
        String coversion = String.valueOf(param.get("coversion"));
        if (param.containsKey("startDate"))
        {
            startDate = String.valueOf(param.get("startDate"));
        }
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery("logdate")
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(startDate))
            .lte(DateUtil.endDate(endDate)));
        
        // 过滤company
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, company).slop(0));
        // 过滤COVERSION
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, coversion).slop(0));
        
        /*        if (param.containsKey("interfaceName"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, param.get("interfaceName")).slop(0));
        }*/
        
        // 是否有指令类型
        if (param.containsKey("opCommandType"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.OPCOMMAND_TYPE, param.get("opCommandType")).slop(0));
        }
        
        // uuid去重后统计当天日活
        CardinalityBuilder cardinality =
            AggregationBuilders.cardinality("distinct_uuids").field("uuid").precisionThreshold(Integer.MAX_VALUE);
        search.setQuery(queryBuilder).addAggregation(cardinality).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        // 获取聚合数据 
        Cardinality agg = response.getAggregations().get("distinct_uuids");
        
        Integer value = (int)agg.getValue();
        
        return value;
    }
    
    /** 
     * 统计各个渠道的日活
     * @param param 查询参数
     * @return Integer 统计结果
     * @see [类、类#方法、类#成员]
     */
    public Integer queryDayOpersByCoversion(Map<String, Object> param)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        String startDate = String.valueOf(param.get("endDate"));
        String endDate = String.valueOf(param.get("endDate"));
        String company = String.valueOf(param.get("company"));
        String coversion = String.valueOf(param.get("coversion"));
        if (param.containsKey("startDate"))
        {
            startDate = String.valueOf(param.get("startDate"));
        }
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery("logdate")
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(startDate))
            .lte(DateUtil.endDate(endDate)));
        
        // 过滤company
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, company).slop(0));
        // 过滤COVERSION
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, coversion).slop(0));
        
        if (param.containsKey("interfaceName"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, param.get("interfaceName")).slop(0));
        }
        
        // 是否有指令类型
        if (param.containsKey("opCommandType"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.OPCOMMAND_TYPE, param.get("opCommandType")).slop(0));
        }
        
        // uuid去重后统计当天日活
        CardinalityBuilder cardinality =
            AggregationBuilders.cardinality("distinct_uuids").field("uuid").precisionThreshold(Integer.MAX_VALUE);
        search.setQuery(queryBuilder).addAggregation(cardinality).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        // 获取聚合数据 
        Cardinality agg = response.getAggregations().get("distinct_uuids");
        
        Integer value = (int)agg.getValue();
        
        return value;
    }
    
    /** 
     * 统计各个渠道的日PV
     * @param param 查询参数
     * @return Integer 统计结果
     * @see [类、类#方法、类#成员]
     */
    public Integer queryDayPvByCoversion(Map<String, Object> param)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        String startDate = String.valueOf(param.get("endDate"));
        String endDate = String.valueOf(param.get("endDate"));
        String company = String.valueOf(param.get("company"));
        String coversion = String.valueOf(param.get("coversion"));
        if (param.containsKey("startDate"))
        {
            startDate = String.valueOf(param.get("startDate"));
        }
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery("logdate")
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(startDate))
            .lte(DateUtil.endDate(endDate)));
        
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, company).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, coversion).slop(0));
        
        // 是否有接口类型
        if (param.containsKey("interfaceName"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, param.get("interfaceName")).slop(0));
        }
        
        // 是否有指令类型
        if (param.containsKey("opCommandType"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.OPCOMMAND_TYPE, param.get("opCommandType")).slop(0));
        }
        
        // 统计记录条数
        ValueCountBuilder countBuiler = AggregationBuilders.count("count_uuid").field("uuid");
        
        search.setQuery(queryBuilder).addAggregation(countBuiler).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        
        // 获取查询数据
        ValueCount count = response.getAggregations().get("count_uuid");
        
        Integer value = (int)count.getValue();
        
        return value;
    }
    
    /** 
     * 统计各个渠道的留存率
     * @param param 查询参数
     * @return Integer 统计结果
     * @see [类、类#方法、类#成员]
     */
    public String queryRateByCoversion(int days, Map<String, Object> param)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        String startDate = String.valueOf(param.get("endDate"));
        String endDate = String.valueOf(param.get("endDate"));
        String coversion = String.valueOf(param.get("coversion"));
        String company = String.valueOf(param.get("company"));
        if (param.containsKey("startDate"))
        {
            startDate = String.valueOf(param.get("startDate"));
        }
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery("logdate")
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(startDate))
            .lte(DateUtil.endDate(endDate)));
        
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, company).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, coversion).slop(0));
        
        // 是否有接口类型
        if (param.containsKey("interfaceName"))
        {
            queryBuilder
                .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, param.get("interfaceName")).slop(0));
        }
        
        // 分组
        TermsBuilder uuidAgg = AggregationBuilders.terms("uuid");
        // 将coversion字段分组，并查询所有
        uuidAgg.field(XiaoyuField.UUID).size(Integer.MAX_VALUE);
        
        DateHistogramBuilder logdateAgg = AggregationBuilders.dateHistogram("logdate").field(XiaoyuField.LOG_DATE);
        logdateAgg.interval(DateHistogramInterval.DAY);
        // 根据company+coversion分组
        uuidAgg.subAggregation(logdateAgg);
        
        search.setQuery(queryBuilder).addAggregation(uuidAgg).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        
        // N天内都在用小鱼的用户
        Integer rateCount = 0;
        StringTerms uuidSt = response.getAggregations().get("uuid");
        for (Terms.Bucket logdateTb : uuidSt.getBuckets())
        {
            Histogram coversionSt = logdateTb.getAggregations().get("logdate");
            if (days == coversionSt.getBuckets().size())
            {
                rateCount++;
            }
        }
        
        // 查询N天前的日活过滤条件
        Map<String, Object> dayOperMap = new HashMap<String, Object>();
        dayOperMap.put("coversion", coversion);
        dayOperMap.put("company", company);
        dayOperMap.put("startDate", startDate);
        dayOperMap.put("endDate", startDate);
        //dayOperMap.put("interfaceName", Constant.INTERFACE_LOG_REPORT);
        
        // 日活结果
        Integer dayOperCount = queryDayUserByCoversion(dayOperMap);
        DecimalFormat numberFormat = new DecimalFormat("0.00");
        String rate =
            0 == dayOperCount ? "0.00%" : numberFormat.format((double)rateCount / (double)dayOperCount * 100) + "%";
        return rate;
    }
    
    /** 
     * 分析心跳 求平均停留时长
     * <功能详细描述>
     * @param param 查询参数
     * @return String
     * @throws ParseException 时间转换异常
     * @see [类、类#方法、类#成员]
     */
    public String analyseAverageTime(Map<String, Object> param)
        throws ParseException
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        String startDate = String.valueOf(param.get("startDate"));
        String endDate = String.valueOf(param.get("endDate"));
        String coversion = String.valueOf(param.get("coversion"));
        String company = String.valueOf(param.get("company"));
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery("logdate")
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(startDate))
            .lte(DateUtil.endDate(endDate)));
        
        queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, company).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, coversion).slop(0))
            .filter(
                QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_USER_HEART).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.A, "").slop(0));
        
        // 分组
        TermsBuilder uuidAgg = AggregationBuilders.terms("uuid");
        // 将coversion字段分组，并查询所有
        uuidAgg.field(XiaoyuField.UUID).size(Integer.MAX_VALUE);
        
        TermsBuilder logdateAgg = AggregationBuilders.terms("logdate");
        logdateAgg.field(XiaoyuField.LOG_DATE).size(Integer.MAX_VALUE);
        // 根据company+coversion分组
        uuidAgg.subAggregation(logdateAgg);
        
        search.addField(XiaoyuField.UUID).addField(XiaoyuField.LOG_DATE);
        search.setQuery(queryBuilder).addAggregation(uuidAgg).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        // 次数
        long times = 0;
        StringTerms uuidSt = response.getAggregations().get("uuid");
        for (Terms.Bucket companyTb : uuidSt.getBuckets())
        {
            // 记录上个时间点
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateUtil.startDate(startDate)));
            long lastDate = calendar.getTimeInMillis();
            
            LongTerms coversionSt = companyTb.getAggregations().get("logdate");
            for (Terms.Bucket coversionTb : coversionSt.getBuckets())
            {
                long logDate = Long.parseLong(coversionTb.getKeyAsString());
                
                if (isFiveMinInterval(lastDate, logDate))
                {
                    times++;
                }
                
                lastDate = logDate;
            }
        }
        
        // 当天总用户数
        int uuids = uuidSt.getBuckets().size();
        // 时分秒格式
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        // 平均停留时长=所有用户时长/用户总数
        String avgTime = 0 == uuids ? "00:00:00" : String.valueOf(formatter.format((times * 5 * 60 * 1000) / uuids));
        // 毫秒转换成时分秒
        return avgTime;
    }
    
    /**
     * 是否一五分钟间隔
     * @param lastDate 上一次时间戳
     * @param logDate 当前心跳时间戳
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    private boolean isFiveMinInterval(long lastDate, long logDate)
        throws ParseException
    {
        long interval = logDate - lastDate;
        // 业务心跳logdate相差5分钟=300000
        // 防止误差 前后扩大一分钟
        return interval > 240000 && interval < 360000;
    }
}
