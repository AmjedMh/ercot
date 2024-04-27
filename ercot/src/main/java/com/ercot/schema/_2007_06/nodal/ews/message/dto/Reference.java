package com.ercot.schema._2007_06.nodal.ews.message.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "Reference")
public class Reference {
    public Transforms transforms;
    public DigestMethod digestMethod;
    public String digestValue;
    public String URI;
    public String text;
    public String valueType;
}
