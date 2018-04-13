package cn.lunzn.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 配置文件帮助类
 * 
 * @author  clark
 * @version  [版本号, 2017年9月4日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PropertyCache
{
    /**
     * 存储所有配置信息
     */
    private static JSONObject properties = new JSONObject();
    
    /**
     * 日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(PropertyCache.class);
    
    /** 
     * 受保护的构造函数，静态类无需共有构造函数
     */
    protected PropertyCache()
    {
        
    }
    
    /** 
     * 封装
     * @return JSONObject
     * @see [类、类#方法、类#成员]
     */
    public static JSONObject getProp()
    {
        return properties;
    }
    
    /**
     * 初始化配置文件，并保存到内存中
     * @throws Exception 异常
     */
    public static void initPoperty()
        throws Exception
    {
        Properties propFlies = new Properties();
        
        // 请求地址配置文件流
        InputStream endpointIs = null;
        // 上下文配置文件流
        InputStream contextIs = null;
        try
        {
            endpointIs = PropertyCache.class.getResourceAsStream("/context.properties");
            
            // 请求地址
            propFlies.load(endpointIs);
            
            Set<Map.Entry<Object, Object>> props = propFlies.entrySet();
            for (Map.Entry<Object, Object> prop : props)
            {
                properties.put(prop.getKey().toString(), prop.getValue());
            }
            propFlies.clear();
        }
        finally
        {
            propFlies.clear();
            if (null != endpointIs)
            {
                try
                {
                    endpointIs.close();
                }
                catch (IOException e)
                {
                    logger.error("Close endpoint.properties stream exception...");
                }
            }
            
            if (null != contextIs)
            {
                try
                {
                    contextIs.close();
                }
                catch (IOException e)
                {
                    logger.error("Close context.properties stream exception...");
                }
            }
        }
    }
}
