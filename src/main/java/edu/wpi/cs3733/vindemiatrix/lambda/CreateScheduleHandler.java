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
import edu.wpi.cs3733.vindemiatrix.db.dao.ScheduleDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class CreateScheduleHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	@SuppressWarnings("unchecked")
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for CreateScheduleHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "PUT,OPTIONS");
		header.put("Access-Control-Allow-Origin",  "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		CreateScheduleResponse responseObj = null;
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
				responseObj = new CreateScheduleResponse(200);  
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
			logger.log(pe.toString() + "\n");
			responseObj = new CreateScheduleResponse(422);
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}

		if (!handled) {
			boolean success = true;
			CreateScheduleRequest request = new Gson().fromJson(body, CreateScheduleRequest.class);
			logger.log(request.toString() + "\n");
			
			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new CreateScheduleResponse(400)));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			java.util.Date date1 = null;
			java.util.Date date2 = null;
			java.util.Date time1 = null;
			java.util.Date time2 = null;
			
			int days = 0;
			int numSlots = 0;
			
			if (success) {
				try {
					date1 = dateFormat.parse(request.start_date);
					date2 = dateFormat.parse(request.end_date);
					
					time1 = timeFormat.parse(request.start_time);
					time2 = timeFormat.parse(request.end_time);
					
					logger.log("Parsed dates and times successfully.\n");
				} catch (java.text.ParseException e) {
					success = false;
					e.printStackTrace();
				}
			}
			
			// create schedule
			if (success) {
				long timeDifference = time2.getTime() - time1.getTime();
				long seconds = timeDifference / 1000;
				long minutes = seconds / 60;
				
				numSlots = (int) minutes / request.meeting_duration;
				
				long dateDifference = date2.getTime() - date1.getTime();
				// ms -> sec -> min -> hr -> day
				days = (int) (dateDifference / 1000 / 60 / 60 / 24);		
				
				logger.log("Determined start and end dates and times, creating schedule...\n");

				// create schedule and generate response
				Schedule s = createSchedule(request.start_date, request.end_date, 
						request.start_time + ":00", request.end_time + ":00", request.meeting_duration);
				if (s != null) {
					responseObj = new CreateScheduleResponse(s.organizer, 200);
			        response.put("body", new Gson().toJson(responseObj));
				} else {
					responseObj = new CreateScheduleResponse(500);
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
						logger.log("parseDates(): Malformed date string\n");
						success = false;
						break;
					}
				}
			} catch (NumberFormatException e) {
				logger.log("parseDates(): Date integer parse error: " + e.toString() + "\n");
				success = false;
			}
			
			// on invalid input, throw a 400 response
			if (!success) { 
				response.put("body", new Gson().toJson(new CreateScheduleResponse(400)));
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
						logger.log("parseTimes(): Malformed time string\n");
						success = false;
						break;
					}
				}
			} catch (NumberFormatException e) {
				logger.log("parseTimes(): Time integer parse error: " + e.toString() + "\n");
				success = false;
			}
			
			// on invalid input, throw a 400 response
			if (!success) { 
				response.put("body", new Gson().toJson(new CreateScheduleResponse(400)));
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
	
	/**
	 * Create a schedule in the database
	 * @param start_date The start date
	 * @param end_date The end date
	 * @param start_time The start time
	 * @param end_time The end time
	 * @param meeting_duration The length of a meeting in minutes
	 * @return the schedule that was created
	 */
	Schedule createSchedule(String start_date, String end_date, String start_time, String end_time, int meeting_duration) {
		Schedule s;
		ScheduleDAO dao = new ScheduleDAO();

		try {
			s = dao.createSchedule(start_date, end_date, start_time, end_time, meeting_duration);
		} catch (Exception e) {
			System.out.println("createSchedule(): Error creating schedule: " + e.toString() + "\n");
			s = null;
		}
		
		return s;
	}

}
