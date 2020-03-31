package com.chinasoft.gmall.service;

import com.chinasoft.gmall.entity.PmsBaseCatalog1;
import com.chinasoft.gmall.entity.PmsBaseCatalog2;
import com.chinasoft.gmall.entity.PmsBaseCatalog3;

import java.util.List;

public interface CataService {

    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
