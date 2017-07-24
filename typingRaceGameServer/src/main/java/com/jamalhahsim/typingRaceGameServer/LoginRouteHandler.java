package com.jamalhahsim.typingRaceGameServer;

import java.util.Map;

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

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) { // (2)
		timesVisited++;
		System.err.println("this connection has been visited " + timesVisited);
		
		if (!(msg instanceof HttpRequest)) {
			System.out.println("not http");
			//ByteBuf bytes = Unpooled.copiedBuffer("test2", CharsetUtil.UTF_8);
			//ctx.write(bytes);
			return;
		}
		
		HttpRequest request = (HttpRequest) msg;
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
		Map<String, java.util.List<String>> params = queryStringDecoder.parameters();
		try {
			System.out.println("message: " + params.get("message").get(0));
		} catch (Exception e) {

			System.out.println("no message");
		}
		ByteBuf bytes = Unpooled.copiedBuffer("test2", CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		System.out.println(msg);

		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);

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
	
}
