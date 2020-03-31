package com.chinasoft.gmall.payment.mq;

import com.chinasoft.gmall.entity.PaymentInfo;
import com.chinasoft.gmall.service.PaymentServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentServer paymentServer;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        Integer count = 0;
        if (mapMessage.getString("count")!=null) {
            count =  Integer.parseInt(""+mapMessage.getString("count"));
        }

        //调用paymentService的支付宝检查接口
        Map<String,Object> resultMap =  paymentServer.checkAlipayment(out_trade_no);

        if (resultMap!=null&&!resultMap.isEmpty()){
            String trade_status =(String)resultMap.get("trade_status");
            if (StringUtils.isNotBlank(trade_status)&&trade_status.equals("TRADE_SUCCESS")){
                //支付成功，更新支付发送支付队列
                //验签通过

                //进行支付更新的幂等性检查


                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));//支付宝的交易凭证号
                paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));//回调请求字符串
                paymentInfo.setCallbackTime(new Date());



                paymentServer.updatePayment(paymentInfo);
                return;
            }
        }
        if (count>0){
            //
            //继续发生延迟检查任务，计算延迟时间
            count--;
            paymentServer.sendDelayPaymentResultCheckQueue(out_trade_no,count);
        }else {
            System.out.println("次数用尽，结束检查");
        }
    }
}
