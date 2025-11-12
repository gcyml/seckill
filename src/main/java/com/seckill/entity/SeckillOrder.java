package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀订单实体类
 *
 * @author seckill
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_order")
public class SeckillOrder extends BaseEntity {

    /**
     * 订单唯一标识
     */
    private String orderKey;

    /**
     * 活动ID
     */
    private String actId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * SKU ID
     */
    private String skuId;

    /**
     * 购买数量
     */
    private Integer buyNum;

    /**
     * 订单时间
     */
    private String orderTime;

    /**
     * 订单状态（1-成功）
     */
    private Integer status;
}

