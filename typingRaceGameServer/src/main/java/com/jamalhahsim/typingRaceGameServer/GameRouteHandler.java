package com.jamalhahsim.typingRaceGameServer;

import java.util.Map;
import java.util.UUID;

import com.jamalhashim.typingRaceGameServer.classes.ConnectionRequest;
import com.jamalhashim.typingRaceGameServer.classes.Game;
import com.jamalhashim.typingRaceGameServer.classes.GameClaim;
import com.jamalhashim.typingRaceGameServer.classes.MessageRequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;

public class GameRouteHandler extends SimpleChannelInboundHandler<Object> { // (1)
	int timesVisited = 0;
	boolean channelAdded = false;
	Game game = null;
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) { // (2)
		
		timesVisited++;
		System.err.println("this connection has been visited " + timesVisited);
		/*if (!channelAdded) {
			App.channels.add(ctx.channel());
			ChannelFuture closed = ctx.channel().closeFuture();
			closed.addListener(new ChannelFutureListener() {
		      
				public void operationComplete(ChannelFuture arg0) throws Exception {
					// TODO Auto-generated method stub
					System.out.println("\n\n\n------------------CLOSED------------------\n\n");
				}});
			channelAdded = true;
		}*/

		if (!(msg instanceof HttpRequest)) {
			System.out.println("not http");
			return;
		}
		// tst
		HttpRequest request = (HttpRequest) msg;
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
		Map<String, java.util.List<String>> params = queryStringDecoder.parameters();
		try {
		if(params.get("type").get(0).equals("getMatch")) {
			String sessionID = params.get("sessionID").get(0);
			GameClaim game = App.claimClaim(sessionID.toString());
			if(game==null) {
				ctxWrite(ctx, "no match");
			}
			else {
				ctxWrite(ctx, game.gameID.toString()+" "+game.threadID.toString());
			}
		}
		if(params.get("type").get(0).equals("connect")&&game==null) {
			UUID sessionID = UUID.fromString(params.get("sessionID").get(0));
			UUID matchID = UUID.fromString(params.get("matchID").get(0));
			UUID threadID = UUID.fromString(params.get("threadID").get(0));
			ConnectionRequest req = new ConnectionRequest(ConnectionRequest.CONNECT, sessionID, matchID, ctx);
			game = App.getThread(threadID).getGame(matchID);
			game.addConnectionToQueue(req);
		}
		if(params.get("type").get(0).equals("message")&&game!=null) {
			UUID sessionID = UUID.fromString(params.get("sessionID").get(0));
			UUID matchID = UUID.fromString(params.get("matchID").get(0));
			String message = params.get("message").get(0);
			MessageRequest req = null;
			if(message.equals("word")) {
				String content = params.get("content").get(0);
				req = new MessageRequest(message,content, sessionID, matchID, ctx);
			}
			else {
				req = new MessageRequest(message, sessionID, matchID, ctx);
			}
			game.sendMessage(req);
			//TODO: undo this and welp
			//System.err.println("IT ACTUALLY WORKED");
		}else if(game==null) {
			//reconnect
			//System.err.println("WELP");
		}
		System.out.println(msg);
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		if (cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
			System.err.println("Timeout, connection closing");
		}
		cause.printStackTrace();
		System.err.println("closing" + cause.getClass());
		ctx.close();
	}
	void ctxWrite(ChannelHandlerContext ctx, String message) {
		ByteBuf bytes = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);
	}
	
}
