package edu.wpi.cs3733.vindemiatrix.lambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;

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
		

		CreateScheduleRequest request = new Gson().fromJson((String) response.get("body"), CreateScheduleRequest.class);
		
		Assert.assertEquals("Test Organizer", request.name);
		Assert.assertEquals("2019-10-29", request.start_date);
		Assert.assertEquals("2019-12-29", request.end_date);
		Assert.assertEquals("08:00", request.start_time);
		Assert.assertEquals("14:00", request.end_time);
		Assert.assertEquals(60, request.meeting_duration);
		Assert.assertEquals(1, request.default_open);

        String sampleOutputString = output.toString();
        System.out.println(sampleOutputString);
    }
}
