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
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.lambda.response.CreateScheduleResponse;

public class CreateScheduleHandlerTest {

    private static final String SAMPLE_INPUT_STRING = "{\n" + 
    		"  \"name\": \"Test Organizer\",\n" + 
    		"  \"start_date\": \"2019-10-29\",\n" + 
    		"  \"end_date\": \"2019-12-29\",\n" + 
    		"  \"start_time\": \"08:00\",\n" + 
    		"  \"end_time\": \"14:00\",\n" + 
    		"  \"meeting_duration\": \"60\",\n" + 
    		"  \"default_open\": \"1\"\n" + 
    		"}";

    @Test
    public void testCreateScheduleHandler() throws IOException {
        CreateScheduleHandler handler = new CreateScheduleHandler();

        InputStream input = new ByteArrayInputStream(SAMPLE_INPUT_STRING.getBytes());;
        OutputStream output = new ByteArrayOutputStream();

        handler.handleRequest(input, output, new TestContext());
        
        JSONObject response = null;
		try {
			JSONParser parser = new JSONParser();
			response = (JSONObject) parser.parse(output.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		CreateScheduleResponse resp = new Gson().fromJson((String) response.get("body"), CreateScheduleResponse.class);
		
		Assert.assertEquals("Test Organizer", resp.name);
		Assert.assertEquals("2019-10-29", resp.start_date);
		Assert.assertEquals("2019-12-29", resp.end_date);
		Assert.assertEquals("08:00", resp.start_time);
		Assert.assertEquals("14:00", resp.end_time);
		Assert.assertEquals(60, resp.meeting_duration);
		Assert.assertEquals("Schedule(" + resp.secret_code + "," + resp.name + "," + resp.id + ")", resp.toString());
		
		CreateScheduleResponse resp2 = new CreateScheduleResponse(400);
		Assert.assertEquals(400, resp2.httpCode);
		
		CreateScheduleResponse resp3 = new CreateScheduleResponse(400, "Bad Input");
		Assert.assertEquals(400, resp3.httpCode);
		Assert.assertEquals("Bad Input", resp3.error);
		Assert.assertEquals("NoSchedule", resp3.toString());

		Pattern p = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
		Matcher m = p.matcher(resp.secret_code);
		
		Assert.assertTrue(m.find());

        String sampleOutputString = output.toString();
        System.out.println(sampleOutputString);
    }
}
