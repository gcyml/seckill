package com.seckill.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @program: SeckillProject
 * @description: 消息队列：发送
 **/
@Service
public class MQSender {
    private static Logger log = LoggerFactory.getLogger(MQSender.class);

    @Resource
    AmqpTemplate amqpTemplate;

    /**
     * 秒杀业务消息发送
     * 使用 JSON 序列化，直接发送对象，无需手动转换
     */
    public void sendSeckillMessage(SeckillMessage sm) {
        log.info("消息队列 MQSender 秒杀业务消息发送 send message: userId={}, actId={}, skuId={}, buyNum={}",
                sm.getUserId(), sm.getActId(), sm.getSkuId(), sm.getBuyNum());
        // 使用交换机路由，直接发送对象，会自动使用 JSON 序列化
        amqpTemplate.convertAndSend(RabbitMQConfig.SECKILL_ORDER_EXCHANGE,
                RabbitMQConfig.SECKILL_ORDER_ROUTING_KEY, sm);
    }
}

