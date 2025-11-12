package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.UserSkuOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户SKU购买记录 Mapper
 *
 * @author seckill
 */
@Mapper
public interface UserSkuOrderMapper extends BaseMapper<UserSkuOrder> {
}

