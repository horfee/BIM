package com.ibm.swg.maximo.oslc.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.ibm.json.java.JSONObject;
import com.ibm.tivoli.maximo.oslc.provider.AbstractRouteHandler;
import com.ibm.tivoli.maximo.oslc.provider.OslcRequest;
import com.ibm.tivoli.maximo.oslc.provider.OslcResourceResponse;

import psdi.util.MXException;

public class ServeIFCFileHandler extends AbstractRouteHandler {

	@Override
	public OslcResourceResponse handleRequest(OslcRequest request) throws MXException, IOException {
		
		String path;
		if ( "GET".equals(request.getHttpMethod())) {
			path = request.getQueryParam("file");
		} else {
			JSONObject r = this.getRequestDataAsJSONObject();
			path = (String) r.get("file");
		}
		File file = new File(path);
		byte[] buffer = org.apache.commons.io.FileUtils.readFileToByteArray(file);
		return this.okResponse(buffer, "application/octet-stream");

	}

}
