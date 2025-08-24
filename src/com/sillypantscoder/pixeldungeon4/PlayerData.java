package com.sillypantscoder.pixeldungeon4;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.entities.Player;

public record PlayerData(ArrayList<String[]> messages, Player player) {
	public PlayerData(Player player) {
		this(new ArrayList<String[]>(), player);
		player.sendMessage = this.messages::add;
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
