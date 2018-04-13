package cn.lunzn.config;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 错误页面定义类
 * 
 * @author  clark
 * @version  [版本号, 2017年9月11日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class PortalConfigCustomizer implements EmbeddedServletContainerCustomizer
{
    @Override
    public void customize(ConfigurableEmbeddedServletContainer container)
    {
        container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/404.html"));
        container.addErrorPages(new ErrorPage(HttpStatus.UNAUTHORIZED, "/error/500.html"));
        container.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500.html"));
    }
}
