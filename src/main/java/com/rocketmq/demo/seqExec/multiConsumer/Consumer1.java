package com.rocketmq.demo.seqExec.multiConsumer;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 顺序消息消费，带事务方式（应用可控制Offset什么时候提交）
 */
public class Consumer1 {

    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("order_Consumer");
        consumer.setNamesrvAddr("127.0.0.1:9876;192.168.100.146:9876;192.168.100.149:9876;192.168.100.239:9876");

        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br> 
         * 如果非第一次启动，那么按照上次消费的位置继续消费 
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.subscribe("TopicOrderTest", "*");

        /**
         * 实现了MessageListenerOrderly表示一个队列只会被一个线程取到  
         *，第二个线程无法访问这个队列 
         */
        consumer.registerMessageListener(new MessageListenerOrderly() {
            AtomicLong consumeTimes = new AtomicLong(0);

            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                // 设置自动提交  
                context.setAutoCommit(true);
                for (MessageExt msg : msgs) {
                    System.out.println(msg + ",内容：" + new String(msg.getBody()));
                }

                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                ;

                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        consumer.start();

        System.out.println("Consumer1 Started.");
    }

}  