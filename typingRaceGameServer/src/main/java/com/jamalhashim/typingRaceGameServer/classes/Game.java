package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;

public class Game {
	
	UUID matchID;
	
	
	public Game(MatchRequest player1, MatchRequest player2, UUID matchID) {
		this.matchID = matchID;
		
	}
}
