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
import java.util.TimeZone;

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
import edu.wpi.cs3733.vindemiatrix.lambda.request.DeleteScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.AdminDeleteSchedulesResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.BasicResponse;

public class AdminDeleteSchedulesHandler implements RequestStreamHandler {

    @SuppressWarnings("unchecked")
	@Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
    	LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler for GetScheduleHandler\n");

		JSONObject header = new JSONObject();
		header.put("Content-Type",  "application/json");
		header.put("Access-Control-Allow-Methods", "GET,DELETE,OPTIONS");
		header.put("Access-Control-Allow-Origin",  "*");
        
		JSONObject response = new JSONObject();
		response.put("headers", header);
		
		AdminDeleteSchedulesResponse responseObj = null;
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
				responseObj = new AdminDeleteSchedulesResponse(200);  
		        response.put("body", new Gson().toJson(responseObj));
		        handled = true;
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
			responseObj = new AdminDeleteSchedulesResponse(422, "Error parsing input.");
			response.put("body", new Gson().toJson(responseObj));
	        handled = true;
		}
		
		if (!handled) {
			boolean success = true;
			int deletion_count = 0;
			String schedules = null;
			ScheduleDAO dao = new ScheduleDAO();
			
			AdminDeleteSchedulesRequest request = new Gson().fromJson(body, AdminDeleteSchedulesRequest.class);
			
			logger.log(request.toString() + "\n");
			
			try {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				c.setTime(new Date(System.currentTimeMillis()));
				logger.log("Current time: " + fullFormat.format(c.getTime()) + "\n");
//				c.set(Calendar.HOUR, 0);
//				c.set(Calendar.MINUTE, 0);
//				c.set(Calendar.SECOND, 0);
				c.add(Calendar.HOUR, -5); // get to our time-zone
				c.add(Calendar.DAY_OF_MONTH, -request.days);
				
				schedules = dao.getSchedulesOlderThan(fullFormat.format(c.getTime()));
				
				logger.log("Found schedules older than " + fullFormat.format(c.getTime()) + " (" + request.days + " days): " + schedules + "\n");
			} catch (Exception e) {
				e.printStackTrace();
				response.put("body", new Gson().toJson(new AdminDeleteSchedulesResponse(500, "SQL error while finding old schedules.")));
				success = false;
			}

			// delete the time slots (and meetings) in this schedule
			if (success) {
				TimeSlotDAO ts_dao = new TimeSlotDAO();
				try {
					int num_ts_deleted = ts_dao.adminDeleteTimeSlots(schedules);
					logger.log("Deleted " + num_ts_deleted + " time slots.\n");
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new AdminDeleteSchedulesResponse(500, "SQL error while deleting time slots.")));
					success = false;
				}
			}
			
			// delete the schedules
			if (success) {
				try {
					deletion_count = dao.adminDeleteSchedules(schedules);
				} catch (Exception e) {
					e.printStackTrace();
					response.put("body", new Gson().toJson(new AdminDeleteSchedulesResponse(500, "SQL error while deleting schedules.")));
					success = false;
				}
			}
			
			// generate final response
			if (success) {
				responseObj = new AdminDeleteSchedulesResponse(deletion_count, 200);
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
