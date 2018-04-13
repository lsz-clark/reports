package cn.lunzn.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import cn.lunzn.model.Report;

/**
 * 报表统计情况
 * 
 * @author  clark
 * @version  [版本号, 2017年10月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Repository
public interface ReportRepository extends ElasticsearchRepository<Report, String>
{
    
}
