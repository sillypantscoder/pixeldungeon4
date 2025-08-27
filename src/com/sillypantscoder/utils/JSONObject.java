package com.sillypantscoder.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class JSONObject {
	public static class Buffer {
		public String data;
		public int pos;
		public Buffer(String data) {
			this.data = data;
			this.pos = 0;
		}
		public char read() {
			this.pos += 1;
			return this.data.charAt(pos - 1);
		}
		public char nextChar() {
			return this.data.charAt(pos);
		}
		public void consumeWhitespace() {
			while (this.pos < this.data.length() && Character.isWhitespace(this.data.charAt(this.pos))) {
				this.pos += 1;
			}
		}
		public void consume(char c) {
			if (this.data.charAt(this.pos) == c) {
				this.pos += 1;
			} else {
				throw new RuntimeException("JSON Parsing: Expected a '" + c + "' character");
			}
		}
		public String toString() {
			return this.data.replace("\n", "↲").replace("\t", "⇥") + "\n" + "-".repeat(this.pos) + "^";
		}
	}
	// public HashMap<String, Void> entries_null = new HashMap<String, Void>();
	public HashMap<String, Boolean> entries_boolean = new HashMap<String, Boolean>();
	public HashMap<String, Double> entries_number = new HashMap<String, Double>();
	public HashMap<String, String> entries_string = new HashMap<String, String>();
	public HashMap<String, ArrayList<Object>> entries_array = new HashMap<String, ArrayList<Object>>();
	public HashMap<String, JSONObject> entries_object = new HashMap<String, JSONObject>();
	public JSONObject() {}
	public static JSONObject create(String jsonData) {
		JSONObject object = new JSONObject();
		object.read(new Buffer(jsonData));
		return object;
	}
	// READING METHODS
	public void readValue(String name, Buffer data) {
		if (data.nextChar() == '{') {
			JSONObject object = new JSONObject();
			object.read(data);
			this.entries_object.put(name, object);
		}
		else if (data.nextChar() == '[') readArray(name, data);
		else if (data.nextChar() == '\"') { this.entries_string.put(name, JSONObject.readString(data)); }
		else if ("0123456789-".contains(String.valueOf(data.nextChar()))) {
			String numberString = "" + data.read();
			while ("0123456789.".contains(String.valueOf(data.nextChar()))) {
				numberString += data.read();
			}
			double number = Double.valueOf(numberString);
			this.entries_number.put(name, number);
		}
		else if (data.nextChar() == 't') {
			data.consume('t'); data.consume('r'); data.consume('u'); data.consume('e');
			this.entries_boolean.put(name, true);
		}
		else if (data.nextChar() == 'f') {
			data.consume('f'); data.consume('a'); data.consume('l'); data.consume('s'); data.consume('e');
			this.entries_boolean.put(name, false);
		}
		else if (data.nextChar() == 'n') {
			data.consume('n'); data.consume('u'); data.consume('l'); data.consume('l');
			// this.entries_null.put(name, null);
		}
		else throw new RuntimeException("Unknown value type! Next char: '" + data.nextChar() + "', Data:\n" + data.toString());
	}
	public static String readString(Buffer b) {
		b.consume('\"');
		String data = "";
		while (true) {
			char c = b.read();
			if (c == '\\') {
				char d = b.read();
				if (d == 'n') data += "\n";
				else if (d == '\\') data += "\\";
				else if (d == '"') data += "\"";
			} else if (c == '"') {
				return data;
			} else {
				data += c;
			}
		}
	}
	public void read(Buffer b) {
		b.consume('{');
		while (true) {
			b.consumeWhitespace();
			// Get key
			String key = JSONObject.readString(b);
			b.consumeWhitespace();
			b.consume(':');
			b.consumeWhitespace();
			// Get value
			readValue(key, b);
			b.consumeWhitespace();
			// Next item
			if (b.nextChar() == '}') {
				b.read();
				break;
			}
			b.consume(',');
			b.consumeWhitespace();
		}
	}
	public void readArray(String name, Buffer b) {
		b.consume('[');
		ArrayList<Object> l = new ArrayList<Object>();
		b.consumeWhitespace();
		if (b.nextChar() == ']') {
			// Empty array
			b.read();
			this.entries_array.put(name, l);
			return;
		}
		while (true) {
			b.consumeWhitespace();
			JSONObject objectHolder = new JSONObject();
			objectHolder.readValue("V", b);
			l.add(objectHolder.get("V"));
			b.consumeWhitespace();
			if (b.nextChar() == ']') {
				b.read();
				break;
			}
			b.consume(',');
			b.consumeWhitespace();
		}
		this.entries_array.put(name, l);
	}
	// WRITING METHODS
	public String write() {
		StringBuilder out = new StringBuilder();
		out.append("{");
		// Helper functions
		Consumer<String> appendString = (s) -> {
			out.append("\"");
			out.append(s.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t").replace("\"", "\\\""));
			out.append("\"");
		};
		AtomicBoolean firstKey = new AtomicBoolean(true);
		Consumer<String> appendKeyValue = (key) -> {
			if (firstKey.get()) firstKey.set(false);
			else out.append(",");
			appendString.accept(key);
			out.append(":");
		};
		// Add entries
		for (String key : this.entries_boolean.keySet()) {
			appendKeyValue.accept(key);
			out.append(this.entries_boolean.get(key));
		}
		for (String key : this.entries_number.keySet()) {
			appendKeyValue.accept(key);
			DecimalFormat format = new DecimalFormat();
			format.setGroupingUsed(false);
			out.append(format.format(this.entries_number.get(key)));
		}
		for (String key : this.entries_string.keySet()) {
			appendKeyValue.accept(key);
			appendString.accept(this.entries_string.get(key));
		}
		for (String key : this.entries_array.keySet()) {
			appendKeyValue.accept(key);
			ArrayList<?> array = this.entries_array.get(key);
			out.append("[");
			boolean firstElement = true;
			for (Object o : array) {
				if (firstElement) firstElement = false;
				else out.append(",");
				// add object
				JSONObject objectHolder = new JSONObject();
				objectHolder.set("V", o);
				String objectString = objectHolder.write();
				objectString = objectString.substring(5, objectString.length() - 1);
				out.append(objectString);
			}
			out.append("]");
		}
		for (String key : this.entries_object.keySet()) {
			appendKeyValue.accept(key);
			out.append(this.entries_object.get(key));
		}
		// Finish!
		out.append("}");
		return out.toString();
	}
	public String toString() { return this.write(); }
	// GETTING METHODS
	public Object get(String name) {
		// if (this.entries_null.keySet().contains(name)) return this.entries_null.get(name);
		if (this.entries_boolean.keySet().contains(name)) return this.entries_boolean.get(name);
		if (this.entries_number.keySet().contains(name)) return this.entries_number.get(name);
		if (this.entries_string.keySet().contains(name)) return this.entries_string.get(name);
		if (this.entries_array.keySet().contains(name)) return this.entries_array.get(name);
		if (this.entries_object.keySet().contains(name)) return this.entries_object.get(name);
		throw new RuntimeException("Item not found: " + name);
	}
	public boolean getBoolean(String name) { return this.entries_boolean.get(name); }
	public double getNumber(String name) { return this.entries_number.get(name); }
	public String getString(String name) { return this.entries_string.get(name); }
	public ArrayList<?> getArray(String name) { return this.entries_array.get(name); }
	public JSONObject getObject(String name) { return this.entries_object.get(name); }
	// SETTINGS METHODS
	public void set(String name, Object value) {
		if (value instanceof Boolean vboolean) this.entries_boolean.put(name, vboolean);
		else if (value instanceof Number vnumber) this.entries_number.put(name, vnumber.doubleValue());
		else if (value instanceof String vstring) this.entries_string.put(name, vstring);
		else if (value instanceof List<? extends Object> varray) this.entries_array.put(name, new ArrayList<Object>(varray));
		else if (value instanceof JSONObject vobject) this.entries_object.put(name, vobject);
		else throw new IllegalArgumentException("Object is of an incompatible type: " + value.toString());
	}
	public void setBoolean(String name, boolean value) { this.entries_boolean.put(name, value); }
	public void setNumber(String name, double value) { this.entries_number.put(name, value); }
	public void setString(String name, String value) { this.entries_string.put(name, value); }
	public void setArray(String name, ArrayList<Object> value) { this.entries_array.put(name, value); }
	public void setObject(String name, JSONObject value) { this.entries_object.put(name, value); }
	// HELPER FUNCTIONS
	public static String encode2DList(List<? extends List<String>> list) {
		JSONObject obj = new JSONObject();
		obj.set("V", list);
		String data = obj.write();
		return data.substring(5, data.length() - 1);
	}
}
