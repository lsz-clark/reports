package cn.lunzn.constant;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * @author clark
 * 通用响应类
 */
public class BaseResponse implements Serializable
{
    private static final long serialVersionUID = -3084265831739968586L;
    
    /**
     * 默认成功，返回码
     */
    private Integer code = CommonResultCode.SUCCESS.getCode();
    
    /**
     * 默认成功，返回体
     */
    private String msg = CommonResultCode.SUCCESS.getMsg();
    
    public Integer getCode()
    {
        return code;
    }
    
    public void setCode(Integer code)
    {
        this.code = code;
    }
    
    public String getMsg()
    {
        return msg;
    }
    
    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    
    public boolean isSuccess()
    {
        return CommonResultCode.SUCCESS.getCode().equals(code);
    }
    
    @Override
    public String toString()
    {
        return JSON.toJSONString(this);
    }
    
}
