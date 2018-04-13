package cn.lunzn.model;

import com.alibaba.fastjson.JSONObject;

import cn.lunzn.xiaoyu.model.MvMenuList;

/**
 * 小鱼点播PV报表 model
 * 
 * @author  clark
 * @version  [版本号, 2017年10月16日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class PlayFenbu implements Comparable<PlayFenbu>
{
    /**
     * 影片信息
     */
    private MvMenuList mv;
    
    /**
     * 请求PV
     */
    private Long detailTotal = 0L;
    
    /**
     * 点播PV
     */
    private Long playTotal = 0L;
    
    /**
     * 切换PV
     */
    private Long switchTotal = 0L;
    
    public MvMenuList getMv()
    {
        return mv;
    }
    
    public void setMv(MvMenuList mv)
    {
        this.mv = mv;
    }
    
    public Long getDetailTotal()
    {
        return detailTotal;
    }
    
    public void setDetailTotal(Long detailTotal)
    {
        this.detailTotal = detailTotal;
    }
    
    public Long getPlayTotal()
    {
        return playTotal;
    }
    
    public void setPlayTotal(Long playTotal)
    {
        this.playTotal = playTotal;
    }
    
    public Long getSwitchTotal()
    {
        return switchTotal;
    }
    
    public void setSwitchTotal(Long switchTotal)
    {
        this.switchTotal = switchTotal;
    }
    
    @Override
    public String toString()
    {
        return JSONObject.toJSONString(this);
        //return String.valueOf(this.playTotal);
    }
    
    @Override
    public int compareTo(PlayFenbu o)
    {
        // 优先播放PV
        if (o.getPlayTotal() > this.getPlayTotal())
        {
            return 1;
        }
        if (o.getPlayTotal() < this.getPlayTotal())
        {
            return -1;
        }
        
        // 其次请求PV
        if (o.getDetailTotal() > this.getDetailTotal())
        {
            return 1;
        }
        if (o.getDetailTotal() < this.getDetailTotal())
        {
            return -1;
        }
        
        // 最后切换PV
        if (o.getSwitchTotal() > this.getSwitchTotal())
        {
            return 1;
        }
        if (o.getSwitchTotal() < this.getSwitchTotal())
        {
            return -1;
        }
        
        return 0;
    }
    
    /*public static void main(String[] args)
    {
        List<PlayFenbu> list = new ArrayList<PlayFenbu>();
        PlayFenbu p1 = new PlayFenbu();
        p1.setDetailTotal(1000L);
        p1.setPlayTotal(800L);
        p1.setSwitchTotal(2L);
        
        PlayFenbu p2 = new PlayFenbu();
        p2.setDetailTotal(1100L);
        p2.setPlayTotal(600L);
        p2.setSwitchTotal(0L);
        
        PlayFenbu p3 = new PlayFenbu();
        p3.setDetailTotal(900L);
        p3.setPlayTotal(800L);
        p3.setSwitchTotal(2L);
        
        PlayFenbu p4 = new PlayFenbu();
        p4.setDetailTotal(900L);
        p4.setPlayTotal(901L);
        p4.setSwitchTotal(2L);
        
        PlayFenbu p5 = new PlayFenbu();
        p5.setDetailTotal(50L);
        p5.setPlayTotal(100L);
        p5.setSwitchTotal(2L);
        
        PlayFenbu p6 = new PlayFenbu();
        p6.setDetailTotal(50L);
        p6.setPlayTotal(800L);
        p6.setSwitchTotal(2L);
        
        PlayFenbu p7 = new PlayFenbu();
        p7.setDetailTotal(50L);
        p7.setPlayTotal(50L);
        p7.setSwitchTotal(2L);
        
        PlayFenbu p8 = new PlayFenbu();
        p8.setDetailTotal(50L);
        p8.setPlayTotal(50L);
        p8.setSwitchTotal(60L);
        
        list.add(p1);
        list.add(p2);
        list.add(p3);
        list.add(p4);
        list.add(p5);
        list.add(p6);
        list.add(p7);
        list.add(p8);
        
        Collections.sort(list);
        
        System.out.println(list);
    }*/
}
