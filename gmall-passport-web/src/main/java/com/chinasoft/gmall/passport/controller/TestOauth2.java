package com.chinasoft.gmall.passport.controller;

import com.chinasoft.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {

    public static String getCode(){
        //获得授权码

        return null;
    }


    public static String getAccess_token(){

        //使用授权码和key和client_secret换取access_token

        Map<String,String> paramMap = new HashMap<>();
//        里面放入参数
//        client_id
//        client_secret
//        grant_type
//        redirect_url
//        code
        paramMap.put("","");
//        只能使用post请求，安全性
        HttpclientUtil.doPost("",paramMap);
        return null;
    }
}
