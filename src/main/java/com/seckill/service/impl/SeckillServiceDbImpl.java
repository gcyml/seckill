package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.SeckillOrder;
import com.seckill.entity.SkuStock;
import com.seckill.entity.UserActOrder;
import com.seckill.entity.UserSkuOrder;
import com.seckill.mapper.*;
import com.seckill.service.SeckillService;
import com.seckill.util.TimeUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据库版本的秒杀服务实现（用于性能对比测试）
 * 实现与Redis Lua脚本相同的限购逻辑：
 * - SKU级限购：每个用户购买当前sku的个数限制
 * - 活动级限购：每个用户购买当前活动内所有sku的总数量限制
 * - 无乐观锁，直接 UPDATE
 *
 * @author seckill
 */
@Service("seckillServiceDb")
public class SeckillServiceDbImpl implements SeckillService {

    @Resource
    private SkuStockMapper skuStockMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private UserSkuOrderMapper userSkuOrderMapper;

    @Resource
    private UserActOrderMapper userActOrderMapper;

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
     * 秒杀功能（数据库版本 - 带限购逻辑）
     * 实现与Redis Lua脚本相同的限购逻辑
     *
     * @param actId 活动id
     * @param userId 用户id
     * @param buyNum 购买数量
     * @param skuId sku的id
     * @param perSkuLim SKU级限购（每个用户购买当前sku的个数限制，0表示不限制）
     * @param perActLim 活动级限购（每个用户购买当前活动内所有sku的总数量限制，0表示不限制）
     * @return 秒杀的结果：
     *         - '-3': SKU不存在或秒杀数量未设置
     *         - '-2': 已超出当前活动允许每人秒杀的数量
     *         - '-1': 已超出当前活动每件sku允许每人秒杀的数量
     *         - '0': 库存数量不足，秒杀失败
     *         - orderKey: 秒杀成功，返回订单唯一标识
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String skuSecond(String actId, String userId, int buyNum, String skuId,
                           int perSkuLim, int perActLim) {
        try {
            // 1. 检查SKU是否存在且有库存
            LambdaQueryWrapper<SkuStock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(SkuStock::getActId, actId)
                    .eq(SkuStock::getSkuId, skuId)
                    .eq(SkuStock::getDeleted, 0);
            SkuStock skuStock = skuStockMapper.selectOne(stockWrapper);

            if (skuStock == null || skuStock.getAmount() == null) {
                return "-3"; // SKU不存在或秒杀数量未设置
            }

            int skuAmount = skuStock.getAmount();
            // 这里查到的值可能和下面的值会不一致
            if (skuAmount <= 0) {
                return "0"; // 库存不足
            }

            // 2. 检查活动级限购（perActLim > 0 时检查）
            if (perActLim > 0) {
                LambdaQueryWrapper<UserActOrder> actWrapper = new LambdaQueryWrapper<>();
                actWrapper.eq(UserActOrder::getActId, actId)
                        .eq(UserActOrder::getUserId, userId)
                        .eq(UserActOrder::getDeleted, 0);
                UserActOrder userActOrder = userActOrderMapper.selectOne(actWrapper);

                int currentActBuyNum = (userActOrder != null && userActOrder.getTotalBuyNum() != null)
                        ? userActOrder.getTotalBuyNum() : 0;
                int newActBuyNum = currentActBuyNum + buyNum;

                if (newActBuyNum > perActLim) {
                    return "-2"; // 已超出当前活动允许每人秒杀的数量
                }
            }

            // 3. 检查SKU级限购（perSkuLim > 0 时检查）
            if (perSkuLim > 0) {
                LambdaQueryWrapper<UserSkuOrder> skuWrapper = new LambdaQueryWrapper<>();
                skuWrapper.eq(UserSkuOrder::getUserId, userId)
                        .eq(UserSkuOrder::getSkuId, skuId)
                        .eq(UserSkuOrder::getDeleted, 0);
                UserSkuOrder userSkuOrder = userSkuOrderMapper.selectOne(skuWrapper);

                int currentSkuBuyNum = (userSkuOrder != null && userSkuOrder.getBuyNum() != null)
                        ? userSkuOrder.getBuyNum() : 0;
                int newSkuBuyNum = currentSkuBuyNum + buyNum;

                if (newSkuBuyNum > perSkuLim) {
                    return "-1"; // 已超出当前活动每件sku允许每人秒杀的数量
                }
            }

            // 4. 检查库存是否满足购买数量
            if (skuAmount < buyNum) {
                return "0"; // 库存不足
            }

            // 5. 扣减库存（直接 UPDATE，无乐观锁）
            int updateCount = skuStockMapper.decreaseStock(actId, skuId, buyNum);
            if (updateCount == 0) {
                return "0"; // 库存不足或不存在（并发情况下可能发生）
            }

            // 6. 更新SKU级限购记录
            if (perSkuLim > 0) {
                LambdaQueryWrapper<UserSkuOrder> skuWrapper = new LambdaQueryWrapper<>();
                skuWrapper.eq(UserSkuOrder::getUserId, userId)
                        .eq(UserSkuOrder::getSkuId, skuId)
                        .eq(UserSkuOrder::getDeleted, 0);
                UserSkuOrder userSkuOrder = userSkuOrderMapper.selectOne(skuWrapper);

                if (userSkuOrder != null) {
                    // 更新已存在的记录
                    userSkuOrder.setBuyNum(userSkuOrder.getBuyNum() + buyNum);
                    userSkuOrderMapper.updateById(userSkuOrder);
                } else {
                    // 创建新记录
                    UserSkuOrder newUserSkuOrder = new UserSkuOrder();
                    newUserSkuOrder.setUserId(userId);
                    newUserSkuOrder.setSkuId(skuId);
                    newUserSkuOrder.setBuyNum(buyNum);
                    userSkuOrderMapper.insert(newUserSkuOrder);
                }
            }

            // 7. 更新活动级限购记录
            if (perActLim > 0) {
                LambdaQueryWrapper<UserActOrder> actWrapper = new LambdaQueryWrapper<>();
                actWrapper.eq(UserActOrder::getActId, actId)
                        .eq(UserActOrder::getUserId, userId)
                        .eq(UserActOrder::getDeleted, 0);
                UserActOrder userActOrder = userActOrderMapper.selectOne(actWrapper);

                if (userActOrder != null) {
                    // 更新已存在的记录
                    userActOrder.setTotalBuyNum(userActOrder.getTotalBuyNum() + buyNum);
                    userActOrderMapper.updateById(userActOrder);
                } else {
                    // 创建新记录
                    UserActOrder newUserActOrder = new UserActOrder();
                    newUserActOrder.setActId(actId);
                    newUserActOrder.setUserId(userId);
                    newUserActOrder.setTotalBuyNum(buyNum);
                    userActOrderMapper.insert(newUserActOrder);
                }
            }

            // 8. 创建订单
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

    /**
     * 处理来自 MQ 的秒杀订单（跳过限购检查）
     * Redis 已经完成了限购检查和预扣减，这里只执行数据库操作
     *
     * @param actId 活动id
     * @param userId 用户id
     * @param buyNum 购买数量
     * @param skuId sku的id
     * @param perSkuLim SKU级限购（用于更新限购记录，不检查）
     * @param perActLim 活动级限购（用于更新限购记录，不检查）
     * @return 订单唯一标识，失败返回 null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String processSeckillOrderFromMQ(String actId, String userId, int buyNum, String skuId,
                                            int perSkuLim, int perActLim) {
        try {
            // 1. 查询 SKU 库存信息
            LambdaQueryWrapper<SkuStock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(SkuStock::getActId, actId)
                    .eq(SkuStock::getSkuId, skuId)
                    .eq(SkuStock::getDeleted, 0);
            SkuStock skuStock = skuStockMapper.selectOne(stockWrapper);

            if (skuStock == null) {
                return null; // SKU不存在
            }

            // 2. 扣减库存（无乐观锁版本）
            // 注意：这里不检查库存是否足够，因为 Redis 已经预扣减了
            int updateCount = skuStockMapper.decreaseStock(actId, skuId, buyNum);
            if (updateCount == 0) {
                // 库存扣减失败
                return null;
            }

            // 3. 更新SKU级限购记录（如果有限购配置）
            if (perSkuLim > 0) {
                LambdaQueryWrapper<UserSkuOrder> skuWrapper = new LambdaQueryWrapper<>();
                skuWrapper.eq(UserSkuOrder::getUserId, userId)
                        .eq(UserSkuOrder::getSkuId, skuId)
                        .eq(UserSkuOrder::getDeleted, 0);
                UserSkuOrder userSkuOrder = userSkuOrderMapper.selectOne(skuWrapper);

                if (userSkuOrder != null) {
                    // 更新已存在的记录
                    userSkuOrder.setBuyNum(userSkuOrder.getBuyNum() + buyNum);
                    userSkuOrderMapper.updateById(userSkuOrder);
                } else {
                    // 创建新记录
                    UserSkuOrder newUserSkuOrder = new UserSkuOrder();
                    newUserSkuOrder.setUserId(userId);
                    newUserSkuOrder.setSkuId(skuId);
                    newUserSkuOrder.setBuyNum(buyNum);
                    userSkuOrderMapper.insert(newUserSkuOrder);
                }
            }

            // 4. 更新活动级限购记录（如果有限购配置）
            if (perActLim > 0) {
                LambdaQueryWrapper<UserActOrder> actWrapper = new LambdaQueryWrapper<>();
                actWrapper.eq(UserActOrder::getActId, actId)
                        .eq(UserActOrder::getUserId, userId)
                        .eq(UserActOrder::getDeleted, 0);
                UserActOrder userActOrder = userActOrderMapper.selectOne(actWrapper);

                if (userActOrder != null) {
                    // 更新已存在的记录
                    userActOrder.setTotalBuyNum(userActOrder.getTotalBuyNum() + buyNum);
                    userActOrderMapper.updateById(userActOrder);
                } else {
                    // 创建新记录
                    UserActOrder newUserActOrder = new UserActOrder();
                    newUserActOrder.setActId(actId);
                    newUserActOrder.setUserId(userId);
                    newUserActOrder.setTotalBuyNum(buyNum);
                    userActOrderMapper.insert(newUserActOrder);
                }
            }

            // 5. 创建订单
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
            throw new RuntimeException("处理秒杀订单失败", e);
        }
    }

    @Override
    public void clearUserBuyAmount(String actId, String skuId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearUserBuyAmount'");
    }
}

