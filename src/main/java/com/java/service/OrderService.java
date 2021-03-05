package com.java.service;

import org.springframework.stereotype.Repository;


public interface OrderService {
    //处理秒杀的下订单方法，并返回订单的id
    int kill(Integer id);
    //用来获取Md5方法
    String getMd5(Integer id, Integer userId);
    //用于处理秒杀的下单方法，并返回订单id，加入md5实现接口隐藏
    int kill(Integer id, Integer userid, String md5);
}
