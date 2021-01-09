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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

/**
 * This class ffoverrides some of the methods in the abstract superclass in
 * order to give more functionality to the server.
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
	public WaitingListController_server server_waitingListController;

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
			/*
			 * This method will check if the visitor have tomorow an order. if so, will
			 * check if already informed, if not informed, will create a new thread. this
			 * thread will run for 2 hourse and will check if the status of the order
			 * changed. 2 fields: Informed, Confirmed, both false at the start.
			 */
			case "havingAlert":
				String orderNumber = sq.checkIfHavingTomorrow(result);
				sb = new StringBuffer();
				sb.append("OrderController");
				sb.append(" ");
				sb.append("havingAlert");
				sb.append(" ");
				if (!(orderNumber.equals(""))) {

				}
				sb.append(orderNumber);
				client.sendToClient(sb.toString());
				break;

				
				/*---------------------------------------------- Discounts Start ---------------------------------------------------*/				
			case "isDiscountWaitingForApprove":
				boolean bool = sq.isDiscountWaitingForApprove(result[0]);//rawIndex
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("isDiscountWaitingForApprove");// The name of the method
				sb.append(" ");
				sb.append(bool);
				client.sendToClient(sb.toString());
				break;
				
			case "getDiscountWaitingForApprove":
				String discount = sq.getDiscountWaitingForApprove(result[0]);//rawIndex
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("getDiscountWaitingForApprove");// The name of the method
				sb.append(" ");
				sb.append(discount);
				client.sendToClient(sb.toString());
				break;
				
			case "ValidDiscount":
				// result = [parkName, dateOfVisit]
				float discount1 = sq.getManagerDiscount(result[0], result[1]);
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("ValidDiscount");// The name of the method
				sb.append(" ");
				sb.append(discount1); // The discount precentage
				client.sendToClient(sb.toString());
				break;
				
			case "setDiscountStatus":
				sq.setDiscountStatus(result[0], result[1]);
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				client.sendToClient(sb.toString());
				break;
				
				//get the prices by type of group
			case "getTotalPrice":
				String  resofDis = sq.getTotalPayload(result[0]);
				sb= new StringBuffer();
				sb.append("DiscountController");
				sb.append(" ");
				sb.append("getTotalPrice");
				sb.append(" ");
				sb.append(result[1]);
				sb.append(" ");
				sb.append(result[2]);
				sb.append(" ");
				sb.append(resofDis);
				client.sendToClient(sb.toString());
				break;
			
				
			case "setManagerDiscount":
				boolean bool1 = sq.updateManagerDiscount(result[0], result[1], result[2], result[3]);
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("setManagerDiscount");// The name of the method
				sb.append(" ");
				sb.append(bool1);
				client.sendToClient(sb.toString());
				break;
				
/*---------------------------------------------- Discounts End ---------------------------------------------------*/

				
/*---------------------------------------------- WaitingList Start ---------------------------------------------------*/				
			case "changeOrderStatus":
				//result = [orderNum, status , comment]
				sq.changeStatusByOrderNum(Integer.valueOf(result[0]),result[1],result[2]);
				sb = new StringBuffer();
				sb.append("WaitingListController"); // The name of the controller
				sb.append(" ");
				sb.append("changeOrderStatus");// The name of the method
				client.sendToClient(sb.toString());
				
			
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
				
			case "deleteFromWaitingList":
				sq.removeFromWaitingList(String.valueOf(result[0]));
				client.sendToClient("WaitingListController ");
				break;
