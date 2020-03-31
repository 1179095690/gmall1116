package com.chinasoft.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chinasoft.gmall.entity.PmsBaseCatalog1;
import com.chinasoft.gmall.entity.PmsBaseCatalog2;
import com.chinasoft.gmall.entity.PmsBaseCatalog3;
import com.chinasoft.gmall.service.CataService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@CrossOrigin
public class CatalogController {

    @Reference
    CataService cataService;


    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatelog1(){
         List<PmsBaseCatalog1> catalog1s = cataService.getCatalog1();
        return catalog1s;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatelog2(String catalog1Id){
        List<PmsBaseCatalog2> catalog2s = cataService.getCatalog2(catalog1Id);
        return catalog2s;
    }


    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatelog3(String catalog2Id){
        List<PmsBaseCatalog3> catalog3s = cataService.getCatalog3(catalog2Id);
        return catalog3s;
    }

}
