package edu.wpi.cs3733.vindemiatrix.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class CreateScheduleHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for CreateScheduleHandler");
//
//		JSONObject header = new JSONObject();
//		header.put("Content-Type",  "application/json");
//		header.put("Access-Control-Allow-Methods", "PUT");
//		header.put("Access-Control-Allow-Origin",  "*");
//        
//		JSONObject response = new JSONObject();
//		response.put("headers", header);

        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write("hellow world");  
        writer.close();
	}

}
