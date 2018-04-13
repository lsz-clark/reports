package cn.lunzn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lishizeng
 * DataSourceAutoConfiguration 不创建自动数据源
 */
@ServletComponentScan
@EnableScheduling
@SpringBootApplication
public class XiaoyuApplication
{
    /** 
     * 受保护的构造函数，静态类无需共有构造函数
     */
    protected XiaoyuApplication()
    {
        
    }
    
    /** 
     * 主入口
     * @param args 参数
     * @see [类、类#方法、类#成员]
     */
    public static void main(String[] args)
    {
        // 程序启动
        SpringApplication.run(XiaoyuApplication.class, args);
    }
}
