package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;

public class MessageRequest {
	String message = "";
	UUID sessionID = null;
	UUID matchID = null;
	ChannelHandlerContext ctx;
	
	public MessageRequest(String message, UUID sessionID, UUID matchID, ChannelHandlerContext ctx) {
		this.message=message;
		this.sessionID=sessionID;
		this.matchID=matchID;
		this.ctx = ctx;
	}
	public String getMessage() {
		return message;
	}
}
