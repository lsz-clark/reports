package cn.lunzn.xiaoyu.model;

/**
 * 影片分类表
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class MvTopGroup
{
    private Integer id;
    
    private Integer groupid;
    
    private String groupname;
    
    private Integer index;
    
    private String imgpath;
    
    private Integer state;
    
    private String summary;
    
    public Integer getId()
    {
        return id;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public Integer getGroupid()
    {
        return groupid;
    }
    
    public void setGroupid(Integer groupid)
    {
        this.groupid = groupid;
    }
    
    public String getGroupname()
    {
        return groupname;
    }
    
    public void setGroupname(String groupname)
    {
        this.groupname = groupname;
    }
    
    public Integer getIndex()
    {
        return index;
    }
    
    public void setIndex(Integer index)
    {
        this.index = index;
    }
    
    public String getImgpath()
    {
        return imgpath;
    }
    
    public void setImgpath(String imgpath)
    {
        this.imgpath = imgpath;
    }
    
    public Integer getState()
    {
        return state;
    }
    
    public void setState(Integer state)
    {
        this.state = state;
    }
    
    public String getSummary()
    {
        return summary;
    }
    
    public void setSummary(String summary)
    {
        this.summary = summary;
    }
}
