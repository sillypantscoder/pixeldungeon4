package com.sillypantscoder.pixeldungeon4;

import com.sillypantscoder.http.HttpServer;

public class Main {
	public static void main(String[] args) {
		MainServer request_handler = new MainServer();
		new HttpServer(9379, request_handler);
	}
}
