-- 秒杀系统数据库表结构

-- 1. SKU库存表
CREATE TABLE IF NOT EXISTS `sku_stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `act_id` VARCHAR(64) NOT NULL COMMENT '活动ID',
  `sku_id` VARCHAR(64) NOT NULL COMMENT 'SKU ID',
  `amount` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_act_sku` (`act_id`, `sku_id`, `deleted`),
  KEY `idx_act_id` (`act_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU库存表';

-- 2. 用户SKU购买记录表（用于限购检查）
CREATE TABLE IF NOT EXISTS `user_sku_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `act_id` VARCHAR(64) NOT NULL COMMENT '活动ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `sku_id` VARCHAR(64) NOT NULL COMMENT 'SKU ID',
  `buy_num` INT NOT NULL DEFAULT 0 COMMENT '购买数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_act_user_sku` (`act_id`, `user_id`, `sku_id`, `deleted`),
  KEY `idx_act_user` (`act_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户SKU购买记录表';

-- 3. 用户活动购买记录表（用于活动总限购检查）
CREATE TABLE IF NOT EXISTS `user_act_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `act_id` VARCHAR(64) NOT NULL COMMENT '活动ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `total_buy_num` INT NOT NULL DEFAULT 0 COMMENT '总购买数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_act_user` (`act_id`, `user_id`, `deleted`),
  KEY `idx_act_id` (`act_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活动购买记录表';

-- 4. 秒杀订单表
CREATE TABLE IF NOT EXISTS `seckill_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_key` VARCHAR(128) NOT NULL COMMENT '订单唯一标识',
  `act_id` VARCHAR(64) NOT NULL COMMENT '活动ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `sku_id` VARCHAR(64) NOT NULL COMMENT 'SKU ID',
  `buy_num` INT NOT NULL DEFAULT 0 COMMENT '购买数量',
  `order_time` VARCHAR(64) NOT NULL COMMENT '订单时间',
  `status` TINYINT DEFAULT 1 COMMENT '订单状态（1-成功）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_key` (`order_key`),
  KEY `idx_act_user` (`act_id`, `user_id`),
  KEY `idx_act_sku` (`act_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

