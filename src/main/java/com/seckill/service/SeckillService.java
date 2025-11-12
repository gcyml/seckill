package com.seckill.service;


public interface SeckillService {
    public boolean skuAdd(String actId,String skuId,int amount);
    public String skuSecond(String actId,String userId,int buyNum,String skuId,int perSkuLim,int perActLim);
}
