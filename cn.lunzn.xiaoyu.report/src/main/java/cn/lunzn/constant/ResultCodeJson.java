package cn.lunzn.constant;

import com.alibaba.fastjson.JSONObject;

/**
 * json格式返回码
 * @author clark
 */
public class ResultCodeJson
{
    /** 
     * 受保护的构造函数，静态类无需共有构造函数
     */
    protected ResultCodeJson()
    {
        
    }
    
    /**
     * 不带数据的返回成功
     * @return JSONObject
     */
    public static JSONObject getSuccess()
    {
        BaseResponse br = new BaseResponse();
        return JSONObject.parseObject(br.toString());
    }
    
    /**
     * 记录已经存在
     * @return JSONObject
     */
    public static JSONObject get2()
    {
        return JSONObject.parseObject(getBr2().toString());
    }
    
    /** 
     * 记录已经存在
     * @return BaseResponse
     * @see [类、类#方法、类#成员]
     */
    public static BaseResponse getBr2()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.RECORD_ALREADY_EXIST.getCode());
        br.setMsg(CommonResultCode.RECORD_ALREADY_EXIST.getMsg());
        return br;
    }
    
    /**
     * 超过最大述职次数
     * @return JSONObject
     */
    public static JSONObject get4()
    {
        return JSONObject.parseObject(getBr4().toString());
    }
    
    /** 
     * 超过最大述职次数
     * @return BaseResponse
     * @see [类、类#方法、类#成员]
     */
    public static BaseResponse getBr4()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.OUT_OF_MAX_TIME.getCode());
        br.setMsg(CommonResultCode.OUT_OF_MAX_TIME.getMsg());
        return br;
    }
    
    /**
     * 数据状态不正确
     * @return JSONObject
     */
    public static JSONObject get3()
    {
        return JSONObject.parseObject(getBr3().toString());
    }
    
    /**
     * 数据状态不正确
     * @return BaseResponse
     */
    public static BaseResponse getBr3()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.INVALID_DATA_STATUS.getCode());
        br.setMsg(CommonResultCode.INVALID_DATA_STATUS.getMsg());
        return br;
    }
    
    /**
     * 未知业务异常
     * @return JSONObject
     */
    public static JSONObject get601()
    {
        return JSONObject.parseObject(getBr601().toString());
    }
    
    /**
     * 未知业务异常
     * @return BaseResponse
     */
    public static BaseResponse getBr601()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.SERVICE_BUSINESS_601.getCode());
        br.setMsg(CommonResultCode.SERVICE_BUSINESS_601.getMsg());
        return br;
    }
    
    /**
     * 请登录
     * @return JSONObject
     */
    public static JSONObject get6011000()
    {
        return JSONObject.parseObject(getBr6011000().toString());
    }
    
    /**
     * 请登录
     * @return JSONObject
     */
    public static BaseResponse getBr6011000()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.SERVICE_BUSINESS_6011000.getCode());
        br.setMsg(CommonResultCode.SERVICE_BUSINESS_6011000.getMsg());
        return br;
    }
    
    /**
     * 用户名或密码不正确
     * @return JSONObject
     */
    public static JSONObject get6011001()
    {
        return JSONObject.parseObject(getBr6011001().toString());
    }
    
    /**
     * 用户名或密码不正确
     * @return JSONObject
     */
    public static BaseResponse getBr6011001()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.SERVICE_BUSINESS_6011001.getCode());
        br.setMsg(CommonResultCode.SERVICE_BUSINESS_6011001.getMsg());
        return br;
    }
    
    /**
     * 服务器内部错误
     * @return JSONObject
     */
    public static JSONObject get500()
    {
        return JSONObject.parseObject(getBr500().toString());
    }
    
    /**
     * 服务器内部错误
     * @return BaseResponse
     */
    public static BaseResponse getBr500()
    {
        BaseResponse br = new BaseResponse();
        br.setCode(CommonResultCode.SERVER_BUSY_NOW.getCode());
        br.setMsg(CommonResultCode.SERVER_BUSY_NOW.getMsg());
        return br;
    }
    
}
