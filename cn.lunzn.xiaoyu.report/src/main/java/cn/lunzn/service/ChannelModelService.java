package cn.lunzn.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.xiaoyu.dao.MvReleaseCompanyDao;
import cn.lunzn.xiaoyu.model.MvReleaseCompany;

/**
 * 小鱼渠道型号业务类
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class ChannelModelService
{
    /**
     * 备份文件，之前统计的各个渠道终端的型号、内核版本信息
     */
    private final static String BACKUP_FILE_PATCH = "7-xiaoyu-channel-model.json";
    
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(ChannelModelService.class);
    
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /**
     * 小鱼版本
     */
    @Autowired
    private MvReleaseCompanyDao mvReleaseCompanyDao;
    
    /** 
     * 获取各个渠道终端的型号、内核版本
     * @return JSONObject
     * @see [类、类#方法、类#成员]
     */
    public JSONObject getChannelModel()
    {
        // 从备份文件中读取之前统计的各个渠道终端的型号、内核版本信息
        // JSONObject channelModel = this.getBackup();
        JSONObject channelModel = null;
        if (null == channelModel)
        {
            channelModel = new JSONObject();
        }
        Client client = esTemplate.getClient();
        
        // 分组company
        TermsBuilder companyAgg = AggregationBuilders.terms("company");
        // 将company字段分组，并查询所有
        companyAgg.field(XiaoyuField.COMPANY).size(Integer.MAX_VALUE);
        
        // 分组coversion
        TermsBuilder coversionAgg = AggregationBuilders.terms("coversion");
        // 将coversion字段分组，并查询所有
        coversionAgg.field(XiaoyuField.COVERSION).size(Integer.MAX_VALUE);
        
        // 分组deviceModel
        TermsBuilder deviceModelAgg = AggregationBuilders.terms("deviceModel");
        // 将deviceModel字段分组，并查询所有
        deviceModelAgg.field(XiaoyuField.DEVICE_MODEL).size(Integer.MAX_VALUE);
        
        // 分组kernelVsn
        TermsBuilder kernelVsnAgg = AggregationBuilders.terms("kernelVsn");
        // 将kernelVsn字段分组，并查询所有
        kernelVsnAgg.field(XiaoyuField.KERNEL_VSN).size(Integer.MAX_VALUE);
        
        // 分组uuid
        CardinalityBuilder uuidAgg =
            AggregationBuilders.cardinality("uuid").field(XiaoyuField.UUID).precisionThreshold(Integer.MAX_VALUE);
        
        kernelVsnAgg.subAggregation(uuidAgg);
        deviceModelAgg.subAggregation(kernelVsnAgg);
        coversionAgg.subAggregation(deviceModelAgg);
        companyAgg.subAggregation(coversionAgg);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        search.addAggregation(companyAgg).setSize(0);
        
        SearchResponse response = search.get();
        // 获取聚合数据 
        StringTerms companySt = response.getAggregations().get("company");
        for (Terms.Bucket companyTb : companySt.getBuckets())
        {
            StringTerms coversionSt = companyTb.getAggregations().get("coversion");
            for (Terms.Bucket coversionTb : coversionSt.getBuckets())
            {
                String channelName = companyTb.getKeyAsString() + coversionTb.getKeyAsString();
                
                MvReleaseCompany param = new MvReleaseCompany();
                param.setCompany(companyTb.getKeyAsString());
                param.setCoversion(coversionTb.getKeyAsString());
                MvReleaseCompany qudaoVo = mvReleaseCompanyDao.findOne(param);
                if (null != qudaoVo)
                {
                    channelName = qudaoVo.getSummary();
                }
                
                // 渠道下的各个终端型号
                JSONObject deviceModelJson = getJSONObject(channelModel, channelName);
                StringTerms deviceModelSt = coversionTb.getAggregations().get("deviceModel");
                for (Terms.Bucket deviceModelTb : deviceModelSt.getBuckets())
                {
                    // 终端内核版本
                    JSONArray kernelVsnJson = getJSONArray(deviceModelJson, deviceModelTb.getKeyAsString());
                    StringTerms kernelVsnSt = deviceModelTb.getAggregations().get("kernelVsn");
                    for (Terms.Bucket kernelVsnTb : kernelVsnSt.getBuckets())
                    {
                        boolean flag = true;
                        if (!kernelVsnJson.isEmpty())
                        {
                            for (Object infoObj : kernelVsnJson)
                            {
                                JSONObject info = JSONObject.parseObject(infoObj.toString());
                                if (info.getString("kernelVsn").equals(kernelVsnTb.getKeyAsString()))
                                {
                                    flag = false;
                                }
                            }
                        }
                        
                        if (flag)
                        {
                            Cardinality count = kernelVsnTb.getAggregations().get("uuid");
                            JSONObject info1 = new JSONObject();
                            info1.put("kernelVsn", kernelVsnTb.getKeyAsString());
                            info1.put("count", count.getValue());
                            kernelVsnJson.add(info1);
                        }
                    }
                }
            }
        }
        
        // 写入备份
        // setBackup(channelModel.toString());
        return channelModel;
    }
    
    private JSONObject getJSONObject(JSONObject source, String key)
    {
        JSONObject result = source.getJSONObject(key);
        if (null == result)
        {
            result = new JSONObject();
            source.put(key, result);
        }
        
        return result;
    }
    
    private JSONArray getJSONArray(JSONObject source, String key)
    {
        JSONArray result = source.getJSONArray(key);
        if (null == result)
        {
            result = new JSONArray();
            source.put(key, result);
        }
        
        return result;
    }
    
    /** 
     * 写入最新的渠道型号信息
     * @param content 渠道型号信息
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("all")
    private void setBackup(String content)
    {
        FileOutputStream o = null;
        try
        {
            o = new FileOutputStream(new File(Constant.BACKUP_PATCH + BACKUP_FILE_PATCH));
            o.write(content.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            logger.error("Write '" + Constant.BACKUP_PATCH + BACKUP_FILE_PATCH + "' failed", e);
        }
        finally
        {
            if (null != o)
            {
                try
                {
                    o.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
    
    /** 
     * 之前统计的各个渠道终端的型号、内核版本信息
     * @return JSONObject
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("all")
    private JSONObject getBackup()
    {
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        try
        {
            fileInputStream = new FileInputStream(Constant.BACKUP_PATCH + BACKUP_FILE_PATCH);
            inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            
            // 构造一个BufferedReader类来读取文件
            br = new BufferedReader(inputStreamReader);
            String s = null;
            while ((s = br.readLine()) != null)
            {
                // 使用readLine方法，一次读一行
                result.append(s);
            }
        }
        catch (Exception e)
        {
            logger.error("Read '" + Constant.BACKUP_PATCH + BACKUP_FILE_PATCH + "' failed", e);
            return null;
        }
        finally
        {
            try
            {
                if (null != br)
                {
                    br.close();
                }
                
                if (null != inputStreamReader)
                {
                    inputStreamReader.close();
                }
                
                if (null != fileInputStream)
                {
                    fileInputStream.close();
                }
            }
            catch (IOException e)
            {
            }
        }
        
        String backup = result.toString();
        if (StringUtils.isNotEmpty(backup))
        {
            return JSONObject.parseObject(backup);
        }
        return null;
    }
}
