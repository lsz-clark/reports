package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.lunzn.service.SumFormService;
import cn.lunzn.util.ExcelCustomize;

/**
 * 小鱼数据报表 汇总表单
 * 
 * @author  clark
 * @version  [版本号, 2017年10月23日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class SumFormReport
{
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "小鱼数据报表_汇总表单.xls";
    
    /**
     * sheet1名称
     */
    public static final String SHEET_NAME_ONE = "汇总表单";
    
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(SumFormReport.class);
    
    /**
     * 汇总表单业务类
     */
    @Autowired
    private SumFormService sumFormService;
    
    /** 
     * 生成-版本用户量分布Excel
     * @param statDate 待统计日期
     * @param statPath 报表存放目录
     * @see [类、类#方法、类#成员]
     */
    public void buildExcel(String statDate, File statPath)
        throws Exception
    {
        String reportFilePath = statPath.getPath() + File.separator + EXCEL_NAME;
        
        HSSFWorkbook workbook = null;
        FileInputStream inputStream = null;
        
        // 判断本月文件是否存在
        File thisMonthReport = new File(reportFilePath);
        if (thisMonthReport.exists())
        {
            // 如果文件存在则读取
            inputStream = new FileInputStream(thisMonthReport);
            workbook = new HSSFWorkbook(inputStream);
        }
        else
        {
            // 不存在则新建
            workbook = new HSSFWorkbook();
        }
        
        // ☛ 汇总表单
        sheetOne(workbook, statDate);
        
        // 写文件
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(reportFilePath);
            workbook.write(outputStream);
            outputStream.flush();
        }
        finally
        {
            try
            {
                if (null != inputStream)
                {
                    inputStream.close();
                }
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
        
        logger.info("[Excel] - [{}] 4、小鱼数据报表_汇总表单.xls-统计完毕", statDate);
    }
    
    /** 
     * 生成-活跃用户版本分布sheet
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @param versions apk版本
     * @see [类、类#方法、类#成员]
     */
    private void sheetOne(HSSFWorkbook workbook, String statDate)
    {
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // =========== 头部 ============
        HSSFSheet sheet = workbook.getSheet(SHEET_NAME_ONE);
        if (null == sheet)
        {
            // 创建页签
            sheet = ExcelCustomize.getCommonSheet(workbook, SHEET_NAME_ONE);
            
            // 创建第一行
            HSSFRow row = sheet.createRow(0);
            
            // 第一行、第1列
            HSSFCell cell1 = row.createCell(0);
            cell1.setCellValue("日期");
            cell1.setCellStyle(defaultStyle);
            
            // 第一行、第2列
            HSSFCell cell2 = row.createCell(1);
            cell2.setCellValue("日升级下载");
            cell2.setCellStyle(defaultStyle);
            
            // 第一行、第3列
            HSSFCell cell3 = row.createCell(2);
            cell3.setCellValue("日激活");
            cell3.setCellStyle(defaultStyle);
            
            // 第一行、第4列
            HSSFCell cell4 = row.createCell(3);
            cell4.setCellValue("日活");
            cell4.setCellStyle(defaultStyle);
            
            // 第一行、第5列
            HSSFCell cell5 = row.createCell(4);
            cell5.setCellValue("日活/日开机");
            cell5.setCellStyle(defaultStyle);
            
            // 第一行、第6列
            HSSFCell cell6 = row.createCell(5);
            cell6.setCellValue("语音日活");
            cell6.setCellStyle(defaultStyle);
            
            // 第一行、第7列
            HSSFCell cell7 = row.createCell(6);
            cell7.setCellValue("日开机");
            cell7.setCellStyle(defaultStyle);
            
            // 第一行、第8列
            HSSFCell cell8 = row.createCell(7);
            cell8.setCellValue("日开机/用户总量");
            cell8.setCellStyle(defaultStyle);
            
            // 第一行、第9列
            HSSFCell cell9 = row.createCell(8);
            cell9.setCellValue("用户总量");
            cell9.setCellStyle(defaultStyle);
            
            // 每个渠道的间隙
            sheet.setColumnWidth(cell9.getColumnIndex() + 1, 2 * 256);
            
            // 第一行、第10列
            HSSFCell cell10 = row.createCell(10);
            cell10.setCellValue("周活");
            cell10.setCellStyle(defaultStyle);
            
            // 第一行、第11列
            HSSFCell cell11 = row.createCell(11);
            cell11.setCellValue("语音周活");
            cell11.setCellStyle(defaultStyle);
            
            // 第一行、第12列
            HSSFCell cell12 = row.createCell(12);
            cell12.setCellValue("月活");
            cell12.setCellStyle(defaultStyle);
            
            // 第一行、第13列
            HSSFCell cell13 = row.createCell(13);
            cell13.setCellValue("语音月活");
            cell13.setCellStyle(defaultStyle);
            
            // 每个渠道的间隙
            sheet.setColumnWidth(cell13.getColumnIndex() + 1, 2 * 256);
            
            // 第一行、第14列
            HSSFCell cell14 = row.createCell(15);
            cell14.setCellValue("次日留存率");
            cell14.setCellStyle(defaultStyle);
            
            // 第一行、第15列
            HSSFCell cell15 = row.createCell(16);
            cell15.setCellValue("3日留存率");
            cell15.setCellStyle(defaultStyle);
            
            // 第一行、第16列
            HSSFCell cell16 = row.createCell(17);
            cell16.setCellValue("7日留存率");
            cell16.setCellStyle(defaultStyle);
            
            // 第一行、第17列
            HSSFCell cell17 = row.createCell(18);
            cell17.setCellValue("30日留存率");
            cell17.setCellStyle(defaultStyle);
            
            // 每个渠道的间隙
            sheet.setColumnWidth(cell17.getColumnIndex() + 1, 2 * 256);
            
            // 第一行、第18列
            HSSFCell cell18 = row.createCell(20);
            cell18.setCellValue("日PV");
            cell18.setCellStyle(defaultStyle);
            
            // 第一行、第19列
            HSSFCell cell19 = row.createCell(21);
            cell19.setCellValue("日语音交互次数");
            cell19.setCellStyle(defaultStyle);
            
            // 第一行、第19列
            HSSFCell cell20 = row.createCell(22);
            cell20.setCellValue("平均停留时长");
            cell20.setCellStyle(defaultStyle);
            
            // 冻结第一行与第一列
            sheet.createFreezePane(1, 1, 1, 1);
        }
        
        // =========== 内容 ============
        // 开始写行号，最新的统计放到最前面
        int rowNum = ExcelCustomize.getRowIndex(sheet, statDate, 1);
        
        // 创建数据行
        HSSFRow dataRow = sheet.createRow(rowNum);
        
        // 第rowNum行、第一列，相应日期
        HSSFCell cell = dataRow.createCell(0);
        cell.setCellValue(statDate);
        cell.setCellStyle(defaultStyle);
        
        // 获取当日之前版本用户注册量
        // 日活
        Long dayActive = sumFormService.dayActive(statDate);
        // 日开机
        Long dayBoot = sumFormService.dayBoot(statDate);
        // 用户总量
        Long userTotal = sumFormService.userTotal(statDate);
        
        // 第x行、第2列，日升级下载
        HSSFCell cell2 = dataRow.createCell(1);
        cell2.setCellValue(sumFormService.dayUpgrade(statDate));
        cell2.setCellStyle(defaultStyle);
        
        // 第x行、第3列，日激活
        HSSFCell cell3 = dataRow.createCell(2);
        cell3.setCellValue(sumFormService.dayUserActive(statDate));
        cell3.setCellStyle(defaultStyle);
        
        // 第x行、第4列，日活
        HSSFCell cell4 = dataRow.createCell(3);
        cell4.setCellValue(dayActive);
        cell4.setCellStyle(defaultStyle);
        
        // 第x行、第5列，日活/日开机
        HSSFCell cell5 = dataRow.createCell(4);
        cell5.setCellValue(this.getRate(dayActive, dayBoot));
        cell5.setCellStyle(defaultStyle);
        
        // 第x行、第6列，语音日活
        HSSFCell cell6 = dataRow.createCell(5);
        cell6.setCellValue(sumFormService.dayVoice(statDate));
        cell6.setCellStyle(defaultStyle);
        
        // 第x行、第7列，日开机
        HSSFCell cell7 = dataRow.createCell(6);
        cell7.setCellValue(dayBoot);
        cell7.setCellStyle(defaultStyle);
        
        // 第x行、第8列，日开机/用户总量
        HSSFCell cell8 = dataRow.createCell(7);
        cell8.setCellValue(this.getRate(dayBoot, userTotal));
        cell8.setCellStyle(defaultStyle);
        
        // 第x行、第9列，用户总量
        HSSFCell cell9 = dataRow.createCell(8);
        cell9.setCellValue(userTotal);
        cell9.setCellStyle(defaultStyle);
        
        // sheet.setColumnWidth(cell9.getColumnIndex() + 1, 2 * 256);
        
        // 第x行、第10列，周活
        HSSFCell cell10 = dataRow.createCell(10);
        cell10.setCellValue(sumFormService.weekActive(statDate));
        cell10.setCellStyle(defaultStyle);
        
        // 第x行、第11列，语音周活
        HSSFCell cell11 = dataRow.createCell(11);
        cell11.setCellValue(sumFormService.weekVoice(statDate));
        cell11.setCellStyle(defaultStyle);
        
        // 第x行、第12列，月活
        HSSFCell cell12 = dataRow.createCell(12);
        cell12.setCellValue(sumFormService.monthActive(statDate));
        cell12.setCellStyle(defaultStyle);
        
        // 第x行、第13列，语音月活
        HSSFCell cell13 = dataRow.createCell(13);
        cell13.setCellValue(sumFormService.monthVoice(statDate));
        cell13.setCellStyle(defaultStyle);
        
        // sheet.setColumnWidth(cell13.getColumnIndex() + 1, 2 * 256);
        
        // 第x行、第14列，次日留存率
        HSSFCell cell14 = dataRow.createCell(15);
        cell14.setCellValue(sumFormService.days2(statDate));
        cell14.setCellStyle(defaultStyle);
        
        // 第x行、第15列，3日留存率
        HSSFCell cell15 = dataRow.createCell(16);
        cell15.setCellValue(sumFormService.days3(statDate));
        cell15.setCellStyle(defaultStyle);
        
        // 第x行、第16列，7日留存率
        HSSFCell cell16 = dataRow.createCell(17);
        cell16.setCellValue(sumFormService.days7(statDate));
        cell16.setCellStyle(defaultStyle);
        
        // 第x行、第17列，30日留存率
        HSSFCell cell17 = dataRow.createCell(18);
        cell17.setCellValue(sumFormService.days30(statDate));
        cell17.setCellStyle(defaultStyle);
        
        // sheet.setColumnWidth(cell17.getColumnIndex() + 1, 2 * 256);
        
        // 第x行、第18列，日PV
        HSSFCell cell18 = dataRow.createCell(20);
        cell18.setCellValue(sumFormService.dayPV(statDate));
        cell18.setCellStyle(defaultStyle);
        
        // 第x行、第19列，日语音交互次数
        HSSFCell cell19 = dataRow.createCell(21);
        cell19.setCellValue(sumFormService.dayVoiceCount(statDate));
        cell19.setCellStyle(defaultStyle);
        
        // 第x行、第20列，平均停留时长
        HSSFCell cell20 = dataRow.createCell(22);
        cell20.setCellValue(sumFormService.analyseAverageTime(statDate));
        cell20.setCellStyle(defaultStyle);
    }
    
    /** 
     * 求比率
     * @param divisor 除数
     * @param dividend 被除数
     * @return String
     * @see [类、类#方法、类#成员]
     */
    private String getRate(Long divisor, Long dividend)
    {
        String rate = "0.00%";
        
        if (divisor <= 0 || dividend <= 0)
        {
            return rate;
        }
        
        BigDecimal divisorB = new BigDecimal(divisor);
        BigDecimal dividendB = new BigDecimal(dividend);
        
        BigDecimal rateB = divisorB.divide(dividendB, 6, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2,
            RoundingMode.HALF_UP);
        
        if (rateB.doubleValue() <= 0)
        {
            rate = "0.00%";
        }
        else
        {
            rate = rateB.toString() + "%";
        }
        
        return rate;
    }
}
