package com.java.service;

import com.java.dao.OrderDAO;
import com.java.dao.StockDAO;
import com.java.dao.userDAO;
import com.java.entity.Order;
import com.java.entity.Stock;
import com.java.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService{
    @Autowired
    private StockDAO stockDAO;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private userDAO userDAO;

    @Override
    public int kill(Integer id, Integer userid, String md5) {
        //Redis有效，若库存有效则继续——校验redis中秒杀商品是否超时
//        if (!stringRedisTemplate.hasKey("kill"+id)) {
//            throw new RuntimeException("当前商品的抢购活动已结束");
//        }
        //验证签名
        String hashKey = "KEY_"+userid+"_"+id;
        String s = stringRedisTemplate.opsForValue().get(hashKey);
        if (s==null) throw new RuntimeException("未携带签名，请求不合法");
        if (!s.equals(md5))
            throw new RuntimeException("当前请求数据不合法，请稍后再试");
        //根据商品id去校验库存
        Stock stock = checkStock(id);
        //扣除库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public String getMd5(Integer id, Integer userId) {
        //验证userid，存在用户信息
        User user = userDAO.findById(userId);
        if (user==null) throw new RuntimeException("用户信息不存在");
        log.info("用户信息:[{}]",user.toString());
        //验证id      存在商品信息
        Stock stock = stockDAO.checkStock(id);
        if (stock==null) throw new RuntimeException("商品信息不合法");
        log.info("商品信息:[{}]",stock.toString());
        //生成hashKey
        String hashKey = "KEY_"+userId+"_"+id;
        //生成md5签名放入redis服务
        String key = DigestUtils.md5DigestAsHex((userId+id+"!QS#").getBytes());//!QS#是一个盐，通常应该用一个工具类实现
        stringRedisTemplate.opsForValue().set(hashKey,key,3600, TimeUnit.SECONDS);
        log.info("Redis写入",hashKey,key);
        return key;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int kill(Integer id) {
        //Redis有效，若库存有效则继续——校验redis中秒杀商品是否超时
        if (!stringRedisTemplate.hasKey("kill"+id)) {
            throw new RuntimeException("当前商品的抢购活动已结束");
        }
        //根据商品id去校验库存
        Stock stock = checkStock(id);
        //扣除库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    //校验库存
    private Stock checkStock(Integer id){
        Stock stock = stockDAO.checkStock(id);
        if (stock.getSale().equals(stock.getCount())){
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    //扣除库存
    private void updateSale(Stock stock){
        //在sql层面更新库存和版本号（数据库不支持并发读写），并且根据商品id和版本号同时查询更新的商品
        int updateRows = stockDAO.updateSale(stock);
        if (updateRows==0){
            throw new RuntimeException("抢购失败");
        }
    }

    //创建订单
    private Integer createOrder(Stock stock){
        Order order = new Order();
        order.setSid(stock.getId()).setName(stock.getName()).setCreate_time(new Date());
        orderDAO.createOrder(order);
        return order.getId();
    }
}
