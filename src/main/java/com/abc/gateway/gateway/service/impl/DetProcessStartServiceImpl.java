package com.abc.gateway.gateway.service.impl;

import com.abc.gateway.constants.Constants;
import com.abc.gateway.avro.ProcessFlowMessage;
import com.abc.gateway.gateway.model.DetProcessStartRequest;
import com.abc.gateway.gateway.model.QueuedMessage;
import com.abc.gateway.gateway.service.DetProcessStartService;
import com.abc.gateway.gateway.service.QueuedMessageUtil;
import com.abc.gateway.kafka.config.KafkaAppConfig;
import com.abc.gateway.kafka.config.KafkaHelper;
import com.abc.gateway.kafka.config.KafkaProducer;
import com.abc.gateway.logger.KafkaLogSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class DetProcessStartServiceImpl implements DetProcessStartService {

    private static final Logger LOG = LoggerFactory.getLogger(DetProcessStartServiceImpl.class);
    private KafkaAppConfig kafkaAppConfig;
    private KafkaLogSender kafkaLogSender;
    private QueuedMessageUtil queuedMessageUtil;
    private KafkaHelper kafkaHelper;
    private ExecutorService executorService;

    public DetProcessStartServiceImpl(KafkaProducer kafkaProducer, KafkaAppConfig kafkaAppConfig, KafkaLogSender kafkaLogSender,
                                      KafkaHelper kafkaHelper){
        this.kafkaAppConfig = kafkaAppConfig;
        this.kafkaLogSender = kafkaLogSender;
        this.queuedMessageUtil = new QueuedMessageUtil(kafkaProducer, kafkaAppConfig, kafkaLogSender, kafkaHelper);
        this.kafkaHelper = kafkaHelper;
    }

    public Queue<QueuedMessage> getQueuedMessageToSendTopicNameQueue() {
        return queuedMessageUtil.getQueuedMessageToSendTopicNameQueue();
    }

    @PostConstruct
    private void startQueuedProcessingThread(){
        this.executorService = Executors.newSingleThreadExecutor();
        executorService.submit(queuedMessageUtil);
    }

    public void startDroolsDeterminationProcess(DetProcessStartRequest startRequest) {

        if(startRequest.getAjtId() == null) {
            LOG.info("Received regular flow start request for legal entity: {} for period Id: {} for phase: {}",
                    startRequest.getEntityName(),
                    startRequest.getPeriodId(),
                    startRequest.getPhase());
        }
        else {
            LOG.info("Received adjustment flow start request for legal entity: {} for period Id: {} for adjustment Id: {} for phase: {}",
                    startRequest.getEntityName(),
                    startRequest.getPeriodId(),
                    startRequest.getAjtId(),
                    startRequest.getPhase());
        }

        if(startRequest.getReports() != null) {
            LOG.info("Received start request with report sequence. Drools is assigned as lead.");
        }

        ProcessFlowMessage messageToSend = buildProcessFlowMessages(startRequest);

        LOG.info("New process flow message has been created as: {}.", messageToSend);

        messageToSend.setStatus(GW_END);
        String topicName = kafkaAppConfig.getOutProcessFlowTopicConfigs().getProcessFlowTopicToProduce();

        LOG.info(Constants.LOG_MESSAGE_QUEUEING_MESSAGE);
        queuedMessageUtil.putMessageToQueuedMessageToSendTopicNameMap(messageToSend, topicName);
    }



    private List<String> extractReportSequence (DetProcessStartRequest startRequest) {
        if (startRequest.getReports() == null)
            return null;

        String[] reportSequence = startRequest.getReports().split(",");

        Arrays.sort(reportSequence, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Double.valueOf(o1).compareTo(Double.valueOf(o2));
            }
        });

        return Arrays.asList(reportSequence);
    }

    private ProcessFlowMessage buildProcessFlowMessages(DetProcessStartRequest startRequest) {
        ProcessFlowMessage message = ProcessFlowMessage.newBuilder()
                .setId(createID(startRequest))
                .setLegalEntityId(startRequest.getEntityName())
                .setRecordCount(0)
                .setPeriodId(startRequest.getPeriodId())
                .setEntityModel(null)
                .setReportSeq(extractReportSequence(startRequest))
                .setAjtId(startRequest.getAjtId())
                .setTrueModel(null)
                .setDbOperation(null)
                .setStatus(GW_START)
                .setPhase(startRequest.getPhase())
                .setIsLead(extractReportSequence(startRequest) != null)
                .setConditionForDelete(null)
                .build();

        return message;
    }

    private String createID(DetProcessStartRequest startRequest) {
        String legalEntityId = startRequest.getEntityName();
        String inNano = String.valueOf(System.nanoTime());

        return legalEntityId.concat("-").concat(inNano);

    }



}
