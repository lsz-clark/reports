package cn.lunzn.xiaoyu.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import cn.lunzn.xiaoyu.model.MvReleaseCompany;

/**
 * 小鱼渠道版本新表Dao
 * 
 * @author  clark
 * @version  [版本号, 2017年10月12日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Mapper
public interface MvReleaseCompanyDao
{
    /** 
     * 查询渠道资料
     * @param MvReleaseCompany 查询参数
     * @return List<MvReleaseCompany>
     * @see [类、类#方法、类#成员]
     */
    List<MvReleaseCompany> find();
    
    /** 
     * 查询渠道资料，单个
     * @param MvReleaseCompany 查询参数
     * @return MvReleaseCompany 
     * @see [类、类#方法、类#成员]
     */
    MvReleaseCompany findOne(MvReleaseCompany MvReleaseCompany);
}
