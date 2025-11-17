package com.seckill.mq;

import com.seckill.service.SeckillService;
import jakarta.annotation.Resource;
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

    /**
     * 秒杀业务消息接收
     * 预扣减已在 Redis 中完成，这里直接调用数据库服务方法完成数据库操作
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
                // TODO: 这里应该回滚 Redis 预扣减的库存
            } else {
                log.info("数据库操作成功: orderKey={}, userId={}, actId={}, skuId={}, buyNum={}",
                        orderKey, sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum());
            }

        } catch (Exception e) {
            log.error("处理秒杀消息失败: userId={}, actId={}, skuId={}, buyNum={}, error={}",
                    sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum(),
                    e.getMessage(), e);
            // TODO: 这里应该回滚 Redis 预扣减的库存
            throw new RuntimeException("处理秒杀消息失败", e);
        }
    }
}
