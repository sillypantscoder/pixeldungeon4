package com.sillypantscoder.pixeldungeon4.items;

import java.util.ArrayList;
import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.DroppedItem;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.pixeldungeon4.entitydef.NumberProvider;
import com.sillypantscoder.utils.JSONObject;

public record ItemButton(String name, ArrayList<JSONObject> actions) {
	public static ItemButton create(JSONObject object) {
		ArrayList<JSONObject> actions = new ArrayList<JSONObject>();
		for (Object data : object.getArray("actions")) {
			if (data instanceof JSONObject jsonData) {
				actions.add(jsonData);
			} else throw new RuntimeException("Item button action object is of the wrong type");
		}
		return new ItemButton(object.getString("name"), actions);
	}
	public static void doAction(Game game, Player player, int inventoryIndex, Item item, JSONObject action) {
		String actionType = action.getString("type");
		if (actionType.equals("consume-item")) {
			if (inventoryIndex == -1) {
				player.mainHand = Optional.empty();
			} else {
				player.inventory.remove(item);
			}
			player.sendInventory();
		} else if (actionType.equals("drop-item")) {
			DroppedItem droppedItem = new DroppedItem(game.level.getNewEntityTime(), player.x, player.y, item.duplicate());
			game.addFreshEntity(droppedItem);
		} else if (actionType.equals("change-time")) {
			int time = (int)(NumberProvider.create(action, "amount").get(null));
			player.time += time;
		} else if (actionType.equals("change-health")) {
			int health = (int)(NumberProvider.create(action, "amount").get(null));
			player.health += health;
			if (player.health > player.maxHealth) player.health = player.maxHealth;
			game.updateEntityHealth(player);
		} else if (actionType.equals("set-animation")) {
			String animationName = action.getString("animation");
			player.sendMessage.accept(new String[] {
				"set_animation", "" + player.id, animationName
			});
		} else {
			System.err.println("Unknown action type: '" + actionType);
		}
	}
	public void click(Game game, Player player, int inventoryIndex) {
		Item item = inventoryIndex == -1 ? player.mainHand.orElseThrow() : player.inventory.get(inventoryIndex);
		for (JSONObject action : this.actions) {
			ItemButton.doAction(game, player, inventoryIndex, item, action);
		}
	}
}
