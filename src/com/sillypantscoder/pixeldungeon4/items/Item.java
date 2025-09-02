package com.sillypantscoder.pixeldungeon4.items;

import com.sillypantscoder.utils.JSONObject;

public class Item {
	public String id;
	public Item(String id) {
		this.id = id;
	}
	public JSONObject serialize() {
		JSONObject object = new JSONObject();
		object.setString("id", this.id);
		return object;
	}
}
