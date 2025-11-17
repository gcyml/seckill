package com.seckill.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 配置队列、交换机、绑定关系等
 *
 * @author seckill
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 秒杀订单队列名称
     */
    public static final String SECKILL_ORDER_QUEUE = "seckill.order.queue";

    /**
     * 秒杀订单交换机名称
     */
    public static final String SECKILL_ORDER_EXCHANGE = "seckill.order.exchange";

    /**
     * 秒杀订单路由键
     */
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order.routing.key";

    /**
     * 消息转换器 - 使用 JSON 格式
     * 这个配置是必要的，否则默认使用 Java 序列化（需要对象实现 Serializable）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置监听器容器工厂
     * 只需要设置消息转换器，其他参数（并发数、预取数、确认模式等）已在 application.yml 中配置
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 设置消息转换器（使用 JSON 格式）
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    /**
     * 秒杀订单队列
     * 持久化队列，即使 RabbitMQ 重启也不会丢失
     */
    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(SECKILL_ORDER_QUEUE).build();
    }

    /**
     * 秒杀订单交换机
     * 使用直连交换机（Direct Exchange）
     */
    @Bean
    public DirectExchange seckillOrderExchange() {
        return ExchangeBuilder.directExchange(SECKILL_ORDER_EXCHANGE)
                .durable(true)  // 持久化交换机
                .build();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder
                .bind(seckillOrderQueue())
                .to(seckillOrderExchange())
                .with(SECKILL_ORDER_ROUTING_KEY);
    }
}


