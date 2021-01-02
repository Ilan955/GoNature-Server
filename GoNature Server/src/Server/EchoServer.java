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
				sb.append("ReportsController");// the name of the controller
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
