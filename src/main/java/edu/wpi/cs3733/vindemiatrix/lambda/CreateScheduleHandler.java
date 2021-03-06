package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
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
		header.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
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
			responseObj = new CreateScheduleResponse(422, "Error parsing input.");
			response.put("body", new Gson().toJson(responseObj));
			handled = true;
		}

		if (!handled) {
			boolean success = true;
			CreateScheduleRequest request = new Gson().fromJson(body, CreateScheduleRequest.class);
			logger.log(request.toString() + "\n");

			// check inputs
			if (request.isMissingFields()) {
				response.put("body", new Gson().toJson(new CreateScheduleResponse(400, "Input is missing fields.")));
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
			
			String new_start_date = request.start_date, new_end_date = request.end_date;

			if (success) {
				try {
					Calendar c = Calendar.getInstance();
					date1 = dateFormat.parse(request.start_date);
					date2 = dateFormat.parse(request.end_date);
					
					c.setTime(date1);
					
					if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
						c.add(Calendar.DAY_OF_YEAR, 1);
						date1 = c.getTime();
						new_start_date = dateFormat.format(date1);
					} else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
						c.add(Calendar.DAY_OF_YEAR, 2);
						date1 = c.getTime();
						new_start_date = dateFormat.format(date1);
					}
					
					c.setTime(date2);
					
					if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
						c.add(Calendar.DAY_OF_YEAR, -2);
						date2 = c.getTime();
						new_end_date = dateFormat.format(date2);
					} else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
						c.add(Calendar.DAY_OF_YEAR, -1);
						date2 = c.getTime();
						new_end_date = dateFormat.format(date2);
					}
					

					time1 = timeFormat.parse(request.start_time);
					time2 = timeFormat.parse(request.end_time);

					logger.log("Parsed dates and times successfully.\n");
				} catch (java.text.ParseException e) {
					responseObj = new CreateScheduleResponse(400, "Error parsing dates/times.");
					response.put("body", new Gson().toJson(responseObj));
					success = false;
					e.printStackTrace();
				}
			}

			// validate date and time ranges
			if (success) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(System.currentTimeMillis()));
				c.add(Calendar.HOUR, -5);
				c.set(Calendar.MINUTE, 59);
				c.set(Calendar.SECOND, 59);
				c.set(Calendar.MILLISECOND, 999);

				logger.log("Checking if in past: " + (c.getTime().getTime() <= date1.getTime()) + "\n");
				success &= c.getTime().getTime() <= date1.getTime();
				success &= date1.getTime() < date2.getTime();
				success &= time1.getTime() < time2.getTime();

				if (success == false) {
					responseObj = new CreateScheduleResponse(400, "Start date must be in the future and must be before end date. Start time must be before end time.");
					response.put("body", new Gson().toJson(responseObj));
				}
			}

			// create schedule
			if (success) {
				long timeDifference = time2.getTime() - time1.getTime();
				long seconds = timeDifference / 1000;
				long minutes = seconds / 60;

				timeSlotsPerDay = (int) minutes / request.meeting_duration;
				Calendar c = Calendar.getInstance();
				
				c.setTime(date1);
				int day_start = c.get(Calendar.DAY_OF_YEAR);
				
				logger.log("Start Date:" + fullFormat.format(c.getTime()) + "\n");
				logger.log("Start Day:" + day_start + "\n");
				
				c.setTime(date2);
				int day_end = c.get(Calendar.DAY_OF_YEAR);
				
				logger.log("End Date:" + fullFormat.format(c.getTime()) + "\n");
				logger.log("End Day:" + day_end + "\n");
				
				days = Math.abs(day_end - day_start) + 1;
				
				logger.log("Calculated days:" + days + "\n");
				
//				if (days > 5) { days--; }
				
				// ms -> sec -> min -> hr -> day)
				//				days = (int) (dateDifference / 1000 / 60 / 60 / 24);

				logger.log("Determined start and end dates and times, creating schedule...\n");
				

				// create schedule and generate response
				Schedule s = createSchedule(request.name, new_start_date, new_end_date, 
						request.start_time + ":00", request.end_time + ":00", request.meeting_duration, request.default_open);


				if (s != null) {
					logger.log("Created schedule. Now creating time slots...\n");
					TimeSlot[] time_slots = new TimeSlot[(days) * timeSlotsPerDay];
					TimeSlotDAO ts_dao = new TimeSlotDAO();
					c = Calendar.getInstance();

					try {
						c.setTime(fullFormat.parse(new_start_date + " " + request.start_time));
						
						int start_hour = c.get(Calendar.HOUR);
						int start_minute = c.get(Calendar.MINUTE);
						
						logger.log("Start Hour: " + start_hour + ", Start Minute: " + start_minute + "\n");
						
						int k = 0;
						for (int i = 0; i < days; i++) {
							logger.log("Adding timeslots to date:" + fullFormat.format(c.getTime()) + "\n");
							
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
							
							logger.log("> Resultant date:" + fullFormat.format(c.getTime()) + "\n");
							
							c.set(Calendar.HOUR_OF_DAY, start_hour);
							c.set(Calendar.MINUTE, start_minute);

							logger.log("> Reset Hours and Minutes:" + fullFormat.format(c.getTime()) + "\n");
							
							c.add(Calendar.DAY_OF_YEAR, 1);
							
							logger.log("> Updated date:" + fullFormat.format(c.getTime()) + "\n");
						}
						
						responseObj = new CreateScheduleResponse(s, days, timeSlotsPerDay, 200);
				        response.put("body", new Gson().toJson(responseObj));
					} catch (Exception e) {
						logger.log("Failed to create time slots.\n");
						e.printStackTrace();
						responseObj = new CreateScheduleResponse(500, "Error creating time slots.");
						response.put("body", new Gson().toJson(responseObj));
					}
				} else {
					responseObj = new CreateScheduleResponse(500, "Error creating schedule.");
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
	Schedule createSchedule(String name, String start_date, String end_date, String start_time, String end_time, int meeting_duration, int default_open) {
		Schedule s;
		ScheduleDAO dao = new ScheduleDAO();

		try {
			s = dao.createSchedule(name, start_date, end_date, start_time, end_time, meeting_duration, default_open);
		} catch (Exception e) {
			System.out.println("createSchedule(): Error creating schedule: " + e.toString() + "\n");
			s = null;
		}

		return s;
	}

}
