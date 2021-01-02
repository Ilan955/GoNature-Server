package SqlConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class sqlConnector {

	private Connection conn;

	public sqlConnector(Connection conn) {
		this.conn = conn;
	}

	/* update the park manager discount and wait for D_M confirmation */
	public boolean updateManagerDiscount(String startDate, String lastDate, String precentage, String parkName) {
		Statement stm;
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					"UPDATE project.managerdiscounts SET startDate=?, lastDate=?, precentage=?, status=?) WHERE parkName=?");
			LocalDate start = LocalDate.parse(startDate);
			Date startDate = java.sql.Date.valueOf(start);

			LocalDate end = LocalDate.parse(lastDate);
			Date endDate = java.sql.Date.valueOf(end);

			ps.setString(5, parkName);// set park name
			ps.setDate(1, startDate);// set start Date
			ps.setDate(2, endDate);// set end Date
			ps.setFloat(3, Float.valueOf(precentage));// set precentage (casting)
			ps.setString(4, "F"); // set status , will be changed by D_M to T if approved
			ps.executeUpdate();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public float getManagerDiscount(String parkName, String dateOfVisit) {
		Statement stm;
		Date visitDate = new Date(dateOfVisit);
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.managerdiscounts WHERE parkName=? ");
			ps.setString(1, parkName);
			stm = conn.createtatement();
			ResultSet rs = ps.executeQuery();
			rs.next();

			Date startDate = rs.getDate(2);
			Date endDate = rs.getDate(3);
			Float precentage = rs.getFloat(4);
			String status = rs.getString(5);

			if (status.equals("F")) // discount is not confirmed by D_M
				return -1; // invalid

			if (startDate.before(visitDate) && endDate.after(visitDate)) // valid
				return precentage;

			if (startDate.equals(visitDate) || endDate.equals(visitDate)) // valid
				return precentage;

			return -1; // invalid

		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}

	}

	public boolean addToWaitingList(String orderNum) {
		Statement stm;
		int cnt, i;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO `project`.`waitinglist` (`watingOrder`, `timestamp`) VALUES (?, 'NOW()')");
			ps.setInt(1, orderNum);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * return sorted ArrayList<Order> of waiting orders where date =
	 * canceledOrderDateOfVisit
	 */
	public ArrayList<Order> getSortedWatingOrders(String canceledOrder_DateOfVisit) {
		Statement stm;
		try {
			ArrayList<Order> waitingOrders = new ArrayList<String>();
			PreparedStatement ps = conn.prepareStatement(
					"SELECT * FROM project.waitinglist join project.order ON project.waitinglist.watingOrder =  project.order.orderNum"
							+ "where DateOfVisit = ?" + "ORDER BY timestamp;");
			ps.setString(1,canceledOrder_DateOfVisit);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int orderNum = rs.getInt(5);
				LocalTime time = (rs.getTime(7)).toLocalTime(); // rs.getTime(7) = TimeInPark
				LocalDate dateOfVisit = (rs.getDate(8)).toLocalDate(); // rs.getDate(8) = DateOfVisit
				String wantedPark = rs.getString(9);
				int numberOfVisitors = rs.getInt(5);
				float totalPrice = rs.get(10);
				Order o = new Order(orderNum, time, dateOfVisit, wantedPark, numberOfVisitors, totalPrice);
				waitingOrders.add(o);
			}

			return waitingOrders;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean removeFromWaitingList(String orderToRemove) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM project.waitinglist WHERE watingOrder = ?");
			stm = conn.createStatement();
			ps.setString(1, orderToRemove);
			ps.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean IsOrderInWaitingList(int numOfOrder) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * from project.waitinglist WHERE watingOrder = ?");
			ps.setString(1, numOfOrder);
			ResultSet rs = ps.executeQuery();
			return rs.next(); // return: true if numOfOrder exists in project.waitinglist DB
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String check_Confirmation(int orderNum) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT confirmed from project.order WHERE orderNum = ?");
			ps.setString(1, orderNum);
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getString(1);// return confirmed
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * method that will check how many visitors allowed to be in wanted park in
	 * total.
	 */
	public int howManyAllowedInPark(String parkName) {
		String res;
		int counter = 0;
		Statement stm;
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT maxAvailableVisitors FROM project.park WHERE ParkName=?");
			ps.setString(1, parkName);

			ResultSet rs = ps.executeQuery();
			rs.next();

			res = rs.getString(1);
			counter += Integer.parseInt(res);

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return counter;
	}

	public int howManyForCurrentTimeAndDate(String[] result) throws ParseException {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[3]);
		Date wanted = java.sql.Date.valueOf(wanted1);

		int counter = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors,orderNum FROM gonaturedb.order WHERE wantedPark=? AND DateOfVisit=? AND TimeInPark BETWEEN ? AND ? ");
			ps.setString(1, result[2]);
			ps.setString(3, result[0]);
			ps.setString(4, result[1]);
			ps.setString(2, result[3]);
			ResultSet rs = ps.executeQuery();
			stm = conn.createStatement();
			while (rs.next()) {
				counter += rs.getInt(1);
				System.out.println(rs.getString(2));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return counter;
	}
	
	public String getMonthlyIncomes(String date_month, String type) {
		int cnt;
		int income = 0;
		LocalDate date = LocalDate.parse(date_month);
		LocalDate from = date.withDayOfMonth(1); // start of month date
		LocalDate to = from.plusMonths(1);//start of next month date
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) ,sum(TotalPrice) FROM project.order WHERE type = ? && (DateOfVisit >= ? &&  DateOfVisit < ?) && status = 'done'");
			ps.setString(1,type);
			ps.setString(2,from.toString());
			ps.setString(3,to.toString());
			ResultSet rs = ps.executeQuery();
			rs.next();
			cnt_Individuals = rs.getInt(1);//count(*)
			if(cnt > 0) {
				income = rs.getInt(2);
			}
			String res = "" + cnt + " " + income;
			
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}// class
