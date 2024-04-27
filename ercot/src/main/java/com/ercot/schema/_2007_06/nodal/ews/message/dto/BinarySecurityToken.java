package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "BinarySecurityToken")
public class BinarySecurityToken {
    public String encodingType;
    public String valueType;
    public String Id;
    public String text;
}