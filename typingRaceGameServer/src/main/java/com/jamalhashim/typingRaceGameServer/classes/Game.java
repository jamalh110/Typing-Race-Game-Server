package com.jamalhashim.typingRaceGameServer.classes;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class Game {

	UUID matchID;
	UUID player1SessionID;
	UUID player2SessionID;
	String player1Username;
	String player2Username;

	String p1Message = "";
	String p2Message = "";
	
	
	ArrayBlockingQueue<ConnectionRequest> connections = new ArrayBlockingQueue<ConnectionRequest>(10);
	ArrayBlockingQueue<MessageRequest> messages = new ArrayBlockingQueue<MessageRequest>(100);
	boolean player1Connected = false;
	boolean player2Connected = false;
	public Game(MatchRequest player1, MatchRequest player2, UUID matchID) {
		this.matchID = matchID;
		player1SessionID = player1.sessionID;
		player2SessionID = player2.sessionID;
		player1Username = player1.username;
		player2Username = player2.username;
	}

	public boolean execute() {
		while(!connections.isEmpty()) {
			ConnectionRequest request = connections.poll();
			if(request.connect) {
				if(request.sessionID.equals(player1SessionID)&&request.matchID.equals(matchID)) {
					player1Connected=true;
					respondConnectionSuccess(request.ctx);
					continue;
				}
				if(request.sessionID.equals(player2SessionID)&&request.matchID.equals(matchID)) {
					player2Connected=true;
					respondConnectionSuccess(request.ctx);
					continue;
				}
				else {
					respondConnectionFailure(request.ctx);
				}
			}
			else {
				System.out.println("disconnect");
			}
		}
		while(!messages.isEmpty()) {
			MessageRequest request = messages.poll();
			if(request.sessionID.equals(player1SessionID)&&request.matchID.equals(matchID)
					&&player1Connected&&player2Connected) {
				p1Message = request.message;
				respondMessageSuccess(request.ctx, p2Message);
				continue;
			}
			if(request.sessionID.equals(player2SessionID)&&request.matchID.equals(matchID)
					&&player1Connected&&player2Connected) {
				p2Message = request.message;
				respondMessageSuccess(request.ctx, p1Message);
				continue;
			}
		}
		
		//check for winner, do game logic, etc.
		return true;
	}

	public boolean addConnectionToQueue(ConnectionRequest request) {
		try {
			connections.put(request);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean sendMessage(MessageRequest request) {
		try {
			messages.put(request);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean respondConnectionSuccess(ChannelHandlerContext ctx) {
		ctxWrite(ctx, "Connection Success");
		return true;
	}
	public boolean respondConnectionFailure(ChannelHandlerContext ctx) {
		ctxWrite(ctx, "Connection Failed");
		return true;
	}
	public boolean respondMessageSuccess(ChannelHandlerContext ctx, String oppMessage) {
		ctxWrite(ctx, "Opponenet message: " + oppMessage);
		return true;
	}
	public void ctxWrite(ChannelHandlerContext ctx, String message) {
		ByteBuf bytes = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);
	}
}
