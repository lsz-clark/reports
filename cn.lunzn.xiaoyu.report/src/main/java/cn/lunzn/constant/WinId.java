package cn.lunzn.constant;

import java.util.ArrayList;
import java.util.List;

import cn.lunzn.util.KeyValue;

/**
 * 页面ID 2017-10-17
 * 
 * @author caolinjie
 */
public class WinId
{
    // 非APP页面
    public final static int WIN_OTHER = -2;
    
    // 欢迎页面
    public final static int WIN_WELCOME = 0;
    
    // 首页
    public final static int WIN_MAIN = 1;
    
    // 收藏、追剧、播放记录
    public final static int WIN_COLL = 2;
    
    // 影片列表-电影
    public final static int WIN_MV_LIST_DIANYING = 3;
    
    // 影片列表-电视剧
    public final static int WIN_MV_LIST_DIANSHIJU = 4;
    
    // 影片列表-动漫
    public final static int WIN_MV_LIST_DONGMA = 5;
    
    // 影片列表-综艺
    public final static int WIN_MV_LIST_ZONGYI = 6;
    
    // 影片列表-纪录片
    public final static int WIN_MV_LIST_JILUPIAN = 7;
    
    // 影片列表-公开课
    public final static int WIN_MV_LIST_GONGKAIKE = 8;
    
    // 搜索结果列表
    public final static int WIN_RESULT = 9;
    
    // 详情
    public final static int WIN_MV_DETAIL = 10;
    
    // 直播
    public final static int WIN_TV = 11;
    
    // 红外搜索
    public final static int WIN_SEARCH = 12;
    
    // 关于-通用设置
    public final static int WIN_ABOUT_SETTING = 13;
    
    // 关于-版本升级
    public final static int WIN_ABOUT_UPDATE = 14;
    
    // 关于-手机遥控
    public final static int WIN_ABOUT_PHONE = 15;
    
    // 关于-联系我们
    public final static int WIN_ABOUT_CONTACT = 16;
    
    // 问题反馈
    public final static int WIN_FEED = 17;
    
    // 日志上传
    public final static int WIN_UPLOAD = 18;
    
    // 体育分类
    public final static int WIN_SPORT_CLASSIFY = 19;
    
    // 体育列表
    public final static int WIN_SPORT_LIST = 20;
    
    // 应用下载界面
    public final static int WIN_DOWNLOAD = 21;
    
    // 老人机列表-曲艺
    public final static int WIN_OLD_LIST_QUYI = 22;
    
    // 老人机列表-健康生活
    public final static int WIN_OLD_LIST_JIANKANG = 23;
    
    // 专辑界面
    public final static int WIN_ABLUM = 24;
    
    // 首页推荐视频播放界面
    public final static int WIN_RECOMM = 25;
    
    // 点播播放
    public final static int WIN_MV_PLAY = 26;
    
    // 回看界面
    public final static int WIN_HUIKAN = 27;
    
    // osd播放界面
    public final static int WIN_OSD = 28;
    
    // 新闻资讯
    public final static int WIN_NEWS = 29;
    
    // 上传错误日志操作弹出框
    public final static int WIN_LOG_DIALOG = 32;
    
    // 云盘文件列表页面
    public final static int WIN_YUNPAN_LIST = 33;
    
    // 云盘图片查看页面
    public final static int WIN_YUNPAN_IMAGE = 34;
    
    // 云盘音乐查看页面
    public final static int WIN_YUNPAN_MUSIC = 35;
    
    // 云盘视频查看页面
    public final static int WIN_YUNPAN_VIDEO = 36;
    
    // 影片列表
    public final static int WIN_MV_LIST = 37;
    
    // 老人机列表
    public final static int WIN_OLD_LIST = 38;
    
    // 关于
    public final static int WIN_ABOUT = 39;
    
    //MV界面
    public final static int WIN_MUSIC_MV_LIST = 40;
    
    // 剧照
    public final static int WIN_STILL = 43;
    
