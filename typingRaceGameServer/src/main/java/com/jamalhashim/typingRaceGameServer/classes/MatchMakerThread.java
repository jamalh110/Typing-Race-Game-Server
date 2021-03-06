package com.jamalhashim.typingRaceGameServer.classes;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.jamalhahsim.typingRaceGameServer.App;

public class MatchMakerThread extends Thread {
	ArrayBlockingQueue<MatchRequest> queue = new ArrayBlockingQueue<MatchRequest>(100);
	MatchRequest current = null;
	//timeout in seconds
	final int TIMEOUT = 35;
	public MatchMakerThread() {
		super("MatchMakerThread");
	}

	public void submitRequest(MatchRequest request) {
		try {
			queue.put(request);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			if (current != null) {
				if (!current.isValidated()) {
					current = null;
				}
				if(System.currentTimeMillis()-current.created.getTime()>TIMEOUT*1000) {
					current.invalidate();
					//current.close();
					current.setReason("timeout");
					current = null;
					System.err.println("match timeout");
				}
			}
			
			MatchRequest polled = queue.poll();
			if (polled != null && polled.isValidated()) {
				if (current == null) {
					current = polled;
				} else {
					System.out.println("match made");
					UUID matchID = UUID.randomUUID();
					GameThread thread = App.findThread();
					
					current.fill(thread.threadID, matchID);
					polled.fill(thread.threadID, matchID);
					
					
					
					
					GameClaim player1Claim = new GameClaim(current.sessionID.toString(), matchID, thread.threadID,new Date());
					GameClaim player2Claim = new GameClaim(polled.sessionID.toString(), matchID, thread.threadID, new Date());
					Game g = new Game(current, polled, matchID,player1Claim,player2Claim);
					//String currentSessionID = current.sessionID.toString();
					//String polledSessionID = polled.sessionID.toString();
					current=null;
					polled=null;
					thread.addGameToQueue(g);
				}
			}

		}
	}
}
