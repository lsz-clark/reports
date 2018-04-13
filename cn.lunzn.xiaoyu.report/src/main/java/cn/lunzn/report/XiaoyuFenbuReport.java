package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

import cn.lunzn.constant.WinId;
import cn.lunzn.service.XiaoyuFenbuService;
import cn.lunzn.util.ExcelCustomize;
import cn.lunzn.util.KeyValue;

/**
 * 小鱼PV分布
 * 
 * @author  clark
 * @version  [版本号, 2017年10月17日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class XiaoyuFenbuReport
{
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "小鱼PV分布.xls";
    
    /**
     * sheet1名称
     */
    public static final String SHEET_NAME_ONE = "PV占比";
    
    /**
     * sheet2名称
     */
    public static final String SHEET_NAME_TWO = "界面PV占比";
    
    /**
     * sheet3名称
     */
    public static final String SHEET_NAME_THREE = "所有请求分时";
    
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(XiaoyuFenbuReport.class);
    
    /**
     * 点播PV分布业务类
     */
    @Autowired
    private XiaoyuFenbuService xiaoyuFenbuService;
    
    /** 
     * 生成-点播PV分布 Excel
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
        
        // ☛ PV占比
        sheetOne(workbook, statDate);
        
        // ☛ 界面PV
        sheetTwo(workbook, statDate);
        
        // ☛ 所有请求分时
        sheetThree(workbook, statDate);
        
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
        
        logger.info("[Excel] - [{}] 3、小鱼PV分布.xls-统计完毕", statDate);
    }
    
    /** 
     * 生成- PV占比 sheet
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @see [类、类#方法、类#成员]
     */
    private void sheetOne(HSSFWorkbook workbook, String statDate)
    {
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // 获取数据
        Map<String, Long> data = xiaoyuFenbuService.ratePV(statDate);
        
        Long noBusiPV = 0L;
        
        // 获取所有的分类
        List<String> groupNames = new ArrayList<String>();
        // 将占比率较高的放到最前面
        groupNames.add("直播");
        groupNames.add("回看");
        groupNames.add("电视剧");
        groupNames.add("动漫");
        groupNames.add("电影");
        for (String title : data.keySet())
        {
            noBusiPV = noBusiPV + data.get(title);
            
            if (!groupNames.contains(title))
            {
                groupNames.add(title);
            }
        }
        
        // =========== 头部 ============
        HSSFSheet sheet = workbook.getSheet(SHEET_NAME_ONE);
        if (null == sheet)
        {
            // 创建页签
            sheet = ExcelCustomize.getCommonSheet(workbook, SHEET_NAME_ONE);
            // 创建首行
            HSSFRow fRow = sheet.createRow(0);
            // 创建第二行
            HSSFRow sRow = sheet.createRow(1);
            
            // 创建日期单元格
            HSSFCell dateCell = fRow.createCell(0);
            dateCell.setCellValue("日期");
            dateCell.setCellStyle(defaultStyle);
            //sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            
            // 创建日PV汇总单元格
            HSSFCell totalCell = fRow.createCell(1);
            totalCell.setCellValue("日PV汇总");
            totalCell.setCellStyle(defaultStyle);
            //sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            
            // 创建去业务PV单元格
            HSSFCell noBusiCell = fRow.createCell(2);
            noBusiCell.setCellValue("播放PV");
            noBusiCell.setCellStyle(defaultStyle);
            //sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            
            // 创建接口单元格
            for (String title : groupNames)
            {
                int cIndex = sRow.getPhysicalNumberOfCells() + 3;
                
                HSSFCell tCell = fRow.createCell(cIndex);
                tCell.setCellValue(title);
                tCell.setCellStyle(defaultStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, tCell.getColumnIndex(), tCell.getColumnIndex() + 1));
                
                HSSFCell t0Cell = sRow.createCell(cIndex);
                t0Cell.setCellValue("日PV");
                t0Cell.setCellStyle(defaultStyle);
                
                HSSFCell t1Cell = sRow.createCell(cIndex + 1);
                t1Cell.setCellValue("占比");
                t1Cell.setCellStyle(defaultStyle);
            }
            
            // 冻结前两行 + 前三列
            sheet.createFreezePane(3, 2, 3, 2);
        }
        
        // =========== 内容 ============
        // 开始写行号，最新的统计放到最前面
        int rowNum = ExcelCustomize.getRowIndex(sheet, statDate, 2);
        
        // 创建数据行
        HSSFRow dataRow = sheet.createRow(rowNum);
        
        // 第rowNum行、第一列，相应日期
        HSSFCell acell = dataRow.createCell(0);
        acell.setCellValue(statDate);
        acell.setCellStyle(defaultStyle);
        
        // 第rowNum行、第二列，日PV汇总
        HSSFCell bcell = dataRow.createCell(1);
        bcell.setCellValue(xiaoyuFenbuService.totalPV(statDate));
        bcell.setCellStyle(defaultStyle);
        
        // 第rowNum行、第三列，去业务PV
        HSSFCell ccell = dataRow.createCell(2);
        ccell.setCellValue(noBusiPV);
        ccell.setCellStyle(defaultStyle);
        
        List<HSSFCell> frowCells = getCellGroups(sheet, groupNames, defaultStyle);
        // 第rowNum行，第X列，日PV，占比
        for (HSSFCell tcell : frowCells)
        {
            String key = tcell.getStringCellValue();
            int columnIndex = tcell.getColumnIndex();
            
            Long xPV = data.get(key);
            if (null == xPV)
            {
                xPV = 0L;
            }
            
            // 日PV
            HSSFCell x0cell = dataRow.createCell(columnIndex);
            x0cell.setCellValue(xPV);
            x0cell.setCellStyle(defaultStyle);
            
            Long zero = new Long(0L);
            String xrate = "0.00%";
            BigDecimal dnoBusiPV = new BigDecimal(noBusiPV);
            if (zero.equals(xPV) || zero.equals(noBusiPV))
            {
                xrate = "0.00%";
            }
            else
            {
                BigDecimal dxPv = new BigDecimal(xPV);
                BigDecimal rate = dxPv.divide(dnoBusiPV, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP);
                
                if (rate.doubleValue() <= 0)
                {
                    xrate = "0.00%";
                }
                else
                {
                    xrate = rate.toString() + "%";
                }
            }
            
            // 占比
            HSSFCell x1cell = dataRow.createCell(columnIndex + 1);
            x1cell.setCellValue(xrate);
            x1cell.setCellStyle(defaultStyle);
        }
    }
    
    /** 
     * 生成- 界面PV分布 sheet
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @see [类、类#方法、类#成员]
     */
    private void sheetTwo(HSSFWorkbook workbook, String statDate)
    {
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // =========== 头部 ============
        HSSFSheet sheet = workbook.getSheet(SHEET_NAME_TWO);
        if (null == sheet)
        {
            // 创建页签
            sheet = ExcelCustomize.getCommonSheet(workbook, SHEET_NAME_TWO);
            
            // 创建首行
            HSSFRow fRow = sheet.createRow(0);
            
            // 创建第一个单元格
            HSSFCell fcell = fRow.createCell(0);
            fcell.setCellStyle(defaultStyle);
            fcell.setCellValue("日期");
            
            // 创建第二个单元格
            HSSFCell scell = fRow.createCell(1);
            scell.setCellStyle(defaultStyle);
            scell.setCellValue("日PV汇总");
            
            int columnIndex = 2;
            
            // 创建标题
            for (KeyValue<Long> keyVal : WinId.getWins())
            {
                HSSFCell cell = fRow.createCell(columnIndex);
                cell.setCellStyle(defaultStyle);
                cell.setCellValue(keyVal.getName());
                
                columnIndex++;
            }
            
            // 冻结前一行,前二列
            sheet.createFreezePane(2, 1, 2, 1);
        }
        
        // =========== 内容 ============
        // 开始写行号，最新的统计放到最前面
        int rowNum = ExcelCustomize.getRowIndex(sheet, statDate, 1);
        
        // 创建数据行
        HSSFRow dataRow = sheet.createRow(rowNum);
        
        // 第rowNum行、第一列，相应日期
        HSSFCell acell = dataRow.createCell(0);
        acell.setCellValue(statDate);
        acell.setCellStyle(defaultStyle);
        
        // 第rowNum行、第二列，PV汇总
        HSSFCell bcell = dataRow.createCell(1);
        bcell.setCellValue(xiaoyuFenbuService.totalPV(statDate));
        bcell.setCellStyle(defaultStyle);
        
        // 如果新增了统计项，那么将会创建它
        List<HSSFCell> frowCells = getCellWin(sheet, WinId.getWins(), defaultStyle);
        
        // 获取界面PV分布结果
        List<KeyValue<Long>> winsPV = xiaoyuFenbuService.winsPV(statDate);
        
        // 第rowNum行，第X列
        for (HSSFCell tcell : frowCells)
        {
            String name = tcell.getStringCellValue();
            Long value = 0L;
            for (KeyValue<Long> kv : winsPV)
            {
                if (kv.getName().equals(name))
                {
                    value = kv.getValue();
                    break;
                }
            }
            HSSFCell xcell = dataRow.createCell(tcell.getColumnIndex());
            xcell.setCellValue(value);
            xcell.setCellStyle(defaultStyle);
        }
    }
    
    /** 
     * 生成- 所有请求分时 sheet
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @see [类、类#方法、类#成员]
     */
    private void sheetThree(HSSFWorkbook workbook, String statDate)
    {
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        // =========== 头部 ============
        HSSFSheet sheet = workbook.getSheet(SHEET_NAME_THREE);
        if (null == sheet)
        {
            // 创建页签
            sheet = workbook.createSheet(SHEET_NAME_THREE);
            
            sheet.setDefaultColumnWidth(16);
            sheet.setDefaultRowHeight((short)(1.2 * 256));
            
            // 创建首行
            HSSFRow fRow = sheet.createRow(0);
            
            // 创建第一个单元格
            HSSFCell fcell = fRow.createCell(0);
            fcell.setCellStyle(defaultStyle);
            fcell.setCellValue("日期");
            
            // 创建时间段标题
            for (int i = 0; i < 24; i++)
            {
                // 00-23时间段
                String v0 = i < 10 ? "0" + i + ":00:00" : i + ":00:00";
                // 01-00时间段
                String v1 = (i + 1) < 10 ? "0" + (i + 1) + ":00:00" : (i + 1) + ":00:00";
                if ((i + 1) == 24)
                {
                    v1 = "00:00:00";
                }
                
                HSSFCell headCell = fRow.createCell(i + 1);
                headCell.setCellStyle(defaultStyle);
                headCell.setCellValue(v0 + "-" + v1);
            }
            
            // 冻结前一行
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
        
        // 获取所有请求分时PV
        Map<String, Long> hours = xiaoyuFenbuService.hoursPV(statDate);
        for (int i = 0; i < 24; i++)
        {
            String key = i < 10 ? "0" + i + ":00:00" : i + ":00:00";
            key = statDate + " " + key;
            
            Long hpv = hours.containsKey(key) ? hours.get(key) : 0L;
            
            HSSFCell hpvCell = dataRow.createCell(i + 1);
            hpvCell.setCellValue(hpv);
            hpvCell.setCellStyle(defaultStyle);
        }
    }
    
    /** 
     * 获取PV占比中的影片分类单元格
     * @param sheet PV占比sheet页
     * @param items 统计项，所有统计项，包括新增的
     * @param style 默认样式
     * @return List<HSSFCell>
     * @see [类、类#方法、类#成员]
     */
    private List<HSSFCell> getCellGroups(HSSFSheet sheet, List<String> items, HSSFCellStyle style)
    {
        // 获取菜单行
        HSSFRow fRow = sheet.getRow(0);
        HSSFRow sRow = sheet.getRow(1);
        
        List<HSSFCell> frowCells = new ArrayList<HSSFCell>();
        // 莫名其妙... 默认从首行取列数，没有数据时从第二行取列数
        int cNum = sRow.getPhysicalNumberOfCells() / 2;
        /*if (sheet.getPhysicalNumberOfRows() == 3)
        {
            // 只有菜单，没有数据时
            cNum = sRow.getPhysicalNumberOfCells() / 2;
        }*/
        // 将excel中的菜单取出
        for (int i = 0; i < cNum; i++)
        {
            HSSFCell cell = fRow.getCell(i * 2 + 3);
            frowCells.add(cell);
        }
        
        int lastColumnIndex = frowCells.get(frowCells.size() - 1).getColumnIndex();
        
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
                lastColumnIndex = lastColumnIndex + 2;
                
                HSSFCell cell = fRow.createCell(lastColumnIndex);
                cell.setCellValue(item);
                cell.setCellStyle(style);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, cell.getColumnIndex(), cell.getColumnIndex() + 1));
                
                HSSFCell t0Cell = sRow.createCell(lastColumnIndex);
                t0Cell.setCellValue("日PV");
                t0Cell.setCellStyle(style);
                
                HSSFCell t1Cell = sRow.createCell(lastColumnIndex + 1);
                t1Cell.setCellValue("占比");
                t1Cell.setCellStyle(style);
                
                frowCells.add(cell);
            }
        }
        
        return frowCells;
    }
    
    /** 
     * 获取界面PV分布各个接口单元格
     * @param sheet 界面PV分布
     * @param items 统计项，所有统计项，包括新增的
     * @param style 默认样式
     * @return List<HSSFCell>
     * @see [类、类#方法、类#成员]
     */
    private List<HSSFCell> getCellWin(HSSFSheet sheet, List<KeyValue<Long>> items, HSSFCellStyle style)
    {
        // 获取菜单行
        HSSFRow fRow = sheet.getRow(0);
        
        List<HSSFCell> frowCells = new ArrayList<HSSFCell>();
        int cNum = fRow.getPhysicalNumberOfCells() - 2;
        // 获取之前的统计项
        for (int i = 0; i < cNum; i++)
        {
            HSSFCell cell = fRow.getCell(i + 2);
            frowCells.add(cell);
        }
        
        // 循环判断是否有新增的统计项
        for (KeyValue<Long> item : items)
        {
            boolean isNoExitst = true;
            for (HSSFCell cell : frowCells)
            {
                if (item.getName().equals(cell.getStringCellValue()))
                {
                    // 存在，不添加
                    isNoExitst = false;
                }
            }
            
            // 如果不存在，那么在最后一列追加一列
            if (isNoExitst)
            {
                HSSFCell cell = fRow.createCell(fRow.getPhysicalNumberOfCells());
                cell.setCellValue(item.getName());
                cell.setCellStyle(style);
                
                frowCells.add(cell);
            }
        }
        
        return frowCells;
    }
}
