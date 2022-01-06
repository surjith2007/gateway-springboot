package com.abc.gateway.gateway.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDeserializer extends JsonDeserializer<Date> {

    private static final Logger LOG = LoggerFactory.getLogger(DateDeserializer.class);
  @Override
  public Date deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {

      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

      Date localDate = null;
      try {
          localDate = formatter.parse(p.getText());
      } catch (ParseException e) {
            LOG.error("Invalid date :" + e.getMessage());
      }

      return localDate;
  }
}