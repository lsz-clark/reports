package cn.lunzn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import cn.lunzn.constant.Constant;

/**
 * 记录每一天的唯一用户（语音操作优先）
 * 
 * @author  clark
 * @version  [版本号, 2017年11月15日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Document(indexName = Constant.ES_XIAOYU_STAT_INDEX, type = "dayuser", refreshInterval = "-1")
public class DayUser
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
    private long opCommandType;
    
    /**
     * 用户注册时间
     */
    private String logdate;
    
    /**
     * 用户ip
     */
    private String ip;
    
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
    
    public long getOpCommandType()
    {
        return opCommandType;
    }
    
    public void setOpCommandType(long opCommandType)
    {
        this.opCommandType = opCommandType;
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
    
    public boolean equals(Object obj)
    {
        if (obj instanceof DayUser)
        {
            DayUser u = (DayUser)obj;
            return this.uuid.equals(u.uuid)/* && this.company.equals(u.company)
                                           && this.coversion.equals(u.coversion)*/;
        }
        return super.equals(obj);
    }
}
