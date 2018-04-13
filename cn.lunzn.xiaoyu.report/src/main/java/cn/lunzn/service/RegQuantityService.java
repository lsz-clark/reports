package cn.lunzn.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
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
 * 渠道注册量汇总
 * 
 * @author  yi.li
 * @version  [版本号, 2017年10月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class RegQuantityService
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
     * <功能详细描述>
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
            for (MvReleaseCompany coversion : coversions)
            {
                for (MvReleaseCompany com : companys)
                {
                    if (coversion.getCompany().equals(com.getCompany())
                        && coversion.getCoversion().equals(com.getCoversion()))
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
     * @return long
     * @see [类、类#方法、类#成员]
     */
    public long findRegCusumers(String date)
    {
        Client client = esTemplate.getClient();
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(
            QueryBuilders.rangeQuery("logdate").format(DateUtil.DATE_FORMAT_SECOND_BAR).lte(DateUtil.endDate(date)));
        
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
     * 统计当日用户总量
     * <功能详细描述>
     * @param date 统计日期
     * @return Integer
     * @see [类、类#方法、类#成员]
     */
    public Long findDayRegCusumers(String date)
    {
        // 创建查询对象
        Map<String, Object> param = new HashMap<String, Object>();
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
        
        // 根据
        if (param.containsKey("company"))
        {
            queryBuilder.filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, param.get("company")).slop(0));
        }
        
        // 根据
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
     * 根据渠道统计注册量
     * @param date 统计日期
     * @param param 其他参数
     * @return Integer
     * @see [类、类#方法、类#成员]
     */
    public Long queryCusumerByCoversion(String date, Map<String, Object> param)
    {
        param.put("date", date);
        
        Long consumerCount = getCusumerByDate(param);
        
        // 统计日期前一天的渠道所有用户注册量
        param.put("date", DateUtil.strDiffDate(date, -1));
        
        Long beforeCusTotal = getCusumerByDate(param);
        
        // 计算统计日期当天的渠道用户注册量
        Long consumers = consumerCount - beforeCusTotal;
        return consumers;
    }
    
    /** 
     * 统计各个渠道的日活
     * @param date 统计日期
     * @param param 查询参数
     * @return Integer 统计结果
     * @see [类、类#方法、类#成员]
     */
    public Integer queryDayOpersByCoversion(String date, Map<String, Object> param)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery("logdate")
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(date))
            .lte(DateUtil.endDate(date)));
        
        // 有操作日志的用户
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COMPANY, param.get("company")).slop(0))
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.COVERSION, param.get("coversion")).slop(0));
        
        // uuid去重后统计当天日活
        CardinalityBuilder cardinality =
            AggregationBuilders.cardinality("distinct_uuids").field("uuid").precisionThreshold(Integer.MAX_VALUE);
        search.setQuery(queryBuilder).addAggregation(cardinality).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.execute().actionGet();
        // 获取聚合数据 
        Cardinality agg = response.getAggregations().get("distinct_uuids");
        
        Integer cusTotal = (int)agg.getValue();
        
        return cusTotal;
    }
}
