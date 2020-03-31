package com.chinasoft.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.chinasoft.gmall.entity.PmsSearchSkuInfo;
import com.chinasoft.gmall.entity.PmsSkuInfo;
import com.chinasoft.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {


    @Reference
    SkuService skuService;//查询mysql

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {


//
//        Search search = new Search.Builder("{\n" +
//                "  \"query\": {\n" +
//                "    \"bool\": {\n" +
//                "      \"filter\": [\n" +
//                "        {\n" +
//                "          \"terms\":{\n" +
//                "            \"skuAttrValueList.valueId\":[\"49\",\"51\"]\n" +
//                "          }\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"term\": {\n" +
//                "          \"skuAttrValueList.valueId\": \"49\"\n" +
//                "          } \n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"term\": {\n" +
//                "          \"skuAttrValueList.valueId\": \"51\"\n" +
//                "          }\n" +
//                "        }\n" +
//                "      ],\n" +
//                "      \"must\": [\n" +
//                "        {\n" +
//                "          \"match\": {\n" +
//                "            \"skuName\": \"小米\"\n" +
//                "          }\n" +
//                "        }\n" +
//                "      ]\n" +
//                "    }\n" +
//                "  }\n" +
//                "}").addIndex("gmall").addType("PmsSkuInfo").build();

        //jest的dsl工具

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","49");
        boolQueryBuilder.filter(termQueryBuilder);
        //TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("","");
        //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","小米");
        boolQueryBuilder.must(matchQueryBuilder);
        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        searchSourceBuilder.highlight(null);

        String dslStr = searchSourceBuilder.toString();

        System.out.println(dslStr);


        //用api执行复杂查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        Search search = new Search.Builder(dslStr).addIndex("gmall").addType("PmsSkuInfo").build();

        JestResult execute = jestClient.execute(search);
        List<SearchResult.Hit<PmsSearchSkuInfo,Void>>  hits =   ((SearchResult) execute).getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfos.add(source);
        }

        System.out.println(pmsSearchSkuInfos.size());
    }

    public void put() throws IOException {
        //查询mysql
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();

        pmsSkuInfoList = skuService.getAllSku();
        //转化为es的数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }

        //导入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(put);
        }
    }

}
