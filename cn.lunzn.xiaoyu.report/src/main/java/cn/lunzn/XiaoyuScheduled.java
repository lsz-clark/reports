package cn.lunzn;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cn.lunzn.constant.Constant;
import cn.lunzn.model.Report;
import cn.lunzn.report.ChannelModelReport;
import cn.lunzn.report.PlayFenbuReport;
import cn.lunzn.report.RegQuantityReport;
import cn.lunzn.report.SumFormReport;
import cn.lunzn.report.VersionFenbuReport;
import cn.lunzn.report.VersionOperReport;
import cn.lunzn.report.XiaoyuFenbuReport;
import cn.lunzn.service.CommonService;
import cn.lunzn.util.DateUtil;

/**
 * 定时任务
 * 
 * @author  clark
 * @version  [版本号, 2017年8月21日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class XiaoyuScheduled
{
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(XiaoyuScheduled.class);
    
    /**
     * 1、版本用户量分布-报表
     */
    @Autowired
    private VersionFenbuReport fenbuReport;
    
    /**
     * 2、点播PV分布-报表
     */
    @Autowired
    private PlayFenbuReport playFenbuReport;
    
    /**
     * 3、小鱼PV分布-报表
     */
    @Autowired
    private XiaoyuFenbuReport xiaoyuFenbuReport;
    
    /**
     * 4、小鱼数据报表-汇总表单
     */
    @Autowired
    private SumFormReport sumFormReport;
    
    /**
     * 公共业务
     */
    @Autowired
    private CommonService commonService;
    
    /**
     * ES-数据源
     */
    /*@Autowired
    private ElasticsearchTemplate esTemplate;*/
    
    /**
     * 渠道注册量汇总
    */
    @Autowired
    private RegQuantityReport regQuantityReport;
    
    /* 渠道注册量汇总
    */
    @Autowired
    private VersionOperReport versionOperReport;
    
    /**
     * 7、小鱼渠道型号
     */
    @Autowired
    private ChannelModelReport channelModelReport;
    
    /** 
     * 开始统计小鱼日志
     * @param start 统计日期
     * @throws Exception 异常
     * @see [类、类#方法、类#成员]
     */
    public int startupXiaoyuReport(Calendar start)
        throws Exception
    {
        // 报表目录
        File statPath = createStatPath(start);
        
        // 需要统计的日期
        String statDate = DateUtil.formatDateToString(DateUtil.DATE_FORMAT_DAY_BAR, start.getTime());
        // 统计当日用户总量
        commonService.statTodayRegCusumers(statDate);
        // 记录每天用户（去重，语音优先）
        commonService.statTodayUser(statDate);
        
        // 1、版本用户量分布
        fenbuReport.buildExcel(statDate, statPath);
        
        // 2、点播PV分布
        playFenbuReport.buildExcel(statDate, statPath);
        
        // 3、小鱼PV分布
        xiaoyuFenbuReport.buildExcel(statDate, statPath);
        
        // 4、小鱼数据报表_汇总表单
        sumFormReport.buildExcel(statDate, statPath);
        
        // 5、小鱼数据报表_xxx
        versionOperReport.buildExcel(statDate, statPath);
        
        // 6、小鱼数据报表_注册量汇总
        regQuantityReport.buildExcel(statDate, statPath);
        
        // 7、小鱼渠道型号 -- 无需测试报表
        channelModelReport.buildExcel(statDate, statPath);
        
        // 发送邮件通知相关负责人
        // sendFlag = commonService.sendEmail(statDate, statPath.getPath());
        return commonService.sendEmailZip(statDate, statPath.getPath());
    }
    
    /** 
     * 每天上午【8点】发送报表
     * @see [类、类#方法、类#成员]
     */
    @Scheduled(cron = "0 0 8 * * ?")
    // @Scheduled(cron = "0/10 * * * * ?")
    public void xiaoyuReport()
    {
        /*DeleteQuery dq = new DeleteQuery();
        dq.setIndex(Constant.ES_XIAOYU_INDEX);
        dq.setType(Constant.ES_XIAOYU_TYPE_REPORT);
        dq.setQuery(QueryBuilders.matchAllQuery());
        esTemplate.delete(dq, Report.class);
        
        Report report1 = new Report();
        report1.setBuildFlag(1);
        report1.setSendFlag(1);
        report1.setStatDate("2017-10-31");
        report1.setReportFile("");
        commonService.insertReport(report1);*/
        
        logger.info("[Scheduled] begin generate a report ...");
        // 报表统计标识
        int buildFlag = Constant.SUCCESS;
        // 报表邮件发送标识
        int sendFlag = Constant.SUCCESS;
        // 报表打包存放路径
        String reportFile = "";
        
        // 统计开始时间，默认两个时间相等
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -1);
        
        // 统计截止时间
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, -1);
        try
        {
            Report report = commonService.queryRecentReport();
            // 如果最近没有生成报表，则补上
            if (null != report)
            {
                Date statDate = DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_DAY_BAR, report.getStatDate());
                Calendar compare = Calendar.getInstance();
                compare.setTime(statDate);
                // 如果那天报表生成失败，或是停机导致未统计
                if (report.getBuildFlag() == Constant.FAILED
                    || (end.get(Calendar.DAY_OF_YEAR) - 1) != compare.get(Calendar.DAY_OF_YEAR))
                {
                    start.setTime(statDate);
                }
            }
            
            while (end.get(Calendar.DAY_OF_YEAR) >= start.get(Calendar.DAY_OF_YEAR))
            {
                sendFlag = startupXiaoyuReport(start);
                // 循环结束因子累计
                start.add(Calendar.DATE, 1);
            }
        }
        catch (Exception e)
        {
            logger.error("[Scheduled] build report failed", e);
            // 报表生成失败
            buildFlag = Constant.FAILED;
        }
        
        // 记录当天统计情况
        Report report = new Report();
        report.setBuildFlag(buildFlag);
        report.setSendFlag(sendFlag);
        report.setStatDate(DateUtil.formatDateToString(DateUtil.DATE_FORMAT_DAY_BAR, end.getTime()));
        report.setReportFile(reportFile);
        commonService.insertReport(report);
        
        logger.info("[Scheduled] end generate a report...");
    }
    
    /** 
     * 每天中午12点清理32天之前的日志
     * @see [类、类#方法、类#成员]
     */
    @Scheduled(cron = "0 0 15 * * ?")
    public void clearLog()
    {
        logger.info("[Scheduled] begin stat clear ES logs ...");
        
        commonService.clearLog();
        
        logger.info("[Scheduled] end stat clear ES logs ...");
    }
    
    /** 
     * 每天凌晨【1点】统计昨天uuid注册情况
     * @see [类、类#方法、类#成员]
     */
    /*@Scheduled(cron = "0 0 1 * * ?")
    // @Scheduled(cron = "0/10 * * * * ?")
    public void uuidStatDay()
    {
        logger.info("[Scheduled] begin stat uuid ...");
        
        // 统计昨天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        
        String statDate = DateUtil.formatDateToString(DateUtil.DATE_FORMAT_DAY_BAR, calendar.getTime());
        
        commonService.uuidStatDay(statDate);
        
        logger.info("[Scheduled] end stat uuid ...");
    }*/
    
    /** 
     * 获取指定月目录，如果目录不存在则创建
     * 
     * @param calendar 日历
     * @return File
     * @see [类、类#方法、类#成员]
     */
    private static File createStatPath(Calendar calendar)
    {
        // 当前年月
        int year = calendar.get(Calendar.YEAR);
        
        // 创建 本年 文件夹
        File thisYear = new File(Constant.REPORT_SAVE_PATCH + year);
        if (!thisYear.exists())
        {
            thisYear.mkdirs();
        }
        
        // 创建 本月 文件夹
        File thisMonth =
            new File(Constant.REPORT_SAVE_PATCH + year + File.separator + (calendar.get(Calendar.MONTH) + 1));
        if (!thisMonth.exists())
        {
            thisMonth.mkdirs();
        }
        
        return thisMonth;
    }
}
