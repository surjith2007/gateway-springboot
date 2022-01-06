package com.abc.gateway.constants;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String ADJUSTMENT_SUFFIX = "Ajt";

    //  Logging constants for MDC
    public static final String LOG_APP_NAME_VALUE = "drools-gateway-ms";
    public static final String LOG_MESSAGE_START = "Start sending ProcessFlowMessage {} to topic {}!";
    public static final String LOG_MESSAGE_FINISH = "Finish sending ProcessFlowMessage {} to topic {}! Starting to process legal entity {}.";
    public static final String LOG_MESSAGE_TOPIC_NOT_AVAILABLE = "The kafka cluster is down or the destination topic ({}) does not exist on the kafka cluster!";
    public static final String LOG_MESSAGE_QUEUEING_MESSAGE = "Queuing process flow message to produce when cluster is up.";

    private Constants() {

    }

}
