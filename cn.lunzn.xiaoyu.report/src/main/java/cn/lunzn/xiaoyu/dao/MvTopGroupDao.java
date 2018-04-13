package cn.lunzn.xiaoyu.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import cn.lunzn.xiaoyu.model.MvTopGroup;

/**
 * 小鱼影片分类信息表Dao
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Mapper
public interface MvTopGroupDao
{
    /** 
     * 查询影片分类
     * @param group 分类id
     * @return List<MvTopGroup>
     * @see [类、类#方法、类#成员]
     */
    List<MvTopGroup> find(MvTopGroup group);
}
