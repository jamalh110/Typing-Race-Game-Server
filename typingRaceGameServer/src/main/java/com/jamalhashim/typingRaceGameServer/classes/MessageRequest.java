package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;

public class MessageRequest {
	String type = "";
	String content = "";
	UUID sessionID = null;
	UUID matchID = null;
	ChannelHandlerContext ctx;
	
	public MessageRequest(String type, UUID sessionID, UUID matchID, ChannelHandlerContext ctx) {
		this.type=type;
		this.sessionID=sessionID;
		this.matchID=matchID;
		this.ctx = ctx;
	}
	public MessageRequest(String type, String content, UUID sessionID, UUID matchID, ChannelHandlerContext ctx) {
		this.type=type;
		this.content=content;
		this.sessionID=sessionID;
		this.matchID=matchID;
		this.ctx = ctx;
	}
	public String getMessage() {
		return type;
	}
	public String getContent() {
		return content;
	}
}
