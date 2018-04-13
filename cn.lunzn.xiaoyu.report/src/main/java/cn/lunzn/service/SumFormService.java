package cn.lunzn.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.util.DateUtil;

/**
 * 汇总表单业务类
 * 
 * @author  clark
 * @version  [版本号, 2017年10月23日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class SumFormService
{
    
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /**
     * 公共业务类
     */
    @Autowired
    private CommonService commonService;
    
    /** 
     * 日升级下载
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayUpgrade(String statDate)
    {
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_NGINX_INDEX).setTypes(Constant.ES_NGINX_TYPE_NGINX).setSize(500000);
        // 统计count字段求总数
        /*SumBuilder aggregation = AggregationBuilders.sum("count").field("count");*/
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 匹配小鱼apk
        queryBuilder.filter(QueryBuilders.wildcardQuery("filepath", "xiaoyu*.apk"));
        // 小鱼软件下载，在秀控类型下
        queryBuilder.filter(QueryBuilders.matchPhraseQuery("type", "showcome").slop(0));
        // 当天日志
        queryBuilder.filter(QueryBuilders.matchPhraseQuery("day", statDate).slop(0));
        
        SearchResponse response = search.setQuery(queryBuilder).get();
        SearchHits hits = response.getHits();
        long sum = 0L;
        if (null != hits && ArrayUtils.isNotEmpty(hits.getHits()))
        {
            for (SearchHit hit : hits)
            {
                JSONObject jo = JSONObject.parseObject(hit.getSourceAsString());
                
                sum = sum + jo.getIntValue("count");
            }
        }
        /*Sum sum = response.getAggregations().get("count");*/
        
        // return new Double(sum.getValue()).longValue();
        return sum;
    }
    
    /** 
     * 日活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayActive(String statDate)
    {
        return active(statDate, 1);
    }
    
    /** 
     * 周活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long weekActive(String statDate)
    {
        return active(statDate, 2);
    }
    
    /** 
     * 月活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long monthActive(String statDate)
    {
        return active(statDate, 3);
    }
    
    /** 
     * 日开机
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayBoot(String statDate)
    {
        Client client = esTemplate.getClient();
        
        CardinalityBuilder agg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 仅查询当天 日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        // 前缀匹配
        // queryBuilder.filter(QueryBuilders.prefixQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_USER_HEART));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        Cardinality c = response.getAggregations().get("uuid");
        
        return c.getValue();
    }
    
    /** 
     * 语音日活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayVoice(String statDate)
    {
        return voiceActive(statDate, 1);
    }
    
    /** 
     * 语音周活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long weekVoice(String statDate)
    {
        return voiceActive(statDate, 2);
    }
    
    /** 
     * 语音月活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long monthVoice(String statDate)
    {
        return voiceActive(statDate, 3);
    }
    
    /** 
     * 日激活
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayUserActive(String statDate)
    {
        Client client = esTemplate.getClient();
        CardinalityBuilder agg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER).setSize(0);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 昨日+昨日前日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(DateUtil.strDiffDate(statDate, -1))));
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        // Cardinality c = response.getAggregations().get("uuid");
        
        // 今日总注册量  - 昨日总注册量  = 今日注册量
        Long todayUserActive = userTotal(statDate) - response.getHits().getTotalHits();
        
        return todayUserActive;
    }
    
    /** 
     * 用户总量
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long userTotal(String statDate)
    {
        Client client = esTemplate.getClient();
        CardinalityBuilder agg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER).setSize(0);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 当日+当日以前日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(statDate)));
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        // Cardinality c = response.getAggregations().get("uuid");
        
        // return c.getValue();
        return response.getHits().getTotalHits();
    }
    
    /** 
     * 次日留存率
     * @param statDate 统计日期
     * @return String
     * @see [类、类#方法、类#成员]
     */
    public String days2(String statDate)
    {
        return getRetentionRate(statDate, 1);
    }
    
    /** 
     * 3日留存率
     * @param statDate 统计日期
     * @return String
     * @see [类、类#方法、类#成员]
     */
    public String days3(String statDate)
    {
        return getRetentionRate(statDate, 2);
    }
    
    /** 
     * 7日留存率
     * @param statDate 统计日期
     * @return String
     * @see [类、类#方法、类#成员]
     */
    public String days7(String statDate)
    {
        return getRetentionRate(statDate, 3);
    }
    
    /** 
     * 30日留存率
     * @param statDate 统计日期
     * @return String
     * @see [类、类#方法、类#成员]
     */
    public String days30(String statDate)
    {
        return getRetentionRate(statDate, 4);
    }
    
    /** 
     * 日PV
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayPV(String statDate)
    {
        return commonService.dayPV(statDate);
    }
    
    /** 
     * 日语音交互次数
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long dayVoiceCount(String statDate)
    {
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 当日日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        // 匹配操作日志中的opCommandType = 1
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));
        // 语音操作
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.OPCOMMAND_TYPE, Constant.OPCOMMAND_TYPE_1).slop(0));
        
        SearchResponse response = search.setQuery(queryBuilder).get();
        
        return response.getHits().getTotalHits();
    }
    
    public static void main(String[] args)
    {
        //{"key":1510871643000,"key_as_string":"2017-11-16 22:34:03","doc_count":1},{"key":1510872197000,"key_as_string":"2017-11-16 22:43:17","doc_count":1}
        
        System.out.println(1510872197000L - 1510871643000L);
        
        Calendar ctime = Calendar.getInstance();
        
        //ctime.setTimeInMillis(1510838579000l);
        
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        System.out.println(formatter.format((5 * 5 * 60 * 1000)));
        
        System.out.println(DateUtil.formatDateToString(DateUtil.DATE_FORMAT_SECOND_BAR, ctime.getTime()));
    }
    
    /** 
     * 分析心跳 求平均停留时长
     * @param statDate 开始时间
     * @return String 
     * @see [类、类#方法、类#成员]
     */
    public String analyseAverageTime(String statDate)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        queryBuilder
            .filter(
                QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_USER_HEART).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.A, "").slop(0));
        
        /*queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, "XIAOYU").slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, "W004").slop(0));*/
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
        SearchResponse response = search.get();
        
        // 次数
        long times = 0;
        StringTerms uuidSt = response.getAggregations().get("uuid");
        for (Terms.Bucket companyTb : uuidSt.getBuckets())
        {
            // 记录上个时间点
            Calendar calendar = Calendar.getInstance();
            calendar
                .setTime(DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_SECOND_BAR, DateUtil.startDate(statDate)));
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
    {
        long interval = logDate - lastDate;
        // 业务心跳logdate相差5分钟=300000
        // 防止误差 前后扩大一分钟
        return interval > 240000 && interval < 360000;
    }
    
    /** 
     * 按天、按周、按月 计算活跃
     * @param statDate 统计日期
     * @param type 统计时长
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    private Long active(String statDate, int type)
    {
        Client client = esTemplate.getClient();
        
        CardinalityBuilder agg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        if (type == 1)
        {
            // 当日
            queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
                .format(DateUtil.DATE_FORMAT_SECOND_BAR)
                .gte(DateUtil.startDate(statDate))
                .lte(DateUtil.endDate(statDate)));
        }
        else if (type == 2)
        {
            String weekFirstDay = DateUtil.strDiffDate(statDate, -6);
            
            // 本周
            queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
                .format(DateUtil.DATE_FORMAT_SECOND_BAR)
                .gte(DateUtil.startDate(weekFirstDay))
                .lte(DateUtil.endDate(statDate)));
        }
        else
        {
            // 本月
            String monthFirstDay = statDate.substring(0, 8) + "01";
            
            queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
                .format(DateUtil.DATE_FORMAT_SECOND_BAR)
                .gte(DateUtil.startDate(monthFirstDay))
                .lte(DateUtil.endDate(statDate)));
        }
        
        // 匹配操作日志
        /*queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));*/
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        Cardinality c = response.getAggregations().get("uuid");
        
        return c.getValue();
    }
    
    /** 
     * 按天、按周、按月 计算语音活跃
     * @param statDate 统计日期
     * @param type 统计时长
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    private Long voiceActive(String statDate, int type)
    {
        Client client = esTemplate.getClient();
        
        CardinalityBuilder agg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        if (type == 1)
        {
            // 当日
            queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
                .format(DateUtil.DATE_FORMAT_SECOND_BAR)
                .gte(DateUtil.startDate(statDate))
                .lte(DateUtil.endDate(statDate)));
        }
        else if (type == 2)
        {
            String weekFirstDay = DateUtil.strDiffDate(statDate, -6);
            
            // 本周
            queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
                .format(DateUtil.DATE_FORMAT_SECOND_BAR)
                .gte(DateUtil.startDate(weekFirstDay))
                .lte(DateUtil.endDate(statDate)));
        }
        else
        {
            // 本月
            String monthFirstDay = statDate.substring(0, 8) + "01";
            
            queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
                .format(DateUtil.DATE_FORMAT_SECOND_BAR)
                .gte(DateUtil.startDate(monthFirstDay))
                .lte(DateUtil.endDate(statDate)));
        }
        
        // 匹配操作日志中的opCommandType = 1
        /*queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));*/
        // 语音操作
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.OPCOMMAND_TYPE, Constant.OPCOMMAND_TYPE_1).slop(0));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        Cardinality c = response.getAggregations().get("uuid");
        
        return c.getValue();
    }
    
    /** 
     * 计算各个天数的用户留存率
     * @param statDate 统计日期
     * @param type 留存天数
     * @return String
     * @see [类、类#方法、类#成员]
     */
    private String getRetentionRate(String statDate, int type)
    {
        String firstDate = statDate;
        if (type == 1)
        {
            // 次日留存
            firstDate = DateUtil.strDiffDate(statDate, -1);
        }
        else if (type == 2)
        {
            // 3日留存
            firstDate = DateUtil.strDiffDate(statDate, -2);
        }
        else if (type == 3)
        {
            // 7日留存
            firstDate = DateUtil.strDiffDate(statDate, -6);
        }
        else
        {
            // 30日留存
            firstDate = DateUtil.strDiffDate(statDate, -29);
        }
        
        Client client = esTemplate.getClient();
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(firstDate))
            .lte(DateUtil.endDate(firstDate)));
        /*queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));*/
        
        // 分组
        CardinalityBuilder uuidAgg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(uuidAgg).setSize(0).get();
        // 当日总用户数
        Cardinality uuidCT = response.getAggregations().get("uuid");
        
        // 留存个数
        Long retentionCount = getRetentionCount(statDate, type);
        
        return getRate(retentionCount, uuidCT.getValue());
    }
    
    /** 
     * 用户留存个数
     * @param statDate 统计日期
     * @param type 留存天数
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    private Long getRetentionCount(String statDate, int type)
    {
        String startDate = statDate;
        String endDate = DateUtil.endDate(statDate);
        int typeDays = 2;
        if (type == 1)
        {
            // 次日留存
            startDate = DateUtil.startDate(DateUtil.strDiffDate(statDate, -1));
        }
        else if (type == 2)
        {
            // 3日留存
            startDate = DateUtil.startDate(DateUtil.strDiffDate(statDate, -2));
            typeDays = 3;
        }
        else if (type == 3)
        {
            // 7日留存
            startDate = DateUtil.startDate(DateUtil.strDiffDate(statDate, -6));
            typeDays = 7;
        }
        else
        {
            // 30日留存
            startDate = DateUtil.startDate(DateUtil.strDiffDate(statDate, -29));
            typeDays = 30;
        }
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(startDate)
            .lte(endDate));
        /*queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));*/
        
        // 分组
        TermsBuilder uuidAgg = AggregationBuilders.terms("uuid");
        // 将uuid字段分组，并查询所有
        uuidAgg.field(XiaoyuField.UUID).size(Integer.MAX_VALUE);
        
        DateHistogramBuilder logdateAgg = AggregationBuilders.dateHistogram("logdate").field(XiaoyuField.LOG_DATE);
        logdateAgg.interval(DateHistogramInterval.DAY);
        
        // 根据uuid+logdate分组
        uuidAgg.subAggregation(logdateAgg);
        
        // 搜索响应对象
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(uuidAgg).setSize(0).get();
        StringTerms uuidSt = response.getAggregations().get("uuid");
        
        Long uuidCount = 0L;
        for (Terms.Bucket uuidTb : uuidSt.getBuckets())
        {
            Histogram logdateSt = uuidTb.getAggregations().get("logdate");
            
            if (logdateSt.getBuckets().size() == typeDays)
            {
                uuidCount++;
            }
        }
        return uuidCount;
    }
    
    /** 
     * 求比率
     * @param divisor 除数
     * @param dividend 被除数
     * @return String
     * @see [类、类#方法、类#成员]
     */
    private String getRate(Long divisor, Long dividend)
    {
        String rate = "0.00%";
        
        if (divisor <= 0 || dividend <= 0)
        {
            return rate;
        }
        
        BigDecimal divisorB = new BigDecimal(divisor);
        BigDecimal dividendB = new BigDecimal(dividend);
        
        BigDecimal rateB = divisorB.divide(dividendB, 6, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2,
            RoundingMode.HALF_UP);
        
        if (rateB.doubleValue() <= 0)
        {
            rate = "0.00%";
        }
        else
        {
            rate = rateB.toString() + "%";
        }
        
        return rate;
    }
}