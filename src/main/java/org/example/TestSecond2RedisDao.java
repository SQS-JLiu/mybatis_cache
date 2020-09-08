package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.example.resource.domain.gen.UserDO;

import java.io.IOException;
import java.io.InputStream;

public class TestSecond2RedisDao {
    private static final Logger logger = Logger.getLogger(TestSecond2RedisDao.class);

    public static void testEnvironment() throws IOException {
        logger.info("");
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

        //注意: 对于二级缓存的配置不能应用于自定义缓存：
        //清除策略eviction, size引用数目,
        //flushInterval刷新间隔, readOnly只读
        // blocking阻塞
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        testEnvironment();
    }
}
