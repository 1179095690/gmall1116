package com.chinasoft.gmall.service;

import com.chinasoft.gmall.entity.PaymentInfo;

import java.util.Map;

public interface PaymentServer {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String outTradeNo,int count);

    Map<String, Object> checkAlipayment(String out_trade_no);
}
