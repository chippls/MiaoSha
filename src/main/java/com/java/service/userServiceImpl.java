package com.java.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class userServiceImpl implements userService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int saveUserCount(Integer userId) {
        //根据不同用户id生成调用接口次数的key
        String limitKey = "LIMIT_"+userId;
        //获得redis中指定用户调用接口次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
        if (limitNum==null){
            //第一次调用接口，则将redis中调用次数设置为0
            stringRedisTemplate.opsForValue().set(limitKey,"0",3600, TimeUnit.SECONDS);
        }else {
            //不是第一次调用接口,则每次加1
            limit = Integer.parseInt(limitNum)+1;
            stringRedisTemplate.opsForValue().set(limitKey,String.valueOf(limit),3600,TimeUnit.SECONDS);
        }
        return limit;
    }

    @Override
    public boolean getUserCount(Integer userId) {
        String limitKey = "LIMIT_"+userId;
        String limit = stringRedisTemplate.opsForValue().get(limitKey);
        if (limit==null){
            log.error("该用户没有访问申请验证记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limit)>10;
    }
}
