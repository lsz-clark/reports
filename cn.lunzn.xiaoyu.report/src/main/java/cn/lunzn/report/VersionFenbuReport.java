package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import cn.lunzn.service.CommonService;
import cn.lunzn.service.VersionFenbuService;
import cn.lunzn.util.ExcelCustomize;

/**
 * 版本用户量分布
 * 
 * @author  clark
 * @version  [版本号, 2017年9月30日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class VersionFenbuReport
{
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "版本用户量分布.xls";
    
    /**
     * sheet1名称
     */
    public static final String SHEET_NAME_ONE = "apk注册量分布";
    
    /**
     * sheet2名称
     */
    public static final String SHEET_NAME_TWO = "活跃用户版本分布";
    
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(VersionFenbuReport.class);
    
    /**
     * 公共业务类
     */
    @Autowired
    private CommonService commonService;
    
    /**
     * 版本分布业务类
     */
    @Autowired
    private VersionFenbuService versionFenbuService;
    
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
        
        // 查询版本
        List<String> versions = commonService.queryAppVersion(statDate);
        
        // ☛ apk注册量分布
        sheetOne(workbook, statDate, versions);
        
        // ☛ 活跃用户版本分布, 统计各个版本
        sheetTwo(workbook, statDate, versions);
        
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
        
        logger.info("[Excel] - [{}] 1、版本用户量分布.xls-统计完毕", statDate);
    }
    
    /** 
     * 生成-活跃用户版本分布sheet
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @param versions apk版本
     * @see [类、类#方法、类#成员]
     */
    private void sheetOne(HSSFWorkbook workbook, String statDate, List<String> versions)
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
            
            // 第一行、第一列
            HSSFCell cell = row.createCell(0);
            cell.setCellValue("日期");
            cell.setCellStyle(defaultStyle);
            
            // 第一行、第n列
            // 设置版本
            for (int i = 0; i < versions.size(); i++)
            {
                cell = row.createCell(i + 1);
                cell.setCellValue(versions.get(i));
                cell.setCellStyle(defaultStyle);
            }
            
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
        List<HSSFCell> frowCells = getHeadCells(sheet, versions, defaultStyle);
        Map<String, Long> versionPV = versionFenbuService.countVersionReg(statDate);
        for (HSSFCell tcell : frowCells)
        {
            cell = dataRow.createCell(tcell.getColumnIndex());
            cell.setCellValue(getVersionPV(versionPV, tcell.getStringCellValue()));
            cell.setCellStyle(defaultStyle);
        }
    }
    
    /** 
     * 生成-apk注册量分布
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @param versions apk版本
     * @see [类、类#方法、类#成员]
     */
    private void sheetTwo(HSSFWorkbook workbook, String statDate, List<String> versions)
    {
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // =========== 头部 ============
        HSSFSheet sheet = workbook.getSheet(SHEET_NAME_TWO);
        if (null == sheet)
        {
            // 创建页签
            sheet = ExcelCustomize.getCommonSheet(workbook, SHEET_NAME_TWO);
            
            // 创建第一行
            HSSFRow row = sheet.createRow(0);
            
            // 第一行、第一列
            HSSFCell cell = row.createCell(0);
            cell.setCellValue("日期");
            cell.setCellStyle(defaultStyle);
            
            // 第一行、第n列
            // 设置版本
            for (int i = 0; i < versions.size(); i++)
            {
                cell = row.createCell(i + 1);
                cell.setCellValue(versions.get(i));
                cell.setCellStyle(defaultStyle);
            }
            
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
        
        // 获取版本用户活跃度
        List<HSSFCell> frowCells = getHeadCells(sheet, versions, defaultStyle);
        Map<String, Long> versionPV = versionFenbuService.countVersionActive(statDate);
        for (HSSFCell tcell : frowCells)
        {
            cell = dataRow.createCell(tcell.getColumnIndex());
            cell.setCellValue(getVersionPV(versionPV, tcell.getStringCellValue()));
            cell.setCellStyle(defaultStyle);
        }
    }
    
    /** 
     * 根据版本号获取版本用户日活，如果没有则给0
     * @param versionPV 版本用户日活
     * @param versionKey 版本号
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    private Long getVersionPV(Map<String, Long> versionPV, String versionKey)
    {
        Long count = versionPV.get(versionKey);
        if (null == count)
        {
            count = new Long(0);
        }
        
        return count;
    }
    
    /** 
     * 获取头部单元格
     * @param sheet 界面PV分布
     * @param items 统计项，所有统计项，包括新增的
     * @param style 默认样式
     * @return List<HSSFCell>
     * @see [类、类#方法、类#成员]
     */
    private List<HSSFCell> getHeadCells(HSSFSheet sheet, List<String> items, HSSFCellStyle style)
    {
        // 获取菜单行
        HSSFRow fRow = sheet.getRow(0);
        
        List<HSSFCell> frowCells = new ArrayList<HSSFCell>();
        int cNum = fRow.getPhysicalNumberOfCells() - 1;
        // 获取之前的统计项
        for (int i = 0; i < cNum; i++)
        {
            HSSFCell cell = fRow.getCell(i + 1);
            frowCells.add(cell);
        }
        
        // 循环判断是否有新增的统计项
        for (String item : items)
        {
            boolean isNoExitst = true;
            for (HSSFCell cell : frowCells)
            {
                if (item.equals(cell.getStringCellValue()))
                {
                    // 存在，不添加
                    isNoExitst = false;
                }
            }
            
            // 如果不存在，那么在最后一列追加一列
            if (isNoExitst)
            {
                HSSFCell cell = fRow.createCell(fRow.getPhysicalNumberOfCells());
                cell.setCellValue(item);
                cell.setCellStyle(style);
                
                frowCells.add(cell);
            }
        }
        
        return frowCells;
    }
}
