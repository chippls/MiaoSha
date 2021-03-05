package com.java.dao;

import com.java.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface userDAO {
    User findById(Integer id);
}
