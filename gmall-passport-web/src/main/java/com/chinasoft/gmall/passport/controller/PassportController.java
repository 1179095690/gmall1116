package com.chinasoft.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.entity.UmsMember;
import com.chinasoft.gmall.service.CartService;
import com.chinasoft.gmall.service.UserService;
import com.chinasoft.gmall.util.HttpclientUtil;
import com.chinasoft.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {


    @Reference
    UserService userService;


    @RequestMapping("vlogin")
    @ResponseBody
    public String vlogin(String code,HttpServletRequest request){


        //授权码换取access_token

        Map<String,String> paramMap = new HashMap<>();
//        里面放入参数
//        client_id
//        client_secret
//        grant_type
//        redirect_url
//        code
        paramMap.put("","");
//        只能使用post请求，安全性
        String access_token_json = HttpclientUtil.doPost("", paramMap);
        Map<String,Object> access_map = JSON.parseObject(access_token_json, Map.class);

        String uid = (String)access_map.get("uid");
        String accsee_token = (String)access_map.get("access_token");

        //accsee_token换取用户信息
        String show_user_url="";
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String,Object> user_map = JSON.parseObject(user_json,Map.class);

        //将用户信息保存数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(accsee_token);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String)user_map.get("location"));
        umsMember.setNickname((String)user_map.get("scree_name"));
        String gender = (String)user_map.get("gender");
        String g = "0";
        if (gender.equals("m")){
            g = "1";
        }
        umsMember.setGender(g);
        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck); //检查该用户（社交用户是否以前登录过系统）

        if (umsMemberCheck==null){
            umsMember = userService.addOauthUser(umsMember);
        }else {
            umsMember = umsMemberCheck;
        }

        //生成jwt的token，并且重定向到首页，携带该token

        String token = null;
        String memberId = umsMember.getId(); //rpc的主键返回策略失效，因为controller层的这个对象和service层的这个对象不是同一个对象，分开了
        String nickname = umsMember.getNickname();
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId); //保存数据库后主键返回策略生成的id
        userMap.put("nickname",nickname);
        String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();
            if (StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }

        //需要按照设计的算法对参数进行加密，生成token
        token = JwtUtil.encode("2020gmall1116", userMap, ip);
        //将token存入redis一份
        userService.addUserToken(token,memberId);



        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String currentIp){

        //通过jwt校验token真假
        Map<String,String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2020gmall1116", currentIp);

        if (decode!=null){
            map.put("status","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("nickname",(String)decode.get("nickname"));

        }else {
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }



    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token = "";
        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin!=null){
            //登录成功

            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String,Object> userMap = new HashMap<>();
            userMap.put("memberId",memberId);
            userMap.put("nickname",nickname);
            String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }

            //需要按照设计的算法对参数进行加密，生成token
            token = JwtUtil.encode("2020gmall1116", userMap, ip);
            //将token存入redis一份
            userService.addUserToken(token,memberId);
        }else {
            //登录失败
            token = "fail";
        }

        return token;
    }


    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){

        map.put("ReturnUrl",ReturnUrl);
        return "index";
    }
}
