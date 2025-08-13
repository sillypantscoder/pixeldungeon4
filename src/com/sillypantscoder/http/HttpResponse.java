package com.sillypantscoder.http;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class HttpResponse {
	public int status;
	public byte[] body;
	public ArrayList<String> headerNames;
	public ArrayList<String> headerValues;
	public HttpResponse() {
		status = 200;
		body = new byte[] {};
		headerNames = new ArrayList<String>();
		headerValues = new ArrayList<String>();
	}
	public HttpResponse setStatus(int newStatus) {
		status = newStatus;
		return this;
	}
	public HttpResponse setBody(byte[] newBody) {
		body = newBody;
		return this;
	}
	public HttpResponse setBody(String newBody) {
		body = newBody.getBytes(StandardCharsets.UTF_8);
		return this;
	}
	public String bodyString() {
		return new String(body, StandardCharsets.UTF_8);
	}
	public HttpResponse addHeader(String name, String value) {
		for (int i = 0; i < headerNames.size(); i++) {
			if (headerNames.get(i) == name) {
				headerValues.set(i, value);
				return this;
			}
		}
		headerNames.add(name);
		headerValues.add(value);
		return this;
	}
	public void send(HttpExchange exchange) throws IOException {
		OutputStream outputStream = exchange.getResponseBody();
		// Set headers
		Headers h = exchange.getResponseHeaders();
		for (int i = 0; i < headerNames.size(); i++) {
			h.add(headerNames.get(i), headerValues.get(i));
		}
		// Send headers then data
		exchange.sendResponseHeaders(status, body.length);
		outputStream.write(body);
		// Finish
		outputStream.flush();
		outputStream.close();
	}
	public void printInfo() {
		System.out.println("[HTTP RESPONSE : " + status + "]\n(" + body.length + ")>>>" + body + "<<<");
	}
	@Override
	public String toString() {
		return "[HTTP RESPONSE : " + status + "]\n(" + body.length + ")>>>" + body + "<<<";
	}
	public static HttpResponse error(String message) {
		return error(400, message);
	}
	public static HttpResponse error(int statusCode, String message) {
		return new HttpResponse().setStatus(statusCode).setBody(message);
	}
}
