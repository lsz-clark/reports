package cn.lunzn.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cn.lunzn.constant.Constant;
import cn.lunzn.constant.XiaoyuField;
import cn.lunzn.model.DayUser;
import cn.lunzn.model.Report;
import cn.lunzn.model.User;
import cn.lunzn.model.XiaoYu;
import cn.lunzn.repository.DayUserRepository;
import cn.lunzn.repository.ReportRepository;
import cn.lunzn.repository.UserRepository;
import cn.lunzn.util.DateUtil;
import cn.lunzn.util.PropertyCache;
import cn.lunzn.util.ZipHelper;

/**
 * 获取常用信息<br>
 * 我就服：elastic search
 * @author  clark
 * @version  [版本号, 2017年9月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Service
public class CommonService
{
    /**
     * 日志记录
     */
    private Logger logger = LoggerFactory.getLogger(CommonService.class);
    
    /**
     * ES-数据源
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;
    
    /**
     * 报表统计情况
     */
    @Autowired
    private ReportRepository reportRepository;
    
    /**
     * 保存新注册用户
     */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 保存每天用户
     */
    @Autowired
    private DayUserRepository dayUserRepository;
    
    /**
     * 邮箱发送者
     */
    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * 公共mongodb处理类
     */
    /*@Autowired
    private CommonDataMService commonDataMService;*/
    
    /**
     * 用户注册表
     */
    /*@Autowired
    private UserRepository userRepository;*/
    
    /** 
     * 查询当前所有版本号，去重
     * 【备份表】
     * @param statDate 待统计日期
     * @return List<String>
     * @see [类、类#方法、类#成员]
     */
    public List<String> queryAppVersion(String statDate)
    {
        Client client = esTemplate.getClient();
        List<String> appVersions = new ArrayList<String>();
        
        // -------------------------从备份表user中获取所有版本--------------------------
        // 分组
        TermsBuilder appversionAgg = AggregationBuilders.terms("appversion");
        // 将appversion字段分组，并查询所有
        appversionAgg.field(XiaoyuField.APP_VERSION).size(Integer.MAX_VALUE);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(statDate)));
        
        search.setQuery(queryBuilder).addAggregation(appversionAgg).setSize(0);
        
        SearchResponse response = search.get();
        // 获取聚合数据 
        StringTerms terms = response.getAggregations().get("appversion");
        for (Terms.Bucket appversion : terms.getBuckets())
        {
            appVersions.add(appversion.getKey().toString());
        }
        // test
        /*versions.add("8.881");
        versions.add("8.882");*/
        // 升序
        Collections.sort(appVersions);
        // 反转，降序
        Collections.reverse(appVersions);
        
        // 将apkversion插入到mongodb中
        // commonDataMService.insertAPPVersion(apkVersions);
        
        logger.debug("ES Search-Group By [appversion], result:{}", appVersions);
        
        return appVersions;
    }
    
    /** 
     * 查询最近报表统计情况
     * @return Report
     * @see [类、类#方法、类#成员]
     */
    public Report queryRecentReport()
    {
        Client client = esTemplate.getClient();
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_REPORT);
        
        // 取最近一条数据
        search.addSort("statDate", SortOrder.DESC).setSize(1);
        
        SearchResponse response = search.get();
        SearchHits hits = response.getHits();
        if (ArrayUtils.isNotEmpty(hits.getHits()))
        {
            SearchHit sh = hits.getHits()[0];
            return JSONObject.toJavaObject(JSONObject.parseObject(sh.getSourceAsString()), Report.class);
        }
        
        return null;
    }
    
    /** 
     * 新增当天报表统计情况
     * @param report 当天报表统计情况
     * @return Report
     * @see [类、类#方法、类#成员]
     */
    public void insertReport(Report report)
    {
        reportRepository.save(report);
    }
    
    /** 
     * 日PV
     * @param statDate 统计日期
     * @return Long
     * @see [类、类#方法、类#成员]
     */
    public long dayPV(String statDate)
    {
        Client client = esTemplate.getClient();
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG).setSize(0);
        
        SearchResponse response = search.setQuery(queryBuilder).get();
        
        return response.getHits().getTotalHits();
    }
    
    /** 
     * 清理数据<br>
     * 1、log 每天清理3天前数据
     * 2、dayuser 每天清理3月前的数据
     * @see [类、类#方法、类#成员]
     */
    public void clearLog()
    {
        // 3月前 - dayuser
        Calendar _3MonthDate = Calendar.getInstance();
        _3MonthDate.add(Calendar.MONTH, -3);
        String _3Month = DateUtil.formatDateToString(DateUtil.DATE_FORMAT_DAY_BAR, _3MonthDate.getTime());
        
        DeleteQuery dayuserDQ = new DeleteQuery();
        dayuserDQ.setIndex(Constant.ES_XIAOYU_STAT_INDEX);
        dayuserDQ.setType(Constant.ES_XIAOYU_TYPE_DAY_USER);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(_3Month)));
        
        dayuserDQ.setQuery(queryBuilder);
        // 标记删除
        esTemplate.delete(dayuserDQ, DayUser.class);
        
        // 3天前 - log
        Calendar _3AgoDate = Calendar.getInstance();
        _3AgoDate.add(Calendar.DATE, -32);
        String _3Ago = DateUtil.formatDateToString(DateUtil.DATE_FORMAT_DAY_BAR, _3AgoDate.getTime());
        
        DeleteQuery logDQ = new DeleteQuery();
        logDQ.setIndex(Constant.ES_XIAOYU_INDEX);
        logDQ.setType(Constant.ES_XIAOYU_TYPE_LOG);
        
        queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(_3Ago)));
        logDQ.setQuery(queryBuilder);
        // 标记删除
        esTemplate.delete(logDQ, XiaoYu.class);
        
        try
        {
            // dayuser-标记删除之后,强制删除标记删除的文件
            esTemplate.getClient()
                .admin()
                .indices()
                .forceMerge(new ForceMergeRequest(Constant.ES_XIAOYU_STAT_INDEX).onlyExpungeDeletes(true))
                .get();
            
            // log-标记删除之后,强制删除标记删除的文件
            esTemplate.getClient()
                .admin()
                .indices()
                .forceMerge(new ForceMergeRequest(Constant.ES_XIAOYU_INDEX).onlyExpungeDeletes(true))
                .get();
            
            logger.info("[Scheduled] Clear logs success...");
        }
        catch (Exception ee)
        {
            logger.error("[Scheduled] Clear logs failed...");
        }
    }
    
    /** 
     * 统计每天UUID注册情况
     * @see [类、类#方法、类#成员]
     */
    /*public void uuidStatDay(String statDate)
    {
        Client client = esTemplate.getClient();
        // key-uuid value-createDate
        Map<String, String> uuids = new HashMap<String, String>();
        List<User> users = new ArrayList<User>();
        // ============先从log表中统计UUID注册情况
        TermsBuilder uuidAgg = AggregationBuilders.terms("uuid").field(XiaoyuField.UUID);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.addAggregation(uuidAgg).setQuery(queryBuilder).setSize(0);
        
        StringTerms uuidSt = search.get().getAggregations().get("uuid");
        for (Terms.Bucket sub : uuidSt.getBuckets())
        {
            if (!uuids.containsKey(sub.getKeyAsString()))
            {
                User user = new User();
                user.setUuid(sub.getKeyAsString());
                user.setCreateDate(statDate);
                users.add(user);
                
                uuids.put(sub.getKeyAsString(), null);
            }
        }
        
        // 当天需要新增的用户
        List<User> newUsers = new ArrayList<User>();
        
        Map<String, User> historyUsers = uuidQueryAll();
        for (User user : users)
        {
            // 已存在的用户不新增
            if (!historyUsers.containsKey(user.getUuid()))
            {
                newUsers.add(user);
            }
        }
        
        if (!newUsers.isEmpty())
        {
            userRepository.save(newUsers);
        }
    }*/
    
    /** 
     * 查询user表下所有的UUID
     * @return Map<String, User>
     * @see [类、类#方法、类#成员]
     */
    /*public Map<String, User> uuidQueryAll()
    {
        Client client = esTemplate.getClient();
        Map<String, User> users = new HashMap<String, User>();
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 匹配所有
        queryBuilder.filter(QueryBuilders.matchAllQuery());
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        
        search.setQuery(queryBuilder).setSize(Integer.MAX_VALUE);
        // 获取查询结果
        SearchHit[] hits = search.get().getHits().getHits();
        if (ArrayUtils.isNotEmpty(hits))
        {
            for (SearchHit hit : hits)
            {
                User user = JSONObject.toJavaObject(JSONObject.parseObject(hit.getSourceAsString()), User.class);
                users.put(user.getUuid(), user);
            }
        }
        
        return users;
    }*/
    
    /** 
     * 初始化UUID
     * @see [类、类#方法、类#成员]
     */
    /*public void uuidInit()
    {
        Client client = esTemplate.getClient();
        // key-uuid+company+coversion value-createDate
        Map<String, String> uuids = new HashMap<String, String>();
        List<User> users = new ArrayList<User>();
        logger.info("[Start Up] Begin init uuid...");
        
        // ============先从log表中统计UUID注册情况
        DateHistogramBuilder uuidAgg = AggregationBuilders.dateHistogram("logdate").field(XiaoyuField.LOG_DATE);
        uuidAgg.interval(DateHistogramInterval.DAY);
        
        TermsBuilder subcompany = AggregationBuilders.terms("company").field(XiaoyuField.COMPANY);
        TermsBuilder subcoversion = AggregationBuilders.terms("coversion").field(XiaoyuField.COVERSION);
        subcompany.subAggregation(subcoversion);
        subcoversion.subAggregation(AggregationBuilders.terms("uuid").field(XiaoyuField.UUID));
        uuidAgg.subAggregation(subcompany);
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        search.addAggregation(uuidAgg).setSize(0);
        
        SearchResponse response = search.get();
        System.out.println(response);
        
        Histogram logdateAgg = response.getAggregations().get("logdate");
        for (Histogram.Bucket b : logdateAgg.getBuckets())
        {
            StringTerms uuidSt = b.getAggregations().get("uuid");
            
            for (Terms.Bucket sub : uuidSt.getBuckets())
            {
                if (!uuids.containsKey(sub.getKeyAsString()))
                {
                    User user = new User();
                    user.setUuid(sub.getKeyAsString());
                    user.setCreateDate(b.getKeyAsString().substring(0, 10));
                    users.add(user);
                    
                    uuids.put(sub.getKeyAsString(), null);
                }
            }
        }
        logger.info("[Start Up] uuid stat finished...");
        
        // ============删除user表，插入最新统计结果
        DeleteQuery dq = new DeleteQuery();
        dq.setIndex(Constant.ES_XIAOYU_INDEX);
        dq.setType(Constant.ES_XIAOYU_TYPE_USER);
        dq.setQuery(QueryBuilders.matchAllQuery());
        esTemplate.delete(dq, User.class);
        
        StringBuilder b = new StringBuilder();
        b.append("{\"query\":{\"match_all\":{}}}");
        DeleteByQueryResponse response1 = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
            .setIndices(Constant.ES_XIAOYU_INDEX)
            .setTypes(Constant.ES_XIAOYU_TYPE_USER)
            .setSource(QueryBuilders.matchAllQuery())
            .get();
        
        // 插入数据
        userRepository.save(users);
        logger.info("[Start Up] End init uuid...");
    }*/
    
    /** 
     * 发送邮件通知相关负责人
     * 不压缩
     * @param statDate 统计日期
     * @param statPath 当前日期报表存放目录
     * @see [类、类#方法、类#成员]
     */
    public int sendEmail(String statDate, String statPath)
    {
        int isSuccess = Constant.SUCCESS;
        
        File fileDir = new File(statPath);
        try
        {
            System.setProperty("mail.mime.splitlongparameters", "false");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("mofang_report@lunzn.com");
            helper.setTo(PropertyCache.getProp().getString("email.recipient").split(","));
            
            helper.setSubject("小鱼报表 - " + statDate);
            // 后续实现网站报表
            // helper.setText("http://xiaoyu.lunzn.com/xiaoyu/report/20171020");
            helper.setText("请查收附件...");
            
            for (File subFile : fileDir.listFiles())
            {
                if (subFile.isFile())
                {
                    String filename = MimeUtility.encodeText(subFile.getName());
                    filename = filename.replaceAll("\r", "").replaceAll("\n", "");
                    helper.addAttachment(filename, subFile);
                }
            }
            mailSender.send(mimeMessage);
        }
        catch (MessagingException me)
        {
            logger.error("Send email failed", me);
            isSuccess = Constant.FAILED;
        }
        catch (UnsupportedEncodingException uee)
        {
            logger.error("Send email failed", uee);
            isSuccess = Constant.FAILED;
        }
        return isSuccess;
    }
    
    /** 
     * 发送邮件通知相关负责人
     * 压缩
     * @param statDate 统计日期
     * @param statPath 当前日期报表存放目录
     * @see [类、类#方法、类#成员]
     */
    public int sendEmailZip(String statDate, String statPath)
    {
        int isSuccess = Constant.SUCCESS;
        
        // 文件夹路径
        String zipPath = Constant.REPORT_SAVE_PATCH + File.separator + "小鱼报表-" + statDate + ".zip";
        
        // 删除之前文件
        File file = new File(Constant.REPORT_SAVE_PATCH);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].getName().endsWith("zip") || files[i].getName().endsWith("rar"))
            {
                files[i].delete();
                continue;
            }
        }
        
        // 压缩文件
        ZipHelper.zipByFolder(zipPath, statPath);
        
        File zipFile = new File(zipPath);
        try
        {
            System.setProperty("mail.mime.splitlongparameters", "false");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("mofang_report@lunzn.com");
            helper.setTo(PropertyCache.getProp().getString("email.recipient").split(","));
            
            helper.setSubject("小鱼报表 - " + statDate);
            // 后续实现网站报表
            // helper.setText("http://xiaoyu.lunzn.com/xiaoyu/report/20171020");
            helper.setText("请查收附件...");
            
            // 添加附件
            String filename = MimeUtility.encodeText(zipFile.getName());
            filename = filename.replaceAll("\r", "").replaceAll("\n", "");
            helper.addAttachment(filename, zipFile);
            mailSender.send(mimeMessage);
        }
        catch (MessagingException me)
        {
            logger.error("Send email failed", me);
            isSuccess = Constant.FAILED;
        }
        catch (UnsupportedEncodingException uee)
        {
            logger.error("Send email failed", uee);
            isSuccess = Constant.FAILED;
        }
        return isSuccess;
    }
    
    /** 
     * 统计当日用户总量
     * @param statDate 统计日期
     * @see [类、类#方法、类#成员]
     */
    public void statTodayRegCusumers(String statDate)
    {
        // 获取当前日期的所有用户（uuid去重）log
        List<User> todayUsers = getTodayCusumer(statDate);
        
        // 获取备份用户user
        List<User> backupUsers = getBackupCusumer(statDate);
        
        // 记录新增的用户
        List<User> users = new ArrayList<User>();
        
        for (User todayUser : todayUsers)
        {
            if (!backupUsers.contains(todayUser))
            {
                users.add(todayUser);
            }
            
        }
        // 保存到ES-user表中
        if (!users.isEmpty())
        {
            userRepository.save(users);
        }
    }
    
    /** 
     * 统计日期前的所有用户注册量
     * @param statDate 统计日期
     * @return List<User>
     * @see [类、类#方法、类#成员]
     */
    private List<User> getTodayCusumer(String statDate)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        // uuid去重后统计总注册量
        /*CardinalityBuilder cardinality =
            AggregationBuilders.cardinality("distinct_uuids").field("uuid").precisionThreshold(0);*/
        TermsBuilder agg = AggregationBuilders.terms("uuid").field(XiaoyuField.UUID).size(Integer.MAX_VALUE);
        TopHitsBuilder thb = AggregationBuilders.topHits("top")
            .setFetchSource(true)
            .addSort(XiaoyuField.LOG_DATE, SortOrder.DESC)
            .setSize(1);
        agg.subAggregation(thb);
        
        search.setQuery(queryBuilder).addAggregation(agg).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.get();
        
        // 日期前的注册量
        List<User> users = new ArrayList<User>();
        StringTerms uuidSt = response.getAggregations().get("uuid");
        User user = null;
        for (Terms.Bucket sub : uuidSt.getBuckets())
        {
            user = new User();
            
            TopHits topHits = sub.getAggregations().get("top");
            SearchHits hits = topHits.getHits();
            if (ArrayUtils.isNotEmpty(hits.getHits()))
            {
                Map<String, Object> source = hits.getHits()[0].getSource();
                
                user.setUuid(getString(source, XiaoyuField.UUID));
                user.setCompany(getString(source, XiaoyuField.COMPANY));
                user.setCoversion(getString(source, XiaoyuField.COVERSION));
                user.setAppversion(getString(source, XiaoyuField.APP_VERSION));
                /*user.setLogdate(DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_SECOND_BAR,
                    getString(source, XiaoyuField.LOG_DATE)));*/
                user.setLogdate(getString(source, XiaoyuField.LOG_DATE));
                user.setIp(getString(source, XiaoyuField.IP));
                
                user.setDeviceModel(getTerminalInfo(source, XiaoyuField.DEVICE_MODEL));
                user.setKernelVsn(getTerminalInfo(source, XiaoyuField.KERNEL_VSN));
                
                users.add(user);
            }
        }
        
        return users;
    }
    
    /** 
     * 获取所有备份的用户
     * @param statDate 统计日期
     * @return List<User>
     * @see [类、类#方法、类#成员]
     */
    private List<User> getBackupCusumer(String statDate)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_USER);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .lte(DateUtil.endDate(statDate)));
        
        search.setQuery(queryBuilder).setSize(Integer.MAX_VALUE);
        
        // 搜索响应对象
        SearchResponse response = search.get();
        
        // 日期前的注册量
        List<User> users = new ArrayList<User>();
        SearchHits hits = response.getHits();
        
        if (ArrayUtils.isEmpty(hits.getHits()))
        {
            return users;
        }
        
        User user = null;
        for (SearchHit hit : hits.getHits())
        {
            user = new User();
            
            Map<String, Object> source = hit.getSource();
            
            user.setUuid(getString(source, XiaoyuField.UUID));
            user.setCompany(getString(source, XiaoyuField.COMPANY));
            user.setCoversion(getString(source, XiaoyuField.COVERSION));
            user.setAppversion(getString(source, XiaoyuField.APP_VERSION));
            /*user.setLogdate(
                DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_SECOND_BAR, getString(source, XiaoyuField.LOG_DATE)));*/
            user.setLogdate(getString(source, XiaoyuField.LOG_DATE));
            user.setIp(getString(source, XiaoyuField.IP));
            
            user.setDeviceModel(getTerminalInfo(source, XiaoyuField.DEVICE_MODEL));
            user.setKernelVsn(getTerminalInfo(source, XiaoyuField.KERNEL_VSN));
            users.add(user);
        }
        
        return users;
    }
    
    /** 
     * 记录当日用户（去重且语音优先）
     * @param statDate 统计日期
     * @see [类、类#方法、类#成员]
     */
    public void statTodayUser(String statDate)
    {
        // 今天活跃用户（语音优先）
        Map<String, DayUser> todayUsers = getTodayUser(statDate);
        
        if (MapUtils.isEmpty(todayUsers))
        {
            return;
        }
        
        // 防止重跑产生重复数据
        List<String> existUuids = getExistUser(statDate);
        
        if (CollectionUtils.isEmpty(existUuids))
        {
            dayUserRepository.save(todayUsers.values());
        }
        else
        {
            for (String uuid : existUuids)
            {
                if (todayUsers.containsKey(uuid))
                {
                    todayUsers.remove(uuid);
                }
            }
            
            if (MapUtils.isEmpty(todayUsers))
            {
                return;
            }
            
            dayUserRepository.save(todayUsers.values());
        }
    }
    
    /** 
     * 获取今天活跃用户（语音优先）
     * @param statDate 统计日期
     * @return Map<String, DayUser>
     * @see [类、类#方法、类#成员]
     */
    private Map<String, DayUser> getTodayUser(String statDate)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));
        
        // uuid去重后统计总注册量
        TermsBuilder agg = AggregationBuilders.terms("uuid").field(XiaoyuField.UUID).size(Integer.MAX_VALUE);
        TopHitsBuilder thb = AggregationBuilders.topHits("top")
            .setFetchSource(true)
            .addSort(XiaoyuField.LOG_DATE, SortOrder.DESC)
            .setSize(1);
        agg.subAggregation(thb);
        
        search.setQuery(queryBuilder).addAggregation(agg).setSize(0);
        
        // 搜索响应对象
        SearchResponse response = search.get();
        
        // 日期前的注册量
        Map<String, DayUser> users = new HashMap<String, DayUser>();
        StringTerms uuidSt = response.getAggregations().get("uuid");
        DayUser user = null;
        for (Terms.Bucket sub : uuidSt.getBuckets())
        {
            user = new DayUser();
            
            TopHits topHits = sub.getAggregations().get("top");
            SearchHits hits = topHits.getHits();
            if (ArrayUtils.isNotEmpty(hits.getHits()))
            {
                Map<String, Object> source = hits.getHits()[0].getSource();
                
                user.setUuid(getString(source, XiaoyuField.UUID));
                user.setCompany(getString(source, XiaoyuField.COMPANY));
                user.setCoversion(getString(source, XiaoyuField.COVERSION));
                user.setOpCommandType(getLong(source, XiaoyuField.OPCOMMAND_TYPE));
                /*user.setLogdate(DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_SECOND_BAR,
                    getString(source, XiaoyuField.LOG_DATE)));*/
                user.setLogdate(getString(source, XiaoyuField.LOG_DATE));
                user.setIp(getString(source, XiaoyuField.IP));
                
                users.put(user.getUuid(), user);
            }
        }
        
        // 已语音操作为条件查询今天的用户（语音优先）
        search = client.prepareSearch(Constant.ES_XIAOYU_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_LOG);
        
        queryBuilder = QueryBuilders.boolQuery();
        
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.INTERFACE_NAME, Constant.INTERFACE_LOG_REPORT).slop(0));
        // 语音操作
        queryBuilder
            .filter(QueryBuilders.matchPhraseQuery(XiaoyuField.OPCOMMAND_TYPE, Constant.OPCOMMAND_TYPE_1).slop(0));
        
        // uuid去重后统计总注册量
        agg = AggregationBuilders.terms("uuid").field(XiaoyuField.UUID).size(Integer.MAX_VALUE);
        thb = AggregationBuilders.topHits("top")
            .setFetchSource(true)
            .addSort(XiaoyuField.LOG_DATE, SortOrder.DESC)
            .setSize(1);
        agg.subAggregation(thb);
        
        search.setQuery(queryBuilder).addAggregation(agg).setSize(0);
        
        // 搜索响应对象
        response = search.get();
        
        uuidSt = response.getAggregations().get("uuid");
        for (Terms.Bucket sub : uuidSt.getBuckets())
        {
            user = new DayUser();
            
            TopHits topHits = sub.getAggregations().get("top");
            SearchHits hits = topHits.getHits();
            if (ArrayUtils.isNotEmpty(hits.getHits()))
            {
                Map<String, Object> source = hits.getHits()[0].getSource();
                
                user.setUuid(getString(source, XiaoyuField.UUID));
                user.setCompany(getString(source, XiaoyuField.COMPANY));
                user.setCoversion(getString(source, XiaoyuField.COVERSION));
                user.setOpCommandType(getLong(source, XiaoyuField.OPCOMMAND_TYPE));
                /*user.setLogdate(DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_SECOND_BAR,
                    getString(source, XiaoyuField.LOG_DATE)));*/
                user.setLogdate(getString(source, XiaoyuField.LOG_DATE));
                user.setIp(getString(source, XiaoyuField.IP));
                
                // 覆盖普通操作用户，语音优先
                users.put(user.getUuid(), user);
            }
        }
        
        return users;
    }
    
    /** 
     * 获取今天插入的用户，防止重复数据
     * @param statDate 统计日期
     * @return List<String>
     * @see [类、类#方法、类#成员]
     */
    public List<String> getExistUser(String statDate)
    {
        // ES连接对象
        Client client = esTemplate.getClient();
        
        SearchRequestBuilder search =
            client.prepareSearch(Constant.ES_XIAOYU_STAT_INDEX).setTypes(Constant.ES_XIAOYU_TYPE_DAY_USER);
        
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 设置查询时间，只查询生成报表日期之前的数据
        queryBuilder.must(QueryBuilders.rangeQuery(XiaoyuField.LOG_DATE)
            .format(DateUtil.DATE_FORMAT_SECOND_BAR)
            .gte(DateUtil.startDate(statDate))
            .lte(DateUtil.endDate(statDate)));
        
        search.setQuery(queryBuilder).setSize(Integer.MAX_VALUE);
        
        // 搜索响应对象
        SearchResponse response = search.get();
        
        // 日期前的注册量
        List<String> uuids = new ArrayList<String>();
        SearchHits hits = response.getHits();
        
        if (ArrayUtils.isEmpty(hits.getHits()))
        {
            return uuids;
        }
        
        for (SearchHit hit : hits.getHits())
        {
            Map<String, Object> source = hit.getSource();
            uuids.add(getString(source, XiaoyuField.UUID));
        }
        
        return uuids;
    }
    
    /** 
     * 获取指定值
     * @param source hit
     * @param key 字段名
     * @return String
     * @see [类、类#方法、类#成员]
     */
    private long getLong(Map<String, Object> source, String key)
    {
        Object o = source.get(key);
        if (null != o)
        {
            return Integer.parseInt(o.toString());
        }
        return 0;
    }
    
    /** 
     * 获取指定值
     * @param source hit
     * @param key 字段名
     * @return String
     * @see [类、类#方法、类#成员]
     */
    private String getString(Map<String, Object> source, String key)
    {
        Object o = source.get(key);
        if (null != o)
        {
            return o.toString();
        }
        return "";
    }
    
    /** 
     * 获取指定值
     * @param source hit
     * @param key 字段名
     * @return String
     * @see [类、类#方法、类#成员]
     */
    private String getTerminalInfo(Map<String, Object> source, String key)
    {
        Object o = source.get(key);
        if (null != o)
        {
            return o.toString();
        }
        return "--";
    }
}
