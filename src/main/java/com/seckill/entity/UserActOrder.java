package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户活动购买记录实体类
 * 限购检查用
 * @author seckill
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_act_order")
public class UserActOrder extends BaseEntity {

    /**
     * 活动ID
     */
    private String actId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 总购买数量
     */
    private Integer totalBuyNum;
}

