package com.chinasoft.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chinasoft.gmall.entity.User;
import com.chinasoft.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    UserService userService;


    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "hello user";
    }

    @RequestMapping("getAllUser")
    @ResponseBody
    public List<User> getAllUser(){
//        List<User> userList = userService.getAllUserser();
        return null;
    }

//    public  User getUserById(int id){
//        User u = userService.getNameById(id);
//        return u;
//    }
}
