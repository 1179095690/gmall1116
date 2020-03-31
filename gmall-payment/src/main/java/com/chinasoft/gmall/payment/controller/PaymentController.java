package com.chinasoft.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.chinasoft.gmall.annotations.LoginRequired;
import com.chinasoft.gmall.entity.OmsOrder;
import com.chinasoft.gmall.entity.PaymentInfo;
import com.chinasoft.gmall.payment.config.AlipayConfig;
import com.chinasoft.gmall.service.OrderService;
import com.chinasoft.gmall.service.PaymentServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentServer paymentServer;

    @Reference
    OrderService orderService;


    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String aa){
        return null;
    }


    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String aliPayBackReturn(HttpServletRequest request){

        //回调请求中获得支付宝参数验签
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        //通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if(StringUtils.isNotBlank(sign)){
            //验签通过

            //进行支付更新的幂等性检查

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());

            //更新用户支付状态
            //支付成功后，引起的系统服务，订单服务的更新，库存服务，物流
            //调用mq发送支付成功的消息
            paymentServer.updatePayment(paymentInfo);
        }
        return "finish";
    }


    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo,BigDecimal totalAmount){

        //获得一个支付宝请求的客户端(它并不是一个链接，而是一个封装好的http的表单请求)
        String form = null;
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request

        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject","honor20pro");

        String param= JSON.toJSONString(map);

        alipayRequest.setBizContent(param);

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //生成并保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("gmall商城商品一件");
        paymentInfo.setTotalAmount(totalAmount);
        paymentServer.savePaymentInfo(paymentInfo);

        //向消息中间件发送一个检查支付状态(支付服务消费)的延迟消息队列
        paymentServer.sendDelayPaymentResultCheckQueue(outTradeNo,5);



        //提交请求到支付宝
        return form;
    }




    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");

        modelMap.put("nickname",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }

}