/*---------------------------------------------- WaitingList End ---------------------------------------------------*/
				
				
				
				
				
				
				
				
				
				
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

		

			

			

			case"makeMonthlyIncomeReport":
			      sb = new StringBuffer();
			      sb.append("ReportsController");
			      sb.append(" ");
			      sb.append("makeMonthlyIncomeReport");// The name of the method
			      sb.append(" ");
			          sb.append(sq.getMonthlyIncomes(result[0],"Traveler"));//result[0] = Date 
			          sb.append(" ");
			          sb.append(sq.getMonthlyIncomes(result[0],"Member"));//result[0] = Date 
					  sb.append(" ");
					  sb.append(sq.getMonthlyIncomes(result[0],"Family"));//result[0] = Date 
					  sb.append(" ");
			          sb.append(sq.getMonthlyIncomes(result[0],"groupGuide"));//result[0] = Date 
			          sb.append(" ");
			          //get traveler in park incomes/
			          sb.append(sq.getMonthlyIncomes_TravelerInPark(result[0]));
			          
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
				sq.changeStatusOfOrder(result, "cancelled", "Manually");
				int orderNumForWaiting = sq.getOrderNum(result);
				server_waitingListController.sendMessageToFirstInLine(orderNumForWaiting);

				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!here
				// WaitingLine!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1

				client.sendToClient(done);
				break;

		

			case "canMakeOrder":
				int currentVisitorsAtBoundry = sq.howManyForCurrentTimeAndDate(result);
				int availableVisitors = sq.howManyAllowedInPark(result[2]);
				sb = new StringBuffer();
				sb.append("OrderController");
				sb.append(" ");
				sb.append("canMakeOrder");
				sb.append(" ");
				sb.append(Integer.toString(currentVisitorsAtBoundry));
				sb.append(" ");
				sb.append(Integer.toString(availableVisitors));
				client.sendToClient(sb.toString());
				break;

			case "getDataForReport":
				int cancelledOrderNumber = sq.checkHowManyCancelled(result, "cancelled");
				int notEnteredOrderNumber = sq.checkHowManyCancelled(result, "done");

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
				sb.append("OrderController");
				sb.append(" ");
				sb.append("getExsistingOrders");// the name of the controller
				sb.append(" ");
				sb.append(res1);
				client.sendToClient(sb.toString());
				break;
			case "ChangeToWaitOrder":
				System.out.println("Here1");
				sq.changeStatusOfOrder(result, "InWaitingList", "Manually");
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
				sb.append(Integer.toString(unexpectedVisitors));
				sb.append(" ");
				sb.append(Integer.toString(maxAvailableVisitors));
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
			case "checkIfTravellerExistsInPark":
				boolean answer = sq.isTravellerExistsInPark(result);
				StringBuffer sb7 = new StringBuffer();
				sb7.append("EntranceParkController");
				sb7.append(" ");
				sb7.append("checkIfTravellerExistsInPark");
				sb7.append(" ");
				sb7.append(answer);
				client.sendToClient(sb7.toString());
				break;

			case "getTravellerInParkDetails":
				String res2 = sq.getTravellerInParkDetails(result[0]);
				sb = new StringBuffer();
				sb.append("EntranceParkController");
				sb.append(" ");
				sb.append("getTravellerInParkDetails");
				sb.append(" ");
				sb.append(res2);
				client.sendToClient(sb.toString());
				break;

			case "checkIfOrderExistsInParkAndConfirmed":
				boolean answer3 = sq.isOrderExistsInPark(result);
				StringBuffer sb8 = new StringBuffer();
				sb8.append("EntranceParkController");
				sb8.append(" ");
				sb8.append("checkIfOrderExistsInParkAndConfirmed");
				sb8.append(" ");
				sb8.append(answer3);
				client.sendToClient(sb8.toString());
				break;

			case "getOrderDetailsForExitPark":
				String res3 = sq.getOrderDetailsForExitPark(result[0]);
				sb = new StringBuffer();
				sb.append("EntranceParkController");
				sb.append(" ");
				sb.append("getOrderDetailsForExitPark");
				sb.append(" ");
				sb.append(res3);
				client.sendToClient(sb.toString());
				break;
				
				/**
				 * this case change enter time for traveller with order
				 */
				case "updateEnterTimeForTravellerWithOrder":
					sq.enterEnterTimeForTravellerWithOrder(result);
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

			case "getDataEntranceTimesAndStay":
				sb = new StringBuffer();
				sb.append("ReportsController");
				sb.append(" ");
				sb.append("getDataEntranceTimesAndStay");
				sb.append(" ");
				sb.append(sq.getEntranceAndStay(result));
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
				
			case "changeMaxcurrentAmountOfVisitorsForCapacityPark":
				sq.changeMaxcurrentAmountOfVisitorsForCapacityPark(result);
				client.sendToClient(done);
				break;
				
			case "checkIfThisDateInFullCapacityTable":
				boolean answer1 = sq.isDateInfullcapacityExists(result);
				StringBuffer sb6 = new StringBuffer();
				sb6.append("ParkConroller");
				sb6.append(" ");
				sb6.append("checkIfThisDateInFullCapacityTable");
				sb6.append(" ");
				sb6.append(answer1);
				client.sendToClient(sb6.toString());
				break;
			case "confirmAlert":
				sq.conAlert(result[0]);
				client.sendToClient(done);
				break;

			case "updateStatusForCapacityParkToFull":
				sq.changeStatusForCapacityParkToFull(result);
				client.sendToClient(done);
				break;
				
				/**
				 * this case get max current of visitors in specfic date and park
				 */
				case "getMaxcurrentVisitorsPerDay":
					int max = sq.getMaxcurrentVisitorsPerDay(result);
					sb = new StringBuffer();
					sb.append("ParkController");
					sb.append(" ");
					sb.append("getMaxcurrentVisitorsPerDay");
					sb.append(" ");
					sb.append(String.valueOf(max));
					client.sendToClient(sb.toString());
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
				sq.changeStatusOfOrder(result, "Entered", "EnteredPark");
				client.sendToClient(done);
				break;
				
				/**
				 * this case get table of unfull capacity dates for usage report
				 */
				case "getUnFullCapacityTableInDatesAndNumbers":
					String st3 = sq.getUnFullCapacityTableInDatesAndNumbers(result);
					sb = new StringBuffer();
					sb.append("ReportsController");
					sb.append(" ");
					sb.append("getUnFullCapacityTableInDatesAndNumbers");
					sb.append(" ");
					sb.append(st3);
					client.sendToClient(sb.toString());
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
	/*
	 * This thread will check 24/7 the next condition: every hour do: 1. take
	 * current hour 2. x= minus 2 hours from that 3. take tomorrow day 4. get all
	 * the orderes for tomorrow who are : a. time of x b. Confirmed = 'f' 5. save
	 * all this orders in a string 6. iterate over this string, change status to
	 * cancelled 7. get next in line in the waiting list.
	 */

	public class UtilityThread extends Thread {
		String h;

		public void run() {
			LocalTime timeNow;

			int min, hour;
			String stringForComplete = "", dat, stringForHalf = "";
			while (true) {

				timeNow = LocalTime.now();
				hour = timeNow.getHour();

				min = timeNow.getMinute();
				if (hour < 10 || hour > 20) {
					try {
						Thread.sleep(1000 * 60 * 60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				else {
					hour -= 2;
					if (hour < 10)
						h = "0" + Integer.toString(hour);
					else
						h = Integer.toString(hour);

					if (min == 30) {
						dat = LocalDate.now().plusDays(1).toString();

						timeNow = LocalTime.parse(h + ":" + "30");

						stringForHalf = sq.checkIfConfirmAlert(dat, timeNow.toString());
						try {
							Thread.sleep(1000 * 60 * 30);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					else if (min > 0) {
						dat = LocalDate.now().plusDays(1).toString();

						timeNow = LocalTime.parse(h + ":" + "00");
						System.out.println(timeNow.getSecond());
						stringForComplete = sq.checkIfConfirmAlert(dat, timeNow.toString());
						try {
							Thread.sleep(1000 * 60 * 30);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					String[] complete = stringForComplete.split(" ");

					String[] halfs = stringForHalf.split(" ");
					if (!(complete.length == 1)) {
						for (int i = 0; i < complete.length; i = i + 2) {
							sq.cancelOrderForWaiting(complete[i]);
							System.out.println(complete[i + 1]);
							server_waitingListController.sendMessageToFirstInLine(Integer.parseInt(complete[i + 1]));
						}
					}
					if (!(halfs.length == 1)) {
						for (int i = 0; i < halfs.length; i = i + 2) {
							sq.cancelOrderForWaiting(halfs[i]);
							System.out.println(halfs[i + 1]);
							server_waitingListController.sendMessageToFirstInLine(Integer.parseInt(halfs[i + 1]));
						}

					}
				}
			}

		}
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
			UtilityThread ut = new UtilityThread();
			ut.start();
			server_waitingListController = new WaitingListController_server(sq);

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
