package com.abc.gateway.gateway.model;

import com.abc.gateway.avro.ProcessFlowMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QueuedMessage {
    private ProcessFlowMessage processFlowMessage;
    private String topicName;


}
