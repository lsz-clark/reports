package cn.lunzn.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 签名工具类
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author  tianqiuming
 * @version  [版本号, 2017年10月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class SignUtil
{
    /**
     * 接口参数签名方法
     * @param params 要签名的参数
     * @param secret 签名密钥
     * @return 生成的签名
     * @throws IOException IO异常
     */
    public static String getSign(Map<String, String> params, String secret)
        throws IOException
    {
        ArrayList<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys)
        {
            if (!"sign".equals(key))
            {
                sb.append(key).append(params.get(key));
            }
        }
        return hmacsha256(sb.toString(), secret);
    }
    
    /**
     * 接口签名加密算法
     * @param data 要签名的字符串
     * @param secret 签名密钥
     * @return 生成的签名
     * @throws IOException IO异常
     */
    public static String hmacsha256(String data, String secret)
        throws IOException
    {
        byte[] bytes = null;
        try
        {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HMACSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes("UTF-8"));
        }
        catch (GeneralSecurityException gse)
        {
            throw new IOException(gse.toString());
        }
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    /** 
     * SHA1加密字符串数据 
     * @param source 要加密的字符串 
     * @return 加密后的字符串 
     */
    public static String SHA1(String source)
    {
        return byte2HexStr(SHA1Bit(source.getBytes()));
    }
    
    /** 
     * SHA1加密Bit数据 
     * @param source byte数组 
     * @return 加密后的byte数组 
     */
    public static byte[] SHA1Bit(byte[] source)
    {
        try
        {
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            sha1Digest.update(source);
            byte targetDigest[] = sha1Digest.digest();
            return targetDigest;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * 将byte数组转换为表示16进制值的字符串. 
     * 如：byte[]{8,18}转换为：0812  
     * 和 byte[] hexStr2Bytes(String strIn) 互为可逆的转换过程. 
     * @param bytes 需要转换的byte数组 
     * @return 转换后的字符串 
     */
    public static String byte2HexStr(byte[] bytes)
    {
        int bytesLen = bytes.length;
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍  
        StringBuffer hexString = new StringBuffer(bytesLen * 2);
        for (int i = 0; i < bytesLen; i++)
        {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制  
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2)
            {
                hexString.append(0);// 如果为1位 前面补个0  
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
}
