package com.java.service;

import org.springframework.stereotype.Repository;


public interface OrderService {
    //处理秒杀的下订单方法，并返回订单的id
    int kill(Integer id);
}
