package com.abc.gateway.gateway.controller;

import com.abc.gateway.config.AppConfig;
import com.abc.gateway.gateway.model.DetProcessStartRequest;
import com.abc.gateway.gateway.service.DetProcessStartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static javax.servlet.http.HttpServletResponse.*;

@RestController
@RequestMapping(value = "/droolsdetermination")
@Api(value = "/droolsdetermination", tags = {"droolsdetermination"})
public class ProcessStartController {

    private final DetProcessStartService detProcessStartService;
    private  RestTemplate restTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessStartController.class);

    private static final String GCK="GCK";
    private static final String DET="DET";

    @Autowired
    private AppConfig appConfig;

    @Autowired
    public ProcessStartController(DetProcessStartService service, RestTemplate restTemplate)
    {
        this.detProcessStartService =service;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/start")
    @ApiOperation(value = "Start Drools determination",
            notes = "Start Drools determination.")
    @ApiResponses(value = {
            @ApiResponse(code = SC_OK, message = "Success."),
            @ApiResponse(code = SC_BAD_REQUEST, message = "Fail."),
            @ApiResponse(code = SC_INTERNAL_SERVER_ERROR, message = "Unexpected error.")})
    public ResponseEntity<String> startDroolsDetermination(@RequestBody DetProcessStartRequest detProcessStartRequest) {
        try {
            if (detProcessStartRequest.getEntityName() == null || detProcessStartRequest.getEntityName().isEmpty())
                return new ResponseEntity("Entity Name is null", HttpStatus.BAD_REQUEST);
            else if (detProcessStartRequest.getPeriodId() == null)
                return new ResponseEntity("Period Id is null", HttpStatus.BAD_REQUEST);
            else if (detProcessStartRequest.getPhase() != null && !detProcessStartRequest.getPhase().equals("GCK")  && !detProcessStartRequest.getPhase().equals("DET"))
                return new ResponseEntity("Invalid Phase", HttpStatus.BAD_REQUEST);

            if(detProcessStartRequest.getPhase() == null)
                detProcessStartRequest.setPhase(DET);
            if((appConfig.isForwardingMode()))
            {
                LOG.info("Drools in forwarding mode..pushing request {} to abc..", detProcessStartRequest );
                URI uri =  UriComponentsBuilder.fromHttpUrl(appConfig.getForwardingURL()).build().toUri();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                ResponseEntity<String> response = restTemplate.exchange(
                        uri,
                        HttpMethod.POST,
                        new HttpEntity<>(detProcessStartRequest, headers),
                        String.class
                );
                LOG.info("Received response after forwarding to URI {}: {} " , uri, response.getBody() );
                return new ResponseEntity<>("Received response after forwarding: " + response.getBody(), HttpStatus.OK);
            }

            if((appConfig.isGckPhaseActive() && detProcessStartRequest.getPhase().equals(GCK))
                    || (appConfig.isDetPhaseActive() && detProcessStartRequest.getPhase().equals(DET)) )
                detProcessStartService.startDroolsDeterminationProcess(detProcessStartRequest);
            else
                LOG.info("Ignoring the push request for {} phase as it is not active.",detProcessStartRequest.getPhase() );

            return new ResponseEntity("Drools Determination process is queued", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
