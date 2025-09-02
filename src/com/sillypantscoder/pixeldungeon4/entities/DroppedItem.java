package com.sillypantscoder.pixeldungeon4.entities;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.actions.WaitAction;
import com.sillypantscoder.pixeldungeon4.items.Item;
import com.sillypantscoder.pixeldungeon4.level.Level;
import com.sillypantscoder.utils.JSONObject;

public class DroppedItem extends TileEntity {
	public Item item;
	public DroppedItem(int time, int x, int y, Item item) {
		super(time, x, y);
		this.item = item;
	}
	public String getTypeID() { return "item"; }
	public JSONObject serialize(boolean allDetails) {
		JSONObject obj = super.serialize(allDetails);
		obj.setObject("item", item.serialize());
		return obj;
	}
	public Optional<Action<?>> getAction(Level level) {
		return Optional.of(new WaitAction(this));
	}
	public Optional<Action<?>> collect(LivingEntity entity) {
		return Optional.of(new PickupItem(entity, this));
	}
	public static class PickupItem extends Action<LivingEntity> {
		public DroppedItem item;
		public PickupItem(LivingEntity target, DroppedItem item) {
			super(target, 16);
			this.item = item;
		}
		public void execute(Game game) {
			// Remove item entity
			for (Player player : game.allPlayers()) {
				if (game.level.isLocVisible(player.x, player.y, this.item.x, this.item.y)) {
					// Send death
					player.visibleEntities.remove(this.item);
					player.sendMessage.accept(new String[] {
						"remove_entity",
						String.valueOf(this.item.id)
					});
				}
			}
			game.level.entities.remove(this.item);
			// Add to inventory
			if (this.entity instanceof Player targetPlayer) {
				targetPlayer.inventory.add(this.item.item);
				targetPlayer.sendInventory();
			} else {
				// Drop previous hand item
				this.entity.mainHand.ifPresent((v) -> {
					DroppedItem fromHand = new DroppedItem(game.level.getNewEntityTime(), this.entity.x, this.entity.y, v);
					game.addFreshEntity(fromHand);
				});
				// Set new hand item.
				this.entity.mainHand = Optional.of(this.item.item);
			}
			// Send animation to clients
			for (Player player : game.allPlayers()) {
				if (game.level.isLocVisible(player.x, player.y, this.entity.x, this.entity.y)) {
					// Animation
					player.sendMessage.accept(new String[] {
						"set_animation",
						String.valueOf(this.entity.id),
						"action"
					});
				}
			}
			// Increase time
			this.entity.time += this.time;
		}
	}
}
