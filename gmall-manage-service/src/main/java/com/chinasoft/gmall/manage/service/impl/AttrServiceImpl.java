package com.chinasoft.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.chinasoft.gmall.entity.PmsBaseAttrInfo;
import com.chinasoft.gmall.entity.PmsBaseAttrValue;
import com.chinasoft.gmall.entity.PmsBaseSaleAttr;
import com.chinasoft.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.chinasoft.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.chinasoft.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.chinasoft.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class AttrServiceImpl implements AttrService{

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;


    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            List<PmsBaseAttrValue> pmsBaseAttrValues = new ArrayList<>();

            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());

            pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }

        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();
        if (StringUtils.isBlank(id)){
            //insert 有的没的都插入数据库
            // insertSertive 只将有值的非空的插入数据库
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
            return "success";
        }else {
            //id不空，修改属性
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            //属性值
            List<PmsBaseAttrValue> attrValueList  =  pmsBaseAttrInfo.getAttrValueList();


            PmsBaseAttrValue pmsBaseAttrValue1 = new PmsBaseAttrValue();
            pmsBaseAttrValue1.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue1);

            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
            return "success";
        }
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {

        String valueIdStr = StringUtils.join(valueIdSet, ",");//41,45,46
        List<PmsBaseAttrInfo> pmsBaseAttrInfos =  pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);

        return pmsBaseAttrInfos;
    }


}
