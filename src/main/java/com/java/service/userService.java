package com.java.service;

import org.springframework.stereotype.Service;


public interface userService {
    //向redis中写入用户访问次数
    int saveUserCount(Integer userId);
    //判断redis中读取用户访问次数
    boolean getUserCount(Integer userId);
}
