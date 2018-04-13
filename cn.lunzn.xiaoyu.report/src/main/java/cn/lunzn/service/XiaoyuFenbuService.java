package cn.lunzn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.WinId;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.util.DateUtil;
import cn.lunzn.util.KeyValue;
import cn.lunzn.xiaoyu.dao.MvTopGroupDao;
import cn.lunzn.xiaoyu.model.MvTopGroup;

/**
 * 小鱼PV分布
 * 
 * @author  clark
 * @version  [版本号, 2017年10月17日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class XiaoyuFenbuService
{
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /**
     * 影片分类
     */
    @Autowired
    private MvTopGroupDao topGroupDao;
    
    /**
     * 公共业务类
     */
    @Autowired
    private CommonService commonService;
    
    /**
     * 汇总表表业务类
     */
    @Autowired
    private SumFormService sumFormService;
    
    /** 
     * 日PV汇总
     * @param statDate 统计时间
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public Long totalPV(String statDate)
    {
        Long total = commonService.dayPV(statDate);
        return total;
    }
    
    /** 
     * PV占比
     * @param statDate 统计时间
     * @return Map<String, Long>
     * @see [类、类#方法、类#成员]
     */
    public Map<String, Long> ratePV(String statDate)
    {
        Map<String, Long> ratePV = new HashMap<String, Long>();
        
        // 直播PV
        Long tvlivePv = 0L;
        // 回看PV
        Long repeatPv = 0L;
        
        // =============================直播区域==============================
        // 直播聚合
        StringTerms tvliveSt =
            this.interfacePrefixPv(statDate, Constant.INTERFACE_LOG_TVLIVE_PREFIX, XiaoyuField.INTERFACE_NAME);
        
        for (Terms.Bucket sub : tvliveSt.getBuckets())
        {
            tvlivePv = tvlivePv + sub.getDocCount();
        }
        
        // =============================回看区域==============================
        // 回看聚合
        StringTerms repeatSt =
            this.interfacePrefixPv(statDate, Constant.INTERFACE_LOG_REPEAT_PREFIX, XiaoyuField.INTERFACE_NAME);
        
        for (Terms.Bucket sub : repeatSt.getBuckets())
        {
            repeatPv = repeatPv + sub.getDocCount();
        }
        ratePV.put("直播", tvlivePv);
        ratePV.put("回看", repeatPv);
        
        // =============================影片分类区域==============================
        // 查询小鱼数据库，获取所有影片分类
        List<MvTopGroup> tops = topGroupDao.find(new MvTopGroup());
        
        // test
        /*MvTopGroup tgroup = new MvTopGroup();
        tgroup.setGroupid(10101);
        tgroup.setGroupname("aaaa");
        tops.add(tgroup);*/
        
        // 移除多余分类，即直播、回看
        for (MvTopGroup top : tops)
        {
            if (!"直播".equals(top.getGroupname()) && !"回看".equals(top.getGroupname()))
            {
                ratePV.put(top.getGroupname(), 0L);
            }
        }
        
        // 影片分类聚合
        StringTerms videoGroupSt = this.interfacePrefixPv(statDate, Constant.INTERFACE_LOG_VIDEO_PREFIX, XiaoyuField.B);
        for (Terms.Bucket sub : videoGroupSt.getBuckets())
        {
            if (StringUtils.isEmpty(sub.getKeyAsString()))
            {
                continue;
            }
            
            Integer groupId = Integer.parseInt(sub.getKeyAsString());
            for (MvTopGroup top : tops)
            {
                if (groupId.equals(top.getGroupid()))
                {
                    ratePV.put(top.getGroupname(), sub.getDocCount());
                    break;
                }
            }
        }
        
        return ratePV;
    }
    
    /** 
     * 界面PV
     * @param statDate 统计时间
     * @return List<KeyValue<Long>>
     * @see [类、类#方法、类#成员]
     */
    public List<KeyValue<Long>> winsPV(String statDate)
    {
        List<KeyValue<Long>> winsPV = WinId.getWins();
        
        StringTerms allST = this.interfaceAllPV(statDate);
        StringTerms winsPVST = this.userReportWinsPV(statDate);
        
        // 所有接口PV
        Map<String, Long> allPV = new HashMap<String, Long>();
        for (Terms.Bucket sub : allST.getBuckets())
        {
            allPV.put(sub.getKeyAsString(), sub.getDocCount());
        }
        
        // 所有界面PV
        Map<String, Long> winsPVMap = new HashMap<String, Long>();
        for (Terms.Bucket sub : winsPVST.getBuckets())
        {
            winsPVMap.put(sub.getKeyAsString(), sub.getDocCount());
        }
        
        for (KeyValue<Long> pv : winsPV)
        {
            switch (pv.getKey())
            {
                case -1999:
                    // 接口PV-心跳
                    Long value = allPV.get(Constant.INTERFACE_LOG_USER_HEART);
                    value = null == value ? 0L : value;
                    pv.setValue(value);
                    break;
                case -1998:
                    // 接口PV-用户操作
                    value = allPV.get(Constant.INTERFACE_LOG_REPORT);
                    value = null == value ? 0L : value;
                    pv.setValue(value);
                    break;
                case -1997:
                    // 接口PV-升级
                    value = allPV.get(Constant.INTERFACE_LOG_SYS_UPGRADE);
                    value = null == value ? 0L : value;
                    
                    // 剔除日开机导致多余升级接口
                    value = value - sumFormService.dayBoot(statDate);
                    value = value < 0 ? 0L : value;
                    
                    pv.setValue(value);
                    break;
                case -1996:
                    // 接口PV-上报错误
                    value = allPV.get(Constant.INTERFACE_LOG_ERROR_REPORT);
                    value = null == value ? 0L : value;
                    pv.setValue(value);
                    break;
                case -1995:
                    // 接口PV-登录
                    value = allPV.get(Constant.INTERFACE_LOG_USER_LOGIN);
                    value = null == value ? 0L : value;
                    pv.setValue(value);
                    break;
                default:
                    // 所有界面PV
                    String key = String.valueOf(pv.getKey());
                    value = winsPVMap.get(key);
                    if (null != value)
                    {
                        pv.setValue(value);
                    }
                    break;
            }
        }
        
        return winsPV;
    }
    
    /** 
     * 所有请求分时，按升序排序，即00:00-24:00
     * @param statDate 统计时间
     * @return Map<String, Long>
     * @see [类、类#方法、类#成员]
     */
    public Map<String, Long> hoursPV(String statDate)
    {
        Map<String, Long> hours = new HashMap<String, Long>(24);
        
        Histogram logdatehist = this.datePv(statDate, DateHistogramInterval.HOUR);
        
        if (CollectionUtils.isNotEmpty(logdatehist.getBuckets()))
        {
            for (Histogram.Bucket b : logdatehist.getBuckets())
            {
                hours.put(b.getKeyAsString(), b.getDocCount());
            }
        }
        
        return hours;
    }
    
    /** 
     * 根据指定的接口前缀，获取聚合结果
     * @param statDate 统计日期
     * @param interfacePrefix 接口前缀名
     * @param fieldName 需要聚合的字段名
     * @return StringTerms 聚合结果
     * @see [类、类#方法、类#成员]
     */
    private StringTerms interfacePrefixPv(String statDate, String interfacePrefix, String fieldName)
    {
        Client client = esTemplate.getClient();
        
        TermsBuilder agg = AggregationBuilders.terms(fieldName).field(fieldName).size(Integer.MAX_VALUE);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 仅查询当天 日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        // 前缀匹配
        queryBuilder.filter(QueryBuilders.prefixQuery(XiaoyuField.INTERFACE_NAME, interfacePrefix));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        return response.getAggregations().get(fieldName);
    }
    
    /** 
     * 获取当天所有接口的PV
     * @param statDate 统计时间
     * @return StringTerms
     * @see [类、类#方法、类#成员]
     */
    private StringTerms interfaceAllPV(String statDate)
    {
        Client client = esTemplate.getClient();
        
        TermsBuilder agg =
            AggregationBuilders.terms("interfaceName").field(XiaoyuField.INTERFACE_NAME).size(Integer.MAX_VALUE);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 仅查询当天 日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        return response.getAggregations().get("interfaceName");
    }
    
    /** 
     * 统计界面PV
     * @param statDate 统计时间
     * @return StringTerms
     * @see [类、类#方法、类#成员]
     */
    private StringTerms userReportWinsPV(String statDate)
    {
        Client client = esTemplate.getClient();
        
        TermsBuilder agg = AggregationBuilders.terms("opToWin").field(XiaoyuField.OP_TO_WIN).size(Integer.MAX_VALUE);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 仅查询当天 操作日志
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).get();
        
        return response.getAggregations().get("opToWin");
    }
    
    /** 
     * 根据时间统计
     * @param statDate 统计时间
     * @param interval 日期统计因子
     * @return Histogram
     * @see [类、类#方法、类#成员]
     */
    private Histogram datePv(String statDate, DateHistogramInterval interval)
    {
        Client client = esTemplate.getClient();
        DateHistogramBuilder logdateAgg = AggregationBuilders.dateHistogram("logdate").field(XiaoyuField.LOG_DATE);
        logdateAgg.interval(interval);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(logdateAgg).get();
        
        return response.getAggregations().get("logdate");
    }
}
