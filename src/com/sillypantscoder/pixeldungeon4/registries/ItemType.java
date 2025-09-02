package com.sillypantscoder.pixeldungeon4.registries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Utils;

public class ItemType {
	public String name;
	public String description;
	public ArrayList<ItemButton> buttons;
	public ItemType(String name, String description, ArrayList<ItemButton> buttons) {
		this.name = name;
		this.description = description;
		this.buttons = buttons;
	}
	public static HashMap<String, ItemType> allItemTypes = getAllItemTypes();
	public static HashMap<String, ItemType> getAllItemTypes() {
		HashMap<String, ItemType> types = new HashMap<String, ItemType>();
		for (String name : new File("data/definitions/item").list()) {
			JSONObject object = JSONObject.create(Utils.readFile("data/definitions/item/" + name));
			// Unpack JSON object
			// - Name / Description
			String itemName = object.getString("name");
			String description = object.getString("description");
			// - Buttons
			ArrayList<ItemButton> buttons = new ArrayList<ItemButton>();
			for (Object o : object.getArray("buttons")) {
				if (o instanceof JSONObject cmd) {
					buttons.add(ItemButton.create(cmd));
				} else {
					throw new RuntimeException("Item button definition is of the wrong type: " + o.toString());
				}
			}
			// Assemble ItemType object
			ItemType type = new ItemType(itemName, description, buttons);
			types.put(name.split("\\.")[0], type);
		}
		return types;
	}
	public static record ItemButton(String name, ArrayList<JSONObject> actions) {
		public static ItemButton create(JSONObject object) {
			ArrayList<JSONObject> actions = new ArrayList<JSONObject>();
			for (Object data : object.getArray("actions")) {
				if (data instanceof JSONObject jsonData) {
					actions.add(jsonData);
				} else throw new RuntimeException("Item button action object is of the wrong type");
			}
			return new ItemButton(object.getString("name"), actions);
		}
	}
}
