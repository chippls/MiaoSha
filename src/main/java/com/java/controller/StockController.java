package com.java.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.java.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("stock")
@Slf4j
public class StockController {

    //创建令牌桶实例
    private RateLimiter rateLimiter = RateLimiter.create(20);

    //开发秒杀方法
    @Autowired
    private OrderService orderService;

    @GetMapping("kill")
    //开发秒杀方法，使用乐观锁防止超卖
    public String kill(Integer id){
        System.out.println("id = "+id);//秒杀商品id
        //根据秒杀商品id去执行秒杀业务
        try {
            int orderID = orderService.kill(id);
            return "秒杀成功，订单id为"+String.valueOf(orderID);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("killToken")
    public String killToken(Integer id){
        System.out.println("id="+id);
        if (!rateLimiter.tryAcquire(2,TimeUnit.SECONDS)){
            log.info("抛弃请求；抢购失败，当前秒杀活动过于火爆，请重试");
            return "活动火爆，请稍后再试";
        }
        try{
            int orderID = orderService.kill(id);
            return "秒杀成功，订单id为"+orderID;
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
