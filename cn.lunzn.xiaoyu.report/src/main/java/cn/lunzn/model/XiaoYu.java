package cn.lunzn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import cn.lunzn.constant.Constant;

/**
 * 对应小鱼日志json
 * 
 * @author  clark
 * @version  [版本号, 2017年9月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Document(indexName = Constant.ES_XIAOYU_INDEX, type = "log", refreshInterval = "-1")
public class XiaoYu
{
    @Id
    private String id;
    
    /**
     * 接口名称
     */
    private String interfaceName;
    
    /**
     * 用户唯一标识
     */
    private String uuid;
    
    /**
     * 不知道
     */
    private String sdkversion;
    
    /**
     * APK版本
     */
    private String appversion;
    
    /**
     * 渠道 one
     */
    private String company;
    
    /**
     * 渠道 two
     */
    private String coversion;
    
    /**
     * 详细信息
     */
    private String data;
    
    /**
     * 日志产生时间
     */
    private String logdate;
    
    public String getInterfaceName()
    {
        return interfaceName;
    }
    
    public void setInterfaceName(String interfaceName)
    {
        this.interfaceName = interfaceName;
    }
    
    public String getUuid()
    {
        return uuid;
    }
    
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
    
    public String getSdkversion()
    {
        return sdkversion;
    }
    
    public void setSdkversion(String sdkversion)
    {
        this.sdkversion = sdkversion;
    }
    
    public String getAppversion()
    {
        return appversion;
    }
    
    public void setAppversion(String appversion)
    {
        this.appversion = appversion;
    }
    
    public String getCompany()
    {
        return company;
    }
    
    public void setCompany(String company)
    {
        this.company = company;
    }
    
    public String getCoversion()
    {
        return coversion;
    }
    
    public void setCoversion(String coversion)
    {
        this.coversion = coversion;
    }
    
    public String getData()
    {
        return data;
    }
    
    public void setData(String data)
    {
        this.data = data;
    }
    
    public String getLogdate()
    {
        return logdate;
    }
    
    public void setLogdate(String logdate)
    {
        this.logdate = logdate;
    }
}
