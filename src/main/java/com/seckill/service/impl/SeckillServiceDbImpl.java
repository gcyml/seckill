package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.SeckillOrder;
import com.seckill.entity.SkuStock;
import com.seckill.mapper.*;
import com.seckill.service.SeckillService;
import com.seckill.util.TimeUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据库版本的秒杀服务实现（用于性能对比测试）
 * 最简版：无乐观锁，直接 UPDATE
 *
 * @author seckill
 */
@Service("seckillServiceDb")
public class SeckillServiceDbImpl implements SeckillService {

    @Resource
    private SkuStockMapper skuStockMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    /**
     * 添加一个秒杀活动的sku
     * @param actId 活动id
     * @param skuId sku id
     * @param amount sku的库存数
     */
    @Override
    public boolean skuAdd(String actId, String skuId, int amount) {
        try {
            // 检查是否已存在
            LambdaQueryWrapper<SkuStock> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SkuStock::getActId, actId)
                    .eq(SkuStock::getSkuId, skuId)
                    .eq(SkuStock::getDeleted, 0);
            
            SkuStock existing = skuStockMapper.selectOne(queryWrapper);
            
            if (existing != null) {
                // 更新库存
                existing.setAmount(amount);
                skuStockMapper.updateById(existing);
            } else {
                // 新增
                SkuStock skuStock = new SkuStock();
                skuStock.setActId(actId);
                skuStock.setSkuId(skuId);
                skuStock.setAmount(amount);
                skuStockMapper.insert(skuStock);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 秒杀功能（数据库版本 - 最简版）
     * 仅用于性能测试，最简功能：扣库存 + 创建订单
     * 无乐观锁，直接 UPDATE
     * 
     * @param actId 活动id
     * @param userId 用户id
     * @param buyNum 购买数量
     * @param skuId sku的id
     * @param perSkuLim 忽略
     * @param perActLim 忽略
     * @return 秒杀的结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String skuSecond(String actId, String userId, int buyNum, String skuId, 
                           int perSkuLim, int perActLim) {
        try {
            // 1. 扣减库存（直接 UPDATE，无乐观锁）
            int updateCount = skuStockMapper.decreaseStock(actId, skuId, buyNum);
            if (updateCount == 0) {
                return "0"; // 库存不足或不存在
            }
            
            // 2. 创建订单
            int randNum = ThreadLocalRandom.current().nextInt(100000, 900001);
            String orderTime = TimeUtil.getTimeNowStr() + "-" + randNum;
            String orderKey = userId + "_" + skuId + "_" + buyNum + "_" + orderTime;
            
            SeckillOrder order = new SeckillOrder();
            order.setOrderKey(orderKey);
            order.setActId(actId);
            order.setUserId(userId);
            order.setSkuId(skuId);
            order.setBuyNum(buyNum);
            order.setOrderTime(orderTime);
            order.setStatus(1);
            seckillOrderMapper.insert(order);
            
            return orderKey;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("秒杀失败", e);
        }
    }
}

