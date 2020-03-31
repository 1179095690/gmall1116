package com.chinasoft.gmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {

    public static void main(String[] args){

        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickname","czh");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyymmdd HHmmss").format(new Date());

        String encode = JwtUtil.encode("2020gmall1116", map, ip + time);

        System.out.println(encode);
    }
}
