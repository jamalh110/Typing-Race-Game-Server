package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class GameThread extends Thread {
	ConcurrentHashMap<UUID, Game> games = new ConcurrentHashMap<UUID, Game>();
	ArrayBlockingQueue<Game> queue = new ArrayBlockingQueue<Game>(100);
	UUID threadID = null;

	public GameThread(UUID id) {
		super("GameThread");
		threadID = id;
	}

	public void run() {

		while (true) {
			try {
				while (!queue.isEmpty()) {
					Game g = queue.poll();
					addGame(g);
				}
				for (Game game : games.values()) {
					game.execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public int getSize() {
		return games.size();
	}

	public boolean addGameToQueue(Game game) {
		try {
			queue.put(game);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public Game getGame(UUID matchID) {
		return games.get(matchID);
	}
	private void addGame(Game game) {
		games.put(game.matchID, game);
	}
}
