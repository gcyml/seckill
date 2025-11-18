package com.seckill.mq;

import com.seckill.service.SeckillService;
import com.seckill.util.RedisLuaUtil;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 消息队列：接收
 * 使用 JSON 序列化，直接接收对象
 * 处理预扣减后的数据库操作
 *
 * @author seckill
 */
@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Resource
    @Qualifier("seckillServiceDbOptimistic")
    private SeckillService seckillService;

    @Resource
    private RedisLuaUtil redisLuaUtil;

    /**
     * 秒杀业务消息接收
     * 预扣减已在 Redis 中完成，这里直接调用数据库服务方法完成数据库操作
     * 
     * 一致性处理：
     * 1. Redis 已经预扣减了库存和限购记录
     * 2. 执行数据库操作（扣减库存、更新限购记录、创建订单）
     * 3. 如果数据库操作失败，回滚 Redis 预扣减的库存和限购记录
     *
     * @param sm 秒杀消息对象
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_QUEUE)
    public void receive(SeckillMessage sm) {
        log.info("收到秒杀消息: userId={}, actId={}, skuId={}, buyNum={}, perSkuLim={}, perActLim={}",
                sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum(),
                sm.getPerSkuLim(), sm.getPerActLim());

        try {
            // 调用专门用于 MQ 处理的方法，跳过限购检查（Redis 已经检查过了）
            // 只执行数据库操作：扣减库存、更新限购记录、创建订单
            String orderKey = seckillService.processSeckillOrderFromMQ(
                    sm.getActId(),
                    sm.getUserId(),
                    sm.getBuyNum(),
                    sm.getSkuId(),
                    sm.getPerSkuLim(),
                    sm.getPerActLim()
            );

            // 检查结果
            if (orderKey == null) {
                log.error("数据库操作失败: userId={}, actId={}, skuId={}, buyNum={}",
                        sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum());
                // 回滚 Redis 预扣减的库存和限购记录
                rollbackRedis(sm);
            } else {
                log.info("数据库操作成功: orderKey={}, userId={}, actId={}, skuId={}, buyNum={}",
                        orderKey, sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum());
            }

        } catch (Exception e) {
            log.error("处理秒杀消息失败: userId={}, actId={}, skuId={}, buyNum={}, error={}",
                    sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum(),
                    e.getMessage(), e);
            
            // 回滚 Redis 预扣减的库存和限购记录
            rollbackRedis(sm);
            
            // 注意：这里不重新抛出异常，避免消息重复消费
            // 如果希望消息重试，可以重新抛出异常
            // throw new RuntimeException("处理秒杀消息失败", e);
        }
    }

    /**
     * 回滚 Redis 预扣减的库存和限购记录
     * 统一使用 Lua 脚本实现原子性回滚
     * 
     * @param sm 秒杀消息对象
     */
    private void rollbackRedis(SeckillMessage sm) {
        try {
            List<String> keyList = new ArrayList<>();
            keyList.add(sm.getUserId());           // KEYS[1]
            keyList.add(String.valueOf(sm.getBuyNum()));  // KEYS[2]
            keyList.add(sm.getSkuId());            // KEYS[3]
            keyList.add(String.valueOf(sm.getPerSkuLim())); // KEYS[4]
            keyList.add(sm.getActId());            // KEYS[5]
            keyList.add(String.valueOf(sm.getPerActLim())); // KEYS[6]
            
            String result = redisLuaUtil.runLuaScript("rollback.lua", keyList);
            
            if ("1".equals(result)) {
                log.info("Redis回滚成功: userId={}, actId={}, skuId={}, buyNum={}",
                        sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum());
            } else {
                log.error("Redis回滚失败: userId={}, actId={}, skuId={}, buyNum={}",
                        sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum());
            }
        } catch (Exception rollbackException) {
            log.error("Redis回滚操作异常: userId={}, actId={}, skuId={}, buyNum={}, error={}",
                    sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum(),
                    rollbackException.getMessage(), rollbackException);
        }
    }
}
