package com.chinasoft.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chinasoft.gmall.annotations.LoginRequired;
import com.chinasoft.gmall.entity.*;
import com.chinasoft.gmall.service.AttrService;
import com.chinasoft.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {


    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){//三级分类参数，关键字，平台属性集合

        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);

        //抽取检索结果锁包含的平台属性集合
        Set<String>  valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfos);

        //对平台属性集合进一步处理，去掉当前条件中value所在的属性组
        String[] delvalueIds = pmsSearchParam.getValueId();

        List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        if (delvalueIds!=null){
            for (String delvalueId : delvalueIds) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                //面包屑
                //如不为空
                //当前请求中包含属性的参数，每一个属性参数都会生成一个面包屑
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delvalueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForGrumb(pmsSearchParam,delvalueId));

                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String ValueId = pmsBaseAttrValue.getId();
                        if (delvalueId.equals(ValueId)) {
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
        }
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam",urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword",keyword);

        }
        modelMap.put("attrValueSelectedList",pmsSearchCrumbs);

        return "list";
    }


    private String getUrlParamForGrumb(PmsSearchParam pmsSearchParam,String delValueId) {

        String keyword  = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;

        }

        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }

        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueId)){
                    urlParam = urlParam+"&valueId="+pmsSkuAttrValue;
                }
            }
        }

        return urlParam;
    }


    private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delValueId) {

        String keyword  = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;

        }

        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }

        if (skuAttrValueList!=null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam+"&valueId="+pmsSkuAttrValue;
            }
        }

        return urlParam;
    }
}
