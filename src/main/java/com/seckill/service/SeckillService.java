package com.seckill.service;


public interface SeckillService {
    public boolean skuAdd(String actId,String skuId,int amount);
    public String skuSecond(String actId,String userId,int buyNum,String skuId,int perSkuLim,int perActLim);
    
    /**
     * 处理来自 MQ 的秒杀订单（跳过限购检查，因为 Redis 已经检查过了）
     * 只执行数据库操作：扣减库存、更新限购记录、创建订单
     * 
     * @param actId 活动id
     * @param userId 用户id
     * @param buyNum 购买数量
     * @param skuId sku的id
     * @param perSkuLim SKU级限购（用于更新限购记录，不检查）
     * @param perActLim 活动级限购（用于更新限购记录，不检查）
     * @return 订单唯一标识，失败返回 null
     */
    public String processSeckillOrderFromMQ(String actId, String userId, int buyNum, String skuId, int perSkuLim, int perActLim);
}
