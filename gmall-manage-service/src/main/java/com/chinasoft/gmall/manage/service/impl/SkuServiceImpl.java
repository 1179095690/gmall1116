package com.chinasoft.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.entity.PmsSkuAttrValue;
import com.chinasoft.gmall.entity.PmsSkuImage;
import com.chinasoft.gmall.entity.PmsSkuInfo;
import com.chinasoft.gmall.entity.PmsSkuSaleAttrValue;
import com.chinasoft.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.chinasoft.gmall.manage.mapper.PmsSkuImageMapper;
import com.chinasoft.gmall.manage.mapper.PmsSkuInfoMapper;
import com.chinasoft.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.chinasoft.gmall.service.SkuService;
import com.chinasoft.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;


    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {


        //插入skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();

        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        //插入图片属性关联
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId){
        //sky商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //sku图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);
        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {

//        //sky商品对象
//        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
//        pmsSkuInfo.setId(skuId);
//        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
//
//        //sku图片集合
//        PmsSkuImage pmsSkuImage = new PmsSkuImage();
//        pmsSkuImage.setSkuId(skuId);
//        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
//        skuInfo.setSkuImageList(pmsSkuImages);


        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:"+skuId+":info";
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)){  //if(skuJson!=null&&!skuJson.equals(""))
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        }else {
            //如果缓存没有。查mysql

            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10*1000);
            if (StringUtils.isNotBlank(OK)&&OK.equals("OK")){
                //设置成功，有权在10秒的过程中访问数据库
                pmsSkuInfo = getSkuByIdFromDb(skuId);
                //把mysql查到的存入redis
                if (pmsSkuInfo!=null){
                    jedis.set("sku:"+skuId+":info",JSON.toJSONString(pmsSkuInfo));

                }else {
                    //数据库中不存在该sku
                    //为了防止缓存穿透，将null或者空字符串设置给redis
                    jedis.setex("sku:"+skuId+":info",60*3,JSON.toJSONString(""));
                }

                //在访问mysql后，将mysql的分布式锁释放
                String loclToken = jedis.get("sku:" + skuId + ":lock");
                if (StringUtils.isNotBlank(loclToken)&&loclToken.equals(token)){
//                    jedis.eval("lua"); //可以用lua脚本，在查询到key的同时删除该key，防止高并发下的意外发生
                    jedis.del("sku:"+skuId+":lock");//用token确认删除的是自己的sku的锁
                }
            }else {
                //设置失败，自旋(该线程在睡眠几秒之后，重新尝试访问本方法)
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);

            }
        }
        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos =  pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }

        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {

        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price = pmsSkuInfo1.getPrice();

        if (price.compareTo(productPrice)==0){
            b = true;
        }

        return b;
    }
}
