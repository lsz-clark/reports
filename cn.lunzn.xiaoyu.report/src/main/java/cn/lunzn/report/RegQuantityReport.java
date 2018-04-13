package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.lunzn.service.RegQuantityService;
import cn.lunzn.util.ExcelCustomize;
import cn.lunzn.xiaoyu.model.MvReleaseCompany;

/**
 * 注册量汇总
 * 
 * @author  yi.li
 * @version  [版本号, 2017年10月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class RegQuantityReport
{
    /**
     * 日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(RegQuantityReport.class);
    
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "小鱼数据报表_注册量汇总.xls";
    
    /**
     * 注册量汇总
     */
    @Autowired
    private RegQuantityService regQuantityService;
    
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
        // 报表存放位置
        String reportFilePath = statPath.getPath() + File.separator + EXCEL_NAME;
        FileInputStream inputStream = null;
        // 判断本月文件是否存在
        File thisMonthReport = new File(reportFilePath);
        HSSFWorkbook workbook = null;
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
            
            // 第二步创建sheet  
            HSSFSheet sheet = ExcelCustomize.getCommonSheet(workbook, "渠道注册量汇总");
            
            // 第三步创建行row:添加表头0行  
            HSSFRow row = sheet.createRow(0);
            
            // 表头 第二行1
            HSSFRow row_1 = sheet.createRow(1);
            
            // 单元格默认样式
            HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
            
            // 第一行、第一列
            HSSFCell cell = row.createCell(0);
            cell.setCellValue("日期");
            cell.setCellStyle(defaultStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            
            // 第一行、第二列
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue("用户总量");
            cell1.setCellStyle(defaultStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            
            // 第一行、第三列
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue("当日总量");
            cell2.setCellStyle(defaultStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            
            // 第一行、第n列
            // 查询所有的渠道，用名称展示
            List<MvReleaseCompany> coversions = regQuantityService.getCoversionName(statDate);
            
            // 渠道从第四列开始展示
            int columnIndex = 3;
            
            // 设置渠道内容
            for (MvReleaseCompany coversion : coversions)
            {
                // 渠道名称
                String name = coversion.getSummary();
                
                cell = row.createCell(columnIndex);
                cell.setCellValue(name);
                cell.setCellStyle(defaultStyle);
                
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cell.getColumnIndex(), cell.getColumnIndex() + 1));
                
                // 创建title
                HSSFCell consumerCell = row_1.createCell(columnIndex);
                consumerCell.setCellValue("注册量");
                consumerCell.setCellStyle(defaultStyle);
                
                HSSFCell dayLivingCell = row_1.createCell(columnIndex + 1);
                dayLivingCell.setCellValue("日活");
                dayLivingCell.setCellStyle(defaultStyle);
                
                // 多了两列
                columnIndex = columnIndex + 2;
                
                logger.debug("coversionName is {}", name);
            }
            
            // 冻结前两行与前三列
            sheet.createFreezePane(3, 2, 3, 2);
        }
        
        // 每天一条报表数据，按日期倒叙展示
        createExcelRow(workbook, statDate, reportFilePath);
        
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
        
        logger.info("[Excel] - [{}] 6、小鱼数据报表_注册量汇总.xls-统计完毕", statDate);
    }
    
    /** 
     * <一句话功能简述>
     * <功能详细描述>
     * @throws IOException 
     * @see [类、类#方法、类#成员]
     */
    public void createExcelRow(HSSFWorkbook workbook, String regDate, String path)
        throws IOException
    {
        // 获取excel的第一个页签
        HSSFSheet sheet = workbook.getSheetAt(0);
        
        // excel单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // 新增一个渠道，刷新表格title
        List<MvReleaseCompany> mvCompany = refreshTitle(sheet, defaultStyle, regDate);
        
        // 要追加的行
        HSSFRow row = sheet.createRow((short)(ExcelCustomize.getRowIndex(sheet, regDate, 2)));
        
        // 第一列【日期】
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(regDate);
        cell.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【日期】");
        
        // 第二列【用户总量】
        HSSFCell cell1 = row.createCell(1);
        cell1.setCellValue(regQuantityService.findRegCusumers(regDate));
        cell1.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【用户总量】");
        
        // 第三列【当日总量】
        HSSFCell cell2 = row.createCell(2);
        cell2.setCellValue(regQuantityService.findDayRegCusumers(regDate));
        cell2.setCellStyle(defaultStyle);
        logger.debug("date:{},column name :{}", regDate, "【当日总量】");
        
        // 第四列到最后显示被使用的【渠道名称】
        int columnIndex = 3;
        // 封装查询条件
        Map<String, Object> param = null;
        
        // 查询有被使用的渠道
        //List<MvReleaseCompany> coversions = regQuantityService.queryCoversions();
        for (MvReleaseCompany coversion : mvCompany)
        {
            // 各个渠道
            param = new HashMap<String, Object>();
            param.put("company", coversion.getCompany());
            param.put("coversion", coversion.getCoversion());
            
            Long value = regQuantityService.queryCusumerByCoversion(regDate, param);
            // 渠道注册量
            HSSFCell coversionCell = row.createCell(columnIndex);
            coversionCell.setCellValue(value);
            coversionCell.setCellStyle(defaultStyle);
            
            // 渠道日活
            HSSFCell dayLivingCell = row.createCell(columnIndex + 1);
            dayLivingCell.setCellValue(regQuantityService.queryDayOpersByCoversion(regDate, param));
            dayLivingCell.setCellStyle(defaultStyle);
            
            columnIndex = columnIndex + 2;
            logger.debug("date:{},column name :{}", regDate, "【" + coversion + "】");
        }
    }
    
    /** 
     * 新增一个渠道后刷新excel表头
     * <功能详细描述>
     * @param sheet
     * @param defaultStyle
     * @return 
     * @see [类、类#方法、类#成员]
     */
    private List<MvReleaseCompany> refreshTitle(HSSFSheet sheet, HSSFCellStyle defaultStyle, String regDate)
    {
        // 第三步创建行row:添加表头0行  
        HSSFRow row = sheet.getRow(0);
        
        // 表头 第二行1
        HSSFRow row1 = sheet.getRow(1);
        
        // 获得渠道总列数
        int cellcolumns = row.getLastCellNum();
        
        // 如果数据库中的渠道跟excel中渠道数量不一致，则重新生成渠道列
        // 渠道从第四列开始展示
        int columnIndex = cellcolumns + 1;
        
        // 从excel获取已经生成的渠道名称
        List<String> titleNames = new ArrayList<String>();
        
        for (int i = 0; i < cellcolumns - 3; i++)
        {
            // 表title合并了两列
            if (null != row.getCell(i + 3) && !"".equals(row.getCell(i + 3).getStringCellValue()))
            {
                titleNames.add(row.getCell(i + 3).getStringCellValue());
            }
        }
        // 重新封装渠道，新渠道放到最后
        List<MvReleaseCompany> oldCompany = new ArrayList<MvReleaseCompany>();
        List<MvReleaseCompany> newCompany = new ArrayList<MvReleaseCompany>();
        
        List<MvReleaseCompany> coversionNames = regQuantityService.getCoversionName(regDate);
        
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
        // 先比较excel表格中
        if ((cellcolumns + 1 - 3) / 2 != coversionNames.size())
        {
            // 设置渠道内容
            for (MvReleaseCompany coversion : coversionNames)
            {
                // 渠道名称
                String name = coversion.getSummary();
                
                // 新渠道名称则在excel表头后面新增一列
                if (!titleNames.contains(name))
                {
                    HSSFCell cell = row.createCell(columnIndex);
                    cell.setCellValue(name);
                    cell.setCellStyle(defaultStyle);
                    
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, cell.getColumnIndex(), cell.getColumnIndex() + 1));
                    
                    // 创建title
                    HSSFCell consumerCell = row1.createCell(columnIndex);
                    consumerCell.setCellValue("注册量");
                    consumerCell.setCellStyle(defaultStyle);
                    
                    HSSFCell dayLivingCell = row1.createCell(columnIndex + 1);
                    dayLivingCell.setCellValue("日活");
                    dayLivingCell.setCellStyle(defaultStyle);
                    
                    // 多了两列
                    columnIndex = columnIndex + 2;
                    
                    // 新增渠道
                    newCompany.add(coversion);
                }
            }
            
            oldCompany.addAll(newCompany);
        }
        
        return oldCompany;
    }
    
}
