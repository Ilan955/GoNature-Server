package Server;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import Entities.Order;
import SqlConnector.sqlConnector;

/** WaitingListController on the server side
 * @author 123ta
 *
 */ 
public class WaitingListController_server {
	private sqlConnector sq;

	/**
	 * @param sq - sqlConnector
	 */
	public WaitingListController_server(sqlConnector sq) {
		this.sq = sq;
	}

	/** create a thread to check for a valid waiting order in line
	 * @param cancelledOrder_dateOfVisit -date
	 */ 
	public void sendMessageToFirstInLine(String cancelledOrder_dateOfVisit) {
		Runnable r = new watingList_Confirmation_thread(cancelledOrder_dateOfVisit);
		Thread t = new Thread(r);
		t.start();
		
	}

	/** inner class thread to check for valid waiting order
	 * @author 123ta
	 *
	 */ 
	private class watingList_Confirmation_thread implements Runnable {
		LocalTime now, limit;
		String confirmation;
		ArrayList<Order> ordersInLine;

		/** ArrayList<Order> from waiting list sorted by timestamp
		 * @param cancelledOrder_dateOfVisit
		 */ 
		public watingList_Confirmation_thread(String cancelledOrder_dateOfVisit) {
			ordersInLine = sq.getSortedWatingOrders(cancelledOrder_dateOfVisit);
		}
		
		public void run() {
			for (Order o : ordersInLine)
			{
				/*check if ordersInLine was changed by other threads OR user*/
				if(sq.IsOrderInWaitingList(o.getOrderNum())) 
				{		/*check if order in line is valid*/
					
					try {
						Thread.sleep(1000 *30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
						try {
							if (canMakeOrder(o.getTimeInPark(), o.getDateOfVisit(), o.getWantedPark(), o.getNumberOfVisitors())) 
							{
										/* remove from waitingList DB */
									if (!removeFromWaitingList(o.getOrderNum())) {
										System.out.println("sql remove ERROR!\n");
										return;
										}

									/*Send Message */
										System.out.println("Message from waitingList : orderNum" + o.getOrderNum() + " to park " + o.getWantedPark() + " date: " + o.getDateOfVisit() + " time: " + o.getTimeInPark() + "\n Is now avialebale , You have 1 hour to confirm your order ");
									/*Change order status to 'waitForConfirm_WaitingList'*/
										sq.changeStatusByOrderNum(o.getOrderNum(), "waitForConfirm_WaitingList","");
									now = LocalTime.now(); // time of message sent 
									limit = now.plusHours(1); // traveler has to confirm within 1 hour
									
									while(true) 
									{
										
										
										 //(check confirmation status every minute)
											confirmation = sq.getOrderStatus(o.getOrderNum());

											now = LocalTime.now(); // Cur Time
											//Time of order passed
											if(now.compareTo(o.getTimeInPark()) < 0)
											{
												removeFromWaitingList(o.getOrderNum());
												sq.changeStatusByOrderNum(o.getOrderNum(), "cancelled","Automatic");
												break; // Go to next waiting order
											}
											 //traveler choosed to cancel
											if(confirmation.equals("CanceledBYUser")) 
											{
												removeFromWaitingList(o.getOrderNum());
												/* Manual cancelation*/
												sq.changeStatusByOrderNum(o.getOrderNum(), "cancelled","Manually");
												
												break; // Go to next waiting order
											}
											// now is after limit
											 if (limit.compareTo(now) < 0) 
											 {
												/*Automatic cancelation (1 hour passed)*/ 
												sq.cancelOrderForWaiting(String.valueOf(o.getOrderNum()));
												break; // Go to next waiting order
											 }
											
											if(confirmation.equals("ApprovedBYUser")) 
											{ /*user confirmed order successfully (Before limit time)*/
												/*change order status in orders table DB*/
												removeFromWaitingList(o.getOrderNum());
												LocalDate toDay = LocalDate.now();
												LocalDate visit = o.getDateOfVisit();
												/*if now is same day of visit 
												 * >> change status to 'OrderConfirmed' 
												 * (dont ask for another confirmation)*/
												if(toDay.equals(visit)) // same day
													sq.conAlert(String.valueOf(o.getOrderNum())); // change status to 'OrderConfirmed'
												else
													/*change status to 'waitForConfirm' >> requires additional confirmation 24 hours before visit*/
													sq.changeStatusByOrderNum(o.getOrderNum(), "waitForConfirm","");
												return; // no more waiters to check (end of thread)
											}
										//catch
									}//while
}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//if
				}//if
			}//for
		}// run
	}// (inner class) watingList_Confirmation_thread 
	
	/** check if order is valid to enter the park
	 * @param time wanted time of visit in the park - item
	 * @param dateOfVisit -date
	 * @param wantedPark -wanted
	 * @param numOfVisitors -num

	 * @throws ParseException - throw
	 */ 
	public boolean canMakeOrder(LocalTime time, LocalDate dateOfVisit, String wantedPark, int numOfVisitors) throws ParseException {
		LocalTime openingTime = LocalTime.of(8, 0);
		LocalTime closingTime = LocalTime.of(23, 30);
		LocalTime turn = LocalTime.of(11, 00);
		/*
		 * check what are the boundary's this will numbers will help us to know how many
		 * park in this times.
		 */
		LocalTime from = null;
		LocalTime to = null;
		LocalTime tmp;
		tmp = time.minusMinutes(30);
		if (tmp.isBefore(turn))
			from = openingTime;
		else {
			for (int i = 3; i >= 0; i--) {
				tmp = time.minusHours(i);
				if (!(tmp.isBefore(openingTime))) {
					from = tmp;
					break;
				}
			}
		}
		tmp = time.minusHours(3);
		tmp = tmp.minusMinutes(30);
		if (!(tmp.isBefore(openingTime)))
			from = tmp;

		for (int i = 3; i >= 0; i--) {
			tmp = time.plusHours(i);
			if (tmp.isBefore(closingTime)) {
				to = tmp;
				break;
			}
		}
		tmp = time.plusHours(3);
		tmp = tmp.plusMinutes(30);
		if (tmp.isBefore(closingTime))
			to = tmp;

		String[] msg = new String[4];
		msg[0] = (from.toString()) + ":00"; // from	
		msg[1] = (to.toString()) + ":00";// to
		msg[2] = wantedPark;// wantedPark
		
		msg[3] = dateOfVisit.toString();// dateOfVisit
		
		
		int currentVisitorsAtBoundry = sq.howManyForCurrentTimeAndDate(msg);
		int availableVisitors = sq.howManyAllowedInPark(msg[2]);
		
		
		if (currentVisitorsAtBoundry + numOfVisitors > availableVisitors)
			return false;// Can't make order!
		else
			return true;// Can make order!
	}
	
	 
	/** remove an order from waitinglist DB
	 * @param orderNumber - order
	
	 */
	public boolean removeFromWaitingList(int orderNumber) {
		return sq.removeFromWaitingList(String.valueOf(orderNumber));
	}
}//class
