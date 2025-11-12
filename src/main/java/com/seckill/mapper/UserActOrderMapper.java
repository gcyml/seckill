package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.UserActOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户活动购买记录 Mapper
 *
 * @author seckill
 */
@Mapper
public interface UserActOrderMapper extends BaseMapper<UserActOrder> {
}

