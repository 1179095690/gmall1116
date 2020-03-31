package com.chinasoft.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chinasoft.gmall.entity.PmsProductImage;
import com.chinasoft.gmall.entity.PmsProductInfo;
import com.chinasoft.gmall.entity.PmsProductSaleAttr;
import com.chinasoft.gmall.manage.util.PmsUploadUtil;
import com.chinasoft.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {


    @Reference
    SpuService spuService;


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId) {
        List<PmsProductImage> pmsProductImages =  spuService.spuImageList(spuId);
        return pmsProductImages;
    }


    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> spuSaleAttrLists =  spuService.spuSaleAttrList(spuId);
        return null;
    }



    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        //将图片或音频上传到分布式的文件储存系统
        String imgUrl = PmsUploadUtil.upploadImage(multipartFile);
        System.out.println(imgUrl);
        //将图片的存储路径返回给页面
        return imgUrl;
    }


    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);

        return pmsProductInfos;
    }
}
