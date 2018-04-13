package cn.lunzn.constant;

/**
 * 错误码
 * 
 * @author  clark
 * @version  [版本号, 2017年9月4日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public enum CommonResultCode
{
    /**
     * success
     */
    SUCCESS(0, "success"),
    
    /**
     * invalid param
     */
    INVALID_PARAM(1, "invalid param"),
    
    /**
     * record already exist
     */
    RECORD_ALREADY_EXIST(2, "record already exist"),
    
    /**
     * out of max time
     */
    OUT_OF_MAX_TIME(4, "out of max time"),
    
    /**
     * invalid data status
     */
    INVALID_DATA_STATUS(3, "invalid data status"),
    
    /**
     * record not exist
     */
    RECORD_NOT_EXIST(404, "record not exist"),
    
    /**
     * server busy now
     */
    SERVER_BUSY_NOW(500, "server busy now"),
    
    /**
     * 业务异常
     */
    SERVICE_BUSINESS_601(601, "service exception"),
    
    /**
     * 请登录
     */
    SERVICE_BUSINESS_6011000(6011000, "please login..."),
    
    /**
     * 账号或密码错误
     */
    SERVICE_BUSINESS_6011001(6011001, "incorrect username or password");
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误信息
     */
    private final String msg;
    
    /** 
     * 默认构造函数
     * @param resultCode 错误码
     * @param resultMsg 错误信息
     */
    CommonResultCode(Integer resultCode, String resultMsg)
    {
        this.code = resultCode;
        this.msg = resultMsg;
    }
    
    public Integer getCode()
    {
        return this.code;
    }
    
    public String getMsg()
    {
        return this.msg;
    }
}
