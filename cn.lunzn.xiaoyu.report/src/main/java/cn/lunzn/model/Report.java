package cn.lunzn.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import cn.lunzn.constant.Constant;

/**
 * 记录每天报表生成信息
 * 
 * @author  clark
 * @version  [版本号, 2017年9月30日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Document(indexName = Constant.ES_XIAOYU_STAT_INDEX, type = "report", refreshInterval = "-1")
public class Report
{
    @Id
    private String id;
    
    /**
     * 报表统计时间（默认统计前一天”小鱼日志“）
     */
    private String statDate;
    
    /**
     * 生成报表标识
     */
    private int buildFlag;
    
    /**
     * 发送报表时间
     */
    private String sendDate;
    
    /**
     * 发送报表标识
     */
    private int sendFlag;
    
    /**
     * 报表文件归档路径
     */
    private String reportFile;
    
    public String getStatDate()
    {
        return statDate;
    }
    
    public void setStatDate(String statDate)
    {
        this.statDate = statDate;
    }
    
    public int getBuildFlag()
    {
        return buildFlag;
    }
    
    public void setBuildFlag(int buildFlag)
    {
        this.buildFlag = buildFlag;
    }
    
    public String getSendDate()
    {
        return sendDate;
    }
    
    public void setSendDate(String sendDate)
    {
        this.sendDate = sendDate;
    }
    
    public int getSendFlag()
    {
        return sendFlag;
    }
    
    public void setSendFlag(int sendFlag)
    {
        this.sendFlag = sendFlag;
    }
    
    public String getReportFile()
    {
        return reportFile;
    }
    
    public void setReportFile(String reportFile)
    {
        this.reportFile = reportFile;
    }
}
