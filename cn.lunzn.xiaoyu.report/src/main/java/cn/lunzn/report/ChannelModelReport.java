package cn.lunzn.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.lunzn.service.ChannelModelService;
import cn.lunzn.util.ExcelCustomize;

/**
 * 小鱼渠道型号
 * 
 * @author  clark
 * @version  [版本号, 2017年10月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class ChannelModelReport
{
    /**
     * excel文件名
     */
    public static final String EXCEL_NAME = "小鱼渠道型号汇总.xls";
    
    /**
     * sheet1名称
     */
    public static final String SHEET_NAME_ONE = "小鱼渠道型号汇总";
    
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(ChannelModelReport.class);
    
    /**
     * 小鱼渠道型号业务类
     */
    @Autowired
    private ChannelModelService channelVersionService;
    
    /** 
     * 生成-小鱼渠道型号
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
        
        // ☛小鱼渠道型号
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
        
        logger.info("[Excel] - [{}] 7、小鱼渠道型号汇总.xls-统计完毕", statDate);
    }
    
    /** 
     * 生成- 小鱼渠道型号汇总 sheet
     * <br>每次都重写
     * @param workbook excel对象
     * @param statDate 待统计日期
     * @see [类、类#方法、类#成员]
     */
    private void sheetOne(HSSFWorkbook workbook, String statDate)
    {
        if (workbook.getSheetIndex(SHEET_NAME_ONE) > -1)
        {
            // 删除之前sheet也
            workbook.removeSheetAt(workbook.getSheetIndex(SHEET_NAME_ONE));
        }
        
        // 单元格默认样式
        HSSFCellStyle defaultStyle = ExcelCustomize.getCommonStyle(workbook);
        
        HSSFCellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.RIGHT);
        numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        HSSFDataFormat format = workbook.createDataFormat();
        numberStyle.setDataFormat(format.getFormat("#"));
        
        HSSFCellStyle kernelVsnStyle = getKernelVsnStyle(workbook);
        
        // =========== 头部 ============
        // 创建页签
        HSSFSheet sheet = ExcelCustomize.getCommonSheet(workbook, SHEET_NAME_ONE);
        // 创建首行
        sheet.createRow(0);
        // 创建第二行
        sheet.createRow(1);
        // 冻结前两行
        sheet.createFreezePane(0, 2, 0, 2);
        
        // 获取首行
        HSSFRow fRow = sheet.getRow(0);
        // 获取第二行
        HSSFRow sRow = sheet.getRow(1);
        
        JSONObject channelModel = channelVersionService.getChannelModel();
        for (String channelName : channelModel.keySet())
        {
            int cNum = sRow.getPhysicalNumberOfCells();
            if (cNum != 0)
            {
                cNum = cNum + cNum / 3;
            }
            
            HSSFCell fCell = fRow.createCell(cNum);
            fCell.setCellValue(channelName);
            fCell.setCellStyle(defaultStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, fCell.getColumnIndex(), fCell.getColumnIndex() + 2));
            
            // 创建title
            HSSFCell sCell0 = sRow.createCell(cNum);
            sCell0.setCellValue("终端型号");
            sCell0.setCellStyle(defaultStyle);
            
            HSSFCell sCell1 = sRow.createCell(cNum + 1);
            sCell1.setCellValue("终端内核版本");
            sCell1.setCellStyle(defaultStyle);
            
            HSSFCell sCell2 = sRow.createCell(cNum + 2);
            sCell2.setCellValue("数量");
            sCell2.setCellStyle(defaultStyle);
            
            // 内核版本宽度加宽
            sheet.setColumnWidth(sCell1.getColumnIndex(), 60 * 256);
            // 每个渠道的间隙
            sheet.setColumnWidth(sCell2.getColumnIndex() + 1, 2 * 256);
            
            JSONObject models = channelModel.getJSONObject(channelName);
            
            if (null == models)
            {
                continue;
            }
            int rowIndex = 2;
            for (String model : models.keySet())
            {
                
                JSONArray kernelVsns = models.getJSONArray(model);
                if (null == kernelVsns)
                {
                    continue;
                }
                
                for (Object kernelVsn : kernelVsns)
                {
                    JSONObject info = JSONObject.parseObject(kernelVsn.toString());
                    
                    // 内容行
                    HSSFRow row = sheet.getRow(rowIndex);
                    if (null == row)
                    {
                        row = sheet.createRow(rowIndex);
                    }
                    
                    HSSFCell modelCell = row.createCell(sCell0.getColumnIndex());
                    modelCell.setCellValue(model);
                    modelCell.setCellStyle(defaultStyle);
                    
                    HSSFCell kernelVsnCell = row.createCell(sCell1.getColumnIndex());
                    kernelVsnCell.setCellValue(info.getString("kernelVsn"));
                    kernelVsnCell.setCellStyle(kernelVsnStyle);
                    
                    HSSFCell uuidCell = row.createCell(sCell2.getColumnIndex());
                    uuidCell.setCellValue(info.getInteger("count"));
                    uuidCell.setCellStyle(numberStyle);
                    
                    rowIndex++;
                }
            }
        }
    }
    
    /** 
     * 内核版本样式
     * @param workbook excel对象
     * @return HSSFCellStyle
     * @see [类、类#方法、类#成员]
     */
    private static HSSFCellStyle getKernelVsnStyle(HSSFWorkbook workbook)
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
    
    /*public static void main(String[] args)
    {
        // 渠道
        JSONObject jo = new JSONObject();
        // 内核
        JSONArray deviceVsn = new JSONArray();
        deviceVsn.add("AiUI 1.0-782 for iFlyBox-dev");
        deviceVsn.add("AiUI 1.1-2494 for iFlyBox-dev");
        
        // 型号
        JSONObject deviceModel = new JSONObject();
        deviceModel.put("IFLYBOX", deviceVsn);
        
        jo.put("xiaoyu,w001", deviceModel);
        
        System.out.println(jo.getJSONObject("xiaoyu,w001").getJSONArray("IFLYBOX").get(0));
    }*/
    
}
