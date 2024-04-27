package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "Signature")
public class Signature {
    public SignedInfo signedInfo;
    public String signatureValue;
    public KeyInfo KeyInfo;
    public String xmlns;
    public String text;
}