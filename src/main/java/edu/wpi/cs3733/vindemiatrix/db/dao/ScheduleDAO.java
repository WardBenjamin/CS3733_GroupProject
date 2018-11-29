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
	 * gets a schedule and all associated time slots and meetings
	 * @param id id of schedule to retrieve 
	 */
	public Schedule getSchedule(int id) throws Exception {
		try {
			Schedule s = null;
			// FIXME invalid query
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM Schedules WHERE id=?;");
			ps.setInt(1, id);
			ResultSet rSet = ps.executeQuery();
			
			while (rSet.next()) {
				// s = generateSchedule(rSet); FIXME: NOT DEFINED
			}
			rSet.close();
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
	public Schedule createSchedule(Date start_date, Date end_date, Time start_time, Time end_time) throws Exception{
		Schedule s = null;
		
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO `schedules` (`start_date`, `end_date`, `start_time`, `end_time`, `organizer`) VALUES (?,?,?,?,?);");
			
			ps.setDate(1, start_date);
			ps.setDate(2, end_date);
			ps.setTime(3, start_time);
			ps.setTime(4, end_time);
			
			UUID uuid = UUID.randomUUID();
			
			ps.setString(5, uuid.toString());
			
			if (ps.execute()) {
				ps = conn.prepareStatement("SELECT `id` FROM `schedule` WHERE `organizer` = ?");
				ps.setString(1, uuid.toString());
				
				if (ps.execute()) {
					ResultSet rs = ps.getResultSet();
					s = new Schedule(rs.getInt(1), uuid.toString(), start_date, end_date, start_time, end_time);	
				}
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
