package cn.lunzn.xiaoyu.model;

/**
 * 渠道版本信息表
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class MvReleaseCompany
{
    private long id;
    
    private String company;
    
    private String coversion;
    
    private String version;
    
    private String account;
    
    private String password;
    
    private int amount;
    
    private int access;
    
    private int cpuid;
    
    private String urlpass;
    
    private String summary;
    
    private int state;
    
    private long remote;
    
    private int versionflag;
    
    private int offline;
    
    private int forward;
    
    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
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
    
    public String getVersion()
    {
        return version;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    public String getAccount()
    {
        return account;
    }
    
    public void setAccount(String account)
    {
        this.account = account;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public int getAmount()
    {
        return amount;
    }
    
    public void setAmount(int amount)
    {
        this.amount = amount;
    }
    
    public int getAccess()
    {
        return access;
    }
    
    public void setAccess(int access)
    {
        this.access = access;
    }
    
    public int getCpuid()
    {
        return cpuid;
    }
    
    public void setCpuid(int cpuid)
    {
        this.cpuid = cpuid;
    }
    
    public String getUrlpass()
    {
        return urlpass;
    }
    
    public void setUrlpass(String urlpass)
    {
        this.urlpass = urlpass;
    }
    
    public String getSummary()
    {
        return summary;
    }
    
    public void setSummary(String summary)
    {
        this.summary = summary;
    }
    
    public int getState()
    {
        return state;
    }
    
    public void setState(int state)
    {
        this.state = state;
    }
    
    public long getRemote()
    {
        return remote;
    }
    
    public void setRemote(long remote)
    {
        this.remote = remote;
    }
    
    public int getVersionflag()
    {
        return versionflag;
    }
    
    public void setVersionflag(int versionflag)
    {
        this.versionflag = versionflag;
    }
    
    public int getOffline()
    {
        return offline;
    }
    
    public void setOffline(int offline)
    {
        this.offline = offline;
    }
    
    public int getForward()
    {
        return forward;
    }
    
    public void setForward(int forward)
    {
        this.forward = forward;
    }
}
