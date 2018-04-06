package com.jamalhashim.typingRaceGameServer.data;

import java.util.Random;

public class Passages {
	static Random g = new Random();
	static String[] passages = {"This is a test passage for the game server of typing race. Will it work?", 
			"This is test passage number two. What is a good length for these?",
			"This is test passage number three. hEre are SoME RandOM CAPitalIzations to Be AnnOying."};
	public static String getPassage() {
		
		return passages[g.nextInt(passages.length)];
	}
}
