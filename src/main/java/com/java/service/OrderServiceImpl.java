package com.java.service;

import com.java.dao.OrderDAO;
import com.java.dao.StockDAO;
import com.java.entity.Order;
import com.java.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{
    @Autowired
    private StockDAO stockDAO;

    @Autowired
    private OrderDAO orderDAO;

    @Override
    public int kill(Integer id) {
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
