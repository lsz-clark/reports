package cn.lunzn.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.model.PlayFenbu;
import cn.lunzn.util.DateUtil;
import cn.lunzn.xiaoyu.dao.MvMenuListDao;
import cn.lunzn.xiaoyu.model.MvMenuList;
import cn.lunzn.xiaoyu.model.MvTopGroup;

/**
 * 点播Pv分布统计业务类
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class PlayFenbuService
{
    /**
     * 分批查询最大值
     */
    private final static int QUERY_CHUNK = 800;
    
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /**
     * 影片信息Dao
     */
    @Autowired
    private MvMenuListDao menuListDao;
    
    /** 
     * 影片PV统计
     * @param statDate 统计日期
     * @return PlayFenbu
     * @see [类、类#方法、类#成员]
     */
    public List<PlayFenbu> statPV(String statDate)
    {
        Map<String, PlayFenbu> pv = new HashMap<String, PlayFenbu>();
        // 统计请求PV
        statPV(statDate, Constant.INTERFACE_LOG_VIDEO_DETAIL, pv, 1);
        // 统计点播PV
        statPV(statDate, Constant.INTERFACE_LOG_VIDEO_PLAY, pv, 2);
        // 统计切换PV
        statPV(statDate, Constant.INTERFACE_LOG_VIDEO_SWITCH, pv, 3);
        
        List<String> mvidAll = new ArrayList<String>();
        mvidAll.addAll(pv.keySet());
        
        int chunk = mvidAll.size() / QUERY_CHUNK;
        if (mvidAll.size() % QUERY_CHUNK > 0)
        {
            chunk++;
        }
        
        // 根据mvid得出影片名称、影片分类名称
        for (int i = 0; i < chunk; i++)
        {
            int a = i * QUERY_CHUNK;
            int b = ((i + 1) * QUERY_CHUNK) > mvidAll.size() ? mvidAll.size() : (i + 1) * QUERY_CHUNK;
            List<String> mvids = mvidAll.subList(a, b);
            List<MvMenuList> mvinfos = menuListDao.findGroups(mvids);
            for (MvMenuList mvinfo : mvinfos)
            {
                pv.get(mvinfo.getMvid()).setMv(mvinfo);
            }
        }
        
        List<PlayFenbu> result = new ArrayList<>();
        result.addAll(pv.values());
        
        /*int total1 = 0;
        int total2 = 0;
        int total3 = 0;
        for (PlayFenbu p : pv.values())
        {
            total1 += p.getPlayTotal();
            total2 += p.getDetailTotal();
            total3 += p.getSwitchTotal();
        }
        System.out.println(total1);
        System.out.println(total2);
        System.out.println(total3);*/
        // 排序，优先播放PV，其次请求PV，最后切换PV
        Collections.sort(result);
        return result;
    }
    
    /** 
     * 影片PV统计
     * @param statDate 统计日期
     * @param interfaceName 接口名称
     * @see [类、类#方法、类#成员]
     */
    private void statPV(String statDate, String interfaceName, Map<String, PlayFenbu> pv, int pvType)
    {
        Client client = esTemplate.getClient();
        
        TermsBuilder agg = AggregationBuilders.terms("A").field(XiaoyuField.A).size(Integer.MAX_VALUE);
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        if (pvType == 2)
        {
            // 点播有多个接口/video/play /video/play2，这里采用前缀匹配方式
            queryBuilder.filter(QueryBuilders.prefixQuery(XiaoyuField.INTERFACE_NAME, interfaceName));
        }
        else
        {
            queryBuilder.filter(QueryBuilders.matchQuery(XiaoyuField.INTERFACE_NAME, interfaceName));
        }
        
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        SearchResponse response = search.setQuery(queryBuilder).addAggregation(agg).execute().actionGet();
        
        StringTerms aggSt = response.getAggregations().get("A");
        
        for (Terms.Bucket mvidB : aggSt.getBuckets())
        {
            String mvid = mvidB.getKeyAsString();
            
            PlayFenbu fenbu = null;
            if (pv.containsKey(mvid))
            {
                fenbu = pv.get(mvid);
            }
            else
            {
                fenbu = new PlayFenbu();
                
                MvMenuList mv = new MvMenuList();
                mv.setMvname(mvid);
                MvTopGroup group = new MvTopGroup();
                group.setGroupname(" ");
                mv.setTopGroup(group);
                
                fenbu.setMv(mv);
                pv.put(mvid, fenbu);
            }
            
            switch (pvType)
            {
                case 1:
                    fenbu.setDetailTotal(mvidB.getDocCount());
                    break;
                case 2:
                    fenbu.setPlayTotal(mvidB.getDocCount());
                    break;
                case 3:
                    fenbu.setSwitchTotal(mvidB.getDocCount());
                    break;
            }
        }
        
    }
}
