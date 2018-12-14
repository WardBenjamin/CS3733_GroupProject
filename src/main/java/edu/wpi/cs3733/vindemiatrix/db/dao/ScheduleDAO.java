package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
	
	/**
	 * Gets a schedule and all associated time slots within the week
	 * @param id id of schedule to retrieve 
	 */
	public Schedule getSchedule(int id) throws Exception {
		try {
			Schedule s = null;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE id=?;");
			ps.setInt(1, id);
			ResultSet result = ps.executeQuery();
			
			if (result.next()) {
				s = new Schedule(id, "", result.getString(3), result.getString(4), 
						result.getString(5), result.getString(6), result.getString(7), result.getInt(8), result.getInt(9));
			}
			
			result.close();
			ps.close();
			return s; 
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to get schedule: " + e.getMessage()) ; 
		}
	}
	
	/**
	 * get schedules older than a given date (exclusive)
	 * @param timestamp cut off timestamp
	 * @return comma separated list of schedules to be used in SQL IN
	 * @throws Exception
	 */
	public String getSchedulesOlderThan(String timestamp) throws Exception {
		try {
			String schedules = "-1"; // place holder, will be ignored by SQL queries as no schedule has ID -1 (this prevents IN () from breaking if no schedules are found)
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE `created_at` < ?;");
			ps.setString(1, timestamp);
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				schedules += "," + result.getInt("id");
			}
			
			result.close();
			ps.close();
			return schedules; 
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to get schedules older than date: " + e.getMessage()) ; 
		}
	}

	
	/**
	 * Gets the number of weeks in a schedule
	 * @param id id of schedule to check 
	 * @return the number of weeks or 0 on failure
	 */
	public int getNumWeeks(int id) throws Exception {
		try {
			Schedule s = null;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE id=?;");
			ps.setInt(1, id);
			ResultSet result = ps.executeQuery();
			
			if (result.next()) {
				s = new Schedule(id, "", result.getString(3), result.getString(4), 
						result.getString(5), result.getString(6), result.getString(7), result.getInt(8), result.getInt(9));
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				
				java.util.Date d = dateFormat.parse(s.end_date);
			    Calendar cal = new GregorianCalendar();
			    cal.setTime(dateFormat.parse(s.start_date));
			    
			    int weeks = 0;
			    while (cal.getTime().before(d)) {
			        cal.add(Calendar.WEEK_OF_YEAR, 1);
			        weeks++;
			    }

				result.close();
				ps.close();
				return weeks; 
			} else { 
				result.close();
				ps.close();
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to get schedule: " + e.getMessage()) ; 
		}
	}
	
	/**
	 * Creates a schedule 
	 * @param name the organizer's name
	 * @param start_date schedule start date
	 * @param end_date schedule end date
	 * @param start_time schedule start time 
	 * @param end_time schedule end time 
	 * @return The schedule or null on failure
	 */
	public Schedule createSchedule(String name, String start_date, String end_date, String start_time, 
			String end_time, int meeting_duration, int default_open) throws Exception{
		Schedule s = null;
		
		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO `schedules` (`name`, `start_date`, `end_date`, "
					+ "`start_time`, `end_time`, `secret_code`, `meeting_duration`, `default_open`) "
					+ "VALUES (?,?,?,?,?,?,?,?);");

			ps.setString(1, name);
			ps.setString(2, start_date);
			ps.setString(3, end_date);
			ps.setString(4, start_time);
			ps.setString(5, end_time);
			ps.setInt(7, meeting_duration);
			ps.setInt(8, default_open);
			
			UUID uuid = UUID.randomUUID();
			
			ps.setString(6, uuid.toString());
			ps.execute();
			
			ps = conn.prepareStatement("SELECT `id` FROM `schedules` WHERE `secret_code` = ?");
			ps.setString(1, uuid.toString());

			if (ps.execute()) {
				ResultSet rs = ps.getResultSet();

				int id = 0;
				if (rs.first()) {
					id = rs.getInt(1);
				}
				
				s = new Schedule(id, uuid.toString(), name, start_date, end_date, start_time, end_time, meeting_duration, default_open);
			}

			ps.close();
		} catch (Exception e) {
			throw new Exception("Exception while creating schedule: " + e.getMessage());
		}
		
		return s;
	}
	
	/**
	 * extends the start date
	 * @param id schedule id 
	 * @param startDate new start date of schedule
	 * @throws Exception on SQL error
	 */
	public boolean extendStartDate(int id, String startDate) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE `schedules` SET `start_date` = ? WHERE `id` = ?");
			ps.setString(1, startDate);
			ps.setInt(2, id);
			int count = ps.executeUpdate();
			ps.close();
			return count == 1;
		} catch (Exception e) {
			throw new Exception("Failed to extend schedule start date: " + e.getMessage());
		}
	}
	
	/**
	 * extends the end date
	 * @param id schedule id 
	 * @param endDate new end date of schedule
	 * @throws Exception on SQL error
	 */
	public boolean extendEndDate(int id, String endDate) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE `schedules` SET `end_date` = ? WHERE `id` = ?");
			ps.setString(1, endDate);
			ps.setInt(2, id);
			int count = ps.executeUpdate();
			ps.close();
			return count == 1;
		} catch (Exception e) {
			throw new Exception("Failed to extend schedule end date: " + e.getMessage());
		}
	}
	
	/**
	 * deletes a schedule 
	 * @param id schedule id
	 * @return true unless exception
	 * @throws Exception on SQL error
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
	 * deletes schedules with given IDs
	 * @param ids schedule ids
	 * @return number deleted
	 * @throws Exception on SQL error
	 */
	public int adminDeleteSchedules(String ids) throws Exception {
		int num_deleted = 0;
		
		try {
			// this is safe from injection as WE generate schedule_ids from the database
			PreparedStatement ps = conn.prepareStatement("DELETE FROM `schedules` WHERE `id` IN (" + ids + ")");
			num_deleted = ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new Exception("Failed to delete schedules: " + e.getMessage());
		}
		
		return num_deleted;
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
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE `id` = ?;");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				System.out.println(rs.getString(2));
				System.out.println(secretCode);
				if (rs.getString(2).equals(secretCode)) {
					ps.close();
					return 0;
				}
			}
			
			ps.close();
			return 2;
		} catch (Exception e) {
			throw new Exception("Failed to check meeting authorization: " + e.getMessage());
		}
	}

}
