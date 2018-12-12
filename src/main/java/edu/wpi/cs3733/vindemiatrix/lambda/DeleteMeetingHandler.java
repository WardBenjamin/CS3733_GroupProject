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

import edu.wpi.cs3733.vindemiatrix.db.SchedulerDatabase;
import edu.wpi.cs3733.vindemiatrix.db.dao.MeetingDAO;
import edu.wpi.cs3733.vindemiatrix.db.dao.ScheduleDAO;
import edu.wpi.cs3733.vindemiatrix.db.dao.TimeSlotDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.DeleteMeetingRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.BasicResponse;

public class DeleteMeetingHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	@SuppressWarnings("unchecked")
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for DeleteMeetingHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "PUT,DELETE,OPTIONS");
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
			responseObj = new BasicResponse(422, "Error parsing input.");
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}

		if (!handled) {
			Boolean success = true; 
			DeleteMeetingRequest request = new Gson().fromJson(body, DeleteMeetingRequest.class);
			MeetingDAO dao = new MeetingDAO(); 
			TimeSlotDAO ts_dao = new TimeSlotDAO();
			logger.log(request.toString() + "\n");
			
			// check if fields are missing
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new BasicResponse(400, "Missing fields in request.")));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			// check permission
			if (success) {
				try {
					int authorized = dao.isAuthorized(request.meeting_id, request.secret_code);
					int authorized2 = ts_dao.isAuthorized(request.time_slot_id, request.secret_code);
					
					if (authorized == 1 || authorized2 == 1) {
						response.put("body", new Gson().toJson(new BasicResponse(401, "Invalid secret code.")));
						success = false;
					} else if (authorized == 2 && authorized2 == 2) {
						response.put("body", new Gson().toJson(new BasicResponse(403, "Incorrect secret code.")));
						success = false;
					}
				} catch (Exception e) {
					response.put("body", new Gson().toJson(new BasicResponse(500, "SQL error checking permissions.")));
					e.printStackTrace();
				}
			}
			
			if (success) {
				try {
					if(ts_dao.setTimeSlotMeeting(request.time_slot_id, 0)) {
						if (dao.deleteMeeting(request.secret_code, request.meeting_id)) {
							response.put("body", new Gson().toJson(new BasicResponse(200)));
						} else {
							response.put("body", new Gson().toJson(new BasicResponse(500, "Error deleting meeting.")));
						}
					} else {
						response.put("body", new Gson().toJson(new BasicResponse(500, "Error updating time slot to open.")));
					}
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new BasicResponse(500, "Error with SQL when deleting meeting.")));
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
