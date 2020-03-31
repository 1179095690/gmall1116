package com.chinasoft.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.annotations.LoginRequired;
import com.chinasoft.gmall.util.CookieUtil;
import com.chinasoft.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws IOException {
//        String newToken = request.getParameter("newToken");
//        if (newToken!=null&&newToken.length()>0){
//            CookieUtil.setCookie(request,response,"token",newToken,WebConst.cookieExpire,false);
//        }

        //拦截代码

        //判断被拦截的请求的访问的方法的注解（是否是需要拦截的）
        HandlerMethod hm = (HandlerMethod)handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        //是否拦截
        if (methodAnnotation==null){
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }

        String newToken = request.getParameter("token");

        //验证

        if (StringUtils.isNotBlank(newToken)){
            token = newToken;
        }

        //调用认证中心进行验证
        String success = "fail";
        Map<String,String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)){

            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }

            String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);

            successMap = JSON.parseObject(successJson, Map.class);

            success = successMap.get("status");
        }

        boolean loginSuccess=methodAnnotation.loginSuccess();
        //是否必须登录
        if (loginSuccess){
            //必须登录成功才能使用

            if (!success.equals("success")){
                //重定向回passport登陆
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+requestURL);
                return false;
            }
            //验证通过，覆盖cookie中的token
            //需要将用户携带的用户信息写入
            request.setAttribute("memberId",successMap.get("memberId"));
            request.setAttribute("nickname",successMap.get("nickname"));
        }else {
            // 没有登录也能用。但必须验证
            //验证
            if (success.equals("success")){
                //需要将用户携带的用户信息写入
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("nickname",successMap.get("nickname"));
            }

        }
        //向cookie中覆盖token
        if (StringUtils.isNotBlank(token)){
            CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
        }

        return true;
    }
}
