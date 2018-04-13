package cn.lunzn.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 解压、压缩文件帮助类
 * 
 * @author  clark
 * @version  [版本号, 2017年10月27日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ZipHelper
{
    /** 
     * 压缩文件夹下面所有的文件
     * @param zipFilePath 压缩文件路径
     * @param folderPath 文件夹路径
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    public static boolean zipByFolder(String zipFilePath, String folderPath)
    {
        ZipOutputStream zos = null;
        try
        {
            // 文件夹路径
            File file = new File(folderPath);
            // 压缩文件路径
            zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
            
            File[] files = file.listFiles();
            
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].getName().endsWith("zip") || files[i].getName().endsWith("rar"))
                {
                    files[i].delete();
                    continue;
                }
                
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(files[i]));
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                while (true)
                {
                    byte[] b = new byte[100];
                    int len = bis.read(b);
                    if (len == -1)
                        break;
                    zos.write(b, 0, len);
                }
                
                bis.close();
            }
        }
        catch (Exception ex)
        {
            return false;
        }
        finally
        {
            if (null != zos)
            {
                try
                {
                    zos.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        
        return true;
    }
}
