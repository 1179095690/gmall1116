package com.chinasoft.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.entity.PmsProductSaleAttr;
import com.chinasoft.gmall.entity.PmsSkuInfo;
import com.chinasoft.gmall.entity.PmsSkuSaleAttrValue;
import com.chinasoft.gmall.service.SkuService;
import com.chinasoft.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class itemController {


    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map, HttpServletRequest request){

        String remoteAddr = request.getRemoteAddr();
//        System.out.println(remoteAddr);

        //request.getHeader();//negix负载均衡代理后获取原访问路径的ip的方法


//        System.out.println(222);
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        map.put("skuInfo",pmsSkuInfo);

//        System.out.println(333);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),pmsSkuInfo.getId());
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);

        //查询当时sku对应的spu的其他sku集合的hash表集合
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());

        Map<String,String> skuSaleAttrHash = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrId()+"|";
            }

            skuSaleAttrHash.put(k,v);
        }

        //将sku销售属性的hash表放到页面
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        map.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);

        return "item";
    }



    @RequestMapping("index")
    public String index(ModelMap modelMap){

        List<String> list = new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            list.add("循环数据："+i);
        }
        modelMap.put("list",list);
        modelMap.put("hello", "hello thymeleaf !!!");
        modelMap.put("check","0");
        return "shangpinxiangqing";
    }
}
