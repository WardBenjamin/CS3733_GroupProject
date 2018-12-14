package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.db.dao.TimeSlotDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.SearchOpenTimeSlotsRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.SearchOpenTimeSlotsResponse;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class SearchOpenTimeSlotsHandler implements RequestStreamHandler {

	@Override
    @SuppressWarnings("unchecked")
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    	LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for SearchOpenTimeSlots\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
		header.put("Access-Control-Allow-Origin",  "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		SearchOpenTimeSlotsResponse responseObj = null;
		String body = null;
		boolean handled = false;

		String _schedule_id = null;
		String _year = null;
		String _month = null;
		String _day_of_week = null;
		String _day_of_month = null;
		String _time_slot = null;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			JSONParser parser = new JSONParser();
			JSONObject event = (JSONObject) parser.parse(reader);
			logger.log("Parsed Input:" + event.toJSONString() + "\n");
			
			String method = (String) event.get("httpMethod");
			if (method != null && method.equalsIgnoreCase("OPTIONS")) {
				// OPTIONS needs a 200 response
				logger.log("OPTIONS request received" + "\n");
				responseObj = new SearchOpenTimeSlotsResponse(200);  
		        response.put("body", new Gson().toJson(responseObj));
		        handled = true;
			} else if (method != null && method.equalsIgnoreCase("GET")) {
				JSONObject params = (JSONObject) event.get("queryStringParameters");
				_schedule_id = (String) params.get("schedule_id");
				_year = (String) params.get("year");
				_month = (String) params.get("month");
				_day_of_week = (String) params.get("day_of_week");
				_day_of_month = (String) params.get("day_of_month");
				_time_slot = (String) params.get("time_slot");
			} else {
				body = (String) event.get("body");
				
				if (body == null) {
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString() + "\n");
			responseObj = new SearchOpenTimeSlotsResponse(422, "Error processing input.");
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}
		
		if (!handled) {
			boolean success = true;
			SearchOpenTimeSlotsRequest request = null;
			
			if (_schedule_id != null) {
				try {
					request = new SearchOpenTimeSlotsRequest(Integer.parseInt(_schedule_id), Integer.parseInt(_year), Integer.parseInt(_month), 
							Integer.parseInt(_day_of_week), Integer.parseInt(_day_of_month), _time_slot);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					responseObj = new SearchOpenTimeSlotsResponse(422, "Error processing input.");
					response.put("body", new Gson().toJson(responseObj));
					success = false;
				}
			} else {
				request = new Gson().fromJson(body, SearchOpenTimeSlotsRequest.class);
			}
			
			logger.log(request.toString() + "\n");
			
			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new SearchOpenTimeSlotsResponse(400, "Input is missing fields.")));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			TimeSlotDAO ts_dao = new TimeSlotDAO();
			List<TimeSlot> time_slots = null;
			
			if (success) {
				String year, month, day, time;
				
				if (request.year == -1) {
					year = "____";
				} else {
					year = String.format("%04d", request.year);
				}
				
				if (request.month == -1) {
					month = "__";
				} else {
					month = String.format("%02d", request.month);
				}
				
				if (request.day_of_month == -1) {
					day = "__";
				} else {
					day = String.format("%02d", request.day_of_month);
				}
				
				if (request.time_slot.equals("")) {
					time = "%";
				} else {
					time = request.time_slot + ":00";
				}
				
				String date_query = year + "-" + month + "-" + day;

				
				try {
					time_slots = ts_dao.findOpenTimeSlots(request.schedule_id, date_query, time, request.day_of_week);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new SearchOpenTimeSlotsResponse(500, "Exception: " + e.toString())));
					success = false;
				}
			}
			
			if (success) {
				responseObj = new SearchOpenTimeSlotsResponse(time_slots, 200);
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
