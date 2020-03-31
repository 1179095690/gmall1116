package com.chinasoft.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.annotations.LoginRequired;
import com.chinasoft.gmall.entity.OmsCartItem;
import com.chinasoft.gmall.entity.PmsSkuInfo;
import com.chinasoft.gmall.service.CartService;
import com.chinasoft.gmall.service.SkuService;
import com.chinasoft.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {


    @Reference
    CartService cartService;

    @Reference
    SkuService skuService;


    @LoginRequired(loginSuccess=false)
    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId,HttpServletRequest request,HttpServletResponse response,ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        //调用服务，修改状态
        cartService.checkCart(omsCartItem);

        //将最新的数据从缓存冲查出，渲染给内嵌页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        //被勾选的商品的总额
        BigDecimal totaAmount = getTotaAmount(omsCartItems);
        modelMap.put("totaAmount",totaAmount);
        return "cartListInner";
    }


    @RequestMapping("cartList")
    @LoginRequired(loginSuccess=false)
    public String cartList(HttpServletRequest request, HttpServletResponse response,ModelMap modelMap){

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        if (StringUtils.isNotBlank(memberId)){
            //已经登录查询db
            omsCartItems = cartService.cartList(memberId);
        }else {
            //没有登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList",omsCartItems);
        //被勾选的商品的总额
        BigDecimal totaAmount = getTotaAmount(omsCartItems);
        modelMap.put("totaAmount",totaAmount);
        return "cartList";
    }

    private BigDecimal getTotaAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totaAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")){
                totaAmount = totaAmount.add(totalPrice);
            }
        }

        return totaAmount;
    }


    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess=false)
    public String addToCart(String skuId,int quantity, HttpServletRequest request, HttpServletResponse response){

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setIsChecked("1");


        //判断用户是否登录
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
//        request.getAttribute("memberId");

         if (StringUtils.isBlank(memberId)){
             //用户没有登录
            //Cookie原有的购物车数据
             String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
             if (StringUtils.isBlank(cartListCookie)){
                 omsCartItems.add(omsCartItem);
             }else {
                 omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                 //判断添加的购物车数据在原cookie中是否存在
                 boolean exist = if_cart_exist(omsCartItems,omsCartItem);
                 
                 if (exist){
                     //之前添加过，更新购物车数据
                     for (OmsCartItem cartItem : omsCartItems) {
                         if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                             cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                         }
                     }
                 }else {
                     //之前没有添加过，新增当前的购物车
                     omsCartItems.add(omsCartItem);
                 }
             }
             //更新Cookie
             CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*72,true);
         }else {
             //用户已经登录
            OmsCartItem omsCartItemFromDb  = cartService.ifCartExisByUser(memberId,skuId);

            if (omsCartItemFromDb == null){
                //该用户没有添加够哦当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("test小明");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);
            }else {
                //用户添加过当前商品
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
            //同步缓存
             cartService.flushCartCache(memberId);

         }

        return "redirect:/success.html";
    }

    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {

        boolean b = false;

        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();
            if (productSkuId.equals(omsCartItem.getProductSkuId())){
                b = true;
            }
        }
        return b;
    }


}
