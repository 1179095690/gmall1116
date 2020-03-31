package com.chinasoft.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.cart.mapper.OmsCartItemMapper;
import com.chinasoft.gmall.entity.OmsCartItem;
import com.chinasoft.gmall.service.CartService;
import com.chinasoft.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public OmsCartItem ifCartExisByUser(String memberId, String skuId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {

        if (StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);//空值不加，只加有值的
        }
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb,e);
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);

        //同步到redis缓存中
        Jedis jedis = redisUtil.getJedis();

        Map<String,String> map = new HashMap<>();
        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",map);

        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {

        Jedis jedis1 = redisUtil.getJedis();
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        String cartKey = "user:"+userId+":cart";
        boolean skuJson = jedis1.exists(cartKey);

        if (!skuJson){
            flushCartCache(userId);
        }
        List<String> hvals = jedis1.hvals("user:"+userId+":cart");

        for (String hval : hvals) {
            OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
            omsCartItems.add(omsCartItem);
        }

        jedis1.close();
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);

        //缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }


}
