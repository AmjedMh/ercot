package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "transform")
public class Transform {
    public String algorithm;
}
