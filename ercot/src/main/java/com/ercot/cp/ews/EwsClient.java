package com.ercot.cp.ews;

import com.ercot.schema._2007_06.nodal.ews.message.*;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EwsClient extends WebServiceGatewaySupport {

	private static final int SOAP_TIMEOUT_SECONDS = 120;

	public ResponseMessage callEWS(final String soapAddress, final String soapAction, RequestMessage input) throws SoapFaultClientException {

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<ResponseMessage> future = executor.submit(() ->
					(ResponseMessage) getWebServiceTemplate()
							.marshalSendAndReceive(
									soapAddress,
									input,
									new SoapActionCallback(soapAction))
			);
			return future.get(SOAP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new RuntimeException("SOAP call timed out after " + SOAP_TIMEOUT_SECONDS + "s for action: " + soapAction, e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof SoapFaultClientException) {
				throw (SoapFaultClientException) cause;
			}
			throw new RuntimeException("SOAP call failed: " + cause.getMessage(), cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("SOAP call interrupted for action: " + soapAction, e);
		} finally {
			executor.shutdownNow();
		}
	}
}
