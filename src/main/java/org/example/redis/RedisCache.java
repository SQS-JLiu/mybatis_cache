package org.example.redis;

import org.apache.ibatis.cache.Cache;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class RedisCache implements Cache { //mybatis缓存接口

    private final String id; //mybatis唯一标识

    private String host;
    private int port;
    private String password;

    private String mybatisKey = "mybatisKey";

    public RedisCache(String id){
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    private Jedis jedis(){
        Jedis jedis = new Jedis(host,port);
        if(password != null){
            jedis.auth(password);
        }
        return jedis;
    }

    private byte[] object2Bytes(Object o){
        if(null == o){
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    private Object bytes2Object(byte[] bytes){
        if(bytes == null){
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return  objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void putObject(Object key, Object value) {
        Jedis jedis = jedis();
        try{
            jedis.hsetnx(mybatisKey.getBytes(),object2Bytes(key),object2Bytes(value));
        }finally {
            jedis.close();
        }
    }

    @Override
    public Object getObject(Object key) {
        Jedis jedis = jedis();
        try{
            byte[] bytes = jedis.hget(mybatisKey.getBytes(),object2Bytes(key));
            return bytes2Object(bytes);
        }finally {
            jedis.close();
        }
    }

    @Override
    public Object removeObject(Object key) {
        Jedis jedis = jedis();
        try{
            byte[] bytes = jedis.hget(mybatisKey.getBytes(),object2Bytes(key));
            jedis.hdel(mybatisKey.getBytes(),object2Bytes(key));
            return bytes2Object(bytes);
        }finally {
            jedis.close();
        }
    }

    @Override
    public void clear() {
        Jedis jedis = jedis();
        try{
            jedis.del(mybatisKey);
        }finally {
            jedis.close();
        }
    }

    @Override
    public int getSize() {
        Jedis jedis =jedis();
        try{
            Map<String,String> map = jedis.hgetAll(mybatisKey);
            return map.size();
        }finally {
            jedis.close();
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
