package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

public class MatchRequest {
	ChannelHandlerContext ctx;
	UUID sessionID;
	String username;
	public MatchRequest(ChannelHandlerContext ctx, UUID sessionID, String username) {
		this.ctx=ctx;
		this.sessionID = sessionID;
		this.username = username;
	}
	
}
