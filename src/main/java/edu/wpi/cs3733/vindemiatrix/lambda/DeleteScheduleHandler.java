package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.db.dao.ScheduleDAO;
import edu.wpi.cs3733.vindemiatrix.db.dao.TimeSlotDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.DeleteScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.request.GetScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.BasicResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.GetScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class DeleteScheduleHandler implements RequestStreamHandler {

    @SuppressWarnings("unchecked")
	@Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    	LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for GetScheduleHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "GET,PUT,DELETE,OPTIONS");
		header.put("Access-Control-Allow-Origin",  "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		BasicResponse responseObj = null;
		String body = null;
		boolean handled = false;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			JSONParser parser = new JSONParser();
			JSONObject event = (JSONObject) parser.parse(reader);
			logger.log("Parsed Input:" + event.toJSONString() + "\n");
			
			String method = (String) event.get("httpMethod");
			if (method != null && method.equalsIgnoreCase("OPTIONS")) {
				// OPTIONS needs a 200 response
				logger.log("OPTIONS request received" + "\n");
				responseObj = new BasicResponse(200);  
		        response.put("body", new Gson().toJson(responseObj));
		        handled = true;
			} else {
				body = (String) event.get("body");
				
				if (body == null) {
					// for testing only
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString() + "\n");
			responseObj = new BasicResponse(422);
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}
		
		if (!handled) {
			boolean deletedSchedule = false;
			boolean success = true;
			int authorized = 0;
			DeleteScheduleRequest request = new Gson().fromJson(body, DeleteScheduleRequest.class);
			
			logger.log(request.toString() + "\n");
			
			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new BasicResponse(400)));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			// check secret code
			ScheduleDAO dao = new ScheduleDAO();
			if (success) {
				try {
					authorized = dao.isAuthorized(request.id, request.secret_code);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new BasicResponse(500)));
					success = false;
				}
			}

			// delete the time slots (and meetings) in this schedule
			if (authorized == 0) {
				TimeSlotDAO ts_dao = new TimeSlotDAO();
				try {
					ts_dao.deleteTimeSlots(request.id);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new BasicResponse(500)));
					success = false;
				}
			}
			
			// delete the schedule
			if (authorized == 0) {
				try {
					deletedSchedule = dao.deleteSchedule(request.id);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new BasicResponse(500)));
					success = false;
				}
			}
			
			// generate final response
			if (success && authorized != 1 && deletedSchedule == false) {
				responseObj = new BasicResponse(404);
		        response.put("body", new Gson().toJson(responseObj));	
			} else if (success && authorized == 0) {
				responseObj = new BasicResponse(200);
		        response.put("body", new Gson().toJson(responseObj));	
			} else if (authorized == 1) {
				responseObj = new BasicResponse(401);
		        response.put("body", new Gson().toJson(responseObj));
			} else if (authorized == 2) {
				responseObj = new BasicResponse(403);
		        response.put("body", new Gson().toJson(responseObj));
			} else {
				responseObj = new BasicResponse(500);
		        response.put("body", new Gson().toJson(responseObj));
			}
		}
		
		String r = response.toJSONString();
        logger.log("end result:" + r + "\n");
        
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(r);  
        writer.close();
    }

}
