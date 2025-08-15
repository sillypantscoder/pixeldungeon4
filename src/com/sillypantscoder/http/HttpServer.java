package com.sillypantscoder.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HttpServer {
	public HttpServer(int port_number, RequestHandler handler) {
		try {
			InetSocketAddress addr = new InetSocketAddress("127.0.0.1", port_number);
			com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(addr, 0);
			server.createContext("/", new ProxyHttpHandler(handler));
			server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(12));
			server.start();
			System.out.println("Server started at: http://" + addr.getHostName() + ":" + addr.getPort() + "/");
		} catch (IOException e) {
			System.out.println("Server failed to start!!!!!");
			e.printStackTrace();
		}
	}
	public static abstract class RequestHandler {
		public abstract HttpResponse get(String path);
		public abstract HttpResponse post(String path, String body);
	}
	public static class ProxyHttpHandler implements HttpHandler {
		public RequestHandler handler;
		public ProxyHttpHandler(RequestHandler handler) {
			super();
			this.handler = handler;
		}
		@Override
		public void handle(HttpExchange httpExchange) {
			try {
				if (httpExchange.getRequestMethod().equals("GET")) {
					handleGetRequest(httpExchange);
				}
				if (httpExchange.getRequestMethod().equals("POST")) {
					handlePostRequest(httpExchange);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				HttpResponse response = new HttpResponse().setStatus(500).addHeader("Content-Type", "text/plain").setBody("The server has crashed! Please see the log messages for details.");
				try {
					response.send(httpExchange);
				} catch (IOException e2) {
					System.err.println("Error occurred while sending 500 message to client:");
					e2.printStackTrace();
				}
			}
		}
		private void handleGetRequest(HttpExchange httpExchange) throws IOException {
			String path = httpExchange.getRequestURI().toString();
			HttpResponse response = handler.get(path);
			response.send(httpExchange);
		}
		private void handlePostRequest(HttpExchange httpExchange) throws IOException {
			String path = httpExchange.getRequestURI().toString();
			byte[] body = httpExchange.getRequestBody().readAllBytes();
			String bodys = new String(body, StandardCharsets.UTF_8);
			HttpResponse response = handler.post(path, bodys);
			response.send(httpExchange);
		}
	}
}
