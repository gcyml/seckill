尝试做一个基于mysql+redis的秒杀系统，1研究前后端分离 2研究redis队列，以及互斥锁，解决超卖问题lua脚本，高级尝试kafka业务逻辑（为保证mq消息可用性，研究kafka 持久化，生产消费机制（生产者确认机制（Producer Confirm）： 当生产者发送消息后，MQ服务器会返回一个确认（ACK），告知生产者"消息已成功接收并持久化"。如果生产者没收到ACK，它可以重发。这解决了消息发送过程中丢失的问题。

消费者确认机制（Consumer Ack）： 当消费者从MQ拉取消息并成功处理（比如订单创建成功）后，会向MQ发送一个ACK。MQ只有在收到这个ACK后，才会将消息从队列中删除。如果消费者处理失败或宕机，没有发送ACK，MQ会在一定时间后将消息重新投递给其他消费者实例。这解决了消息处理过程中丢失的问题。））
https://www.cnblogs.com/architectforest/p/13094795.html
https://www.cnblogs.com/javastack/p/15740523.html
https://www.cnblogs.com/hefeng2014/p/17750831.html
还有研究扩容
https://github.com/HermanCho/seckill
认真研究乐观锁、悲观锁
研究扩容，简单方法，就是用nginx做负载均衡，即水平扩容，而后需要redis做会话


加入10个库存，1000个线程循环10次时，吞吐量大约是3400每秒 还是可以的。

lua语言比较强大，可以执行复杂的逻辑，保证操作原子性，可以用Redis 事务（MULTI/EXEC）替代，缺点就是：不是真正的原子性（中间可能失败）

