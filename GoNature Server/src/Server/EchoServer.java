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
 *       will decrypte what type of action the server need to do
 *       using switch case to simplify the actions the server will make
 *    	every time the first place in the string array will tell what type of method to triger.
 *     
 */
		String[] bar_String;

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
			    for(int i = 0; i < user.length; i++) {
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
				

			//////////////////////// NEW UPDATES ////////////////////////////////
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
				res = sq.addMember(result);
				client.sendToClient("Done");
/*
			case "addMember":
				res = sq.addMember(result);
				StringBuffer sb4 = new StringBuffer();
				String se = "Done";

				client.sendToClient(se);
				break;
				*/
			case "getEmployeeDetails":
				if (sq.canGetEmployee(result[0])) {
					bar_String = new String[12];
					StringBuffer checkString = new StringBuffer();
					checkString.append(result[0]);
					checkString.append(" ");
					checkString.append(result[1]);
					//System.out.println("I am getEmployeeDetails: " + checkString.toString());
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
			case "getTravellerDetails":

				if (sq.canGetTraveller(result[0]))
				{
					//System.out.print(result[0]);
					bar_String = new String[11];

					bar_String = sq.getTravellerFromDB(result[0]);
					sb = new StringBuffer();
					for (int i = 0; i < bar_String.length; i++) {
						sb.append(bar_String[i]);
						sb.append(" ");
					}
					String sendMe = sb.toString();
					// System.out.print(sb4.toString());
					client.sendToClient(sendMe);
				}

				break;
				
			case "exit":
				serverStopped();
				break;


			case "setManagerDiscount":
				boolean bool = sq.addManagerDiscount(result); // returns boolean <<<<<<<<<<<<<<<<<<<<<<<<
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("setManagerDiscount");// The name of the method
				sb.append(" ");
				sb.append(bool);
				client.sendToClient(sb.toString()); // send the name of the controller called this method

				break;

			case "ValidDiscount":
				// result = [parkName, dateOfVisit]
				String discount = sq.getManagerDiscount(result);
				sb = new StringBuffer();
				sb.append("DiscountController"); // The name of the controller
				sb.append(" ");
				sb.append("ValidDiscount");// The name of the method
				sb.append(" ");
				sb.append(discount); // The discount row
				sb.append(" ");
				sb.append(result[1]);// dateOfVisit
				client.sendToClient(sb.toString());
				break;

			case "enterWaitingList":
				// result = [parkName[0],dateOfVisit[1],hour[2],numOfVistors[3],orderNum[4]]
				String orderNums = sq.findOrderToWaitFor(result);
				sb = new StringBuffer();
				sb.append(result[4]);// orderNum to enter the DB
				sb.append(" ");
				sb.append(orderNums);
				boolean addToWaitingList_flag = sq.addToWaitingList(sb.toString());

				sb = new StringBuffer();
				sb.append("WaitingListController");// the name of the controller
				sb.append(" ");
				sb.append("enterWaitingList");// The name of the method
				sb.append(" ");
				sb.append(addToWaitingList_flag);

				client.sendToClient(sb.toString());
				break;

			case "sendMessageFirstWaitingList":
				// result = [canceldOrderNum[0]]
				String orderNum_firstInLine = sq.findOrderFirstInLine(result[0]);// canceldOrderNum
				sb = new StringBuffer();
				sb.append("WaitingListController");// the name of the controller
				sb.append(" ");
				sb.append("sendMessageFirstWaitingList");// The name of the method
				sb.append(" ");
				sb.append(orderNum_firstInLine);

				client.sendToClient(sb.toString());
				break;

			case "removeFromWaitingList":
				boolean bool2 = sq.removeFromWaitingList(result[0]);// Num Of orderToRemove
				sb = new StringBuffer();
				sb.append("WaitingListController");// the name of the controller
				sb.append(" ");
				sb.append("removeFromWaitingList");// The name of the method
				sb.append(" ");
				sb.append(bool2);

				client.sendToClient(sb.toString());
				break;

			case "removeAllwaiters":
				sq.removeAllwaiters(result[0]);// Num Of orderToRemove_AllWaiters
				sb = new StringBuffer();
				sb.append("WaitingListController");// the name of the controller
				sb.append(" ");
				sb.append("removeAllwaiters");// The name of the method

				client.sendToClient(sb.toString());
				break;

			/*
			 * will check with the order table all the orders in the current date and time
			 * return the number of visitors in all this orders will compare the total
			 * number of visitors in park (from this orders) with the number of visitors can
			 * enter to the wanted park, if the sum of the desired visitors with the current
			 * visitors from the order greater then visitors can enter park will return
			 * false to the order controller AvailableVisitors= How many allowed every X
			 * hours in the park currentVisitorsAtBoundry=How many visitors already will be
			 * in park in the gapTime
			 */
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
				sq.cancelOrder(result);
				client.sendToClient(done);
				break;
			case "getExsistingOrders":
				String res1 = sq.getOrders(result[0]);
				sb = new StringBuffer();
				sb.append("OrderController");
				sb.append(" ");
				sb.append("getExsistingOrders");
				sb.append(" ");
				sb.append(res1);
				client.sendToClient(sb.toString());
				break;

			case "DetailsPark":

				int currentVisitors=sq.howManyCurrentvisitorsForOrdersInPark(result[0]);
				int unexpectedVisitors=sq.howManyUnexpectedVisitorsInPark(result[0]);
				int maxAvailableVisitors=sq.howManyAllowedInPark(result[0]);
				int maxVisitors=sq.howManyMaxvisitorsAllowedInPark(result[0]);
				sb= new StringBuffer();
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
				client.sendToClient(sb.toString());

				break;

			case "enterWithoutOrder":
				sq.insertTravellerInPark(result);
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
