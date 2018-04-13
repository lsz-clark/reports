package cn.lunzn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import cn.lunzn.constant.Constant;

/**
 * 记录用户
 * 
 * @author  clark
 * @version  [版本号, 2017年9月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Document(indexName = Constant.ES_XIAOYU_STAT_INDEX, type = "user", refreshInterval = "-1")
public class User
{
    @Id
    private String id;
    
    /**
     * 用户唯一标识
     */
    private String uuid;
    
    /**
     * 用户 渠道1
     */
    private String company;
    
    /**
     * 用户 渠道2
     */
    private String coversion;
    
    /**
     * 版本
     */
    private String appversion;
    
    /**
     * 用户注册时间
     */
    private String logdate;
    
    /**
     * 用户ip
     */
    private String ip;
    
    /**
     * 终端型号
     */
    private String deviceModel;
    
    /**
     * 终端内核版本
     */
    private String kernelVsn;
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getUuid()
    {
        return uuid;
    }
    
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
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
    
    public String getAppversion()
    {
        return appversion;
    }
    
    public void setAppversion(String appversion)
    {
        this.appversion = appversion;
    }
    
    public String getLogdate()
    {
        return logdate;
    }
    
    public void setLogdate(String logdate)
    {
        this.logdate = logdate;
    }
    
    public String getIp()
    {
        return ip;
    }
    
    public void setIp(String ip)
    {
        this.ip = ip;
    }
    
    public String getDeviceModel()
    {
        return deviceModel;
    }
    
    public void setDeviceModel(String deviceModel)
    {
        this.deviceModel = deviceModel;
    }
    
    public String getKernelVsn()
    {
        return kernelVsn;
    }
    
    public void setKernelVsn(String kernelVsn)
    {
        this.kernelVsn = kernelVsn;
    }
    
    public boolean equals(Object obj)
    {
        if (obj instanceof User)
        {
            User u = (User)obj;
            return this.uuid.equals(u.uuid)/* && this.company.equals(u.company)
                                           && this.coversion.equals(u.coversion)*/;
        }
        return super.equals(obj);
    }
}
