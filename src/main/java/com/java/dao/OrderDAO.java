package com.java.dao;

import com.java.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDAO {
    //生成订单
    void createOrder(Order order);
}
