package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.ercot.cp.ews.config.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@JacksonXmlRootElement(localName = "Report")
public class Report {
    public Date operatingDate;
    public String reportGroup;
    public String fileName;

    @JsonDeserialize(using = DateDeserializer.class)
    public LocalDateTime created;

    public Long size;
    public String format;
    public String URL;
}