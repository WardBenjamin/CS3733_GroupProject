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
import edu.wpi.cs3733.vindemiatrix.db.dao.TimeSlotDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class CreateScheduleHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	@Override
	@SuppressWarnings("unchecked")
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for CreateScheduleHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "GET,PUT,DELETE,OPTIONS");
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
			SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			java.util.Date date1 = null;
			java.util.Date date2 = null;
			java.util.Date time1 = null;
			java.util.Date time2 = null;
			
			int days = 0;
			int timeSlotsPerDay = 0;
			
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
			
			// validate date and time ranges
			if (success) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(System.currentTimeMillis()));
//				c.set(Calendar.HOUR, 0);
//				c.set(Calendar.MINUTE, 0);
//				c.set(Calendar.SECOND, 0);
//				c.set(Calendar.MILLISECOND, 0);

				logger.log("Checking if in past: " + (c.getTime().getTime() <= date1.getTime()) + "\n");
				success &= c.getTime().getTime() <= date1.getTime();
				success &= date1.getTime() < date2.getTime();
				success &= time1.getTime() < time2.getTime();
				
				if (success == false) {
					responseObj = new CreateScheduleResponse(400);
			        response.put("body", new Gson().toJson(responseObj));
				}
			}
			
			// create schedule
			if (success) {
				long timeDifference = time2.getTime() - time1.getTime();
				long seconds = timeDifference / 1000;
				long minutes = seconds / 60;
				
				timeSlotsPerDay = (int) minutes / request.meeting_duration;
				
				long dateDifference = date2.getTime() - date1.getTime();
				// ms -> sec -> min -> hr -> day)
				days = (int) (dateDifference / 1000 / 60 / 60 / 24);		
				
				logger.log("Determined start and end dates and times, creating schedule...\n");

				// create schedule and generate response
				Schedule s = createSchedule(request.name, request.start_date, request.end_date, 
						request.start_time + ":00", request.end_time + ":00", request.meeting_duration);
				if (s != null) {
					logger.log("Created schedule. Now creating time slots...\n");
					TimeSlot[] time_slots = new TimeSlot[(int) (days * timeSlotsPerDay)];
					TimeSlotDAO ts_dao = new TimeSlotDAO();
					Calendar c = Calendar.getInstance();
					
					try {
						c.setTime(fullFormat.parse(request.start_date + " " + request.start_time));
						int k = 0;
						for (int i = 0; i < days; i++) {
							if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
								c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
								for(int j = 0; j < timeSlotsPerDay; j++) {
									String date = dateFormat.format(c.getTime());
									String start_time = timeFormat.format(c.getTime()) + ":00";
									c.add(Calendar.MINUTE, request.meeting_duration);
									String end_time = timeFormat.format(c.getTime()) + ":00";
									
									time_slots[k++] = ts_dao.createTimeSlot(s.id, date, start_time, end_time, request.default_open);
								}
							}
							
							c.add(Calendar.MINUTE, -1 * timeSlotsPerDay * request.meeting_duration);
							c.add(Calendar.DAY_OF_MONTH, 1);
						}
						
						responseObj = new CreateScheduleResponse(s.name, s.secret_code, request, s.id, days, timeSlotsPerDay, 200);
				        response.put("body", new Gson().toJson(responseObj));
					} catch (Exception e) {
						logger.log("Failed to create time slots.\n");
						e.printStackTrace();
						responseObj = new CreateScheduleResponse(500);
				        response.put("body", new Gson().toJson(responseObj));
					}
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
	 * Create a schedule in the database
	 * @param start_date The start date
	 * @param end_date The end date
	 * @param start_time The start time
	 * @param end_time The end time
	 * @param meeting_duration The length of a meeting in minutes
	 * @return the schedule that was created
	 */
	Schedule createSchedule(String name, String start_date, String end_date, String start_time, String end_time, int meeting_duration) {
		Schedule s;
		ScheduleDAO dao = new ScheduleDAO();

		try {
			s = dao.createSchedule(name, start_date, end_date, start_time, end_time, meeting_duration);
		} catch (Exception e) {
			System.out.println("createSchedule(): Error creating schedule: " + e.toString() + "\n");
			s = null;
		}
		
		return s;
	}

}
