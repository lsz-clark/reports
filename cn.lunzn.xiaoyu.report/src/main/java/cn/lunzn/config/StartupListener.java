package cn.lunzn.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.lunzn.util.PropertyCache;

/**
 * 启动加载监听器
 * 
 * @author  clark
 * @version  [版本号, 2017年9月11日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@WebListener
public class StartupListener implements ServletContextListener
{
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(StartupListener.class);
    
    /**
     * 公共业务类
     */
    /*@Autowired
    private CommonService commonService;*/
    
    @Override
    public void contextInitialized(ServletContextEvent arg0)
    {
        logger.info("hi-hello");
        
        // 1、加载配置文件
        try
        {
            
            PropertyCache.initPoperty();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        
        // 2、初始化UUID
        /*if (PropertyCache.getProp().getBooleanValue("init.uuid.flag"))
        {
            commonService.uuidInit();
        }*/
        
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0)
    {
        logger.debug("bye-bye");
    }
    
}
