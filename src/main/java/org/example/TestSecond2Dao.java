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

public class TestSecond2Dao {
    private static final Logger logger = Logger.getLogger(TestSecond2Dao.class);

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
                selectOne("org.example.resource.mapping.gen.UserDOMapper.selectByPrimaryKey",2);
        System.out.println(userDO2);
        System.out.println(userDO == userDO2);  //为true  mybatis开启二级缓存
        sqlSession2.close();
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        test();
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
