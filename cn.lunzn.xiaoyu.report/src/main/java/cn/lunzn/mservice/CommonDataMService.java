/*package cn.lunzn.mservice;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.lunzn.constant.Constant;

*//**
  * 公共历史数据备份到mongodb
  * 
  * @author  clark
  * @version  [版本号, 2017年10月25日]
  * @see  [相关类/方法]
  * @since  [产品/模块版本]
  */
/*
@Service
public class CommonDataMService
{
 *//**
   * apk版本key
   */
/*
private final static String COMMON_APK_VERSION = "apkversions";

*//**
  * mongo操作类
  */
/*
@Autowired
private MongoTemplate mongoTemplate;

*//** 
  * 插入最新版本
  * @param versions apk 版本
  * @see [类、类#方法、类#成员]
  */
/*
public void insertAPPVersion(List<String> versions)
{
 // 获取历史版本
 List<String> historyVersions = this.queryAPPVersion();
 
 boolean isInsert = false;
 if (null == historyVersions || historyVersions.isEmpty())
 {
     isInsert = true;
 }
 
 for (String apkversion : versions)
 {
     if (!historyVersions.contains(apkversion))
     {
         // 添加新版本
         historyVersions.add(apkversion);
     }
 }
 
 JSONArray saveData = JSONArray.parseArray(historyVersions.toString());
 
 // 获取表  
 DBCollection collection = mongoTemplate.getCollection(Constant.COLLECTION_COMMON_DATA);
 
 // 创建
 BasicDBObject iu = new BasicDBObject();
 iu.put(COMMON_APK_VERSION, saveData.toString());
 
 if (isInsert)
 {
     collection.insert(iu);
 }
 else
 {
     BasicDBObject query = new BasicDBObject();
     query.get(COMMON_APK_VERSION);
     
     collection.update(query, iu);
 }
 
}

*//** 
  * 插入最新版本
  * @param versions apk 版本
  * @see [类、类#方法、类#成员]
  *//*
    public List<String> queryAPPVersion()
    {
     List<String> versions = new ArrayList<String>();
     
     // 获取表  
     DBCollection collection = mongoTemplate.getCollection(Constant.COLLECTION_COMMON_DATA);
     
     // 创建查询  
     BasicDBObject basic = new BasicDBObject();
     basic.get(COMMON_APK_VERSION);
     
     DBCursor cursor = collection.find(basic);
     
     while (cursor.hasNext())
     {
         // 打印查询结果  
         DBObject result = cursor.next();
         if (result.containsField(COMMON_APK_VERSION))
         {
             versions.addAll(JSONArray.parseArray(result.get(COMMON_APK_VERSION).toString(), String.class));
             break;
         }
     }
     
     return versions;
    }
    }
    */