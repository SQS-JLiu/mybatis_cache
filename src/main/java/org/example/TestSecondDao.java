package org.example;

import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.example.resource.domain.gen.UserDO;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

public class TestSecondDao {
    private static final Logger logger = Logger.getLogger(TestSecondDao.class);

    public static void test() throws IOException {
        InputStream inputStream = Resources.
                getResourceAsStream("mybatis.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder()
                .build(inputStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserDO userDO = sqlSession.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO);
        sqlSession.close();

        SqlSession sqlSession2 = sessionFactory.openSession();
        UserDO userDO2 = sqlSession2.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO2);
        System.out.println(userDO == userDO2);  //为true  mybatis开启二级缓存
        sqlSession2.close();
    }

    public static void testEnvironment() throws IOException {
        InputStream inputStream = Resources.
                getResourceAsStream("mybatis.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder()
                .build(inputStream,"development");
        SqlSession sqlSession = sessionFactory.openSession();
        UserDO userDO = sqlSession.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO);
        sqlSession.close();

        InputStream inputStream2 = Resources.
                getResourceAsStream("mybatis.xml");
        SqlSessionFactory sessionFactory2 = new SqlSessionFactoryBuilder()
                .build(inputStream2,"development");
        SqlSession sqlSession2 = sessionFactory2.openSession();
        UserDO userDO2 = sqlSession2.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO2);
        System.out.println(userDO == userDO2);  //为true  mybatis开启二级缓存
        sqlSession2.close();
        // 二级缓存是在同一个SqlSessionFactory内
        // 二级缓存命中的条件：
        // 1) StatementId
        // 2) 查询参数
        // 3) 分页参数
        // 4) sql语句
        // 5) 同一环境
        // 二级缓存的生命周期
        // 生成二级级缓存条件:1) 满足一级缓存的条件 2) close Session或commit Session
        // 二级缓存销毁：
        // 1)update更新会销毁缓存
        // 2)回滚Rollback和清除clearCache不会销毁缓存

        //flushCache和useCache在xml的查询等标签中使用来显示标识
        // 是生成缓存还是销毁缓存

        //eviction清除缓存的策略:
        // LRU最近最少使用, FIFO先进先出,SOFT,WEAK
        // size引用数据，当缓存达到一定值(默认：1024)时执行清除策略
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        testEnvironment();
    }

    public static void getCacheClassByReflect() throws IllegalAccessException, NoSuchFieldException, IOException {
        //获取一级缓存的类
        InputStream inputStream = Resources.
                getResourceAsStream("mybatis.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder()
                .build(inputStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserDO userDO = sqlSession.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO);

        Field executorField = sqlSession.getClass().getDeclaredField("executor");
        executorField.setAccessible(true);

        CachingExecutor cachingExecutor = (CachingExecutor) executorField.get(sqlSession);
        Field declaredField = cachingExecutor.getClass().getDeclaredField("delegate");
        declaredField.setAccessible(true);
        SimpleExecutor simpleExecutor = (SimpleExecutor) declaredField.get(cachingExecutor);

        Field localCacheField = simpleExecutor.getClass().getSuperclass().getDeclaredField("localCache");
        localCacheField.setAccessible(true);
        PerpetualCache perpetualCache = (PerpetualCache) localCacheField.get(simpleExecutor);

        Field cacheField = perpetualCache.getClass().getDeclaredField("cache");
        cacheField.setAccessible(true);

        Map<Object, Object> map = (Map<Object, Object>) cacheField.get(perpetualCache);
        // 关闭session前缓存情况
        System.out.println("关闭session前缓存情况");
        for(Object key : map.keySet()){
            System.out.println("############"+key+" === "+map.get(key));
        }
        sqlSession.close();
        // 关闭session后缓存情况
        System.out.println("关闭session后缓存情况:");
        for(Object key : map.keySet()){
            System.out.println(key+" = "+map.get(key));
        }

    }
}
