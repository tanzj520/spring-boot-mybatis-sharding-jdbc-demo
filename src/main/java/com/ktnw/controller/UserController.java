package com.ktnw.controller;

import com.ktnw.entity.User;
import com.ktnw.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("createUser")
    public User createUser(){
        User user = new User();
        user.setUserId(100);
        user.setName("张三");
        user.setMobile("1342121421");
        user.setAddress("阿诗丹顿啊是多少撒按时打撒");
        this.userMapper.createUser(user);
        user.setUserId(101);
        user.setName("李四");
        user.setMobile("1342121422");
        user.setAddress("阿诗丹顿啊是多少撒按时打撒");
        this.userMapper.createUser(user);
        return user;
    }
}
