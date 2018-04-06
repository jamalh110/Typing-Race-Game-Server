package com.jamalhashim.typingRaceGameServer.classes;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import com.jamalhashim.typingRaceGameServer.data.Passages;

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

	String p1MessageType = "";
	String p2MessageType = "";
	
	public GameClaim player1Claim;
	public GameClaim player2Claim;
	
	//in seconds
	int countdownLength = 10;
	boolean countdownStarted= false;
	long countdownStartTime;
	boolean gameStarted = false;
	boolean gameEnded = false; 
	UUID winner = null;
	
	String passage;
	int passageLength=0;
	LinkedList<String> player1Words;
	LinkedList<String> player2Words;
	
	ArrayBlockingQueue<ConnectionRequest> connections = new ArrayBlockingQueue<ConnectionRequest>(10);
	ArrayBlockingQueue<MessageRequest> messages = new ArrayBlockingQueue<MessageRequest>(100);
	boolean player1Connected = false;
	boolean player2Connected = false;
	public Game(MatchRequest player1, MatchRequest player2, UUID matchID, GameClaim player1Claim, GameClaim player2Claim) {
		this.matchID = matchID;
		
		player1SessionID = player1.sessionID;
		player2SessionID = player2.sessionID;
		player1Username = player1.username;
		player2Username = player2.username;
		this.player1Claim=player1Claim;
		this.player2Claim=player2Claim;
		passage = Passages.getPassage();
		player1Words = splitList(passage);
		player2Words = splitList(passage);
		passageLength = player1Words.size();
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
				else if(request.sessionID.equals(player2SessionID)&&request.matchID.equals(matchID)) {
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
		
		if(player1Connected&&player2Connected&&!countdownStarted) {
			countdownStarted=true;
			countdownStartTime = System.currentTimeMillis();
			
		}
		if(!gameStarted&&System.currentTimeMillis()-countdownStartTime>countdownLength*1000&&countdownStarted) {
			gameStarted=true;
		}
		while(!messages.isEmpty()) {
			
			MessageRequest request = messages.poll();
			if(request.sessionID.equals(player1SessionID)&&request.matchID.equals(matchID)) {
				//TODO: check for both players connection
				p1MessageType = request.type;
				//respondMessageSuccess(request.ctx, p2Message);
				
				if(p1MessageType.equals("oppName")) {
					ctxWrite(request.ctx, player2Username);
				}
				if(p1MessageType.equals("getPassage")) {
					ctxWrite(request.ctx, passage);
				}
				if(p1MessageType.equals("matchStatus")) {
					System.out.println("countdown time: " + (System.currentTimeMillis()-countdownStartTime)+"   "+ gameStarted);
					if(countdownStarted==false) {
						ctxWrite(request.ctx, "Code:0");
					}
					else if(gameStarted == false) {
						ctxWrite(request.ctx, "Code:1"+(countdownLength*1000-(System.currentTimeMillis()-countdownStartTime)));
					}
					else if(gameStarted==true&&!gameEnded) {
						ctxWrite(request.ctx, "Code:2"+percentThrough(player2Words));
					}
					else if(gameEnded == true) {
						ctxWrite(request.ctx,"Code:3"+winner.toString());
					}
				}
				
				if(p1MessageType.equals("word")&&gameStarted&&!gameEnded) {
					String word = request.getContent();
					if(word.equals(player1Words.get(0))) {
						player1Words.removeFirst();
						ctxWrite(request.ctx,"received");
					}
					else {
						ctxWrite(request.ctx,"failed");
						//TODO: return correct word to be on. 
					}
				}
				
				
				continue;
			}
			if(request.sessionID.equals(player2SessionID)&&request.matchID.equals(matchID)) {
				//TODO: check for both players connection
				p2MessageType = request.type;
				//respondMessageSuccess(request.ctx, p1Message);
				
				
				if(p2MessageType.equals("oppName")) {
					ctxWrite(request.ctx, player1Username);
				}
				if(p2MessageType.equals("getPassage")) {
					ctxWrite(request.ctx, passage);
				}
				if(p2MessageType.equals("matchStatus")) {
					if(countdownStarted==false) {
						ctxWrite(request.ctx, "Code:0");
					}
					else if(gameStarted == false) {
						ctxWrite(request.ctx, "Code:1"+(countdownLength*1000-(System.currentTimeMillis()-countdownStartTime)));
					}
					else if(gameStarted==true&&gameEnded == false) {
						ctxWrite(request.ctx, "Code:2"+percentThrough(player1Words));
					}
					else if(gameEnded == true) {
						ctxWrite(request.ctx,"Code:3"+winner.toString());
					}
				}
				
				if(p2MessageType.equals("word")&&gameStarted&&!gameEnded) {
					String word = request.getContent();
					if(word.equals(player2Words.get(0))) {
						player2Words.removeFirst();
						ctxWrite(request.ctx,"received");
					}
					else {
						//TODO: return correct word to be on. 
						ctxWrite(request.ctx,"failed");
					}
				}
				continue;
			}
		}
		if (!gameEnded) {
			if (player1Words.size() == 0) {
				gameEnded = true;
				winner = player1SessionID;
			} else if (player2Words.size() == 0) {
				gameEnded = true;
				winner = player2SessionID;
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
	public LinkedList<String> splitList(String passage){
		String[] split = passage.split("\\s+");
		LinkedList<String> list = new LinkedList<String>();
		for(int i = 0;i<split.length;i++) {
			list.add(split[i]);
		}
		return list;
	}
	public int percentThrough(LinkedList<String> words) {
		return 100-(words.size()*100)/passageLength;
	}
}
