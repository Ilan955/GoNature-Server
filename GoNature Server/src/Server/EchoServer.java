// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package Server;

import java.io.*;
import SqlConnector.*;
import src.ocsf.server.AbstractServer;
import src.ocsf.server.ConnectionToClient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

import Client.ClientUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */

public class EchoServer extends AbstractServer {
	final public static int DEFAULT_PORT = 5555;
	private Connection conn;
	sqlConnector sq;
	public waitingListController_server server_waitingListController;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 * 
	 */

	public EchoServer(int port) {
		super(port);
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 * @param
	 */

	public void handleMessageFromClient(Object msg, ConnectionToClient client) {

		/*
		 * 
		 * will decrypte what type of action the server need to do using switch case to
		 * simplify the actions the server will make every time the first place in the
		 * string array will tell what type of method to triger.
		 * 
		 */
		String[] bar_String;
		String sendMe;
		String done = "Done";
		int flag = 0;
		String st = (String) msg;
		String[] user = null;
		String action = getAction(st);
		String[] result = DecrypteMassege(st);

		StringBuffer sb;

		try {
			boolean res;
			switch (action) {


			case "submitVisitor":
				user = sq.CheckForId(result[0]);

				sb = new StringBuffer();
				for (int i = 0; i < user.length; i++) {
					sb.append(user[i]);
					sb.append(" ");
				}

				sb = new StringBuffer();
				for (int i = 0; i < user.length; i++) {
					sb.append(user[i]);
					sb.append(" ");
				}

				String str = sb.toString();
				client.sendToClient(str);

				break;

			case "updateVisitor":
				if (sq.updateEmail(result)) {
					user = sq.CheckForId(result[0]);

					sb = new StringBuffer();
					for (int i = 0; i < user.length; i++) {
						sb.append(user[i]);
						sb.append(" ");
					}
					String str2 = sb.toString();
					client.sendToClient(str2);
				}
				break;

			case "connectivity":

				sb = new StringBuffer();
				sb.append(getPort());
				sb.append(" ");
				sb.append(client);
				String s = sb.toString();

				client.sendToClient(s);

			case "isMemberExists":
				res = sq.isMemberExists(result);
				StringBuffer sb3 = new StringBuffer();
				sb3.append("SignUpController");
				sb3.append(" ");
				sb3.append("isMemberExists");
				sb3.append(" ");
				sb3.append(res);
				client.sendToClient(sb3.toString());
				break;

			case "addMember":
				sb3 = new StringBuffer();
				sb3.append("SignUpController");
				sb3.append(" ");
				sb3.append("addMember");
				sb3.append(" ");
				sb3.append(sq.addMember(result));
				client.sendToClient(sb3.toString());
				break;
			case "deleteFromDbWhenlogOutTraveller":
				sq.deleteFromDbWhenTravellerLogOut(result[0], result[1]);
				client.sendToClient("UserController UpdateFieldofLoggedInTraveller");
			case "getEmployeeDetails":
				if (sq.canGetEmployee(result[0])) {
					bar_String = new String[12];
					StringBuffer checkString = new StringBuffer();
					checkString.append(result[0]);
					checkString.append(" ");
					checkString.append(result[1]);
					// System.out.println("I am getEmployeeDetails: " + checkString.toString());
					bar_String = sq.getEmployeeUN(checkString.toString());
					sb = new StringBuffer();
					for (int i = 0; i < bar_String.length; i++) {
						sb.append(bar_String[i]);
						sb.append(" ");
					}
					String s2 = sb.toString();
					client.sendToClient(s2);
				}
				break;
			case "logOutEmployee":
				if (sq.logOutEmployee(result[0]))
					client.sendToClient("LoggedOfSuccess");
			case "getTravellerDetails":
				if (sq.canGetTraveller(result[0])) {
					// System.out.print(result[0]);
					bar_String = new String[12];
					bar_String = sq.getTravellerFromDB(result[0]);
					sb = new StringBuffer();
					for (int i = 0; i < bar_String.length; i++) {
						sb.append(bar_String[i]);
						sb.append(" ");
					}
					sendMe = sb.toString();
					// System.out.print(sb4.toString());
					client.sendToClient(sendMe);
				}
				break;

			case "updateParkChangeRequestStatus": // xxxxxxxxxxx
				if (sq.updateParkChangeRequestStatus(result[0])) {
					client.sendToClient("ChangeIsSababa ");
				}
				break;
			case "updateParkChangesWhenPressedApprove":
				if (sq.updateParkChangesInParkTable(result[0], result[1], result[2], result[3])) {
					client.sendToClient("parkSettingsAreUpdated ");
				}
			case "getParkSettingsRequestsFromDB":// #TRY$%YTRGFG%^Y%^#H
				String send = sq.getParkSettingsRequests();
				client.sendToClient(send);
				break;
			case "SendParkChangesToDepartmentManager":
				bar_String = new String[3];
				bar_String[0] = "RequestsController";
				bar_String[1] = "parkSettingsChangesSent";
				bar_String[2] = sq.sendParkSettingsRequestToDepManager(result);
				sb = new StringBuffer();
				for (int i = 0; i < bar_String.length; i++) {
					sb.append(bar_String[i]);
					sb.append(" ");
				}
				sendMe = sb.toString();
				client.sendToClient(sendMe);
				break;

			case "exit":
				serverStopped();
				break;


			case "setManagerDiscount":
				boolean bool = sq.updateManagerDiscount(result[0], result[1], result[2], result[3]);
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("setManagerDiscount");// The name of the method
				sb.append(" ");
				sb.append(bool);
				client.sendToClient(sb.toString());

				break;

			case "ValidDiscount":
				// result = [parkName, dateOfVisit]
				Float discount = sq.getManagerDiscount(result[0], result[1]);
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("ValidDiscount");// The name of the method
				sb.append(" ");
				sb.append(discount); // The discount precentage

				client.sendToClient(sb.toString());
				break;

			case "enterWaitingList":
				// result = [orderNum]
				boolean addToWaitingList_flag = sq.addToWaitingList(result[0]); // orderNum
				sb = new StringBuffer();
				sb.append("WaitingListController");// the name of the controller
				sb.append(" ");
				sb.append("enterWaitingList");// The name of the method
				sb.append(" ");
				sb.append(addToWaitingList_flag);
				client.sendToClient(sb.toString());
				break;

				
			case"makeMonthlyIncomeReport":
      sb = new StringBuffer();
          sb.append(sq.getMonthlyIncomes(result[0],"Traveler"));//result[0] = Date 
          sb.append(" ");
          sb.append("makeMonthlyIncomeReport");// The name of the method
          sb.append(" ");
          sb.append(sq.getMonthlyIncomes(result[0],"Member"));//result[0] = Date 
				sb.append(" ");
				sb.append(sq.getMonthlyIncomes(result[0],"Family"));//result[0] = Date 
				sb.append(" ");
          sb.append(sq.getMonthlyIncomes(result[0],"groupGuide"));//result[0] = Date 
				client.sendToClient(sb.toString());
				break;
				
			/*
			 * This case will check first what number the order id will be Will insert into
			 * the Order table the new order got from client
			 */
			case "confirmOrder":
				int orderNum = sq.nextOrder();
				sq.addOrder(orderNum, result);
				client.sendToClient(done);
				break;
			/*
			 * This method will search for the order and delete it
			 */
			case "cancelOrder":
				sq.changeStatusOfOrder(result, "cancelled");
				client.sendToClient(done);
				break;

			case "getDataForReport":
				int cancelledOrderNumber = sq.checkHowManyCancelled(result, "canceled");
				int notEnteredOrderNumber = sq.checkHowManyCancelled(result, "confirmed");

				sb = new StringBuffer();
				sb.append("OrderController");
				sb.append(" ");
				sb.append("getDataForReport");
				sb.append(" ");
				sb.append(Integer.toString(cancelledOrderNumber));
				sb.append(" ");
				sb.append(Integer.toString(notEnteredOrderNumber));
				client.sendToClient(sb.toString());
				break;

			case "getExsistingOrders":
				String res1 = sq.getOrders(result[0]);

				sb = new StringBuffer();
				sb.append("ReportsController");// the name of the controller
				sb.append(" ");
				

				sb.append(res1);
				client.sendToClient(sb.toString());
				break;
			case "ChangeToWaitOrder":
				sq.changeStatusOfOrder(result, "waiting");
				client.sendToClient(done);
				break;
			case "DetailsPark":

				int currentVisitors = sq.howManyCurrentvisitorsForOrdersInPark(result[0]);
				int unexpectedVisitors = sq.howManyUnexpectedVisitorsInPark(result[0]);
				int maxAvailableVisitors = sq.howManyAllowedInPark(result[0]);
				int maxVisitors = sq.howManyMaxvisitorsAllowedInPark(result[0]);
				float maxDuration = sq.howmanyTimeEveryVisitorInPark(result[0]);
				sb = new StringBuffer();
				sb.append("ParkController");
				sb.append(" ");
				sb.append("DetailsPark");
				sb.append(" ");
				sb.append(Integer.toString(currentVisitors));
				sb.append(" ");
				sb.append(Integer.toString(maxVisitors));
				sb.append(" ");
				sb.append(Float.toString(maxDuration));
				client.sendToClient(sb.toString());
				break;
			case "setNumOfVisitorEntringPark":
				sq.updateUnexpectedVisitors(result);
				client.sendToClient(done);
				break;

			case "setCurrentVisitros":
				sq.updateCurrentVisitors(result);
				client.sendToClient(done);
				break;

			case "enterWithoutOrder":
				sq.insertTravellerInPark(result);
				client.sendToClient(done);
				break;
			case "updateExitTimeForTravellerWithOrder":
				sq.enterExitTimeForTravellerWithOrder(result);
				client.sendToClient(done);
				break;
			case "updateExitTimeForcasualTraveller":
				sq.enterExitTimeForcasualTraveller(result);
				client.sendToClient(done);
				break;
			////// Reports start/////
			case "getData":
				String ans = sq.getVisitorsDataReport(result);
				sb = new StringBuffer();
				sb.append("ReportsController");
				sb.append(" ");
				sb.append("getData");
				sb.append(" ");
				sb.append(ans);
				client.sendToClient(sb.toString());
				break;
			////// Reports end/////
			case "insertRequestToDB":
				sq.insertRequest(result);
				client.sendToClient(done);
				break;
			case "checkIfApproveRequest":
				int status = sq.IsApproveEnterParkForTraveller(result);
				StringBuffer sb5 = new StringBuffer();
				sb5.append("RequestsController");
				sb5.append(" ");
				sb5.append("checkIfApproveRequest");
				sb5.append(" ");
				sb5.append(Integer.toString(status));
				client.sendToClient(sb5.toString());
				break;
			case "getRequestsTravellerOfEnterPark":
				String string = sq.getRequestTableOfEnterPark(result[0]);
				sb = new StringBuffer();
				sb.append("RequestsController");
				sb.append(" ");
				sb.append("getRequestsTravellerOfEnterPark");
				sb.append(" ");
				sb.append(string);
				client.sendToClient(sb.toString());
				break;

			case "changeStatusForCasualTraveller":
				sq.changeRequestStatusForCasualTraveller(result);
				client.sendToClient(done);
				break;

			case "enterDateofFullCapcityPark":
				sq.insertfullcapacityPark(result);
				client.sendToClient(done);
				break;
			case "checkIfThisDateInFullCapacityTable":
				sq.isDateInfullcapacityExists(result);
				sb = new StringBuffer();
				sb.append("ParkController");
				sb.append(" ");
				sb.append("checkIfThisDateInFullCapacityTable");
				sb.append(" ");
				sb.append(result);
				client.sendToClient(sb.toString());
				break;
			case "updateStatusForCapacityParkToFull":
				sq.changeStatusForCapacityParkToFull(result);
				client.sendToClient(done);
				break;
			case "getTableOfUnFullCapacityInDates":
				String st1 = sq.getUnFullCapacityTableInDates(result);
				sb = new StringBuffer();
				sb.append("ReportsController");
				sb.append(" ");
				sb.append("getTableOfUnFullCapacityInDates");
				sb.append(" ");
				sb.append(st1);
				client.sendToClient(sb.toString());
				break;
			case "setEnterOrder":
				sq.changeStatusOfOrder(result, "Entered");
				client.sendToClient(done);
				break;

			default:
				System.out.println("Sorry, don't know what you presse Now");
					
			}
		} catch (Exception e) {
			System.out.println("Error");

		}
	}

	/*
	 * This method will return the information about the id got Return a string
	 * array containing all the informations.
	 * 
	 * 
	 */

	public String[] DecrypteMassege(String msg) {
		String[] gotFromClient = msg.split(" ");
		String[] res = new String[gotFromClient.length - 1];
		for (int i = 1; i < gotFromClient.length; i++) {
			res[i - 1] = gotFromClient[i];
		}
		return res;

	}

	public String getAction(String msg) {
		String[] result = msg.split(" ");
		return result[0];
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {

		System.out.println("Server listening for connections on port " + getPort());

		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			// System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
		}

		try {

			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/project?serverTimezone=IST", "root", "");

			System.out.println("Successfuly loged-in");
			sq = new sqlConnector(conn);
			server_waitingListController = new waitingListController_server(sq);

		} catch (SQLException ex) {/* handle any errors */
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 * 
	 * @throws IOException
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
		try {
			close();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("The server is closed now");
		}

	}

}
//End of EchoServer class
