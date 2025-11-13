package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU库存实体类
 *
 * @author seckill
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sku_stock")
public class SkuStock extends BaseEntity {

    /**
     * 活动ID
     */
    private String actId;

    /**
     * SKU ID
     */
    private String skuId;

    /**
     * 库存数量
     */
    private Integer amount;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;
}

