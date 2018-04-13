/*package cn.lunzn.mservice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

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
public class VersionUsersFenbuMService
{
 *//**
   * mongo操作类
   */
/*
@Autowired
private MongoTemplate mongoTemplate;

*//** 
  * 插入最新版本
  * @param versionPV 版本注册量
  * @see [类、类#方法、类#成员]
  *//*
    public void insertVersionReg(String statDate, Map<String, Long> versionPV)
    {
     BasicDBObject saveData0 = new BasicDBObject("logdate", statDate);
     
     JSONObject data = JSONObject.parseObject(JSON.toJSON(versionPV).toString());
     
     saveData0.put("value", data.toString());
     
     DBCollection collection = mongoTemplate.getCollection("version_reg_fenbu");
     
     collection.insert(saveData0);
    }
    
    
    }
    */