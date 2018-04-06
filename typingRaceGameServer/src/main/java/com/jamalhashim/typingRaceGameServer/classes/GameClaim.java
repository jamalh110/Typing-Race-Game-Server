package com.jamalhashim.typingRaceGameServer.classes;

import java.util.Date;
import java.util.UUID;

public class GameClaim {
	//string so I can remove from the map.
	public String sessionID;
	//equal to matchID
	public UUID gameID;
	//game thread id
	public UUID threadID;
	public Date timeCreated;
	
	public GameClaim(String sessionID, UUID gameID, UUID threadID, Date now) {
		this.sessionID=sessionID;
		this.gameID=gameID;
		this.threadID=threadID;
		this.timeCreated=now;
	}
}
