package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.request.GetScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.GetScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class GetScheduleHandler implements RequestStreamHandler {

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
		
		GetScheduleResponse responseObj = null;
		String body = null;
		boolean handled = false;
		
		String _id = null;
		String _week_start_date = null;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			JSONParser parser = new JSONParser();
			JSONObject event = (JSONObject) parser.parse(reader);
			logger.log("Parsed Input:" + event.toJSONString() + "\n");
			
			String method = (String) event.get("httpMethod");
			if (method != null && method.equalsIgnoreCase("OPTIONS")) {
				// OPTIONS needs a 200 response
				logger.log("OPTIONS request received" + "\n");
				responseObj = new GetScheduleResponse(200);  
		        response.put("body", new Gson().toJson(responseObj));
		        handled = true;
			} else if (method != null && method.equalsIgnoreCase("GET")) {
				JSONObject params = (JSONObject) event.get("queryStringParameters");
				_id = (String) params.get("id");
				_week_start_date = (String) params.get("week_start_date");
			} else {
				body = (String) event.get("body");
				
				if (body == null) {
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString() + "\n");
			responseObj = new GetScheduleResponse(422, "Error processing input.");
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}
		
		if (!handled) {
			boolean success = true;
			GetScheduleRequest request = null;
			
			if (_week_start_date != null) {
				request = new GetScheduleRequest(_id, _week_start_date);
			} else {
				request = new Gson().fromJson(body, GetScheduleRequest.class);
			}
			
			logger.log(request.toString() + "\n");
			
			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new GetScheduleResponse(400, "Input is missing fields.")));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date week_start_date = null;
			
			if (success) {
				try {
					week_start_date = dateFormat.parse(request.week_start_date);
					logger.log("Parsed start date successfully.\n");
				} catch (java.text.ParseException e) {
					response.put("body", new Gson().toJson(new GetScheduleResponse(400, "Error parsing start date.")));
					success = false;
					e.printStackTrace();
				}
			}
			
			// get the schedule
			if (success) {
				
				ScheduleDAO dao = new ScheduleDAO();
				Schedule s = null;
				try {
					s = dao.getSchedule(request.id);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new GetScheduleResponse(500, "SQL error getting schedule.")));
					success = false;
				}

				// get the start day of week (make sure request is for a Monday, or find the Monday)
				Calendar c = Calendar.getInstance();
				if (success) {
					try {
						if (week_start_date.before(dateFormat.parse(s.start_date))) {
							c.setTime(dateFormat.parse(s.start_date));
						} else {
							c.setTime(week_start_date);
						}
					} catch (java.text.ParseException e1) {
						response.put("body", new Gson().toJson(new GetScheduleResponse(500, "Error parsing dates.")));
						success = false;
						e1.printStackTrace();
					}
				}
				
				if (success) {
					c.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - c.get(Calendar.DAY_OF_WEEK));
					String week_start = dateFormat.format(c.getTime());
					c.add(Calendar.DAY_OF_MONTH, 7);
					String week_end = dateFormat.format(c.getTime());

					logger.log("determed start and end of week.\n");
					logger.log("start of week:" + week_start + "\n");
					logger.log("end of week:" + week_end + "\n");

					TimeSlotDAO ts_dao = new TimeSlotDAO();
					List<TimeSlot> time_slots = null;
					try {
						time_slots = ts_dao.getTimeSlots(request.id, week_start, week_end);
					} catch (Exception e) {
						e.printStackTrace();
						response.put("body", new Gson().toJson(new GetScheduleResponse(500, "SQL error getting time slots.")));
						success = false;
					}
					
					if (s == null) {
						responseObj = new GetScheduleResponse(404, "Schedule not found.");
				        response.put("body", new Gson().toJson(responseObj));	
					} else if (s != null && time_slots != null) {
						responseObj = new GetScheduleResponse(s, time_slots, 200);
				        response.put("body", new Gson().toJson(responseObj));	
					} else {
						responseObj = new GetScheduleResponse(500, "Unknown system error.");
				        response.put("body", new Gson().toJson(responseObj));
					}
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
