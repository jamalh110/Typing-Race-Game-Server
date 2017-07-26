package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;

public class ConnectionRequest {
	boolean connect = false;
	boolean disconnect = false;
	UUID sessionID = null;
	UUID matchID = null;
	ChannelHandlerContext ctx;
	public static final int CONNECT = 0;
	public static final int DISCONNECT = 1;
	public ConnectionRequest(int type, UUID sessionID, UUID matchID, ChannelHandlerContext ctx) {
		if(type==CONNECT) {
			connect=true;
		}
		else {
			disconnect=true;
		}
		this.sessionID=sessionID;
		this.matchID=matchID;
		this.ctx = ctx;
	}
	public boolean isConnect() {
		return connect;
	}
	public boolean isDisconnect() {
		return disconnect;
	}
}
