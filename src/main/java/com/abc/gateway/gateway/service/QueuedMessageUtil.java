package com.abc.gateway.gateway.service;

import com.abc.gateway.constants.Constants;
import com.abc.gateway.gateway.model.QueuedMessage;
import com.abc.gateway.avro.ProcessFlowMessage;
import com.abc.gateway.kafka.config.KafkaAppConfig;
import com.abc.gateway.kafka.config.KafkaHelper;
import com.abc.gateway.kafka.config.KafkaProducer;
import com.abc.gateway.logger.KafkaLogSender;
import com.abc.gateway.logger.model.LogModel;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static com.abc.gateway.logger.constants.LogConstants.*;

public class QueuedMessageUtil implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(QueuedMessageUtil.class);

    KafkaProducer kafkaProducer;
    private KafkaAppConfig kafkaAppConfig;
    private Queue<QueuedMessage> queuedMessageToSendTopicNameQueue;
    private Thread innerThread;
    private KafkaLogSender kafkaLogSender;
    private KafkaHelper kafkaHelper;


    public QueuedMessageUtil(KafkaProducer kafkaProducer, KafkaAppConfig kafkaAppConfig,
                             KafkaLogSender kafkaLogSender, KafkaHelper kafkaHelper){
        this.kafkaAppConfig = kafkaAppConfig;
        this.kafkaProducer = kafkaProducer;
        this.queuedMessageToSendTopicNameQueue = new ConcurrentLinkedQueue<>();
        this.kafkaLogSender = kafkaLogSender;
        this.kafkaHelper = kafkaHelper;
    }

    public Queue<QueuedMessage> getQueuedMessageToSendTopicNameQueue() {
        return queuedMessageToSendTopicNameQueue;
    }

    public void putMessageToQueuedMessageToSendTopicNameMap(ProcessFlowMessage queuedMessageToSend, String topicName){
        queuedMessageToSendTopicNameQueue.add(new QueuedMessage(queuedMessageToSend, topicName));
    }

    @Override
    public void run() {
        try {
            innerThread = Thread.currentThread();
            while (true) {
                if(getQueuedMessageToSendTopicNameQueue().peek() != null) {
                    try {
                        if (kafkaHelper.checkIfTopicExists(kafkaAppConfig.getOutProcessFlowTopicConfigs().getProcessFlowTopicToProduce())) {
                            QueuedMessage queuedMessage = queuedMessageToSendTopicNameQueue.poll();
                            produceMessageToTopic(queuedMessage.getTopicName(), queuedMessage.getProcessFlowMessage());
                        } else {
                            try {
                                LOG.error(Constants.LOG_MESSAGE_TOPIC_NOT_AVAILABLE, kafkaAppConfig.getOutProcessFlowTopicConfigs().getProcessFlowTopicToProduce());
                                TimeUnit.SECONDS.sleep(kafkaAppConfig.getSecondsBetweenClusterChecks());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e){
            LOG.error("Inner thread logic failed: {}", e.getMessage());
        }
    }

    public void produceMessageToTopic(String topicName, ProcessFlowMessage message) {
        try {
            kafkaLogSender.sendLogToKafka(decorateLogMessage(message, ACTION_KEY_START_VALUE));
            kafkaProducer.initialize(kafkaAppConfig.getOutProcessFlowTopicConfigs());
            kafkaProducer.produceToTopic(topicName, message);
        } catch (SerializationException e1){
            LOG.error("Serialization error while producing with schema registry to topic {}. Cause: {}", topicName, e1.getCause().getMessage());
            tryToProduceAgain(topicName, message);
        } catch (Exception e2) {
            LOG.error("Error while producing message to topic:{}\t err msg:{}", topicName, e2.getMessage());
        } finally {
            kafkaLogSender.sendLogToKafka(decorateLogMessage(message, ACTION_KEY_FINISH_VALUE));
        }
    }

    public void tryToProduceAgain(String topicName, ProcessFlowMessage message) {
        while(true){
            try{
                kafkaProducer.produceToTopic(topicName, message);
                break;
            } catch (SerializationException e){
                LOG.info("Trying again in {} seconds.", kafkaAppConfig.getSleepSecondsAtRetryOnSerializationError());
                try {
                    Thread.sleep(kafkaAppConfig.getSleepSecondsAtRetryOnSerializationError()*1000);
                } catch (InterruptedException interruptedException) {
                    LOG.error(interruptedException.getMessage());
                    break;
                }
            } catch (Exception e){
                break;
            }
        }

    }

    public LogModel decorateLogMessage(ProcessFlowMessage message, String status) {

        String logMessage = (status.equals(ACTION_KEY_START_VALUE) ? Constants.LOG_MESSAGE_START : Constants.LOG_MESSAGE_FINISH);

        return LogModel.builder()
                .appName(Constants.LOG_APP_NAME_VALUE)
                .scope(SCOPE_VALUE_LEGAL_ENTITY)
                .legalEntityId(message.getLegalEntityId())
                .ajtId(message.getAjtId())
                .status(status)
                .reportSequenceNumber(null)
                .message(logMessage)
                .params(new Object[] {message,
                        kafkaAppConfig.getOutProcessFlowTopicConfigs().getProcessFlowTopicToProduce(),
                        message.getLegalEntityId()}).build();
    }
}
