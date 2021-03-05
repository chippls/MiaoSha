package com.java.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.java.service.OrderService;
import com.java.service.userService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Autowired
    private userService userService;

    @RequestMapping("md5")
    public String getMd5(Integer id, Integer userId){
        String md5;
        try {
            md5 = orderService.getMd5(id,userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取md5失败"+e.getMessage();
        }
        return "获取md5信息为："+md5;
    }

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

    @GetMapping("killTokenMd5")//乐观锁防止超卖+令牌桶算法限流+md5签名（hash接口隐藏）
    public String killToken(Integer id, Integer userId, String md5){
        System.out.println("id="+id);
        if (!rateLimiter.tryAcquire(2,TimeUnit.SECONDS)){
            log.info("抛弃请求；抢购失败，当前秒杀活动过于火爆，请重试");
            return "活动火爆，请稍后再试";
        }
        try{
            int orderID = orderService.kill(id,userId,md5);
            return "秒杀成功，订单id为"+orderID;
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("killTokenMd5limit")//乐观锁防止超卖+令牌桶算法限流+md5签名（hash接口隐藏）+单用户访问频率限制
    public String killTokenLimit(Integer id, Integer userId, String md5){
        if (!rateLimiter.tryAcquire(2,TimeUnit.SECONDS)){
            log.info("抛弃请求；抢购失败，当前秒杀活动过于火爆，请重试");
            return "活动火爆，请稍后再试";
        }
        try{
            //单用户调用接口频率限制
            int userCount = userService.saveUserCount(userId);
            log.info("用户截至该次的访问次数为：[{}]",userCount);
            //进行调用次数的判断
            boolean isBanned = userService.getUserCount(userId);
            if (isBanned){
                log.info("购买失败，超过频率限制！");
                return "购买失败，超过频率限制！";
            }
            int orderID = orderService.kill(id,userId,md5);
            return "秒杀成功，订单id为"+orderID;
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
