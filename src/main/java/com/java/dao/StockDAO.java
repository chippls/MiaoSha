package com.java.dao;

import com.java.entity.Stock;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDAO {
    //根据商品id查询库存信息
    Stock checkStock(Integer id);
    //根据商品id去扣除库存，返回值是受影响行数
    int updateSale(Stock stock);
}
