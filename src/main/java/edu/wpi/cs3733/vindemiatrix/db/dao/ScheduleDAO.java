package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.wpi.cs3733.vindemiatrix.db.SchedulerDatabase;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class ScheduleDAO {
	Connection conn; 
	
	public ScheduleDAO() {
		try {
			conn = SchedulerDatabase.connect(); 
		} catch (Exception e) {
			conn = null;
		}
	}
	
	/*
	 * Gets a schedule and all associated time slots within the week
	 * @param id id of schedule to retrieve 
	 * @param start_date Start day of week for the weekly schedule to get
	 */
	public Schedule getSchedule(int id, String start_date) throws Exception {
		try {
			Schedule s = null;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE id=?;");
			ps.setInt(1, id);
			ResultSet result = ps.executeQuery();
			
			if (result.next()) {
				s = new Schedule(id, "", result.getString(3), 
						result.getString(4), result.getString(5), result.getString(6), result.getInt(7));
			}
			
			result.close();
			ps.close();
			return s; 
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to get schedule: " + e.getMessage()) ; 
		}
	}
	
	/*
	 * Creates a schedule 
	 * @param start_date schedule start date
	 * @param end_date schedule end date
	 * @param start_time schedule start time 
	 * @param end_time schedule end time 
	 * @return The schedule or null on failure
	 */
	public Schedule createSchedule(String start_date, String end_date, String start_time, 
			String end_time, int meeting_duration) throws Exception{
		Schedule s = null;
		
		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO `schedules` (`start_date`, `end_date`, "
					+ "`start_time`, `end_time`, `organizer`, `meeting_duration`) "
					+ "VALUES (?,?,?,?,?,?);");
			
			ps.setString(1, start_date);
			ps.setString(2, end_date);
			ps.setString(3, start_time);
			ps.setString(4, end_time);
			ps.setInt(6, meeting_duration);
			
			UUID uuid = UUID.randomUUID();
			
			ps.setString(5, uuid.toString());
			ps.execute();
			
			ps = conn.prepareStatement("SELECT `id` FROM `schedules` WHERE `organizer` = ?");
			ps.setString(1, uuid.toString());

			if (ps.execute()) {
				ResultSet rs = ps.getResultSet();

				int id = 0;
				if (rs.first()) {
					id = rs.getInt(1);
				}
				
				s = new Schedule(id, uuid.toString(), start_date, end_date, start_time, end_time, meeting_duration);
			}

			ps.close();
		} catch (Exception e) {
			throw new Exception("Exception while creating schedule: " + e.getMessage());
		}
		
		return s;
	}
	
	/*
	 * extends the date range 
	 * @param secretCode secret code for the schedule
	 * @param id schedule id 
	 * @param startDate new start date of schedule
	 * @param endDate new end date of schedule
	 */
	//TODO: Extend date range 
	public boolean extendDateRange(String secretCode, int id, String startDate, String endDate) {
		return true; 
	}
	
	/*
	 * deletes a schedule 
	 * @param id schedule id
	 * @return true unless exception
	 */
	public boolean deleteSchedule(int id) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM `schedules` WHERE `id` = ?");
			ps.setInt(1, id);
			ps.executeUpdate();
			ps.close();
			return true;
		} catch (Exception e) {
			throw new Exception("Failed to delete schedule: " + e.getMessage());
		}
	}
	
	/**
	 * check if the secret code given is the code for the given schedule
	 * @param id the schedule ID
	 * @param secretCode the schedule secret code
	 * @return 0 if good, 1 if invalid form, 2 if incorrect code
	 * @throws Exception on SQL failure
	 */
	public int isAuthorized(int id, String secretCode) throws Exception {
		Pattern p = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
		Matcher m = p.matcher(secretCode);
		
		if (!m.find()) {
			return 1;
		}
		
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE id = ?;");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next() && rs.getString(2).equals(secretCode)) {
				ps.close();
				return 0;
			}
			
			ps.close();
			return 2;
		} catch (Exception e) {
			throw new Exception("Failed to delete schedule: " + e.getMessage());
		}
	}

}
