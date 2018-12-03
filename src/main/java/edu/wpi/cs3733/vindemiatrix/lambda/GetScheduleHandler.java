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
		header.put("Access-Control-Allow-Methods", "PUT,OPTIONS");
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
			} else if (method.equalsIgnoreCase("GET")) {
				JSONObject params = (JSONObject) event.get("queryStringParameters");
				_id = (String) params.get("id");
				_week_start_date = (String) params.get("week_start_date");
			} else {
				body = (String) event.get("body");
				
				if (body == null) {
					// FIXME for testing only
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString() + "\n");
			responseObj = new GetScheduleResponse(422);
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
				response.put("body", new Gson().toJson(new GetScheduleResponse(400)));
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
					response.put("body", new Gson().toJson(new GetScheduleResponse(400)));
					success = false;
					e.printStackTrace();
				}
			}
			
			// get the schedule
			if (success) {
				// get the start day of week (make sure request is for a Monday, or find the Monday)
				Calendar c = Calendar.getInstance();
				c.setTime(week_start_date);
				c.add(Calendar.DAY_OF_MONTH, Calendar.MONDAY - c.get(Calendar.DAY_OF_WEEK));
				String week_start = dateFormat.format(c.getTime());
				c.add(Calendar.DAY_OF_MONTH, 4);
				String week_end = dateFormat.format(c.getTime());
				
				ScheduleDAO dao = new ScheduleDAO();
				Schedule s = null;
				try {
					s = dao.getSchedule(request.id, week_start);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new GetScheduleResponse(500)));
					success = false;
				}

				TimeSlotDAO ts_dao = new TimeSlotDAO();
				List<TimeSlot> time_slots = null;
				try {
					time_slots = ts_dao.getTimeSlots(request.id, week_start, week_end);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new GetScheduleResponse(500)));
					success = false;
				}
				
				if (s != null && time_slots != null) {
					responseObj = new GetScheduleResponse(s, time_slots, 200);
			        response.put("body", new Gson().toJson(responseObj));	
				} else {
					responseObj = new GetScheduleResponse(500);
			        response.put("body", new Gson().toJson(responseObj));
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
