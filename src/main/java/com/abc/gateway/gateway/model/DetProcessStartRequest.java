package com.abc.gateway.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DetProcessStartRequest {
    @JsonProperty(value = "entityName"  )
    private String entityName;

    @JsonProperty(value = "periodId"  )
    private String periodId;

    @JsonProperty(value = "ajtId"  )
    private Long ajtId;

    @JsonProperty(value = "phase" )
    private String phase;

    @JsonProperty(value = "reports" )
    private String reports;
}
