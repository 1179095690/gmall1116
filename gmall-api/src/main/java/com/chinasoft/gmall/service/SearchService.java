package com.chinasoft.gmall.service;

import com.chinasoft.gmall.entity.PmsSearchParam;
import com.chinasoft.gmall.entity.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
