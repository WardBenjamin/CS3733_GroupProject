package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.db.dao.MeetingDAO;
import edu.wpi.cs3733.vindemiatrix.db.dao.ScheduleDAO;
import edu.wpi.cs3733.vindemiatrix.db.dao.TimeSlotDAO;
import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.request.DeleteScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateMeetingResponse;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateScheduleResponse;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class CreateScheduleHandlerTest {

	private static final String CREATE_SCHEDULE_STRING = "{\n" + 
			"  \"name\": \"Test Schedule\",\n" + 
			"  \"start_date\": \"2019-10-29\",\n" + 
			"  \"end_date\": \"2019-12-29\",\n" + 
			"  \"start_time\": \"08:00\",\n" + 
			"  \"end_time\": \"14:00\",\n" + 
			"  \"meeting_duration\": \"60\",\n" + 
			"  \"default_open\": \"1\"\n" + 
			"}";

	private static final String CREATE_MEETING_STRING ="{\n" + 
			"  \"name\": \"Corrin\",\n" + 
			"  \"time_slot_id\": \"0\n" +
			"}";

	@Test
	public void testCreateScheduleHandler() throws IOException {
		//input info
		CreateScheduleHandler csHandler = new CreateScheduleHandler();

		InputStream csInput = new ByteArrayInputStream(CREATE_SCHEDULE_STRING.getBytes());;
		OutputStream csOutput = new ByteArrayOutputStream();

		csHandler.handleRequest(csInput, csOutput, new TestContext());

		JSONObject csResponse = null;
		try {
			JSONParser parser = new JSONParser();
			csResponse = (JSONObject) parser.parse(csOutput.toString());
		} catch (org.json.simple.parser.ParseException e2) {
			e2.printStackTrace();
		}
		//-----Create schedule-----\\ 
		CreateScheduleResponse csResp = new Gson().fromJson((String) csResponse.get("body"), CreateScheduleResponse.class);

		Assert.assertEquals("Test Schedule", csResp.name);
		Assert.assertEquals("2019-10-29", csResp.start_date);
		Assert.assertEquals("2019-12-29", csResp.end_date);
		Assert.assertEquals("08:00", csResp.start_time);
		Assert.assertEquals("14:00", csResp.end_time);
		Assert.assertEquals(60, csResp.meeting_duration);
		Assert.assertEquals("Schedule(" + csResp.secret_code + "," + csResp.name + "," + csResp.id + ")", csResp.toString());

		CreateScheduleResponse resp2 = new CreateScheduleResponse(400);
		Assert.assertEquals(400, resp2.httpCode);

		CreateScheduleResponse resp3 = new CreateScheduleResponse(400, "Bad Input");
		Assert.assertEquals(400, resp3.httpCode);
		Assert.assertEquals("Bad Input", resp3.error);

		Pattern p = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
		Matcher m = p.matcher(csResp.secret_code);

		String csOutputString = csOutput.toString();
		System.out.println(csOutputString);
		//
		//		//Assert.assertTrue(m.find());
		//

		TimeSlotDAO sTimeSlotDAO = new TimeSlotDAO();
		ScheduleDAO sScheduleDAO = new ScheduleDAO();

		CreateScheduleRequest csRequest = new CreateScheduleRequest(csResp.name, csResp.start_date, csResp.end_date,
				csResp.start_time, csResp.end_time, csResp.meeting_duration, 0);


		try {

			//retrieves list of time slots for schedule
			//List<TimeSlot> t = sTimeSlotDAO.getTimeSlots(csResp.id, csResp.start_date, csResp.end_date);

			//sets time slots to close on day
			sTimeSlotDAO.updateTimeSlotsOnDay(csResp.start_date, csResp.id, false);

			//validates time slots are closed 
			Assert.assertFalse(sTimeSlotDAO.hasMeetingOnDay(csResp.start_date, csResp.id));

			//validates there there are no meeting when schedule is initalized
			Assert.assertFalse(sTimeSlotDAO.hasMeetingOnDay(csResp.start_date, csResp.id));

			//validated validity of secret code for timeslot
			Assert.assertEquals(sTimeSlotDAO.isAuthorized(csResp.id, csResp.secret_code), 2);
			Assert.assertEquals(sScheduleDAO.isAuthorized(csResp.id, csResp.secret_code), 0);

			//creates schedule with an id 
			Schedule s = sScheduleDAO.getSchedule(csResp.id);

			Assert.assertEquals(sScheduleDAO.getNumWeeks(csResp.id), 9);
			Assert.assertTrue(sScheduleDAO.extendEndDate(csResp.id, "2019-12-30"));
			Assert.assertTrue(sScheduleDAO.extendStartDate(csResp.id, "2019-10-12"));

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		//		//-----CreateMeeting-----\\

		//
		//		sMeetingDAO.
		InputStream cmInput = new ByteArrayInputStream(CREATE_MEETING_STRING.getBytes());
		OutputStream cmOutput = new ByteArrayOutputStream();
		MeetingDAO sMeetingDAO = new MeetingDAO();
		CreateMeetingHandler cmHandler = new CreateMeetingHandler();
		cmHandler.handleRequest(cmInput, cmOutput, new TestContext());
		JSONObject cmResponse = null;
		JSONParser parser1 = new JSONParser();
		try {
			cmResponse = (JSONObject) parser1.parse(cmOutput.toString());
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CreateMeetingResponse cmResp = new Gson().fromJson((String) cmResponse.get("body"), CreateMeetingResponse.class);
		//validate creation of meeting
		try {
			Assert.assertTrue(sTimeSlotDAO.hasMeetingOnDay(csResp.start_date, csResp.id));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		//
		//
		//-----DeleteScheduleTest-----\\ 
		InputStream dsInput = new ByteArrayInputStream(csOutputString.getBytes());
		OutputStream dsOutput = new ByteArrayOutputStream();
		DeleteScheduleRequest dsRequest = new DeleteScheduleRequest(csResp.id, csResp.secret_code);
		DeleteScheduleHandler dsHandler = new DeleteScheduleHandler();
		dsHandler.handleRequest(dsInput, dsOutput, new TestContext());

		//-----testing model objects-----\\
		Meeting m1 = new Meeting(10, "password", "Me");
		Schedule s1 = new Schedule(10, "password", "me", "2018/12/15", "2018/12/30", "08:00", "03:00", 15, 0);
		TimeSlot t1 = new TimeSlot(10, "2018/12/15", "08:00", "08:30", 
				true, m1);

		//-----MeetingDOA-----\\
		MeetingDAO sMeetingDOA = new MeetingDAO();
		try {
			sMeetingDOA.createMeeting(0, null);
		} catch (Exception e) {
			e.printStackTrace();
		}






	}
}
