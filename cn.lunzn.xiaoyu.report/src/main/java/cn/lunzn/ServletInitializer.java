package cn.lunzn;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * 注册启动类
 * 用war包方式部署时用到
 */
public class ServletInitializer extends SpringBootServletInitializer
{
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        return application.sources(XiaoyuApplication.class);
    }
}
