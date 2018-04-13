package cn.lunzn.constant;

import java.io.File;

/**
 * 常量
 * 
 * @author  clark
 * @version  [版本号, 2017年9月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class Constant
{
    /**
     * ES-log库名
     */
    public static final String ES_XIAOYU_INDEX = "xiaoyu-inteface-log";
    
    /**
     * ES-stat库名
     */
    public static final String ES_XIAOYU_STAT_INDEX = "xiaoyu-log-stat";
    
    /**
     * ES-库名
     */
    public static final String ES_NGINX_INDEX = "nginx_log";
    
    /**
     * ES-表名-小鱼日志
     */
    public static final String ES_XIAOYU_TYPE_LOG = "log";
    
    /**
     * ES-表名-小鱼报表情况
     */
    public static final String ES_XIAOYU_TYPE_REPORT = "report";
    
    /**
     * ES-表名-备份用户
     */
    public static final String ES_XIAOYU_TYPE_USER = "user";
    
    /**
     * ES-表名-备份每天用户
     */
    public static final String ES_XIAOYU_TYPE_DAY_USER = "dayuser";
    
    /**
     * ES-表名-nginx
     */
    public static final String ES_NGINX_TYPE_NGINX = "nginx_log";
    
    /**
     * 小鱼-心跳接口
     */
    public static final String INTERFACE_LOG_USER_HEART = "/user/heart";
    
    /**
     * 小鱼-用户操作日志接口，表示用户的动作，是活跃的
     */
    public static final String INTERFACE_LOG_REPORT = "/user/log_report";
    
    /**
     * 小鱼-上报错误接口
     */
    public static final String INTERFACE_LOG_ERROR_REPORT = "/user/error_report";
    
    /**
     * 小鱼-登录接口
     */
    public static final String INTERFACE_LOG_USER_LOGIN = "/user/login";
    
    /**
     * 小鱼-影片请求接口-请求PV
     */
    public static final String INTERFACE_LOG_VIDEO_DETAIL = "/video/detail";
    
    /**
     * 小鱼-影片播放接口-播放PV
     */
    public static final String INTERFACE_LOG_VIDEO_PLAY = "/video/play";
    
    /**
     * 小鱼-影片播放接口-播放PV
     */
    public static final String INTERFACE_LOG_VIDEO_PLAY2 = "/video/play2";
    
    /**
     * 小鱼-影片切源接口-切换PV
     */
    public static final String INTERFACE_LOG_VIDEO_SWITCH = "/video/switch";
    
    /**
     * 小鱼-升级接口
     */
    public static final String INTERFACE_LOG_SYS_UPGRADE = "/sys/upgrade";
    
    /**
     * 小鱼-影片播放接口前缀
     */
    public static final String INTERFACE_LOG_VIDEO_PREFIX = "/video/play";
    
    /**
     * 小鱼-直播播放接口前缀
     */
    public static final String INTERFACE_LOG_TVLIVE_PREFIX = "/tvlive/play";
    
    /**
     * 小鱼-回看播放接口前缀
     */
    public static final String INTERFACE_LOG_REPEAT_PREFIX = "/repeat/play";
    
    /**
     * 小鱼-报表存放路径
     */
    public static final String REPORT_SAVE_PATCH =
        Thread.currentThread().getContextClassLoader().getResource("").getPath() + "reports" + File.separator;
    
    /*public static void main(String[] args)
    {
        System.out.println(Thread.currentThread().getContextClassLoader().getResource("").getPath());
    }*/
    
    /**
     * 小鱼-数据备份目录
     */
    public static final String BACKUP_PATCH =
        Thread.currentThread().getContextClassLoader().getResource("").getPath() + "backup" + File.separator;
    
    /**
     * 小鱼company
     */
    public static final String COMPANY = "XIAOYU";
    
    /**
     * 成功
     */
    public static final int SUCCESS = 1;
    
    /**
     * 失败
     */
    public static final int FAILED = 2;
    
    /**
     * data中字段，opCommandType：指令类型 = 1 语音
     */
    public static final String OPCOMMAND_TYPE_1 = "1";
    
    /**
     * mongodb 库
     */
    public static final String MONGODB_DB_NAME = "xiaoyu_report";
    
    /**
     * mongodb common_data表
     */
    public static final String COLLECTION_COMMON_DATA = "common_data";
    
    /** 
     * 受保护的构造函数，静态类无需共有构造函数
     */
    protected Constant()
    {
    }
}
