package cn.lunzn.service;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.util.DateUtil;

/**
 * 版本用户量分布业务类
 * 
 * @author  clark
 * @version  [版本号, 2017年9月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class VersionFenbuService
{
    
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /*@Autowired
    private VersionUsersFenbuMService versionUsersFenbuMService;*/
    
    /** 
     * 所有版本当日总注册量
     * 【备份表】
     * @param statDate 统计日期
     * @return long
     * @see [类、类#方法、类#成员]
     */
    public Map<String, Long> countVersionReg(String statDate)
    {
        Map<String, Long> versionPV = new HashMap<String, Long>();
        
        Client client = esTemplate.getClient();
        // Tips:将版本+uuid分组，并仅查询当日之前的log数据，求出每个版本的用户量
        TermsBuilder appversionAgg = AggregationBuilders.terms("appversion").field(XiaoyuField.APP_VERSION);
        CardinalityBuilder uuidAgg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        appversionAgg.subAggregation(uuidAgg).size(Integer.MAX_VALUE);
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER).setSize(0);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(statDate)));
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(appversionAgg).execute().actionGet();
        
        StringTerms aggAppversion = response.getAggregations().get("appversion");
        
        for (Terms.Bucket appversion : aggAppversion.getBuckets())
        {
            Cardinality agg = appversion.getAggregations().get("uuid");
            
            versionPV.put(appversion.getKeyAsString(), agg.getValue());
        }
        
        // versionUsersFenbuMService.insertVersionReg(statDate, versionPV);
        
        return versionPV;
    }
    
    /** 
     * 所有版本日活跃量
     * @param statDate 统计日期
     * @return long
     * @see [类、类#方法、类#成员]
     */
    public Map<String, Long> countVersionActive(String statDate)
    {
        Map<String, Long> versionPV = new HashMap<String, Long>();
        
        Client client = esTemplate.getClient();
        // Tips:将版本+uuid分组，并仅查询当日+"/user/log_report"接口的log数据，求出每个版本的用户活跃度
        TermsBuilder appversionAgg = AggregationBuilders.terms("appversion").field(XiaoyuField.APP_VERSION);
        CardinalityBuilder uuidAgg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        appversionAgg.subAggregation(uuidAgg).size(Integer.MAX_VALUE);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        /*JSONObject matchContent = new JSONObject();
        matchContent.put("query", "/user/log_report");
        matchContent.put("minimum_should_match", "100%");*/
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(appversionAgg).execute().actionGet();
        
        StringTerms aggAppversion = response.getAggregations().get("appversion");
        
        for (Terms.Bucket appversion : aggAppversion.getBuckets())
        {
            Cardinality agg = appversion.getAggregations().get("uuid");
            
            versionPV.put(appversion.getKeyAsString(), agg.getValue());
        }
        return versionPV;
    }
    
}
