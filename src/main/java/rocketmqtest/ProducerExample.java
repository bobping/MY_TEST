package rocketmqtest;

import java.io.IOException;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerExample {
    private static final Logger logger = LoggerFactory.getLogger(ProducerExample.class);

    public static void main(String[] args) throws ClientException, IOException {
        String endpoint = "192.168.8.186:8081";
        String topic = "TestTopic";
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        ClientConfiguration configuration = builder.build();
        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();

        try {
            for(int i = 0; i < Integer.MAX_VALUE; i++){
                Message message = provider.newMessageBuilder()
                        .setTopic(topic)
                        .setKeys("messageKey")
                        .setTag("messageTag")
                        .setBody(("第"+i+"个hello!rocketmq").getBytes())
                        .build();
                SendReceipt sendReceipt = producer.send(message);
                Thread.sleep(100);
                logger.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
            }

        } catch (ClientException e) {
            logger.error("Failed to send message", e);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        // producer.close();
    }
}