package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "ResponseMessage")
public class ResponseMessage {
    public Header header;
    public Reply reply;
    public Payload payload;
    public String ns0;
    public String text;
}