package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "Header")
public class Header {
    public Security Security;
    public String Verb;
    public String Noun;
    public ReplayDetection ReplayDetection;
    public String Revision;
    public String Source;
    public String UserID;
}
