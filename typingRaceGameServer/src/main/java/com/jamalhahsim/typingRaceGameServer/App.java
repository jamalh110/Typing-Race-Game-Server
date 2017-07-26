package com.jamalhahsim.typingRaceGameServer;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.jamalhashim.typingRaceGameServer.classes.GameThread;
import com.jamalhashim.typingRaceGameServer.classes.MatchMakerThread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class App {
	/**
	 * 
	 * Things to Clean: active channels thread number of threads in boss and worker
	 * groups channel closed alert connection visited variable
	 * 
	 * The request.getURI depreciation, change it to something not depreciated
	 * fix the findthread method and actually do the logic instead of returning the first one
	 * handle nulls and wrong requests or out of order requests from the client. For example if they
	 * try and connect when they already have or send a message if they havent connected
	 * 
	 * ADD A READY REQUEST that checks if both players have connected, and sends a countdown start time
	 */
	final static int GAME_PORT = 8080;
	final static int LOGIN_PORT = 8081;
	static boolean SSL = false;
	// for tracking active connections
	static CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<Channel>();
	static HashMap<UUID, GameThread> gameThreads = new HashMap<UUID, GameThread>();
	static MatchMakerThread matchMaker = null;
	public static void main(String[] args) {
		
		matchMaker = new MatchMakerThread();
		matchMaker.start();
		UUID u1 = UUID.randomUUID();
		GameThread g1 = new GameThread(u1);
		gameThreads.put(u1, g1);
		g1.start();
		// change as necessary, probably keep 1 bossGroup
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(10);
		try {
			//for client communication
			ServerBootstrap b1 = new ServerBootstrap();
			b1.option(ChannelOption.SO_BACKLOG, 1024);
			b1.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new GameInitializer());
			b1.option(ChannelOption.TCP_NODELAY, true);
			b1.option(ChannelOption.SO_KEEPALIVE, false);
			Channel ch = b1.bind(GAME_PORT).sync().channel();

			
			//for login server communication
			ServerBootstrap b2 = new ServerBootstrap();
			b2.option(ChannelOption.SO_BACKLOG, 1024);
			b2.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new LoginServerInitializer());
			b2.option(ChannelOption.TCP_NODELAY, true);
			b2.option(ChannelOption.SO_KEEPALIVE, false);
			Channel ch2 = b2.bind(LOGIN_PORT).sync().channel();
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
	/**
	 * get a specific game thread based on its id
	 * @param threadID the id of the thread
	 * @return
	 */
	public static GameThread getThread(UUID threadID) {
		return gameThreads.get(threadID);
	}
	/**
	 * returns the thread with the least amount of games
	 */
	public static GameThread findThread() {
		for(GameThread g:gameThreads.values()) {
			return g;
		}
		return null;
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
