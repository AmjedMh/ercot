package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "Envelope")
public class Envelope {
    public Header header;
    public Body body;

    @JacksonXmlProperty(localName ="SOAP-ENV")
    public String SOAPENV;
    public String wsu;
    public String text;
}