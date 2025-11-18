尝试做一个基于mysql+redis的秒杀系统，1研究前后端分离 2研究redis队列，以及互斥锁，解决超卖问题lua脚本，高级尝试kafka业务逻辑（为保证mq消息可用性，研究kafka 持久化，生产消费机制（生产者确认机制（Producer Confirm）： 当生产者发送消息后，MQ服务器会返回一个确认（ACK），告知生产者"消息已成功接收并持久化"。如果生产者没收到ACK，它可以重发。这解决了消息发送过程中丢失的问题。

消费者确认机制（Consumer Ack）： 当消费者从MQ拉取消息并成功处理（比如订单创建成功）后，会向MQ发送一个ACK。MQ只有在收到这个ACK后，才会将消息从队列中删除。如果消费者处理失败或宕机，没有发送ACK，MQ会在一定时间后将消息重新投递给其他消费者实例。这解决了消息处理过程中丢失的问题。））
https://www.cnblogs.com/architectforest/p/13094795.html
https://www.cnblogs.com/javastack/p/15740523.html
https://www.cnblogs.com/hefeng2014/p/17750831.html
https://cloud.tencent.com/developer/article/2540700
还有研究扩容
https://github.com/HermanCho/seckill

认真研究乐观锁、悲观锁
研究扩容，简单方法，就是用nginx做负载均衡，即水平扩容，而后需要redis做会话


加入10个库存，1000个线程循环10次时，吞吐量大约是3400每秒 还是可以的。

加入7个库存，1000个线程循环10次时，吞吐量大约是1100每秒，差距明显

根据ai给的建议，把redis连接数填大,日志减少，可以优化速度

然后把mysql的连接数扩大，加上索引，感觉优化效果并不明显。可能是因为没有加上限购逻辑，导致差距变小

关键代码处，因为并没有锁，所以是可能会出现超卖的情况，尝试 着给代码加了synchronized 锁，吞吐量变成200了

加上乐观锁之后，吞吐量变成了950

lua语言比较强大，可以执行复杂的逻辑，保证操作原子性，可以用Redis 事务（MULTI/EXEC）替代，缺点就是：不是真正的原子性（中间可能失败）



