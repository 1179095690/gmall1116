package com.chinasoft.gmall.service;

import com.chinasoft.gmall.entity.PmsProductImage;
import com.chinasoft.gmall.entity.PmsProductInfo;
import com.chinasoft.gmall.entity.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);
}
