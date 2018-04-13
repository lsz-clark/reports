package cn.lunzn.controller;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.lunzn.XiaoyuScheduled;
import cn.lunzn.constant.Constant;
import cn.lunzn.constant.ResultCodeJson;
import cn.lunzn.model.Report;
import cn.lunzn.service.CommonService;
import cn.lunzn.util.DateUtil;

/**
 * 首页跳转
 * @author clark
 */
@Controller
@RequestMapping("/")
public class IndexController
{
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(IndexController.class);
    
    /** 
     * 主页
     * @return String
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping("/")
    public String gohome()
    {
        return "redirect:/portal/home.html";
    }
    
    /**
     * 触发报表统计服务
     */
    @Autowired
    private XiaoyuScheduled xiaoyuScheduled;
    
    /**
     * 公共业务
     */
    @Autowired
    private CommonService commonService;
    
    /** 
     * 通过get方式立即触发 报表统计
     * @return String
     * @see [类、类#方法、类#成员]
     */
    @RequestMapping(value = "/report/startup", method = RequestMethod.GET)
    @ResponseBody
    public String triggerReport(HttpServletRequest request, HttpServletResponse response)
    {
        if ("192.168.30.170".equals(request.getRemoteHost()))
        {
            logger.info(request.getRemoteHost() + "，未知操作者...");
            return "黑名单...";
        }
        
        String date = request.getParameter("date");
        
        Calendar start = Calendar.getInstance();
        start.setTime(DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_DAY_BAR, date));
        try
        {
            // 触发统计
            xiaoyuScheduled.startupXiaoyuReport(start);
            
            // 记录当天统计情况
            Report report = new Report();
            report.setBuildFlag(Constant.SUCCESS);
            report.setSendFlag(Constant.SUCCESS);
            report.setStatDate(date);
            report.setReportFile("");
            commonService.insertReport(report);
        }
        catch (Exception ex)
        {
            logger.error("Startup report failed...", ex);
        }
        
        logger.info("[Manual trigger] [{}] Report builder success...", request.getRemoteHost());
        // xiaoyuScheduled.xiaoyuReport();
        return ResultCodeJson.getSuccess().toString();
    }
}
