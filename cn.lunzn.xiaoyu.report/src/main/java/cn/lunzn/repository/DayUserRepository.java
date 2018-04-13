package cn.lunzn.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import cn.lunzn.model.DayUser;

/**
 * ES-操作-数据类
 * 
 * @author  clark
 * @version  [版本号, 2017年11月15日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface DayUserRepository extends ElasticsearchRepository<DayUser, String>
{
}
