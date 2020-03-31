package com.chinasoft.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chinasoft.gmall.entity.PmsSkuInfo;
import com.chinasoft.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;

    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){


        //将spuId封装给productId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());

        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }

        skuService.saveSkuInfo(pmsSkuInfo);

        return "success";
    }
}
