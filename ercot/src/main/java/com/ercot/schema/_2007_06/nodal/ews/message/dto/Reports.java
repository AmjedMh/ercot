package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;
import java.util.List;

@Data
public class Reports {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("Report")
    private List<Report> report;
}
