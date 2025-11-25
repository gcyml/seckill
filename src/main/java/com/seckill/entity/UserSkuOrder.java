package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户SKU购买记录实体类
 * 限购检查用
 * @author seckill
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_sku_order")
public class UserSkuOrder extends BaseEntity {

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
}

