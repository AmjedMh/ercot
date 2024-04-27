package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "SignedInfo")
public class SignedInfo {
    public CanonicalizationMethod canonicalizationMethod;
    public SignatureMethod signatureMethod;
    public Reference reference;
}