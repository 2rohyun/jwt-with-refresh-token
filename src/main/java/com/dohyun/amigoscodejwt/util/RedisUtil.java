package com.dohyun.amigoscodejwt.util;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getData(String key){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key,value);
    }

    public void setDataExpire(String key,String value,long duration){
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key,value,expireDuration);
    }

    public void addDataFromList(String key, String value) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        listOperations.leftPush(key,value);
    }

    public String getRecentDataFromList(String key) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        return listOperations.index(key,0);
    }

    public Long getIndexOfList(String key, String value) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        return listOperations.indexOf(key, value);
    }

    public void deleteData(String key){
        redisTemplate.delete(key);
    }
}
