package cn.lunzn.util;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * excel自定义，公共样式类
 * 
 * @author  clark
 * @version  [版本号, 2017年9月30日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ExcelCustomize
{
    private ExcelCustomize()
    {
        
    }
    
    /** 
     * 单元格默认样式
     * @param workbook excel对象
     * @return HSSFCellStyle
     * @see [类、类#方法、类#成员]
     */
    public static HSSFCellStyle getCommonStyle(HSSFWorkbook workbook)
    {
        // 文字居中对齐
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 文本格式
        HSSFDataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));
        return style;
    }
    
    /** 
     * 页签单元格默认设置
     * @param workbook excel对象
     * @param sheetName 页签名称
     * @return HSSFSheet
     * @see [类、类#方法、类#成员]
     */
    public static HSSFSheet getCommonSheet(HSSFWorkbook workbook, String sheetName)
    {
        HSSFSheet sheet = workbook.createSheet(sheetName);
        
        sheet.setDefaultColumnWidth(12);
        sheet.setDefaultRowHeight((short)(1.2 * 256));
        return sheet;
    }
    
    /** 
     * [追加]根据单元格内容获取指定行号，如果没有找到则从最后一行开始
     * @param sheet 页签
     * @param cellContent 单元格内容 
     * @return int
     * @see [类、类#方法、类#成员]
     */
    public static int getRowNum(HSSFSheet sheet, String cellContent)
    {
        if (StringUtils.isEmpty(cellContent))
        {
            return sheet.getLastRowNum() + 1;
        }
        
        for (Row row : sheet)
        {
            if (row.getCell(0).getRichStringCellValue().getString().equals(cellContent))
            {
                return row.getRowNum();
            }
        }
        
        return sheet.getLastRowNum() + 1;
    }
    
    /** 
     * [移动]根据单元格内容获取指定行号，如果没有找到则从标题行下行开始
     * @param sheet 页签
     * @param statDate 统计日期 
     * @param headRowNum 标题有几行
     * @return int
     * @see [类、类#方法、类#成员]
     */
    public static int getRowIndex(HSSFSheet sheet, String statDate, int headRowNum)
    {
        boolean isMoveRow = true;
        // 默认在标题的下一行开始写
        int rowIndex = headRowNum;
        for (Row row : sheet)
        {
            if (null == row.getCell(0))
            {
                continue;
            }
            
            if (row.getCell(0).getRichStringCellValue().getString().equals(statDate))
            {
                isMoveRow = false;
                rowIndex = row.getRowNum();
            }
        }
        
        if (isMoveRow && sheet.getLastRowNum() != (headRowNum - 1))
        {
            // 将标题的下一行与最后一行，向下移动一行
            sheet.shiftRows(headRowNum, sheet.getLastRowNum(), 1);
        }
        
        return rowIndex;
    }
}
