package com.sillypantscoder.pixeldungeon4;

import java.util.ArrayList;

import com.sillypantscoder.http.HttpResponse;
import com.sillypantscoder.http.HttpServer;
import com.sillypantscoder.utils.JSON;
import com.sillypantscoder.utils.Utils;

public class MainServer extends HttpServer.RequestHandler {
	public Game game;
	public MainServer() {
		this.game = new Game();
	}
	public HttpResponse get(String path) {
		if (path.equals("/")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/html").setBody(Utils.readFile("client/index.html"));
		if (path.equals("/style.css")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/css").setBody(Utils.readFile("client/style.css"));
		if (path.equals("/graphics.js")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/javascript").setBody(Utils.readFile("client/graphics.js"));
		if (path.equals("/zip.min.js")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/javascript; charset=utf-8").setBody(Utils.readFile("client/zip.min.js"));
		if (path.equals("/main.js")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/javascript").setBody(Utils.readFile("client/main.js"));
		if (path.startsWith("/get_messages/")) {
			String playerID = path.split("/")[2];
			if (! game.messages.containsKey(playerID)) return new HttpResponse().setStatus(400).setBody("That player is not logged in");
			game.doEntityTurns();
			ArrayList<String[]> messages = game.messages.get(playerID);
			game.messages.put(playerID, new ArrayList<String[]>());
			return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain").setBody(JSON.make2DStringList(messages).toString());
		}
		if (path.equals("/data.zip")) {
			byte[] zipData = Utils.zipFiles(game.getAllData());
			return new HttpResponse().setStatus(200).addHeader("Content-Type", "application/zip").setBody(zipData);
		}
		return new HttpResponse().setStatus(404).setBody("File not found");
	}
	public HttpResponse post(String path, String body) {
		if (path.equals("/login")) return new HttpResponse().setStatus(200).addHeader("Content-Type", "text/plain").setBody(game.loginPlayer());
		return new HttpResponse().setStatus(404).setBody("POST path not found");
	}
}
