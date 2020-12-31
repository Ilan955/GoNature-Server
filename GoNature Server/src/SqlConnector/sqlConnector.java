package SqlConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

import java.text.ParseException;
import java.time.LocalDate;

public class sqlConnector {

	private Connection conn;

	public sqlConnector(Connection conn) {
		this.conn = conn;
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

	public boolean addManagerDiscount(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"insert into project.managerdiscounts (parkName, startDate, lastDate, precentage, status) values (?, ?, ?, ?, ?)");
			// msg = [startDate , lastDate , precentage , parkName]
			LocalDate start = LocalDate.parse(msg[0]);
			Date startDate = java.sql.Date.valueOf(start);

			LocalDate end = LocalDate.parse(msg[1]);
			Date endDate = java.sql.Date.valueOf(end);

			ps.setString(1, msg[3]);// set park name
			ps.setDate(2, startDate);// set start Date
			ps.setDate(3, endDate);// set end Date
			ps.setFloat(4, Float.valueOf(msg[2]));// set precentage (casting)
			ps.setString(5, "F"); // set status , will be changed by D_M to T if approved

			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String addMember(String[] msg) {
		Statement stm;
		String memberCNT = String.valueOf(nextMember());
		try {
			PreparedStatement ps = conn.prepareStatement(

					"INSERT project.person SET ID=? ,firstName=?, lastName=?, phoneNumber=? ,Email=? ,creditCardNum=? ,maxFamilyMembers=? ,memberId=?");

			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.setString(3, msg[2]);
			ps.setString(4, msg[3]);
			ps.setString(5, msg[4]);
			ps.setString(6, msg[5]);
			ps.setString(7, msg[6]);
			ps.setString(8, memberCNT);
			ps.executeUpdate();
			System.out.println(memberCNT);
			return memberCNT.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "false";
		}

	}

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

	public String getManagerDiscount(String[] msg) {
		/* works only for one row in DB */

		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.managerdiscounts WHERE parkName=? ");
			// msg = [parkName, dateOfVisit]
			ps.setString(1, msg[0]);// parkName
			stm = conn.createStatement();
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

	public String findOrderToWaitFor(String[] result) {
		// result = [parkName[0],dateOfVisit[1],hour[2],numOfVistors[3],orderNum[4]]
		// SELECT orderNUm FROM project.order where wantedPark = 'testPark' and
		// numOfVisitors >=1 and DateOfVisit = '2020-01-01' and (TimeInPark > '10:00:00'
		// and TimeInPark <= '13:00:00') and status = 'confirmed' ;
		Statement stm;
		try {
			/* get park maxDuration time */
			PreparedStatement ps = conn.prepareStatement("SELECT maxDuration FROM project.park where parkName = ?;");
			ps.setString(1, result[0]);// parkName
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			rs.next();
			Time maxDuration = rs.getTime(1);

			Time hour = java.sql.Time.valueOf(result[2]);// hour timeWantedVisit
			Time before_visit = new Time(hour.getTime() - maxDuration.getTime()); // before_visit = hour - maxDuration

			/* get orderNum to wait for */
			ps = conn.prepareStatement(
					"SELECT orderNum FROM project.order where wantedPark = ? and numOfVisitors >=? and DateOfVisit = ? and (TimeInPark > ? and TimeInPark <= ?) and status = ?");
			stm = conn.createStatement();

			ps.setString(1, result[0]);// parkName
			ps.setString(2, result[3]);// numOfVisitors
			ps.setString(3, result[1]);// dateOfVisit
			ps.setString(4, before_visit.toString());// hour - maxDuration
			ps.setString(5, result[2]);// hour
			ps.setString(6, "confirmed");

			String orderNUms = "";

			while (rs.next()) {
				orderNUms += rs.getInt(1);
				orderNUms += " ";
			}

			return orderNUms;// splited by space

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean addToWaitingList(String result) {
		String msg[] = result.split(" ");
		// msg = [orderToAdd[0] , ordersToWaitFor...]
		Statement stm;
		int cnt, i;
		int numOf_ordersToWaitFor = msg.length - 1; // minus 1 because msg[0] = orderToAdd
		try {
			for (i = 1; i < numOf_ordersToWaitFor; i++) {
				cnt = 0;
				PreparedStatement ps = conn
						.prepareStatement("SELECT * FROM project.waitinglist where orderToWaitFor = ?");
				stm = conn.createStatement();
				ps.setString(1, msg[i]);// orderToWaitFor
				ResultSet rs = ps.executeQuery();
				while (rs.next())
					cnt++; // count how many orders already wait for this orderToWaitFor

				/* enter orderToAdd to waitinglist */
				ps = conn.prepareStatement(
						"insert into project.waitinglist (orderToWaitFor, waitingOrder, placeInLine) values (?, ?, ?)");
				ps.setString(1, msg[i]);// orderToWaitFor
				ps.setString(2, msg[0]);// orderToAdd
				ps.setString(3, String.valueOf(cnt));// index
				ps.executeUpdate();
			} // for

			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public String findOrderFirstInLine(String canceldOrderNum) {
		Statement stm;
		int orderNum_firstInLine = -1;
		int minIndex = Integer.MAX_VALUE;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT watingOrder,placeInLine FROM project.waitinglist WHERE orderToWaitFor=? ");
			ps.setString(1, canceldOrderNum);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getInt(2) < minIndex)// placeInLine
					orderNum_firstInLine = rs.getInt(1);// watingOrder
			}

			if (orderNum_firstInLine == -1) // there is no watingOrder for canceldOrderNum
				return "null";

			/* get orderNum_firstInLine details */
			ps = conn.prepareStatement("SELECT * FROM project.order WHERE orderNum=?");
			ps.setString(1, String.valueOf(orderNum_firstInLine));
			stm = conn.createStatement();
			rs = ps.executeQuery();
			String orderToSend = "";
			rs.next();
			orderToSend += rs.getString(1);// ID
			orderToSend += " ";
			orderToSend += rs.getString(2);// type
			orderToSend += " ";
			orderToSend += rs.getString(3);// numOfVisitors
			orderToSend += " ";
			orderToSend += rs.getInt(4);// orderNum
			orderToSend += " ";
			orderToSend += rs.getTime(5);// timeInPark
			orderToSend += " ";
			orderToSend += rs.getDate(6);// DateOfVisit
			orderToSend += " ";
			orderToSend += rs.getString(4);// wantedPark
			orderToSend += " ";
			orderToSend += rs.getFloat(4);// totalPrice
			orderToSend += " ";
			orderToSend += rs.getString(4);// status

			return orderToSend;

		} catch (SQLException e) {
			e.printStackTrace();
			return "null";
		}

	}

	public boolean removeFromWaitingList(String orderToRemove) {
		/* find all rows of orderToRemove */
		Statement stm;
		String orderToWaitFor = "";
		String orderToWaitFor_arr[];
		int i;
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT orderToWaitFor FROM project.waitinglist WHERE watingOrder=?");
			ps.setString(1, orderToRemove);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				orderToWaitFor += rs.getInt(1);
				orderToWaitFor += " ";
			}

			orderToWaitFor_arr = orderToWaitFor.split(" ");

			/*
			 * To Do: for each orderToWaitFor_arr[i] : remove(orderToRemove) , decrease
			 * placeInLine of all watingOrders for orderToWaitFor_arr[i]
			 */

			for (i = 0; i < orderToWaitFor_arr.length; i++) {
				/* remove */
				ps = conn.prepareStatement("DELETE FROM project.waitinglist WHERE orderToWaitFor=? and watingOrder=?");
				ps.setString(1, orderToWaitFor_arr[i]);
				ps.setString(2, orderToRemove);
				ps.executeUpdate();

				/* decrease placeInLine of all watingOrders */
				ps = conn.prepareStatement("SELECT * FROM project.waitinglist where orderToWaitFor = ?"); // get all
																											// remain
																											// watingOrders
																											// to
																											// orderToWaitFor
				stm = conn.createStatement();
				ps.setString(1, orderToWaitFor_arr[i]);// orderToWaitFor
				rs = ps.executeQuery();
				while (rs.next()) {
					/* get details */
					int waitingOrder = rs.getInt(2); // waitingOrder
					int oldPlaceInLine = rs.getInt(3);// placeInLine
					int newPlace = oldPlaceInLine - 1;
					/* update placeInLine */
					ps = conn.prepareStatement(
							"UPDATE project.waitinglist SET placeInLine=? where orderToWaitFor = ? and waitingOrder = ?");
					ps.setString(1, String.valueOf(newPlace));// placeInLine
					ps.setString(2, orderToWaitFor_arr[i]);// orderToWaitFor
					ps.setString(3, String.valueOf(waitingOrder));
				}
			} // for
			return true;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public void removeAllwaiters(String orderToRemove_AllWaiters) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM project.waitinglist WHERE orderToWaitFor=?");
			stm = conn.createStatement();
			ps.setString(1, orderToRemove_AllWaiters);
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
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
			PreparedStatement ps = conn
					.prepareStatement("SELECT orderNum FROM gonaturedb.order ORDER BY orderNum DESC ");
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
		i = i / 9;
		return i++;
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

		int counter = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors,orderNum FROM gonaturedb.order WHERE wantedPark=? AND DateOfVisit=? AND TimeInPark BETWEEN ? AND ? AND status= 'confirmed' OR status='entered' ");
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
					"INSERT INTO gonaturedb.order (orderNum, TimeInPark, DateOfVisit, wantedPark, TotalPrice, ID,type,numOfVisitors) VALUES (?,?,?,?,?,?,?,?)");
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
			PreparedStatement ps = conn
					.prepareStatement("SELECT *  FROM project.departmentemployee WHERE userName = ?");
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
		String[] s = new String[12];
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.departmentemployee WHERE userName = ?");
			stm = conn.createStatement();
			ps.setString(1, check[0]);
			ResultSet rs = ps.executeQuery();
			s[0] = "EmployeeController"; // For GoClient.HandleMessageFromServer
			s[1] = "IdentifyEmployee"; // in this case we assume that employee is in our DB --> if it isn't it will
										// change
			while (rs.next()) {
				for (int i = 2; i < 12; i++)
					s[i] = rs.getString(i - 1);
			}
			if (s[10] == null)
				s[1] = "IdentifyNotExistingEmployee"; // Means that we didn't find an employee with this userName
			else if (!(s[11].equals(check[1]))) {// It means that passwords do not match and employee can not enter to
													// GoNature system
				s[1] = "IdentifyPasswordDoesNotMatch";
				s[11] = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return s;
	}

	public void changeStatusOfOrder(String[] result, String status) {
		Statement stm;

		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE gonaturedb.order SET status=? WHERE TimeInPark=? AND DateOfVisit=? AND wantedPark=? AND ID=?");
			ps.setString(1, status);
			ps.setString(2, result[0]);
			ps.setString(3, result[1]);
			ps.setString(4, result[2]);
			ps.setString(5, result[3]);
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
					"SELECT orderNum,DateOfVisit,wantedPark,TimeInPark,numOfVisitors,TotalPrice FROM gonaturedb.order WHERE ID=?");
			ps.setString(1, iD);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				for (int i = 1; i <= 6; i++) {
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
					.prepareStatement("SELECT AmountOfUnExpectedVisitors FROM project.park WHERE ParkName=?");

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
				s[9] = null;
				temp = rs.getInt(8);
				s[10] = rs.getString(9);
				s[11] = rs.getString(10);
			}
			s[9] = ("" + temp);
			if (s[5] == null) {
				s[1] = "IdentifyNotExistingTraveller";
				s[5] = travellerID;
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
					.prepareStatement("UPDATE project.park SET AmountOfUnExpectedVisitors=? WHERE ParkName=?");

			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateCurrentVisitors(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE project.park SET currentVisitors=? WHERE ParkName=?");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void enterExitTimeForTravellerWithOrder(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE project.order SET ExitTime=TIME_FORMAT(curtime(), '%h:%i') WHERE wantedPark=? AND DateOfVisit=curdate() AND ID=? AND atatus=\"in park\" ");
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
					"UPDATE project.travellerinpark SET exitTime=TIME_FORMAT(curtime(), '%h:%i') WHERE wantedPark=? AND DateOfVisit=curdate() AND ID=? ");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
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
					"SELECT IdOfAsks,timeOfRequest,numberOfVisitors FROM project.requests WHERE wantedpark=? and dateOfRequest=curdate() and type=\"EnterPark\" and status=\"-1\" ORDER BY timeOfRequest ASC");
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
					"INSERT INTO project.travellerInPark(ID,numOfVisitors,Date,enterTime,exitTime,price,wantedPark) VALUES (?,?,?,?,null,?,?)");
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
			ps.setString(1, result[2]);
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
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;

		}
		return true;
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

	///////////////////////////// Reports start /////////////////////////////
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
					"SELECT numOfVisitors FROM project.order WHERE wantedPark=? AND MONTH(DateOfVisit)=? AND YEAR(DateOfVisit)=? AND status='Confirmed' AND (type='Member' OR type='Family Member') ");
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
					"SELECT numOfVisitors FROM project.order WHERE wantedPark=? AND MONTH(DateOfVisit)=? AND YEAR(DateOfVisit)=? AND status='Confirmed' AND type='Group Guide' ");
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

	////////////////// Entrance and Stay Report ////////////////////
	public String getEntranceAndStay(String[] monthYearPark) {
		String month = monthYearPark[0];
		String year = monthYearPark[1];
		String park = monthYearPark[2];
		StringBuffer sb = new StringBuffer();
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT numOfVisitors,enterTime,exitTime,Date FROM project.travellerinpark WHERE wantedPark=? AND MONTH(Date)=? AND YEAR(Date)=?");
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

/////////////////////////////  Reports end ///////////////////////////// 

	public int checkHowManyCancelled(String[] result, String status) {
		Statement stm;
		int counter = 0;

		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT * from gonaturedb.order WHERE status = ? AND DateOfVisit BETWEEN ? AND ?");

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

}
