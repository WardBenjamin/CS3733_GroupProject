package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.db.dao.ScheduleDAO;
import edu.wpi.cs3733.vindemiatrix.db.dao.TimeSlotDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.UpdateTimeSlotRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.BasicResponse;

public class UpdateTimeSlotsHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	@SuppressWarnings("unchecked")
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for OpenCloseTimeSlotsHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type", "application/json");
		header.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
		header.put("Access-Control-Allow-Origin", "*");
        
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
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString() + "\n");
			responseObj = new BasicResponse(422, "Error processing input.");
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}

		if (!handled) {
			boolean success = true; 
			UpdateTimeSlotRequest request = new Gson().fromJson(body, UpdateTimeSlotRequest.class);
			logger.log(request.toString() + "\n");
			
			// check if fields are missing
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new BasicResponse(400, "Request is missing fields.")));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			ScheduleDAO dao = new ScheduleDAO();
			TimeSlotDAO ts_dao = new TimeSlotDAO();
			
			if (success) {
				try {
					int authorized = dao.isAuthorized(request.schedule_id, request.secret_code);
					
					if (authorized == 1) {
						response.put("body", new Gson().toJson(new BasicResponse(401, "Invalid secret code.")));
						success = false;
					} else if (authorized == 2) {
						response.put("body", new Gson().toJson(new BasicResponse(403, "Incorrect secret code.")));
						success = false;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					response.put("body", new Gson().toJson(new BasicResponse(500, "SQL error checking secret code.")));
					success = false;
				}
			}
			
			if (success) {
				try {
					switch (request.mode) {
						case "indiv":
							if (ts_dao.timeSlotHasMeeting(request.time_slot_id)) {
								response.put("body", new Gson().toJson(new BasicResponse(409, "Slot cannot be closed as there is a meeting in it.")));
							} else {
								if (ts_dao.updateTimeSlot(request.time_slot_id, request.open)) {
									response.put("body", new Gson().toJson(new BasicResponse(200)));
								} else {
									response.put("body", new Gson().toJson(new BasicResponse(500, "Error updating time slot.")));
								}
							}
							break;
						case "day":
							if (ts_dao.hasMeetingOnDay(request.day, request.time_slot_id)) {
								response.put("body", new Gson().toJson(new BasicResponse(409, "Slots cannot be closed as there are one or more meetings in it.")));
							} else {
								if (ts_dao.updateTimeSlotsOnDay(request.day, request.schedule_id, request.open == 1)) {
									response.put("body", new Gson().toJson(new BasicResponse(200)));
								} else {
									response.put("body", new Gson().toJson(new BasicResponse(500, "Error updating time slots.")));
								}
							}
							break;
						case "slot":
							if (ts_dao.hasMeetingAtTimeSlot(request.timeslot, request.time_slot_id)) {
								response.put("body", new Gson().toJson(new BasicResponse(409, "Slots cannot be closed as there are one or more meetings in it.")));
							} else {
								if (ts_dao.updateTimeSlotOnHours(request.timeslot, request.schedule_id, request.open == 1)) {
									response.put("body", new Gson().toJson(new BasicResponse(200)));
								} else {
									response.put("body", new Gson().toJson(new BasicResponse(500, "Error updating time slots.")));
								}
							}
							break;
					}
				} catch (Exception e) {
					response.put("body", new Gson().toJson(new BasicResponse(500, "Error with SQL queries.")));
					e.printStackTrace();
				}
			}
		}
		
		String r = response.toJSONString();
        logger.log("end result:" + r + "\n");
        
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(r);  
        writer.close();
    }
}
