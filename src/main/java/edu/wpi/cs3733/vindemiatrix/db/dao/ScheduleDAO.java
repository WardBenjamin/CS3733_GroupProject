package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
				s = new Schedule(id, result.getString(2), result.getString(3), 
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
				System.out.println("executed select statement");
				ResultSet rs = ps.getResultSet();
				System.out.println("got result set");
				int id = 0;
				if (rs.first()) {
					id = rs.getInt(1);
				}
				
				System.out.println("got id of " + id);
				
				s = new Schedule(id, uuid.toString(), start_date, end_date, start_time, end_time, meeting_duration);
				System.out.println("created schedule");
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
	 * @param secretCode secret code for the schedule 
	 * @param id schedule id 
	 * FIXME fix the queries and function
	 */
	public boolean deleteSchedule(String secretCode, int id) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM Schedules WHERE id=?;");
			ps.setInt(1, id);
			int numAffect = ps.executeUpdate(); 
			ps.close();
			return (numAffect == 1);
		} catch (Exception e) {
			throw new Exception("Failed to delete schedule: " + e.getMessage());
		}
	}

}
