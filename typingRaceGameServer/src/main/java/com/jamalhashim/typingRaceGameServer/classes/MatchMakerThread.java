package com.jamalhashim.typingRaceGameServer.classes;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MatchMakerThread extends Thread{
	ArrayBlockingQueue<MatchRequest> queue = new ArrayBlockingQueue<MatchRequest>(100);
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
		while(true) {
			
		}
	}
}
