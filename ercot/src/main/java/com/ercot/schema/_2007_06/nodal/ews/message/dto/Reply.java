package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import java.util.Date;

@Data
@JacksonXmlRootElement(localName = "Reply")
public class Reply {

    public String replyCode;
    public Date timestamp;
}