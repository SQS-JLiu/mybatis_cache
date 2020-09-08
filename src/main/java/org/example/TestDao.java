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

public class TestDao {
    private static final Logger logger = Logger.getLogger(TestDao.class);

    public static void test() throws IOException {
        InputStream inputStream = Resources.
                getResourceAsStream("mybatis.xml");
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder()
                .build(inputStream);
        SqlSession sqlSession = sessionFactory.openSession();
        UserDO userDO = sqlSession.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO);

        UserDO userDO2 = sqlSession.
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectById",2);
        System.out.println(userDO2);
        System.out.println(userDO == userDO2);  //为true  mybatis自动开启一级缓存

        // mybatis 一级缓存命中要求:
        // 1)查询mapper.xml文件中的id要相同
        // 2)传递给sql的参数必须要相同
        // 3)要求分页的参数必须要相同
        // 4)要求最后传递给jdbc的sql语句要是相同的
        // 5)要求执行的sql环境要相同

        // mybatis 一级缓存的生命周期
        // 缓存的生成，执行select查询语句
        // 缓存的销毁：
        // 1)关闭session
        // 2)sqlSession提交
        // 3)rollback回滚
        // 4)执行更新: update, insert, delete
        // 5)执行sqlSession.clearCache()等会清空缓存

        //mybatis一级缓存不存在脏读问题(mybatis一级缓存是默认开启的)
        //mybatis一级缓存的生命周期是在数据库事务的生命周期之内的
        //不同的sqlSession,一级缓存不同，不共享. 一级缓存是基于sqlSession来讨论的
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        //test();
        getCacheClassByReflect();
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
