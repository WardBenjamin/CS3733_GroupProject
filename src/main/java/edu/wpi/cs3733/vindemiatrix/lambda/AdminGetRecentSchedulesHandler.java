package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
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
import edu.wpi.cs3733.vindemiatrix.lambda.request.AdminDeleteSchedulesRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.request.AdminGetRecentSchedulesRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.AdminDeleteSchedulesResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.AdminGetRecentSchedulesResponse;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class AdminGetRecentSchedulesHandler implements RequestStreamHandler {

    @SuppressWarnings("unchecked")
	@Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    	LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for AdminGetRecentSchedules\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "GET,DELETE,OPTIONS");
		header.put("Access-Control-Allow-Origin",  "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		AdminGetRecentSchedulesResponse responseObj = null;
		String body = null;
		boolean handled = false;
		
		String _hours = null;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			JSONParser parser = new JSONParser();
			JSONObject event = (JSONObject) parser.parse(reader);
			logger.log("Parsed Input:" + event.toJSONString() + "\n");
			
			String method = (String) event.get("httpMethod");
			if (method != null && method.equalsIgnoreCase("OPTIONS")) {
				// OPTIONS needs a 200 response
				logger.log("OPTIONS request received" + "\n");
				responseObj = new AdminGetRecentSchedulesResponse(200);  
		        response.put("body", new Gson().toJson(responseObj));
		        handled = true;
			} else if (method != null && method.equalsIgnoreCase("GET")) {
				JSONObject params = (JSONObject) event.get("queryStringParameters");
				_hours = (String) params.get("hours");
			} else {
				body = (String) event.get("body");
				
				if (body == null) {
					// for testing only
					body = event.toJSONString();
				}
			}
		} catch (ParseException pe) {
			// unable to process input
			logger.log(pe.toString() + "\n");
			responseObj = new AdminGetRecentSchedulesResponse(422, "Error parsing input.");
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}
		
		if (!handled) {
			boolean success = true;
			List<Schedule> schedules = null;
			ScheduleDAO dao = new ScheduleDAO();
			
			AdminGetRecentSchedulesRequest request = null;
			
			if (_hours != null) {
				request = new AdminGetRecentSchedulesRequest(Integer.parseInt(_hours));
			} else {
				request = new Gson().fromJson(body, AdminGetRecentSchedulesRequest.class);
			}
			
			logger.log(request.toString() + "\n");
			
			try {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				c.setTime(new Date(System.currentTimeMillis()));
//				c.add(Calendar.HOUR, -5); // get to our time-zone // not actually since the DB is in UTC+0
				logger.log("Current time: " + fullFormat.format(c.getTime()) + "\n");
				c.add(Calendar.HOUR, -request.hours);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				
				schedules = dao.getRecentSchedules(fullFormat.format(c.getTime()));
				
				logger.log("Found schedules created since " + fullFormat.format(c.getTime()) + " (last " + request.hours + " hours).\n");
			} catch (Exception e) {
				e.printStackTrace();
				response.put("body", new Gson().toJson(new AdminDeleteSchedulesResponse(500, "SQL error while finding recent schedules.")));
				success = false;
			}
			
			// generate final response
			if (success) {
				responseObj = new AdminGetRecentSchedulesResponse(schedules, 200);
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
