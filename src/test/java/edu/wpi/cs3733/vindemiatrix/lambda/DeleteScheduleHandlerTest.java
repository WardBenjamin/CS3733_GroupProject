//package edu.wpi.cs3733.vindemiatrix.lambda;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.Reader;
//
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.junit.Assert;
//import org.junit.Test;
//
//import com.google.gson.Gson;
//
//import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
//import edu.wpi.cs3733.vindemiatrix.lambda.request.GetScheduleRequest;
//
///**
// * A simple test harness for locally invoking your Lambda function handler.
// */
//public class DeleteScheduleHandlerTest {
//
//    private static final String SAMPLE_INPUT_STRING = "{\"foo\": \"bar\"}";
//    private static final String EXPECTED_OUTPUT_STRING = "{\"FOO\": \"BAR\"}";
//
//    @Test
//    public void testDeleteScheduleHandler() throws IOException {
//        DeleteScheduleHandler handler = new DeleteScheduleHandler();
//
//        InputStream input = new ByteArrayInputStream(SAMPLE_INPUT_STRING.getBytes());;
//        OutputStream output = new ByteArrayOutputStream();
//
//        handler.handleRequest(input, output, null);
//
//        // TODO: validate output here if needed.
//        String sampleOutputString = output.toString();
//        System.out.println(sampleOutputString);
//        Assert.assertEquals(EXPECTED_OUTPUT_STRING, sampleOutputString);
//    }
//}
