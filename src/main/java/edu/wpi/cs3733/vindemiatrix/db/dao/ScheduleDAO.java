package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
	java.sql.Connection conn; 
	
	public ScheduleDAO() {
		try {
			conn = SchedulerDatabase.java; 
		}catch (Exceptione) {
			conn = null;
		}
	}
	/*
	 * gets a schedule and all associated time slots and meetings
	 * @param id id of schedule to retrieve 
	 */
	public Schedule getSchedule(Int id) {
		try {
			Schedule s = null;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM Schedules WHERE id=?;");
			ps.setInt(1, id);
			ResultSet rSet = ps.executeQuery();
			
			while (rSet.next()) {
				s = generateSchedule(rSet);
			}
			rSet.close();
			ps.close();
			return s; 
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to get schedule: " + e.getMessage()) ; 
		}
		
	}
	/*
	 * creates a schedule 
	 * @param startDate schedule start date
	 * @param endDate scheudle end date
	 * @param startTime schedule start time 
	 * @param endTime scheudle end time 
	 */
	public Schedule createSchedule(String startDate, String endDate, String startTime, String endTime) throws Exception{
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM Constants WHERE name = ?;");
			ps.setString(1, startDate);
			ResultSet rSet ps.executeQuery(); 
	 
			ps = conn.prepareStatement("INSERT INTO Schedule (startDate, endDate, startTime, endTime) values(?,?,?,?);");
			ps.setString(1, startDate);
			ps.setString(2, endDate);
			ps.setString(3,  startTime);
			ps.setString(4, endTime);
			ps.execute();
			return true; 
		}
		catch {
			throw new Exception("Failed to insert schedule: " + e.getMessage());
		}
	}
	
	/*
	 * extends the date range 
	 * @param secretCode secret code for the schedule
	 * @param id schedule id 
	 * @param startDate new start date of schedule
	 * @param endDate new end date of schedule
	 */
	//TODO: Extend date renge 
	public boolean extendDateRange(String secretCode, Int id, String startDate, String endDate) {
		return true; 
	}
	
	/*
	 * deletes a schedule 
	 * @param secretCode secret code for the schedule 
	 * @param id schedule id 
	 */
	public boolean deleteSchedule(String secretCode, Int id) {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM Schedules WHERE id=?;");
			ps.setInt(1, id);
			int numAffect = ps.executeUpdate(); 
			ps.close();
			return (numAffect == 1);
		} catch (Exception e) {
			throw new Exception("Failed ot delete schedule: " + e.getMessage());
		}
	}

}
