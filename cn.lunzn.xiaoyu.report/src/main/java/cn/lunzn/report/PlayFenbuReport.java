package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.lunzn.model.PlayFenbu;
import cn.lunzn.service.PlayFenbuService;
import cn.lunzn.util.ExcelCustomize;

/**
 * 点播PV分布
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class PlayFenbuReport
{
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "点播PV分布.xls";
    
    /**
     * sheet1名称
     */
    public static final String SHEET_NAME_ONE = "点播PV分布";
    
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(PlayFenbuReport.class);
    
    /**
     * 点播PV分布业务类
     */
    @Autowired
    private PlayFenbuService playFenbuService;
    
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
        
        // ☛ 点播PV分布
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
        
        logger.info("[Excel] - [{}] 2、点播PV分布.xls-统计完毕", statDate);
    }
    
    /** 
     * 生成- 点播PV分布 sheet
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @see [类、类#方法、类#成员]
     */
    private void sheetOne(HSSFWorkbook workbook, String statDate)
    {
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        HSSFCellStyle mvNameStyle = getMvNameStyle(workbook);
        // =========== 头部 ============
        HSSFSheet sheet = workbook.getSheet(SHEET_NAME_ONE);
        if (null == sheet)
        {
            // 创建页签
            sheet = ExcelCustomize.getCommonSheet(workbook, SHEET_NAME_ONE);
            // 创建首行
            sheet.createRow(0);
            // 创建第二行
            sheet.createRow(1);
            
            // 冻结前两行
            sheet.createFreezePane(0, 2, 0, 2);
        }
        
        // 获取首行
        HSSFRow fRow = sheet.getRow(0);
        // 获取第二行
        HSSFRow sRow = sheet.getRow(1);
        
        // 判断统计日期是否存在
        if (!containCell(sheet, statDate))
        {
            // 追加一列
            int cNum = sRow.getPhysicalNumberOfCells();
            if (cNum != 0)
            {
                cNum = cNum + cNum / 5;
            }
            
            HSSFCell fCell = fRow.createCell(cNum);
            fCell.setCellValue(statDate);
            fCell.setCellStyle(defaultStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, fCell.getColumnIndex(), fCell.getColumnIndex() + 4));
            
            // 创建title
            HSSFCell sCell0 = sRow.createCell(cNum);
            sCell0.setCellValue("影片名");
            sCell0.setCellStyle(defaultStyle);
            
            HSSFCell sCell1 = sRow.createCell(cNum + 1);
            sCell1.setCellValue("分类");
            sCell1.setCellStyle(defaultStyle);
            
            HSSFCell sCell2 = sRow.createCell(cNum + 2);
            sCell2.setCellValue("播放PV");
            sCell2.setCellStyle(defaultStyle);
            
            HSSFCell sCell3 = sRow.createCell(cNum + 3);
            sCell3.setCellValue("请求PV");
            sCell3.setCellStyle(defaultStyle);
            
            HSSFCell sCell4 = sRow.createCell(cNum + 4);
            sCell4.setCellValue("切换PV");
            sCell4.setCellStyle(defaultStyle);
            
            sheet.setColumnWidth(sCell4.getColumnIndex() + 1, 2 * 256);
        }
        
        // 根据日期找到 内容行
        int dataColumnIndex = getCellNum(sheet, statDate);
        
        // 获取数据
        List<PlayFenbu> pvs = playFenbuService.statPV(statDate);
        if (CollectionUtils.isEmpty(pvs))
        {
            return;
        }
        
        int rowIndex = 2;
        for (PlayFenbu pv : pvs)
        {
            if (null == pv.getMv())
            {
                continue;
            }
            
            // 创建 数据行
            HSSFRow row = sheet.getRow(rowIndex);
            if (null == row)
            {
                row = sheet.createRow(rowIndex);
            }
            
            HSSFCell mvname = row.createCell(dataColumnIndex);
            mvname.setCellValue(pv.getMv().getMvname());
            mvname.setCellStyle(mvNameStyle);
            sheet.setColumnWidth(mvname.getColumnIndex(), 30 * 256);
            
            HSSFCell groupName = row.createCell(dataColumnIndex + 1);
            groupName.setCellValue(pv.getMv().getTopGroup().getGroupname());
            groupName.setCellStyle(defaultStyle);
            
            HSSFCell qingqiuPV = row.createCell(dataColumnIndex + 2);
            qingqiuPV.setCellValue(pv.getPlayTotal());
            qingqiuPV.setCellStyle(defaultStyle);
            
            HSSFCell dianboPv = row.createCell(dataColumnIndex + 3);
            dianboPv.setCellValue(pv.getDetailTotal());
            dianboPv.setCellStyle(defaultStyle);
            
            HSSFCell qiehuanPv = row.createCell(dataColumnIndex + 4);
            qiehuanPv.setCellValue(pv.getSwitchTotal());
            qiehuanPv.setCellStyle(defaultStyle);
            
            rowIndex++;
        }
    }
    
    /** 
     * 根据单元格内容获取指定列
     * @param sheet 页签
     * @param cellContent 单元格内容 
     * @return int
     * @see [类、类#方法、类#成员]
     */
    private int getCellNum(HSSFSheet sheet, String cellContent)
    {
        HSSFRow fRow = sheet.getRow(0);
        HSSFRow sRow = sheet.getRow(1);
        
        int cNum = sRow.getPhysicalNumberOfCells() / 5;
        
        for (int i = 0; i < cNum; i++)
        {
            HSSFCell cell = fRow.getCell(i * 6);
            if (cell.getRichStringCellValue().getString().equals(cellContent))
            {
                return cell.getColumnIndex();
            }
            
        }
        
        return 0;
    }
    
    /** 
     * 判断是否包含某列
     * @param sheet 页签
     * @param cellContent 单元格内容 
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    private boolean containCell(HSSFSheet sheet, String cellContent)
    {
        HSSFRow fRow = sheet.getRow(0);
        HSSFRow sRow = sheet.getRow(1);
        
        int cNum = sRow.getPhysicalNumberOfCells() / 5;
        
        for (int i = 0; i < cNum; i++)
        {
            HSSFCell cell = fRow.getCell(i * 6);
            if (cell.getRichStringCellValue().getString().equals(cellContent))
            {
                return true;
            }
            
        }
        
        return false;
    }
    
    /** 
     * 影片名称样式
     * @param workbook excel对象
     * @return HSSFCellStyle
     * @see [类、类#方法、类#成员]
     */
    private static HSSFCellStyle getMvNameStyle(HSSFWorkbook workbook)
    {
        // 文字居中对齐
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 文本格式
        HSSFDataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));
        return style;
    }
}
