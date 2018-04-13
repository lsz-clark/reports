package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.lunzn.constant.Constant;
import cn.lunzn.service.VersionOperService;
import cn.lunzn.util.DateUtil;
import cn.lunzn.util.ExcelCustomize;
import cn.lunzn.xiaoyu.model.MvReleaseCompany;

/**
 * 渠道用户使用详情数据
 * 
 * @author  yi.li
 * @version  [版本号, 2017年10月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class VersionOperReport
{
    /**
     * 日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(VersionOperReport.class);
    
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "小鱼各渠道详情";
    
    /**
     * 注册量汇总
     */
    @Autowired
    private VersionOperService versionOperService;
    
    /** 
     * 生成excel报表
     * <功能详细描述>
     * @param statDate
     * @throws Exception
     * @see [类、类#方法、类#成员]
     */
    public void buildExcel(String statDate, File statPath)
        throws Exception
    {
        FileInputStream inputStream = null;
        HSSFWorkbook workbook = null;
        // 报表存放位置
        String reportFilePath = statPath.getPath() + File.separator + EXCEL_NAME + ".xls";
        
        File thisMonthReport = new File(reportFilePath);
        // 判断本月文件是否存在
        if (thisMonthReport.exists())
        {
            // 如果文件存在则读取
            inputStream = new FileInputStream(thisMonthReport);
            workbook = new HSSFWorkbook(inputStream);
        }
        else
        {
            // 第一步创建workbook  
            workbook = new HSSFWorkbook();
        }
        // 根据渠道生成不同的报表
        List<MvReleaseCompany> list = refreshSheetTitle(workbook, statDate);
        Map<String, Object> param = null;
        for (int i = 0; i < list.size(); i++)
        {
            MvReleaseCompany coversion = list.get(i);
            HSSFSheet sheet = workbook.getSheet(coversion.getSummary());
            // 新增渠道则在工作薄后面追加新的渠道详情数据sheet页
            if (null == sheet)
            {
                // 第二步创建sheet  
                sheet = ExcelCustomize.getCommonSheet(workbook, coversion.getSummary());
                
                // 第三步创建行row:添加表头0行  
                HSSFRow row = sheet.createRow(0);
                
                // 单元格默认样式
                HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
                
                // 第一行、第一列
                HSSFCell cell = row.createCell(0);
                cell.setCellValue("日期");
                cell.setCellStyle(defaultStyle);
                
                // 第一行、第二列
                /* HSSFCell cell1 = row.createCell(1);
                cell1.setCellValue("日下载");
                cell1.setCellStyle(defaultStyle);*/
                
                // 第一行、第三列
                HSSFCell cell2 = row.createCell(1);
                cell2.setCellValue("日激活");
                cell2.setCellStyle(defaultStyle);
                
                HSSFCell cell3 = row.createCell(2);
                cell3.setCellValue("日活");
                cell3.setCellStyle(defaultStyle);
                
                HSSFCell cell4 = row.createCell(3);
                cell4.setCellValue("日活/日开机");
                cell4.setCellStyle(defaultStyle);
                
                HSSFCell cell5 = row.createCell(4);
                cell5.setCellValue("语音日活");
                cell5.setCellStyle(defaultStyle);
                
                HSSFCell cell6 = row.createCell(5);
                cell6.setCellValue("日开机");
                cell6.setCellStyle(defaultStyle);
                
                HSSFCell cell7 = row.createCell(6);
                cell7.setCellValue("日开机/用户总量");
                cell7.setCellStyle(defaultStyle);
                
                HSSFCell cell8 = row.createCell(7);
                cell8.setCellValue("用户总量");
                cell8.setCellStyle(defaultStyle);
                
                /* HSSFCell cell9 = row.createCell(9);
                cell9.setCellValue("下载总量");
                cell9.setCellStyle(defaultStyle);*/
                
                sheet.setColumnWidth(8, 2 * 256);
                
                // ============================
                HSSFCell cell10 = row.createCell(9);
                cell10.setCellValue("周活");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(10);
                cell10.setCellValue("语音周活");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(11);
                cell10.setCellValue("月活");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(12);
                cell10.setCellValue("语音月活");
                cell10.setCellStyle(defaultStyle);
                
                sheet.setColumnWidth(13, 2 * 256);
                
                // ============================
                cell10 = row.createCell(14);
                cell10.setCellValue("次日留存率");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(15);
                cell10.setCellValue("3日留存率");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(16);
                cell10.setCellValue("7日留存率");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(17);
                cell10.setCellValue("30日留存率");
                cell10.setCellStyle(defaultStyle);
                
                sheet.setColumnWidth(18, 2 * 256);
                
                // ============================
                
                cell10 = row.createCell(19);
                cell10.setCellValue("日PV");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(20);
                cell10.setCellValue("日语音交互次数");
                cell10.setCellStyle(defaultStyle);
                
                cell10 = row.createCell(21);
                cell10.setCellValue("平均停留时长");
                cell10.setCellStyle(defaultStyle);
                
                sheet.setColumnWidth(22, 2 * 256);
                
                // 冻结前一行与前一列
                sheet.createFreezePane(1, 1, 1, 1);
            }
            
            param = new HashMap<String, Object>();
            param.put("company", coversion.getCompany());
            param.put("coversion", coversion.getCoversion());
            
            createExcelRow(workbook, sheet, statDate, reportFilePath, param);
        }
        
        // 写文件
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(reportFilePath);
            workbook.write(outputStream);
            outputStream.flush();
            logger.debug("create report success{}", EXCEL_NAME);
        }
        finally
        {
            try
            {
                if (null != outputStream)
                {
                    outputStream.close();
                }
                
                workbook.close();
            }
            catch (IOException e)
            {
                logger.error("close stream exception", e);
            }
        }
        
        logger.info("[Excel] - [{}] 5、小鱼渠道详情数据报表.xls-统计完毕", statDate);
        
    }
    
    /** 
     * <一句话功能简述>
     * <功能详细描述>
     * @throws IOException 
     * @throws ParseException 
     * @see [类、类#方法、类#成员]
     */
    public void createExcelRow(HSSFWorkbook workbook, HSSFSheet sheet, String regDate, String path,
        Map<String, Object> param)
        throws IOException, ParseException
    {
        // 获取excel的第一个页签
        //HSSFSheet sheet = workbook.getSheetAt(0);
        
        // excel单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // 要追加的行
        HSSFRow row = sheet.createRow((short)(ExcelCustomize.getRowIndex(sheet, regDate, 1)));
        
        // 第一列【日期】
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(regDate);
        cell.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日期】");
        
        // 第二列【用户总量】
        /*HSSFCell cell1 = row.createCell(1);
        cell1.setCellValue("--");
        cell1.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日下载】");*/
        
        // 第三列【日激活】:当天第一次使用的用户数量 
        // 参考注册量的统计方式
        HSSFCell cell2 = row.createCell(1);
        cell2.setCellValue(versionOperService.findDayRegCusumers(regDate, param));
        cell2.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日激活】");
        
        param.put("endDate", regDate);
        
        // 日开机
        int openNum = versionOperService.queryDayOpersByCoversion(param);
        
        // 用户总量
        long consumers = versionOperService.findRegCusumers(regDate, param);
        
        // 根据操作日志统计
        param.put("interfaceName", Constant.INTERFACE_LOG_REPORT);
        // 日活
        int actionNum = versionOperService.queryDayOpersByCoversion(param);
        
        // 第四列【日活】：当天有使用小鱼的用户数量  
        HSSFCell cell3 = row.createCell(2);
        cell3.setCellValue(actionNum);
        cell3.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日活】");
        
        // 第五列【日活/日开机】
        DecimalFormat numberFormat = new DecimalFormat("0.00");
        String perNum = 0 == openNum ? "0.00%" : numberFormat.format((double)actionNum / (double)openNum * 100) + "%";
        
        HSSFCell cell4 = row.createCell(3);
        cell4.setCellValue(perNum);
        cell4.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日活/日开机】");
        
        // 第六列【语音日活】
        param.put("opCommandType", Constant.OPCOMMAND_TYPE_1);
        
        HSSFCell cell5 = row.createCell(4);
        cell5.setCellValue(versionOperService.queryDayOpersByCoversion(param));
        cell5.setCellStyle(defaultStyle);
        // 清除语音类型条件
        param.remove("opCommandType");
        logger.debug("date:{},column name :{}", regDate, "【语音日活】");
        
        // 第七列【日开机】
        HSSFCell cell6 = row.createCell(5);
        cell6.setCellValue(openNum);
        cell6.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日开机】");
        
        // 第八列【日开机/用户总量】
        String perNum2 =
            0 == consumers ? "0.00%" : numberFormat.format((double)openNum / (double)consumers * 100) + "%";
        
        HSSFCell cell7 = row.createCell(6);
        cell7.setCellValue(perNum2);
        cell7.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日开机/用户总量】");
        
        // 第九列【用户总量】
        HSSFCell cell8 = row.createCell(7);
        cell8.setCellValue(consumers);
        cell8.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【用户总量】");
        
        // 第十列【下载总量】
        /* HSSFCell cell9 = row.createCell(9);
        cell9.setCellValue("--");
        cell9.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【下载总量】");*/
        
        // ==================================
        // 第十二列【周活】
        // 参数查询
        param.put("startDate", DateUtil.strDiffDate(regDate, -6));
        
        HSSFCell cell10 = row.createCell(9);
        cell10.setCellValue(versionOperService.queryDayUserByCoversion(param));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【周活】");
        
        // 第十三列【语音周活】
        param.put("opCommandType", Constant.OPCOMMAND_TYPE_1);
        
        cell10 = row.createCell(10);
        cell10.setCellValue(versionOperService.queryDayUserByCoversion(param));
        cell10.setCellStyle(defaultStyle);
        param.remove("opCommandType");
        logger.debug("date:{},column name :{}", regDate, "【语音周活】");
        
        // 第十四列【月活】
        // 参数查询
        param.put("startDate", regDate.substring(0, 8) + "01");
        
        cell10 = row.createCell(11);
        cell10.setCellValue(versionOperService.queryDayUserByCoversion(param));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【月活】");
        
        // 第十五列【语音月活】
        param.put("opCommandType", Constant.OPCOMMAND_TYPE_1);
        
        cell10 = row.createCell(12);
        cell10.setCellValue(versionOperService.queryDayUserByCoversion(param));
        cell10.setCellStyle(defaultStyle);
        param.remove("opCommandType");
        logger.debug("date:{},column name :{}", regDate, "【语音月活】");
        
        // ==================================
        // 第十七列【次日留存率】
        Map<String, Object> rateMap = new HashMap<String, Object>();
        rateMap.put("startDate", DateUtil.strDiffDate(regDate, -1));
        rateMap.put("endDate", regDate);
        rateMap.put("company", param.get("company"));
        rateMap.put("coversion", param.get("coversion"));
        //rateMap.put("interfaceName", Constant.INTERFACE_LOG_REPORT);
        
        cell10 = row.createCell(14);
        cell10.setCellValue(versionOperService.queryRateByCoversion(2, rateMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【次日留存率】");
        
        // 第十八列【3日留存率】
        rateMap.put("startDate", DateUtil.strDiffDate(regDate, -2));
        
        cell10 = row.createCell(15);
        cell10.setCellValue(versionOperService.queryRateByCoversion(3, rateMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【3日留存率】");
        
        // 第十九列【7日留存率】
        rateMap.put("startDate", DateUtil.strDiffDate(regDate, -6));
        
        cell10 = row.createCell(16);
        cell10.setCellValue(versionOperService.queryRateByCoversion(7, rateMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【7日留存率】");
        
        // 第二十列【30日留存率】
        rateMap.put("startDate", DateUtil.strDiffDate(regDate, -29));
        
        cell10 = row.createCell(17);
        cell10.setCellValue(versionOperService.queryRateByCoversion(30, rateMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【30日留存率】");
        
        // ==================================
        // 第二十二列【日PV】
        Map<String, Object> pvMap = new HashMap<String, Object>();
        pvMap.put("endDate", regDate);
        pvMap.put("company", param.get("company"));
        pvMap.put("coversion", param.get("coversion"));
        
        cell10 = row.createCell(19);
        cell10.setCellValue(versionOperService.queryDayPvByCoversion(pvMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日PV】");
        
        // 第二十二列【日语音交互次数】
        pvMap.put("interfaceName", Constant.INTERFACE_LOG_REPORT);
        pvMap.put("opCommandType", Constant.OPCOMMAND_TYPE_1);
        
        cell10 = row.createCell(20);
        cell10.setCellValue(versionOperService.queryDayPvByCoversion(pvMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日语音交互次数】");
        
        // 第二十三列【平均停留时长】
        Map<String, Object> avgTimeMap = new HashMap<String, Object>();
        avgTimeMap.put("startDate", regDate);
        avgTimeMap.put("endDate", regDate);
        avgTimeMap.put("company", param.get("company"));
        avgTimeMap.put("coversion", param.get("coversion"));
        
        cell10 = row.createCell(21);
        cell10.setCellValue(versionOperService.analyseAverageTime(avgTimeMap));
        cell10.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【平均停留时长】");
        
        logger.debug("build coversions details report success{}", EXCEL_NAME);
    }
    
    /** 
     * 新增一个渠道后刷新sheet excel名称
     * <功能详细描述>
     * @param sheet
     * @param defaultStyle
     * @return 
     * @see [类、类#方法、类#成员]
     */
    private List<MvReleaseCompany> refreshSheetTitle(HSSFWorkbook workbook, String statDate)
    {
        // 从excel获取已经生成的渠道名称
        List<String> titleNames = new ArrayList<String>();
        
        for (int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            titleNames.add(workbook.getSheetName(i));
        }
        // 重新封装渠道，新渠道放到最后
        List<MvReleaseCompany> oldCompany = new ArrayList<MvReleaseCompany>();
        List<MvReleaseCompany> newCompany = new ArrayList<MvReleaseCompany>();
        
        List<MvReleaseCompany> coversionNames = versionOperService.getCoversionName(statDate);
        
        // 旧渠道集合
        for (String titleName : titleNames)
        {
            for (MvReleaseCompany coversion : coversionNames)
            {
                if (titleName.equals(coversion.getSummary()))
                {
                    oldCompany.add(coversion);
                    break;
                }
            }
        }
        for (MvReleaseCompany coversion : coversionNames)
        {
            // 新渠道名称则在excel表头后面新增一列
            if (!titleNames.contains(coversion.getSummary()))
            {
                // 新增渠道
                newCompany.add(coversion);
            }
        }
        
        oldCompany.addAll(newCompany);
        return oldCompany;
    }
}
