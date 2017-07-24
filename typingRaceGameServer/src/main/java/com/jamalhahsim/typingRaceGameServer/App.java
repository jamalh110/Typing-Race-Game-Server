package com.jamalhahsim.typingRaceGameServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;



import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Hello world!
 *
 */
public class App {
	/**
	 * 
	 * Things to Clean: active channels thread number of threads in boss and worker
	 * groups channel closed alert connection visited variable
	 *
	 */
	final static int PORT = 8080;
	static boolean SSL = false;
	// for tracking active connections
	static CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<Channel>();

	public static void main(String[] args) {
		
		Thread matchMaker = new Thread();
		
		
		// change as necessary, probably keep 1 bossGroup
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(10);
		try {
			//for no ssl
			ServerBootstrap b1 = new ServerBootstrap();
			b1.option(ChannelOption.SO_BACKLOG, 1024);
			b1.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new GameInitializer());
			b1.option(ChannelOption.TCP_NODELAY, true);
			b1.option(ChannelOption.SO_KEEPALIVE, false);
			Channel ch = b1.bind(PORT).sync().channel();

			
			//for ssl
			ServerBootstrap b2 = new ServerBootstrap();
			b2.option(ChannelOption.SO_BACKLOG, 1024);
			b2.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new LoginServerInitializer());
			b2.option(ChannelOption.TCP_NODELAY, true);
			b2.option(ChannelOption.SO_KEEPALIVE, false);
			Channel ch2 = b2.bind(PORT).sync().channel();
			// thread for tracking active channels
			
			ch.closeFuture().sync();
			ch2.closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	// Thread thread = new Thread(new Runnable() {
	//
	// public void run() {
	// String FILENAME = "C:\\javalogs\\netty.txt";
	// PrintWriter bw = null;
	// try {
	// //bw = new BufferedWriter(new FileWriter(FILENAME));
	// bw = new PrintWriter("netty.txt");
	// } catch (IOException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// while(true) {
	// //System.out.print(channels.size());
	// int active = 0;
	// for(int i = 0;i<channels.size();i++) {
	// if(channels.get(i).isActive()) {
	// active++;
	// }
	// }
	// try {
	// bw.println(channels.size()+"<-Channels Active->"+active);
	// bw.flush();
	// } catch (Exception e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	// });
	// thread.start();
}
