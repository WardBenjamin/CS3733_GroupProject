package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.ScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class CreateScheduleHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	@SuppressWarnings("unchecked")
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for CreateScheduleHandler");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "PUT,OPTIONS");
		header.put("Access-Control-Allow-Origin",  "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		ScheduleResponse responseObj = null;
		String body = null;
		boolean handled = false;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			JSONParser parser = new JSONParser();
			JSONObject event = (JSONObject) parser.parse(reader);
			logger.log("Parsed Input:" + event.toJSONString());
			
			String method = (String) event.get("httpMethod");
			if (method != null && method.equalsIgnoreCase("OPTIONS")) {
				// OPTIONS needs a 200 response
				logger.log("OPTIONS request received");
				responseObj = new ScheduleResponse(200);  
		        response.put("body", new Gson().toJson(responseObj));
		        handled = true;
			} else {
				body = (String) event.get("body");
				
				if (body == null) {
					// FIXME for testing only
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString());
			responseObj = new ScheduleResponse(422);
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}

		if (!handled) {
			boolean success = false;
			CreateScheduleRequest request = new Gson().fromJson(body, CreateScheduleRequest.class);
			logger.log(request.toString());
			
			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new ScheduleResponse(400)));
				success = false;
			}
			
			// holders for date and time integers
			int start_date_arr[] = null;
			int end_date_arr[] = null;
			int start_time_arr[] = null;
			int end_time_arr[] = null;

			// parse dates
			if (success) { success = parseDates(request, response, start_date_arr, end_date_arr); }

			// parse times
			if (success) { success = parseTimes(request, response, start_time_arr, end_time_arr); }
			
			// create schedule
			if (success) {
				Calendar c = Calendar.getInstance();
			    c.set(Calendar.YEAR, start_date_arr[0]);
			    c.set(Calendar.MONTH, start_date_arr[1]);
			    c.set(Calendar.DAY_OF_MONTH, start_date_arr[2]);
			    c.set(Calendar.HOUR, start_time_arr[0]);
			    c.set(Calendar.MINUTE, start_time_arr[1]);
			    c.set(Calendar.SECOND, 0);
			    c.set(Calendar.MILLISECOND, 0);
			    
			    // automatically removes hours/minutes/seconds/ms
				Date start_date = new Date(c.getTimeInMillis());
				
			    c.set(Calendar.YEAR, end_date_arr[0]);
			    c.set(Calendar.MONTH, end_date_arr[1]);
			    c.set(Calendar.DAY_OF_MONTH, end_date_arr[2]);

			    // automatically removes hours/minutes/seconds/ms
				Date end_date = new Date(c.getTimeInMillis());

			    // automatically removes year/month/day
				Time start_time = new Time(c.getTimeInMillis());
				
			    c.set(Calendar.HOUR, end_time_arr[0]);
			    c.set(Calendar.MINUTE, end_time_arr[1]);

			    // automatically removes year/month/day
				Time end_time = new Time(c.getTimeInMillis());

				// generate response

				if (createSchedule(start_date, end_date, start_time, end_time)) {
					responseObj = new ScheduleResponse("ID GOES HERE", 200);
			        response.put("body", new Gson().toJson(responseObj));
				} else {
					responseObj = new ScheduleResponse("ID GOES HERE", 200);
			        response.put("body", new Gson().toJson(responseObj));
				}
			}
		}
		
		String r = response.toJSONString();
        logger.log("end result:" + r);
        
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(r);  
        writer.close();
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param start_date_arr
	 * @param end_date_arr
	 * @return True on success, false on failure
	 */
	@SuppressWarnings("unchecked")
	boolean parseDates(CreateScheduleRequest request, JSONObject response, 
			int[] start_date_arr, int[] end_date_arr) {
		boolean success = true;
		
		for (int a = 0; a < 2; a++) {
			String date_str[];
			int date[] = new int[3];
			
			if (a == 0) {
				date_str = request.start_date.split("/");
			} else {
				date_str = request.end_date.split("/");
			}
			
			try {
				for (int i = 0; i < 3; i++) {
					if (date_str[i] != null) {
						date[i] = Integer.parseInt(date_str[i]);
					} else {
						logger.log("parseDates(): Malformed date string");
						success = false;
						break;
					}
				}
			} catch (NumberFormatException e) {
				logger.log("parseDates(): Date integer parse error: " + e.toString());
				success = false;
			}
			
			// on invalid input, throw a 400 response
			if (!success) { 
				response.put("body", new Gson().toJson(new ScheduleResponse(400)));
				break; 
			}
			
			// store the date
			if (a == 0) {
				start_date_arr = date;
			} else {
				end_date_arr = date;
			}
		}
		
		return success;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param start_time_arr
	 * @param end_time_arr
	 * @return True on success, false on failure
	 */
	@SuppressWarnings("unchecked")
	boolean parseTimes(CreateScheduleRequest request, JSONObject response, 
			int[] start_time_arr, int[] end_time_arr) {
		boolean success = true;
		
		for (int a = 0; a < 2; a++) {
			String time_str[];
			int time[] = new int[2];
			
			if (a == 0) {
				time_str = request.start_time.split(":");
			} else {
				time_str = request.end_time.split(":");
			}
			
			try {
				for (int i = 0; i < 2; i++) {
					if (time_str[i] != null) {
						time[i] = Integer.parseInt(time_str[i]);
					} else {
						logger.log("parseTimes(): Malformed time string");
						success = false;
						break;
					}
				}
			} catch (NumberFormatException e) {
				logger.log("parseTimes(): Time integer parse error: " + e.toString());
				success = false;
			}
			
			// on invalid input, throw a 400 response
			if (!success) { 
				response.put("body", new Gson().toJson(new ScheduleResponse(400)));
				break; 
			}
			
			// store the time
			if (a == 0) {
				start_time_arr = time;
			} else {
				end_time_arr = time;
			}
		}
		
		return success;
	}
	
	boolean createSchedule(Date start_date, Date end_date, Time start_time, Time end_time) {
		return false;
	}

}
