package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "Security")
public class Security {
    public BinarySecurityToken BinarySecurityToken;
    public Signature Signature;
    public String wsse;
    public int mustUnderstand;
    public String text;
}