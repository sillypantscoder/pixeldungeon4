package com.sillypantscoder.pixeldungeon4;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.entities.Player;

public class PlayerData {
	public Player player;
	public ArrayList<String[]> messages;
	public boolean hasWon;
	public PlayerData(Player player) {
		this.player = player;
		this.messages = new ArrayList<String[]>();
		player.sendMessage = this.messages::add;
		this.hasWon = false;
	}
	public void sendMessage(String[] message) {
		this.messages.add(message);
	}
	public ArrayList<String[]> getMessages() {
		return new ArrayList<String[]>(this.messages);
	}
	public void clearMessages() {
		this.messages.clear();
	}
}
