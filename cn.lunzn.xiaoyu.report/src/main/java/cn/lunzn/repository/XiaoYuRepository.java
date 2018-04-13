package cn.lunzn.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import cn.lunzn.model.XiaoYu;

/**
 * ES-操作-数据类
 * 
 * @author  clark
 * @version  [版本号, 2017年9月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface XiaoYuRepository extends ElasticsearchRepository<XiaoYu, String>
{
    
}
