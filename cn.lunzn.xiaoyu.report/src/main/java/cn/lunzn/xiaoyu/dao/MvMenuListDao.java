package cn.lunzn.xiaoyu.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import cn.lunzn.xiaoyu.model.MvMenuList;

/**
 * 小鱼影片信息表Dao
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Mapper
public interface MvMenuListDao
{
    /** 
     * 根据影片id查询影片名、分类名
     * @param mvid 影片id
     * @return MvMenuList
     * @see [类、类#方法、类#成员]
     */
    MvMenuList findGroup(String mvid);
    
    /** 
     * 每次查询n个影片信息
     * <br>
     * 根据影片id查询影片名、分类名
     * @param mvids 影片id
     * @return MvMenuList
     * @see [类、类#方法、类#成员]
     */
    List<MvMenuList> findGroups(List<String> mvids);
}