    /** 
     * 获取界面对应ID与描述
     * @return List<KeyValue<Long>>
     * @see [类、类#方法、类#成员]
     */
    public static List<KeyValue<Long>> getWins()
    {
        List<KeyValue<Long>> wins = new ArrayList<KeyValue<Long>>();
        
        // 非界面PV
        wins.add(new KeyValue<Long>(-1999, "心跳", 0L));
        wins.add(new KeyValue<Long>(-1998, "用户操作", 0L));
        wins.add(new KeyValue<Long>(-1997, "升级", 0L));
        wins.add(new KeyValue<Long>(-1996, "上报错误", 0L));
        wins.add(new KeyValue<Long>(-1995, "登录", 0L));
        
        // 界面PV
        wins.add(new KeyValue<Long>(WIN_OTHER, "非APP页面", 0L));
        wins.add(new KeyValue<Long>(WIN_WELCOME, "欢迎页面", 0L));
        wins.add(new KeyValue<Long>(WIN_MAIN, "首页", 0L));
        wins.add(new KeyValue<Long>(WIN_COLL, "播放记录", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST, "影片列表", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_DETAIL, "详情", 0L));
        wins.add(new KeyValue<Long>(WIN_TV, "直播", 0L));
        wins.add(new KeyValue<Long>(WIN_HUIKAN, "回看", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_PLAY, "点播播放", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST_DIANYING, "电影", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST_DIANSHIJU, "电视剧", 0L));
        wins.add(new KeyValue<Long>(WIN_NEWS, "新闻资讯", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST_DONGMA, "动漫", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST_ZONGYI, "综艺", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST_JILUPIAN, "纪录片", 0L));
        wins.add(new KeyValue<Long>(WIN_MV_LIST_GONGKAIKE, "公开课", 0L));
        wins.add(new KeyValue<Long>(WIN_RESULT, "搜索结果", 0L));
        wins.add(new KeyValue<Long>(WIN_SEARCH, "红外搜索", 0L));
        wins.add(new KeyValue<Long>(WIN_FEED, "问题反馈", 0L));
        wins.add(new KeyValue<Long>(WIN_UPLOAD, "日志上传", 0L));
        wins.add(new KeyValue<Long>(WIN_SPORT_CLASSIFY, "体育分类", 0L));
        wins.add(new KeyValue<Long>(WIN_SPORT_LIST, "体育列表", 0L));
        wins.add(new KeyValue<Long>(WIN_DOWNLOAD, "应用下载", 0L));
        wins.add(new KeyValue<Long>(WIN_OLD_LIST, "老人机列表", 0L));
        wins.add(new KeyValue<Long>(WIN_OLD_LIST_QUYI, "老人机-曲艺", 0L));
        wins.add(new KeyValue<Long>(WIN_OLD_LIST_JIANKANG, "老人机-健康生活", 0L));
        wins.add(new KeyValue<Long>(WIN_ABLUM, "专辑", 0L));
        wins.add(new KeyValue<Long>(WIN_RECOMM, "首页推荐视频播放", 0L));
        wins.add(new KeyValue<Long>(WIN_OSD, "OSD播放", 0L));
        wins.add(new KeyValue<Long>(WIN_LOG_DIALOG, "上传错误日志", 0L));
        /*wins.add(new KeyValue<Long>(WIN_YUNPAN_LIST, "云盘文件列表", 0L));
        wins.add(new KeyValue<Long>(WIN_YUNPAN_IMAGE, "云盘图片查看", 0L));
        wins.add(new KeyValue<Long>(WIN_YUNPAN_MUSIC, "云盘音乐查看", 0L));
        wins.add(new KeyValue<Long>(WIN_YUNPAN_VIDEO, "云盘视频查看", 0L));*/
        wins.add(new KeyValue<Long>(WIN_ABOUT, "关于", 0L));
        wins.add(new KeyValue<Long>(WIN_ABOUT_SETTING, "通用设置", 0L));
        wins.add(new KeyValue<Long>(WIN_ABOUT_UPDATE, "版本升级", 0L));
        wins.add(new KeyValue<Long>(WIN_ABOUT_PHONE, "手机遥控", 0L));
        wins.add(new KeyValue<Long>(WIN_ABOUT_CONTACT, "联系我们", 0L));
        wins.add(new KeyValue<Long>(WIN_MUSIC_MV_LIST, "MV", 0L));
        wins.add(new KeyValue<Long>(WIN_STILL, "剧照", 0L));
        
        // test
        // wins.add(new KeyValue<Long>(99, "a", 0L));
        // wins.add(new KeyValue<Long>(100, "b", 0L));
        return wins;
    }
}
