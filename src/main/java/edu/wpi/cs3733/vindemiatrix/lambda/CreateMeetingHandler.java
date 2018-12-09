package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateMeetingRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateMeetingResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.GetScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class CreateMeetingHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	@SuppressWarnings("unchecked")
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for CreateMeetingHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "PUT,DELETE,OPTIONS");
		header.put("Access-Control-Allow-Origin", "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		CreateMeetingResponse responseObj = null;
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
				responseObj = new CreateMeetingResponse(200);  
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
			responseObj = new CreateMeetingResponse(422);
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}

		if (!handled) {
			Boolean success = true; 
			CreateMeetingRequest request = new Gson().fromJson(body, CreateMeetingRequest.class);
			logger.log(request.toString() + "\n");
			
			// check if fields are missing
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new CreateMeetingResponse(400)));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			TimeSlotDAO ts_dao = new TimeSlotDAO();
			
			try {
				if (ts_dao.timeSlotHasMeeting(request.time_slot_id)) {
					response.put("body", new Gson().toJson(new CreateMeetingResponse(400)));
					success = false;
				}
			} catch (Exception e) {
				response.put("body", new Gson().toJson(new CreateMeetingResponse(500)));
				success = false;
				e.printStackTrace();
			}

			if (success) {
				Meeting m = createMeeting(request.time_slot_id, request.name);
				if (m != null) {
					response.put("body", new Gson().toJson(new CreateMeetingResponse(m.id, m.name, m.secret_code, 200)));
				} else {
					response.put("body", new Gson().toJson(new CreateMeetingResponse(500)));
				}
			}
		}
		
		String r = response.toJSONString();
        logger.log("end result:" + r + "\n");
        
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(r);  
        writer.close();
	}
		
	/**
	 * Update meeting in the database 
	 * @param id meeting id 
	 * @param name creator of meeting 
	 * @return the Meeting that was created 
	 */
	Meeting createMeeting(int id, String name) {
		Meeting s;
		MeetingDAO dao = new MeetingDAO();
		TimeSlotDAO ts_dao = new TimeSlotDAO();

		try {  
			s = dao.createMeeting(id, name);
			ts_dao.setTimeSlotMeeting(id, s.id);
		} catch (Exception e) {
			System.out.println("createMeeting(): Error creating Meeting: " + e.toString() + "\n");
			s = null;
		}
		
		return s;
	}
}
