package com.ercot.cp.ews;

import com.ercot.schema._2007_06.nodal.ews.message.*;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class EwsClient extends WebServiceGatewaySupport {

	public ResponseMessage callEWS(final String soapAddress, final String soapAction, RequestMessage input) throws SoapFaultClientException {

        return (ResponseMessage) getWebServiceTemplate()
				.marshalSendAndReceive(
						soapAddress,
						input,
						new SoapActionCallback(soapAction));
	}
}
