package com.sillypantscoder.pixeldungeon4;

import java.util.Optional;

import com.sillypantscoder.http.HttpResponse;
import com.sillypantscoder.http.HttpServer;
import com.sillypantscoder.utils.Utils;

public class MainServer extends HttpServer.RequestHandler {
	public Game game;
	public MainServer() {
		this.game = new Game();
	}
	public HttpResponse get(String path) {
		if (path.equals("/")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/html").setBody(Utils.readFile("client/index.html"));
		if (path.equals("/main.js")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/javascript").setBody(Utils.readFile("client/main.js"));
		if (path.startsWith("/get_message/")) {
			String playerID = path.split("/")[2];
			if (! game.messages.containsKey(playerID)) return new HttpResponse().setStatus(400).setBody("That player is not logged in");
			Optional<String> message = game.getPlayerMessage(playerID);
			HttpResponse res = new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain");
			message.ifPresentOrElse((v) -> {
				res.setBody(v);
			}, () -> {
				res.setBody("");
			});
			return res;
		}
		return new HttpResponse().setStatus(404).setBody("File not found");
	}
	public HttpResponse post(String path, String body) {
		if (path.equals("/login")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain").setBody(game.loginPlayer());
		return new HttpResponse().setStatus(404).setBody("POST path not found");
	}
}
