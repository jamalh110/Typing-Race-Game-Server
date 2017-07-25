package com.jamalhashim.typingRaceGameServer.classes;

import java.util.Date;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class MatchRequest {
	//ChannelHandlerContext ctx;
	public UUID sessionID;
	public String username;
	boolean validated = true;
	Date created = null;
	boolean matchMade = false;
	UUID threadID=null;
	UUID matchID=null;
	String closeReason = "";
	public MatchRequest(UUID sessionID, String username) {
		//this.ctx=ctx;
		this.sessionID = sessionID;
		this.username = username;
		created = new Date();
	}
	public void invalidate() {
		validated = false;
	}
	public boolean isValidated() {
		return validated;
	}
	public void setReason(String reason) {
		this.closeReason = reason;
	}
	public String closeReason() {
		return this.closeReason;
	}
	public void fill(UUID threadID,UUID matchID) {
		this.threadID=threadID;
		this.matchID = matchID;
		this.matchMade = true;
	}
	public boolean matchMade() {
		return this.matchMade;
	}
	public boolean writeResponse(ChannelHandlerContext ctx) throws Exception{
		ByteBuf bytes = Unpooled.copiedBuffer("Thread ID: "+threadID+"\nMatch ID: "+matchID, CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);
		return true;
	}
	/*public boolean close() {
		ByteBuf bytes = Unpooled.copiedBuffer("timeout", CharsetUtil.UTF_8);
		DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		HttpUtil.setContentLength(resp, bytes.readableBytes());
		ctx.writeAndFlush(resp);
		return true;
	}*/
}
