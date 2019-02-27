package com.jamalhahsim.typingRaceGameServer;

import java.util.Map;
import java.util.UUID;

import com.jamalhashim.typingRaceGameServer.classes.MatchRequest;

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

public class LoginRouteHandler extends SimpleChannelInboundHandler<Object> { // (1)
	int timesVisited = 0;
	boolean channelAdded = false;
	MatchRequest req = null;

	// types: match, exit;
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) { // (2)
		timesVisited++;
		System.err.println("this connection has been visited " + timesVisited);
		if (!(msg instanceof HttpRequest)) {
			System.out.println("not http");
			return;
		}

		HttpRequest request = (HttpRequest) msg;
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
		Map<String, java.util.List<String>> params = queryStringDecoder.parameters();
		String type = "";
		System.out.println(msg);

		try {
			System.out.println("type: " + params.get("type").get(0));
			type = params.get("type").get(0);

		} catch (Exception e) {
			System.out.println("Incorrect message format");
			return;
		}
		/*if (type.equals("query")) {
			if (req != null) {
				if (req.matchMade()) {
					try {
						req.writeResponse(ctx);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (!req.isValidated()) {
					ctxWrite(ctx, "match failed. Reason: " + req.closeReason());
				} else {
					ctxWrite(ctx, "searching");
				}
			} else {
				ctxWrite(ctx, "failed. Request is null");
			}
		}*/
		if (req != null) {
			if (!req.isValidated()) {
				req = null;
			}
		}
		if (type.equals("match")) {
			handleMatch(ctx, params);
			
		}
		if (type.equals("exit")) {
			req.invalidate();
			ByteBuf bytes = Unpooled.copiedBuffer("success", CharsetUtil.UTF_8);
			DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					bytes);
			resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
			HttpUtil.setContentLength(resp, bytes.readableBytes());
			ctx.writeAndFlush(resp);
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
		if (req != null) {
			req.invalidate();
		}
		ctx.close();
	}

	public void handleMatch(ChannelHandlerContext ctx, Map<String, java.util.List<String>> params) {
		UUID sessionID = null;
		String username = "";
		try {
			System.out.println("sessionID: " + params.get("sessionID").get(0));
			sessionID = UUID.fromString(params.get("sessionID").get(0));
			System.out.println("username: " + params.get("username").get(0));
			username = params.get("username").get(0);
		} catch (Exception e) {
			System.out.println("Incorrect message format");
			return;
		}

		/*if (req != null && req.isValidated()) {
			System.out.println("has match already");
			ByteBuf bytes = Unpooled.copiedBuffer("already has match", CharsetUtil.UTF_8);
			DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					bytes);
			resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
			HttpUtil.setContentLength(resp, bytes.readableBytes());
			ctx.writeAndFlush(resp);
			return;
		}*/
		req = new MatchRequest(sessionID, username);
		App.matchMaker.submitRequest(req);

		/*ByteBuf bytes = Unpooled.copiedBuffer("success", CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);*/
		ctxWrite(ctx, "success");
		ctx.close();
	}

	public void ctxWrite(ChannelHandlerContext ctx, String message) {
		ByteBuf bytes = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);
	}

}
