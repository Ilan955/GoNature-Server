package SqlConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import Entities.Order;

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
			Date startDate1 = java.sql.Date.valueOf(start);

			LocalDate end = LocalDate.parse(lastDate);
			Date endDate = java.sql.Date.valueOf(end);

			ps.setString(5, parkName);// set park name
			ps.setDate(1, startDate1);// set start Date
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

	public String[] CheckForId(String msg) {
		Statement stm;
		String[] s = new String[5];
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.person WHERE ID=?");
			stm = conn.createStatement();
			ps.setString(1, msg);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				s[0] = rs.getString(1);
				s[1] = rs.getString(2);
				s[2] = rs.getString(3);
				s[3] = rs.getString(4);
				s[4] = rs.getString(5);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return s;
	}
	/*
	 * 
	 * This method will get a string with the email and the id to be changed will
	 * return true or false
	 * 
	 * 
	 */

	public boolean updateEmail(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE project.person SET email=? WHERE ID=?");
			ps.setString(1, msg[1]);
			ps.setString(2, msg[0]);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

///////////////// Start Sign Up New Member /////////////////////////
	/**
	 * Description of isMemberExists(String[] msg) - this function check if a member
	 * is already in the DB
	 *
	 * @param msg - String containing visitors id.
	 * 
	 * @return boolean - true if member exists, false if not.
	 */
	public boolean isMemberExists(String[] msg) {
		Statement stm;
		try {

			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.person WHERE ID=?");
			stm = conn.createStatement();
			ps.setString(1, msg[0]);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;

		}
		return true;
	}

	/**
	 * Description of addMember(String[] msg) - this function ads a member to the DB
	 *
	 * @param msg[0]    - String containing visitors id.
	 * @param msg[1]    - String containing visitors first name.
	 * @param msg[2]    - String containing visitors last name.
	 * @param msg[3]    - String containing visitors phone number
	 * @param msg[4]    - String containing visitors email.
	 * @param msg[5]    - String containing visitors payment method
	 * @param msg[6]    - String containing visitors type
	 * @param msg[7]    - String containing visitors max visitors (or family
	 *                  members).
	 * @param memberCNT - next available member id.
	 * 
	 * @return String - containing member id or false if process failed.
	 */
	public String addMember(String[] msg) {
		Statement stm;
		String memberCNT = String.valueOf(nextMember());
		try {
			PreparedStatement ps = conn.prepareStatement(

					"INSERT project.person SET ID=? ,firstName=?, lastName=?, phoneNumber=? ,Email=? ,creditCardNum=? ,Type=? ,maxFamilyMembers=?,memberId=?");

			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.setString(3, msg[2]);
			ps.setString(4, msg[3]);
			ps.setString(5, msg[4]);
			ps.setString(6, msg[5]);
			ps.setString(7, msg[6]);
			ps.setString(8, msg[7]);
			ps.setString(9, memberCNT);
			ps.executeUpdate();
			return memberCNT.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "false";
		}

	}

	/**
	 * Description of nextMember() - this function finds next available membership
	 * id.
	 * 
	 * @return int - next next available membership id. .
	 */
	public int nextMember() {
		Statement stm;
		int i = 0;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS rowcount FROM project.person");
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();

			rs.next();
			i = rs.getInt("rowcount");
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i += 1000000002;

	}

///////////////////// End Sign Up Member /////////////////////////////
	public String getManagerDiscount(String parkName, String dateOfVisit) {
		Statement stm;
		LocalDate visitDate = LocalDate.parse(dateOfVisit);
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.managerdiscounts WHERE parkName=? ");

			ps.setString(1, parkName);
			stm = conn.createStatement();

			// msg = [parkName, dateOfVisit]

			ResultSet rs = ps.executeQuery();
			rs.next();
			String discount = "";
			discount += rs.getString(1);// get parkName
			discount += " ";// add space
			discount += rs.getString(2);// get startDate
			discount += " ";// add space
			discount += rs.getDate(3);// get lastDate
			discount += " ";// add space
			discount += rs.getFloat(4);// get percentage
			discount += " ";// add space
			discount += rs.getString(5);// get percentage

			return discount;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean addToWaitingList(String orderNum) {
		Statement stm;
		int cnt, i;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO `project`.`waitinglist` (`watingOrder`, `timestamp`) VALUES (?, 'NOW()')");
			ps.setString(1, orderNum);
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
	public ArrayList<Order> getSortedWatingOrders(int canceledOrder_DateOfVisit) {
		Statement stm;
		try {
			ArrayList<Order> waitingOrders = new ArrayList<>();
			PreparedStatement ps = conn.prepareStatement(
					"SELECT * FROM project.waitinglist join project.order ON project.waitinglist.watingOrder =  project.order.orderNum"
							+ "where DateOfVisit = ?" + "ORDER BY timestamp;");
			ps.setInt(1, canceledOrder_DateOfVisit);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int orderNum = rs.getInt(5);
				LocalTime time = (rs.getTime(7)).toLocalTime(); // rs.getTime(7) = TimeInPark
				LocalDate dateOfVisit = (rs.getDate(8)).toLocalDate(); // rs.getDate(8) = DateOfVisit
				String wantedPark = rs.getString(9);
				int numberOfVisitors = rs.getInt(5);
				float totalPrice = rs.getFloat(10);
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
			ps.setInt(1, numOfOrder);
			ResultSet rs = ps.executeQuery();
			return rs.next(); // return: true if numOfOrder exists in project.waitinglist DB
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * will check the amount of orders that is in the system
	 * 
	 */
	public int nextOrder() {
		Statement stm;
		int i = 0;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT orderNum FROM project.order ORDER BY orderNum DESC ");
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			if (rs.isLast())
				System.out.println("GREAT!");
			while (rs.next()) {
				String tmp = rs.getString(1);
				i = Integer.parseInt(tmp);
				break;
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}

		return ++i;

	}

	public String check_Confirmation(int orderNum) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT confirmed from project.order WHERE orderNum = ?");
			ps.setInt(1, orderNum);
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getString(1);// return confirmed
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 
	 * check how many visitors in total there is for the desired time and date and
	 * return this value
	 * 
	 */
	public int howManyForCurrentTimeAndDat1e(String[] msg) {
		String res;
		int counter = 0;
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors FROM project.order WHERE TimeInPark=? AND DateOfVisit=? AND wantedPark=?");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.setString(3, msg[2]);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				res = rs.getString(1);
				counter += Integer.parseInt(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return counter;
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

					.prepareStatement("SELECT maxAvailableVisitors FROM project.park WHERE parkName=?");

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

		int counter = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(

					"SELECT numOfVisitors,orderNum FROM project.order WHERE wantedPark=? AND DateOfVisit=? AND TimeInPark BETWEEN ? AND ? AND status= 'confirmed' OR status='entered' ");

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
		LocalDate to = from.plusMonths(1);// start of next month date
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT count(*) ,sum(TotalPrice) FROM project.order WHERE type = ? && (DateOfVisit >= ? &&  DateOfVisit < ?) && status = 'done'");
			ps.setString(1, type);
			ps.setString(2, from.toString());
			ps.setString(3, to.toString());
			ResultSet rs = ps.executeQuery();
			rs.next();
			cnt = rs.getInt(1);// count(*)
			if (cnt > 0) {
				income = rs.getInt(2);
			}
			String res = "" + cnt + " " + income;

			return res;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;

		}
	}

	/*
	 * Method to insert new order to table res[0]=time res[1]=date, res[2]= parkname
	 * , res[3]=price, res[4]=id, res[5]=type, res[6]=numOfVisit
	 * 
	 */

	public void addOrder(int orderNum, String[] result) {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[1]);
		Date wanted = java.sql.Date.valueOf(wanted1);

		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO project.order (orderNum, TimeInPark, DateOfVisit, wantedPark, TotalPrice, ID,type,numOfVisitors) VALUES (?,?,?,?,?,?,?,?)");
			ps.setInt(1, orderNum);
			ps.setString(2, result[0]);
			ps.setDate(3, wanted);
			ps.setString(4, result[2]);
			ps.setFloat(5, Float.parseFloat(result[3]));
			ps.setString(6, result[4]);
			ps.setString(7, result[5]);
			ps.setString(8, result[6]);

			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();

		}

	}

	public boolean canGetEmployee(String userName) { // method checks if employee exists on our DB (By UserName)
		Statement stm;
		try {
			String[] s = userName.split(" "); // new row

			PreparedStatement ps = conn.prepareStatement("SELECT *  FROM project.Employees WHERE userName = ?");

			stm = conn.createStatement();
			ps.setString(1, s[0]);// new row
			ps.executeQuery();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String[] getEmployeeUN(String empID) // if employee exists, DB returns this employee as a tuple (String[])
	{
		Statement stm;
		String check[] = empID.split(" ");
		String[] s = new String[14];
		int isLogged = 0;
		try {

			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.Employees WHERE userName = ?");

			stm = conn.createStatement();
			ps.setString(1, check[0]);
			ResultSet rs = ps.executeQuery();
			s[0] = "EmployeeController"; // For GoClient.HandleMessageFromServer
			s[1] = "IdentifyEmployee"; // in this case we assume that employee is in our DB --> if it isn't it will
										// change
			while (rs.next()) {
				for (int i = 2; i < 12; i++)
					s[i] = rs.getString(i - 1);
				isLogged = rs.getInt(11);
			}
			if (s[10] == null)
				s[1] = "IdentifyNotExistingEmployee"; // Means that we didn't find an employee with this userName
			else if (!(s[11].equals(check[1]))) {// It means that passwords do not match and employee can not enter to
													// GoNature system
				s[1] = "IdentifyPasswordDoesNotMatch";
				s[11] = null;
			} else if (isLogged == 1) {
				s[1] = "employeeAlreadyLoggedIn";
			} else {
				PreparedStatement ps1 = conn.prepareStatement("UPDATE employees SET isLoggedIn = 1 Where userName = ?");
				Statement stm1 = conn.createStatement();
				ps1.setString(1, check[0]);
				ps1.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return s;
	}

	public void changeStatusOfOrder(String[] result, String status, String comment) {
		Statement stm;

		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.order SET status=? ,comment=? WHERE TimeInPark=? AND DateOfVisit=? AND wantedPark=? AND ID=?");

			ps.setString(1, status);
			ps.setString(2, comment);
			ps.setString(3, result[0]);
			ps.setString(4, result[1]);
			ps.setString(5, result[2]);
			ps.setString(6, result[3]);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	public String getOrders(String iD) {
		Statement stm;
		StringBuffer s = new StringBuffer();
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT orderNum,DateOfVisit,wantedPark,TimeInPark,numOfVisitors,TotalPrice,status,comment FROM project.order WHERE ID=? AND status='waitForConfirm' OR status='confirmed'");
			ps.setString(1, iD);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				for (int i = 1; i <= 8; i++) {
					s.append(rs.getString(i));
					s.append(" ");
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		s.append("Done");

		return s.toString();
	}

	public int howManyUnexpectedVisitorsInPark(String parkName) {
		String res;
		int unexpectedVisitors = 0;
		Statement stm;
		try {
			PreparedStatement ps = conn

					.prepareStatement("SELECT AmountOfUnExpectedVisitors FROM project.park WHERE parkName=?");

			ps.setString(1, parkName);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				res = rs.getString(1);
				unexpectedVisitors = Integer.parseInt(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return unexpectedVisitors;
	}

	public int howManyCurrentvisitorsForOrdersInPark(String parkName) {
		String res;
		int currentvisitors = 0;
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT currentVisitors FROM project.park WHERE parkName=?");
			ps.setString(1, parkName);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				res = rs.getString(1);
				currentvisitors = Integer.parseInt(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return currentvisitors;
	}

	public int howManyMaxvisitorsAllowedInPark(String parkName) {
		String res;
		int Maxvisitors = 0;
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT maxVisitors FROM project.park WHERE parkName=?");
			ps.setString(1, parkName);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				res = rs.getString(1);
				Maxvisitors = Integer.parseInt(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return Maxvisitors;
	}

	public String[] getTravellerFromDB(String travellerID) // returns a String[] with this traveller info
	{
		int temp = 0;
		Statement stm;
		String[] s = new String[12]; // should be as number fields number in traveller class
		// I am working currently on a DB with 5 fields for traveller. It works
		try {
			PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM project.loggedintravellers WHERE id = ?");
			ps1.setString(1, travellerID);
			ResultSet rs1 = ps1.executeQuery();
			if (rs1.next()) {
				s[0] = "UserController";
				s[1] = "AlreadyLoggedIn";
				return s;
			} else {
				PreparedStatement ps2 = conn.prepareStatement("insert into loggedintravellers values (?,1)");
				ps2.setString(1, travellerID);
				ps2.execute();
			}
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.person WHERE id = ? OR memberID = ?");
			stm = conn.createStatement();
			ps.setString(1, travellerID);
			ps.setString(2, travellerID);
			ResultSet rs = ps.executeQuery();
			s[0] = "UserController"; // for use of GoClient.HandleMessageFromServer
			s[1] = "IdentifyTraveller";
			while (rs.next()) {
				s[2] = rs.getString(1);
				s[3] = rs.getString(2);
				s[4] = rs.getString(3);
				s[5] = rs.getString(4);
				s[6] = rs.getString(5);
				s[7] = rs.getString(6);
				s[8] = rs.getString(7);
				temp = rs.getInt(8);
				s[10] = rs.getString(9);
			}
			s[9] = ("" + temp);
			if (s[5] == null) {
				s[1] = "IdentifyNotExistingTraveller";
				s[5] = "" + travellerID;
			}
			// System.out.print(rs.getString(i).toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return s;
	}

	public boolean canGetTraveller(String travellerID) // Checks if traveller exists in our DB (By ID)
	{
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT *  FROM project.person WHERE id = ? OR memberID = ?");
			// stm = conn.createStatement();
			ps.setString(1, travellerID);
			ps.setString(2, travellerID);
			ps.executeQuery();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public float howmanyTimeEveryVisitorInPark(String parkName) {
		String res;
		float time = 0;
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT maxDuration FROM project.park WHERE parkName=?");
			ps.setString(1, parkName);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				res = rs.getString(1);
				time = Float.parseFloat(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return time;
	}

	public void updateUnexpectedVisitors(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn

					.prepareStatement("UPDATE project.park SET AmountOfUnExpectedVisitors=? WHERE parkName=?");

			ps.setInt(1, Integer.parseInt(msg[1]));
			ps.setString(2, msg[0]);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateCurrentVisitors(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE project.park SET currentVisitors=? WHERE ParkName=?");
			ps.setInt(1, Integer.parseInt(msg[1]));
			ps.setString(2, msg[0]);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void enterExitTimeForTravellerWithOrder(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.order SET ExitTime=TIME_FORMAT(curtime(), '%k:%i'),status=\"done\" WHERE wantedPark=? AND DateOfVisit=curdate() AND ID=? AND status=\"in park\" ");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	public void enterExitTimeForcasualTraveller(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.travellerinpark SET exitTime=TIME_FORMAT(curtime(), '%k:%i'), inPark=0 WHERE wantedPark=? AND Date=curdate() AND ID=? ");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isTravellerExistsInPark(String[] msg) {
		Statement stm;
		try {

			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM project.travellerinpark WHERE ID=? AND inPark=1");
			stm = conn.createStatement();
			ps.setString(1, msg[0]);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;

		}
		return false;
	}

	public String getTravellerInParkDetails(String id) {
		Statement stm;
		int num;
		StringBuffer s = new StringBuffer();
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT numOfVisitors, wantedPark FROM project.travellerinpark WHERE ID=?");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				num = Integer.parseInt(rs.getString(1));
				s.append(num);
				s.append(" ");
				s.append(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}

		return s.toString();
	}

	public boolean isOrderExistsInPark(String[] msg) {
		Statement stm;
		try {

			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM project.order WHERE ID=? AND status=\"in park\" ");
			stm = conn.createStatement();
			ps.setString(1, msg[0]);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;

		}
		return false;
	}

	public String getOrderDetailsForExitPark(String id) {
		Statement stm;
		int num;
		StringBuffer s = new StringBuffer();
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors, wantedPark FROM project.order WHERE ID=? AND status=\"in park\" ");
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				num = Integer.parseInt(rs.getString(1));
				s.append(num);
				s.append(" ");
				s.append(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}

		return s.toString();
	}

	public void changeStatusForTravellerInPark(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.travellerinpark SET inpark=0 WHERE ID=? AND wantedpark=? AND Date=curdate()");
			ps.setString(1, msg[0]); // ID
			ps.setString(2, msg[1]); // park
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertRequest(String[] result) {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[1]);
		Date wanted = java.sql.Date.valueOf(wanted1);

		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO project.requests(IdOfAsks,dateOfRequest,timeOfRequest,wantedpark,numberOfVisitors,type,status) VALUES (?,?,?,?,?,?,-1)");
			ps.setString(1, result[0]); // ID
			ps.setDate(2, wanted); // Date
			ps.setString(3, result[2]); // park
			ps.setString(4, result[3]); // numOfVisitors
			ps.setString(5, result[4]); // time
			ps.setString(6, result[5]); // type
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public int IsApproveEnterParkForTraveller(String[] result) {
		String res;
		int status = -1;
		Statement stm;
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT status FROM project.requests WHERE idOfAsks=? AND type=? ");
			ps.setString(1, result[0]); // ID
			ps.setString(2, result[1]); // type
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				res = rs.getString(1);
				status = Integer.parseInt(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return status;
	}

	public String getRequestTableOfEnterPark(String park) {
		Statement stm;
		StringBuffer s = new StringBuffer();
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT IdOfAsks,timeOfRequest,numberOfVisitors FROM project.requests WHERE wantedpark=? and dateOfRequest=curdate() and type=\"EnterPark\" and ( status=\"-1\" OR status=\"0\" ) ORDER BY timeOfRequest ASC");
			ps.setString(1, park);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				for (int i = 1; i <= 3; i++) {
					s.append(rs.getString(i));
					s.append(" ");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		s.append("Done");

		return s.toString();
	}

	public void changeRequestStatusForCasualTraveller(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.requests SET status=? WHERE IdOfAsks=? AND wantedpark=? and dateOfRequest=curdate() and type=\"EnterPark\"");
			ps.setString(1, msg[0]); // status
			ps.setString(2, msg[1]); // ID
			ps.setString(3, msg[2]); // park
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void insertTravellerInPark(String[] result) {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[2]);
		Date wanted = java.sql.Date.valueOf(wanted1);

		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO project.travellerInPark(ID,numOfVisitors,Date,enterTime,exitTime,price,wantedPark,inPark) VALUES (?,?,?,?,null,?,?,1)");
			ps.setString(1, result[0]); // ID
			ps.setString(2, result[1]); // numberOfVisitors
			ps.setDate(3, wanted); // Date
			ps.setString(4, result[3]); // time
			ps.setFloat(5, Float.parseFloat(result[4]));
			ps.setString(6, result[5]);
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();

		}

	}

	public void insertfullcapacityPark(String[] result) {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[1]);
		Date wanted = java.sql.Date.valueOf(wanted1);

		try {
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO project.fullcapacity(park,date,full) VALUES (?,?,?)");
			ps.setString(1, result[0]); // park
			ps.setDate(2, wanted); // Date
			ps.setString(3, result[2]);
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();

		}

	}

	public boolean isDateInfullcapacityExists(String[] msg) {
		Statement stm;
		try {

			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM project.fullcapacity WHERE park=? AND Date=curdate()");
			stm = conn.createStatement();
			ps.setString(1, msg[0]);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;

		}
		return false;
	}

	public void changeStatusForCapacityParkToFull(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn
					.prepareStatement("UPDATE project.fullcapacity SET full=1 WHERE park=? and date=curdate()");
			ps.setString(1, msg[0]); // park
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String getUnFullCapacityTableInDates(String[] msg) {
		Statement stm;
		StringBuffer s = new StringBuffer();
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT date FROM project.fullcapacity WHERE month(date)=? and year(date)=? and park=? and full=0");
			ps.setString(1, msg[0]);// month
			ps.setString(2, msg[1]);// year
			ps.setString(3, msg[2]); // park
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				s.append(rs.getString(1));
				s.append(" ");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		s.append("Done");

		return s.toString();
	}

	////// Reports start/////
	/**
	 * Description of getVisitorsDataReport(String[] monthYearPark) this function
	 * collected data for report.
	 * 
	 * @param monthYearPark is a string containing wanted park, wanted month and
	 *                      wanted year.
	 * 
	 * @return string - the string contains number of visitors for every type of
	 *         visitors entered the park.
	 */
	public String getVisitorsDataReport(String[] monthYearPark) {
		String month = monthYearPark[0];
		String year = monthYearPark[1];
		String park = monthYearPark[2];
		StringBuffer sb = new StringBuffer();
		Statement stm;
		int sumSolo = 0, sumMembers = 0, sumGroups = 0;
		try {// try to get travellers with out order
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors FROM project.travellerinpark WHERE wantedPark=? AND MONTH(Date)=? AND YEAR(Date)=?");
			ps.setString(1, park);
			ps.setString(2, month);
			ps.setString(3, year);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sumSolo += rs.getInt("numOfVisitors");
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("Fail gett sumSolo");
			e.printStackTrace();
		}
		try {// try to get members and family members with order
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors FROM project.order WHERE wantedPark=? AND MONTH(DateOfVisit)=? AND YEAR(DateOfVisit)=? AND status='done' AND (type='Member' OR type='Family') ");
			ps.setString(1, park);
			ps.setString(2, month);
			ps.setString(3, year);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sumMembers += rs.getInt("numOfVisitors");
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("Fail get sumMember");
			e.printStackTrace();
		}
		try {// try to get Groups members with order
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors FROM project.order WHERE wantedPark=? AND MONTH(DateOfVisit)=? AND YEAR(DateOfVisit)=? AND status='done' AND type='Group' ");
			ps.setString(1, park);
			ps.setString(2, month);
			ps.setString(3, year);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sumGroups += rs.getInt("numOfVisitors");
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("Fail geting sumGroup");
			e.printStackTrace();
		}
		sb.append(sumSolo);
		sb.append(" ");
		sb.append(sumMembers);
		sb.append(" ");
		sb.append(sumGroups);
		return sb.toString();
	}

//////////////////Entrance and Stay Report ////////////////////
	/**
	 * Description of getEntranceAndStay(String[] monthYearPark) this function
	 * collected data for report.
	 * 
	 * @param monthYearPark is a string containing wanted park, wanted month and
	 *                      wanted year.
	 * 
	 * @return string - the string contains number of visitors, type, enter time,
	 *         exit time and visit date for all the visitors..
	 */
	public String getEntranceAndStay(String[] monthYearPark) {
		String month = monthYearPark[0];
		String year = monthYearPark[1];
		String park = monthYearPark[2];
		StringBuffer sb = new StringBuffer();
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors,Date,enterTime,exitTime FROM project.travellerinpark WHERE wantedPark=? AND MONTH(Date)=? AND YEAR(Date)=?");
			ps.setString(1, park);
			ps.setString(2, month);
			ps.setString(3, year);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sb.append(rs.getInt("numOfVisitors") + " " + "traveller" + " " + rs.getString("enterTime") + " "
						+ rs.getString("exitTime") + " " + rs.getString("Date") + " ");
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("Fail to get visitors data");
			e.printStackTrace();
		}
		try {// try to get Groups members with order
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors, type, enterTime, exitTime,DateOfVisit FROM project.order WHERE wantedPark=? AND MONTH(DateOfVisit)=? AND YEAR(DateOfVisit)=? AND status='Confirmed'");
			ps.setString(1, park);
			ps.setString(2, month);
			ps.setString(3, year);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sb.append(rs.getInt("numOfVisitors") + " " + rs.getString("type") + " " + rs.getString("enterTime")
						+ " " + rs.getString("exitTime") + " " + rs.getString("DateOfVisit") + " ");
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("Fail to get members and group guides data");
			e.printStackTrace();
		}

		sb.append("Done");
		return sb.toString();
	}

////// Reports end/////

	public boolean updateParkChangeRequestStatus(String string) {

		Statement stm;
		try {
			PreparedStatement ps = conn
					.prepareStatement("UPDATE project.newparksettingsrequest SET status=? WHERE requestID=?");
			ps.setInt(1, 1);
			ps.setInt(2, Integer.parseInt(string));
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateParkChangesInParkTable(String parkName, String maxVisitors, String gap, String maxDur) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.park SET maxVisitors=? , maxAvailableVisitors = ?, maxDuration = ?  WHERE parkName= ? ");
			ps.setInt(1, Integer.parseInt(maxVisitors));
			int maxAvailable = Integer.parseInt(maxVisitors) - Integer.parseInt(gap);
			ps.setInt(2, maxAvailable);
			ps.setFloat(3, Float.parseFloat(maxDur));
			ps.setString(4, parkName);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		// need to return a value to parkController (set new changes)
	}

	public int checkHowManyCancelled(String[] result, String status) {
		Statement stm;
		int counter = 0;

		try {
			PreparedStatement ps = conn.prepareStatement(

					"SELECT * from project.order WHERE status = ? AND DateOfVisit BETWEEN ? AND ?");

			ps.setString(1, status);
			ps.setString(2, result[0]);
			ps.setString(3, result[1]);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				counter++;
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return counter;
	}

	// this method will go into the db and check what the discount and the price
	// need to be given for the current visitor
	public String getTotalPayload(String typeOfService) {
		StringBuffer sb = new StringBuffer();

		Statement stm;
		int counter = 0;

		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT departmentPrice,valueOfDiscount,Members from project.discounts WHERE typeOfService = ?");

			ps.setString(1, typeOfService);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sb.append(rs.getString(1));
				sb.append(" ");
				sb.append(rs.getString(2));
				sb.append(" ");
				sb.append(rs.getString(3));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return sb.toString();

	}

	/*
	 * method that will check if some traveller have order for tomorrow, if so will
	 * check if its not been informed for him yet. if informed, will not do
	 * anything. if found some of them, will return to the client a massege that he
	 * have some orders need to be approved. also this method will change akk the
	 * Informed values to 't'
	 */
	public String checkIfHavingTomorrow(String[] result) {
		String s = "";
		System.out.println("HOWMANY");
		int flag = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT orderNum from project.order WHERE DateOfVisit = ? AND ID=? AND Informed = 'f'");

			ps.setString(1, result[0]);
			ps.setString(2, result[1]);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				s = rs.getString(1);
				flag++;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (flag > 0) {
			try {// try to get travellers with out order
				PreparedStatement ps = conn
						.prepareStatement("UPDATE project.order SET Informed='t' WHERE DateOfVisit=? AND ID=?");
				ps.setString(1, result[0]);
				ps.setString(2, result[1]);
				ps.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Fail gett sumSolo");
				e.printStackTrace();
			}
		}
		return s;
	}

	public String checkIfConfirmAlert(String dat, String timeOfVisit) {
		StringBuffer sb = new StringBuffer();
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT orderNum from project.order WHERE DateOfVisit =? AND TimeInPark =? AND status='waitForConfrim'");
			ps.setString(1, dat);
			ps.setString(2, timeOfVisit);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sb.append(rs.getString(1));
				sb.append(" ");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void cancelOrderForWaiting(String orderNum) {
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.order SET status='cancelled',comment='Automatic' WHERE orderNum=?");
			ps.setString(1, orderNum);
			ps.executeUpdate();
			// waitingList here

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void conAlert(String orderNum) {
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.order SET status='confirmed',comment='OrderConfirmed' WHERE orderNum=?");
			ps.setString(1, orderNum);
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public boolean logOutEmployee(String userName) {
		try {
			PreparedStatement ps1 = conn.prepareStatement("UPDATE employees SET isLoggedIn = 0 Where userName = ?");
			Statement stm1 = conn.createStatement();
			ps1.setString(1, userName);
			ps1.executeUpdate();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	public void deleteFromDbWhenTravellerLogOut(String travellerID, String memberID) {
		try {
			PreparedStatement ps1 = conn.prepareStatement("delete from loggedintravellers where id = ?");
			Statement stm1 = conn.createStatement();
			ps1.setString(1, travellerID);
			ps1.execute();
			PreparedStatement ps2 = conn.prepareStatement("delete from loggedintravellers where id = ?");
			Statement stm2 = conn.createStatement();
			ps2.setString(1, memberID);
			ps2.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getParkSettingsRequests() {
		String s;
		StringBuffer sb = new StringBuffer();
		sb.append("EmployeeController ");
		sb.append("displayParkSettingsRequestsToDepartmentManager ");
		int i = 2;
		int idReq = 0;
		String date = "";
		String time = "";
		String wantedPark = "";
		int maxVisit = 0, gapBetween = 0, len = 0;
		float maxDur = 0;

		Statement stm;
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM project.newparksettingsrequest WHERE status = ?");
			stm = conn.createStatement();
			ps.setInt(1, 0);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				idReq = rs.getInt(1);
				date += rs.getString(2);
				time += rs.getString(3);
				wantedPark = rs.getString(4);
				maxVisit = rs.getInt(5);
				gapBetween = rs.getInt(6);
				maxDur = rs.getFloat(7);
				sb.append("" + idReq);
				sb.append(" ");
				sb.append(wantedPark + " ");
				sb.append(date + " ");
				sb.append(time + " ");
				sb.append("" + maxVisit + " ");
				sb.append("" + gapBetween + " ");
				sb.append("" + maxDur + " ");
				idReq = 0;
				date = "";
				time = "";
				wantedPark = "";
				maxVisit = 0;
				gapBetween = 0;
				maxDur = 0;
			}
		} catch (SQLException e) {
			return null;
		}
		sb.append("Done");
		s = sb.toString();
		return s;
	}

	public String sendParkSettingsRequestToDepManager(String[] s) {
		Statement stm;
		int id = sendNewRequestID();
		try {
			PreparedStatement ps = conn
					.prepareStatement("insert into project.newparksettingsrequest values (?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setInt(1, id);
			LocalDate start = LocalDate.parse(s[1]);
			Date startDate = java.sql.Date.valueOf(start);
			LocalTime time = LocalTime.parse(s[2]);
			Time hour = java.sql.Time.valueOf(time);
			ps.setDate(2, startDate);
			ps.setTime(3, hour);
			ps.setString(4, s[3]);
			ps.setInt(5, Integer.parseInt(s[4]));
			ps.setInt(6, Integer.parseInt(s[5]));
			ps.setFloat(7, Float.parseFloat(s[6]));
			ps.setInt(8, Integer.parseInt(s[7]));
			ps.executeUpdate();
		} catch (SQLException e) {
			return "false";
		}
		return "true";
	}

	public int sendNewRequestID() { // xxxxxxxxxxx
		Statement stm;
		int i = 0;
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT COUNT(*) AS rowcount FROM project.newparksettingsrequest");
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			rs.next();
			i = rs.getInt("rowcount");
			rs.close();
		} catch (SQLException e) {
			return -1;
		}
		return ++i;
	}

}
