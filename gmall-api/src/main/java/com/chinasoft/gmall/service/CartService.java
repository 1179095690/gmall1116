package com.chinasoft.gmall.service;

import com.chinasoft.gmall.entity.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem ifCartExisByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String userId);

    void checkCart(OmsCartItem omsCartItem);
}
