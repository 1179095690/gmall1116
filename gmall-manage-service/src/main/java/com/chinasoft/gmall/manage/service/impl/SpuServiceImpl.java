package com.chinasoft.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chinasoft.gmall.entity.PmsProductImage;
import com.chinasoft.gmall.entity.PmsProductInfo;
import com.chinasoft.gmall.entity.PmsProductSaleAttr;
import com.chinasoft.gmall.entity.PmsProductSaleAttrValue;
import com.chinasoft.gmall.manage.mapper.PmsProductImageMapper;
import com.chinasoft.gmall.manage.mapper.PmsProductInfoMapper;
import com.chinasoft.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.chinasoft.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.chinasoft.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public class SpuServiceImpl implements SpuService {


    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;


    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;



    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {

        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);

        return pmsProductInfos;
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }

        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {

        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {

        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId, skuId);
        return pmsProductSaleAttrs;
    }

//    @Override
//    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {
//
////        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
////        pmsProductSaleAttr.setProductId(productId);
////        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
////
////        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
////            String saleAttrId = productSaleAttr.getSaleAttrId();
////
////            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
////            pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
////            pmsProductSaleAttrValue.setProductId(productId);
////            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
////            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
////        }
//        System.out.println(111);
//        List<PmsProductSaleAttr> pmsProductSaleAttrs =  pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);
//        return pmsProductSaleAttrs;
//    }
}
