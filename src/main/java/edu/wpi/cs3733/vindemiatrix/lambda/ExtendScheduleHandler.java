package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import edu.wpi.cs3733.vindemiatrix.lambda.request.ExtendScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.BasicResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.GetScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class ExtendScheduleHandler implements RequestStreamHandler {

    @SuppressWarnings("unchecked")
	@Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for CreateScheduleHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
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
			boolean success = true;
			ExtendScheduleRequest request = new Gson().fromJson(body, ExtendScheduleRequest.class);
			logger.log(request.toString() + "\n");
			
			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new BasicResponse(400, "Input is missing fields.")));
				success = false;
				logger.log("Input is missing fields!\n");
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			java.util.Date date1 = null;
			java.util.Date date2 = null;
			java.util.Date date1_new = null;
			java.util.Date date2_new = null;
			java.util.Date time1 = null;
			java.util.Date time2 = null;
			
			// get existing schedule
			ScheduleDAO dao = new ScheduleDAO();
			Schedule s = null;
			int authorized = 2;
			
			if (success) {
				try {
					authorized = dao.isAuthorized(request.id, request.secret_code);
				} catch (Exception e) {
					response.put("body", new Gson().toJson(new BasicResponse(500, "SQL error with authorization.")));
					success = false;
				}
				
				if (authorized == 1) {
					response.put("body", new Gson().toJson(new BasicResponse(401, "Invalid secret code.")));
					success = false;
				} else if (authorized == 2) {
					response.put("body", new Gson().toJson(new BasicResponse(403, "Incorrect secret code.")));
					success = false;
				}
			}
			
			if (success) {
				try {
					s = dao.getSchedule(request.id);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new BasicResponse(500, "SQL error getting schedule.")));
					success = false;
				}
			}
			
			if (success) {
				try {
					date1 = dateFormat.parse(s.start_date);
					date2 = dateFormat.parse(s.end_date);
					date1_new = dateFormat.parse(request.start_date);
					date2_new = dateFormat.parse(request.end_date);
					
					time1 = timeFormat.parse(s.start_time);
					time2 = timeFormat.parse(s.end_time);
					
					logger.log("Parsed dates and times successfully.\n");
				} catch (java.text.ParseException e) {
					responseObj = new BasicResponse(400, "Error parsing dates/times.");
			        response.put("body", new Gson().toJson(responseObj));
					success = false;
					e.printStackTrace();
				}
			}
			
			if (success) {
				if (date1_new.getTime() > date1.getTime()) { date1_new = date1; }
				if (date2_new.getTime() < date2.getTime()) { date2_new = date2; }
				
				long timeDifference = time2.getTime() - time1.getTime();
				long seconds = timeDifference / 1000;
				long minutes = seconds / 60;
				
				int timeSlotsPerDay = (int) minutes / s.meeting_duration;
				
				
				if (date1_new.getTime() != date1.getTime()) {
					Calendar c = Calendar.getInstance();
					c.setTime(date1_new);
					int day_start = c.get(Calendar.DAY_OF_YEAR);
					c.setTime(date1);
					int day_end = c.get(Calendar.DAY_OF_YEAR);
					
					int days = Math.abs(day_end - day_start);
					
					TimeSlot[] time_slots = new TimeSlot[(int) ((days) * timeSlotsPerDay)];
					TimeSlotDAO ts_dao = new TimeSlotDAO();
					c = Calendar.getInstance();
					
					try {
						c.setTime(fullFormat.parse(request.start_date + " " + s.start_time));
						c.add(Calendar.DAY_OF_MONTH, 1);
						int k = 0;
						for (int i = 0; i < days; i++) {
							if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
								c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
								for(int j = 0; j < timeSlotsPerDay; j++) {
									String date = dateFormat.format(c.getTime());
									String start_time = timeFormat.format(c.getTime());
									c.add(Calendar.MINUTE, s.meeting_duration);
									String end_time = timeFormat.format(c.getTime());
									
									time_slots[k++] = ts_dao.createTimeSlot(s.id, date, start_time, end_time, s.default_open);
								}
							}
							
							c.add(Calendar.MINUTE, -1 * timeSlotsPerDay * s.meeting_duration);
							c.add(Calendar.DAY_OF_MONTH, 1);
						}
					} catch (Exception e) {
						logger.log("Failed to create time slots.\n");
						e.printStackTrace();
						responseObj = new BasicResponse(500, "Error creating time slots.");
				        response.put("body", new Gson().toJson(responseObj));
				        success = false;
					}
					
					try {
						dao.extendStartDate(s.id, request.start_date);
					} catch (Exception e) {
						responseObj = new BasicResponse(500, "Error updating schedule end date.");
				        response.put("body", new Gson().toJson(responseObj));
				        success = false;
						e.printStackTrace();
					}
				}
				
				
				if (date2_new.getTime() != date2.getTime()) {
					Calendar c = Calendar.getInstance();
					c.setTime(date2);
					int day_start = c.get(Calendar.DAY_OF_YEAR);
					c.setTime(date2_new);
					int day_end = c.get(Calendar.DAY_OF_YEAR);
					
					int days = Math.abs(day_end - day_start);
					
					TimeSlot[] time_slots = new TimeSlot[(int) ((days) * timeSlotsPerDay)];
					TimeSlotDAO ts_dao = new TimeSlotDAO();
					c = Calendar.getInstance();
					
					try {
						c.setTime(fullFormat.parse(request.end_date + " " + s.start_time));
						int k = 0;
						for (int i = 0; i < days; i++) {
							if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
								c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
								for(int j = 0; j < timeSlotsPerDay; j++) {
									String date = dateFormat.format(c.getTime());
									String start_time = timeFormat.format(c.getTime());
									c.add(Calendar.MINUTE, s.meeting_duration);
									String end_time = timeFormat.format(c.getTime());
									
									time_slots[k++] = ts_dao.createTimeSlot(s.id, date, start_time, end_time, s.default_open);
								}
							}
							
							c.add(Calendar.MINUTE, -1 * timeSlotsPerDay * s.meeting_duration);
							c.add(Calendar.DAY_OF_MONTH, 1);
						}
					} catch (Exception e) {
						logger.log("Failed to create time slots.\n");
						e.printStackTrace();
						responseObj = new BasicResponse(500, "Error creating time slots.");
				        response.put("body", new Gson().toJson(responseObj));
				        success = false;
					}
					
					try {
						dao.extendEndDate(s.id, request.end_date);
					} catch (Exception e) {
						responseObj = new BasicResponse(500, "Error updating schedule end date.");
				        response.put("body", new Gson().toJson(responseObj));
				        success = false;
						e.printStackTrace();
					}
				}
				
			}
			
			if (success) {
				responseObj = new BasicResponse(200, "Successfully extended schedule and added new timeslots.");
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
