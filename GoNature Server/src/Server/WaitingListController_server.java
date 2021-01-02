package Server;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import Entities.Order;
import SqlConnector.sqlConnector;

public class WaitingListController_server {
	private sqlConnector sq;

	public WaitingListController_server(sqlConnector sq) {
		this.sq = sq;
	}

	public void sendMessageToFirstInLine(int cancelledOrder_num) {
		Runnable r = new watingList_Confirmation_thread(cancelledOrder_num);
		Thread t = new Thread(r);
		
	}

	private class watingList_Confirmation_thread implements Runnable {
		LocalTime now, limit;
		String confirmation;
		ArrayList<Order> ordersInLine;

		public watingList_Confirmation_thread(int cancelledOrder_num) {
			ordersInLine = sq.getSortedWatingOrders(cancelledOrder_num);
		}
		/* 1 hour to approve */
		public void run() {
			/*check if confirmed*/
			for (Order o : ordersInLine){
				/*check if ordersInLine was changed*/
				if(sq.IsOrderInWaitingList(o.getOrderNum()))
					try {
						if (canMakeOrder(o.getTimeInPark(), o.getDateOfVisit(), o.getWantedPark(), o.getNumberOfVisitors())) {
								/* remove from waitingList DB */
							if (!removeFromWaitingList(o.getOrderNum())) {
								System.out.println("sql remove ERROR!\n");
								return;
								}

							/*TODO send Message  */
								System.out.println("Message from waitingList : orderNum" + o.getOrderNum() + " please confirm your order");
								
							now = LocalTime.now(); //message sent time of order o
							limit = now.plusHours(1); // traveler as to confirm within 1 hour
							
							while(true) {
								try 
								{
									Thread.sleep(1000 * 60);// 1 minute sleep 
								}
								catch(InterruptedException e) 
								{ //(check confirmation status every minute)
									confirmation = sq.check_Confirmation(o.getOrderNum());
									now = LocalTime.now(); // Cur Time
									if(limit.compareTo(now) < 0 || confirmation.equals("f")) // now is after limit OR traveler chose to cancel 
									{
										/*Cancel order in orders table DB*/
										if(confirmation.equals("f")) {
											//TODO Manual cancelation
											break; // Go to next waiting order
										}
										else if (limit.compareTo(now) < 0) {
											//TODO Automatic cancelation (1 hour passed) 
											break; // Go to next waiting order
										}
									}
									if(confirmation.equals("t")) {
										/*user confirmed order successfully (Before limit time)*/
										//TODO change order status in orders table DB
										return; // no more waiters to check (end of thread)
									}
								}//catch
							}//while
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//if
			}//for
		}// run
	}// (inner class) watingList_Confirmation_thread 
	
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
	
	// remove an order from waitinglist DB
	public boolean removeFromWaitingList(int orderNumber) {
		return sq.removeFromWaitingList(String.valueOf(orderNumber));
	}
}//class
