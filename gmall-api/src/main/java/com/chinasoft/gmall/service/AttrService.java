package com.chinasoft.gmall.service;

import com.chinasoft.gmall.entity.PmsBaseAttrInfo;
import com.chinasoft.gmall.entity.PmsBaseAttrValue;
import com.chinasoft.gmall.entity.PmsBaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface AttrService {
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);
}
