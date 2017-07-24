package com.jamalhahsim.typingRaceGameServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class GameInitializer extends ChannelInitializer<SocketChannel> {
	  
	      private final SslContext sslCtx;
	  
	      public GameInitializer(SslContext sslCtx) {
	          this.sslCtx = sslCtx;
	      }
	      public GameInitializer() {
	    	  this.sslCtx = null;
	      }
	      @Override
	      public void initChannel(SocketChannel ch) {
	          ChannelPipeline p = ch.pipeline();
	          if (sslCtx != null) {
	              p.addLast(sslCtx.newHandler(ch.alloc()));
	          }
	          System.out.println("read");
	      
	          //p.addLast(new HttpRequestDecoder());
	          // Uncomment the following line if you don't want to handle HttpChunks.
	          p.addLast("timeout",new ReadTimeoutHandler(30));
	         
	          p.addLast("codec",new HttpServerCodec());
	          p.addLast("aggregator",new HttpObjectAggregator(1048576));
	        
	          p.addLast("handler",new GameRouteHandler());
	      }
	  }