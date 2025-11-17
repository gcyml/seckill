package com.seckill.mq;


import lombok.Getter;
import lombok.Setter;

/**
 * @program: SeckillProject
 * @description: 秒杀消息封装
 **/
@Getter
@Setter
public class SeckillMessage {
    private String userId;
    private String actId;
    private String skuId;
    private int buyNum;
    /**
     * SKU级限购（每个用户购买当前sku的个数限制，0表示不限制）
     */
    private int perSkuLim;
    /**
     * 活动级限购（每个用户购买当前活动内所有sku的总数量限制，0表示不限制）
     */
    private int perActLim;
}

