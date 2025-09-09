package com.sillypantscoder.pixeldungeon4.items;

import java.util.ArrayList;

import com.sillypantscoder.pixeldungeon4.registries.ItemType;
import com.sillypantscoder.utils.JSONObject;

public class Item {
	public String id;
	public Item(String id) {
		this.id = id;
	}
	public Item duplicate() {
		return new Item(this.id);
	}
	public JSONObject serialize() {
		JSONObject object = new JSONObject();
		object.setString("id", this.id);
		object.setArray("buttons", this.getButtons().stream().map((v) -> {
			JSONObject btn = new JSONObject();
			btn.setString("name", v.name());
			return btn;
		}).toList());
		return object;
	}
	public ArrayList<ItemButton> getButtons() {
		ArrayList<ItemButton> buttons = new ArrayList<ItemButton>();
		// Drop
		{
			ArrayList<JSONObject> drop_actions = new ArrayList<JSONObject>();
			{
				JSONObject a = new JSONObject();
				a.setString("type", "consume-item");
				drop_actions.add(a);
			}
			{
				JSONObject a = new JSONObject();
				a.setString("type", "drop-item");
				drop_actions.add(a);
			}
			{
				JSONObject a = new JSONObject();
				a.setString("type", "change-time");
				a.setNumber("amount", 6);
				drop_actions.add(a);
			}
			buttons.add(new ItemButton("Drop", drop_actions));
		}
		// Additional buttons
		ArrayList<ItemButton> customButtons = ItemType.allItemTypes.get(this.id).buttons;
		for (int i = 0; i < customButtons.size(); i++) {
			buttons.add(customButtons.get(i));
		}
		// Finish
		return buttons;
	}
	public ItemButton getButton(int buttonIndex) {
		return this.getButtons().get(buttonIndex);
	}
}
