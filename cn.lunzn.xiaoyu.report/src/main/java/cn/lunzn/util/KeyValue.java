package cn.lunzn.util;

/**
 * key-value类
 * 
 * @author  clark
 * @version  [版本号, 2017年10月19日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class KeyValue<T>
{
    private int key;
    
    private String name;
    
    private T value;
    
    public KeyValue()
    {
        super();
    }
    
    public KeyValue(int key, String name, T value)
    {
        super();
        this.key = key;
        this.name = name;
        this.value = value;
    }
    
    public int getKey()
    {
        return key;
    }
    
    public void setKey(int key)
    {
        this.key = key;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public T getValue()
    {
        return value;
    }
    
    public void setValue(T value)
    {
        this.value = value;
    }
    
}
