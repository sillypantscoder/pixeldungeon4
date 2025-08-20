package com.sillypantscoder.pixeldungeon4.actions;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;

public abstract class Action<T extends Entity> {
	public T entity;
	public int time;
	public Action(T target, int time) {
		this.entity = target;
		this.time = time;
	}
	public abstract void execute(Game game);
	public static Action<?> create(Entity e, String actionName, double timeScale, int extraTime) {
		Action<?> action = null;
		if (actionName.equals("wait")) {
			action = new WaitAction(e);
		}
		if (action == null) throw new IllegalArgumentException("Unknown action name: " + actionName);
		// System.out.print("(" + action.time + " * " + timeScale + ") + " + extraTime);
		action.time = (int)(action.time * timeScale) + extraTime;
		// System.out.println(" => " + action.time);
		return action;
	}
	public static class WaitAction extends Action<Entity> {
		public WaitAction(Entity target) {
			super(target, 1);
		}
		public void execute(Game game) {
			// Update time
			this.entity.time += this.time;
			// Send idle animation to clients
			if (this.entity instanceof TileEntity tileEntity) {
				for (String playerID : game.messages.keySet()) {
					Player player = game.getPlayerByID(playerID);
					if (player == tileEntity || game.level.isLocVisible(player.x, player.y, tileEntity.x, tileEntity.y)) {
						// Set animation
						game.messages.get(playerID).add(new String[] {
							"set_animation",
							String.valueOf(tileEntity.id),
							"idle"
						});
					}
				}
			}
		}
	}
	public static class MoveAction extends Action<TileEntity> {
		public int targetX;
		public int targetY;
		public MoveAction(TileEntity target, int targetX, int targetY) {
			super(target, 1);
			this.targetX = targetX;
			this.targetY = targetY;
		}
		public void execute(Game game) {
			// Validate
			if (this.targetX < 0 || this.targetY < 0 || this.targetX >= game.level.tiles.length || this.targetY >= game.level.tiles[0].length) {
				throw new RuntimeException("Can't move to a location out of bounds! Entity: " + this.entity + ", TargetX: " + this.targetX + ", TargetY: " + this.targetY);
			}
			// Save previous position
			int previousX = this.entity.x;
			int previousY = this.entity.y;
			// Entity position
			this.entity.x = this.targetX;
			this.entity.y = this.targetY;
			// Update time
			this.entity.time += this.time;
			// Inform clients about this update
			for (String playerID : game.messages.keySet()) {
				Player player = game.getPlayerByID(playerID);
				if (game.level.isLocVisible(player.x, player.y, previousX, previousY) || game.level.isLocVisible(player.x, player.y, this.targetX, this.targetY) || player == this.entity) {
					// Create entity if it does not exist
					if (! player.visibleEntities.contains(this.entity)) {
						player.visibleEntities.add(this.entity);
						String[] data = new String[] {
							"create_entity",
							this.entity.serialize().toString()
						};
						game.messages.get(playerID).add(data);
					}
					// Move entity
					game.messages.get(playerID).add(new String[] {
						"move_entity",
						String.valueOf(this.entity.id),
						String.valueOf(this.entity.x),
						String.valueOf(this.entity.y)
					});
					// Set animation
					game.messages.get(playerID).add(new String[] {
						"set_animation",
						String.valueOf(this.entity.id),
						"move"
					});
				} else {
					// Remove entity if it exists and is no longer visible
					if (player.visibleEntities.contains(this.entity)) {
						player.visibleEntities.remove(this.entity);
						String[] data = new String[] {
							"remove_entity",
							this.entity.id + ""
						};
						player.sendMessage.accept(data);
					}
				}
			}
			// TODO: Split all this player-specific stuff into a method (e.g. TileEntity.afterMove(Game))
			if (this.entity instanceof Player targetPlayer) {
				if (targetPlayer.target.map((v) -> (targetPlayer.x == v.getX()) && (targetPlayer.y == v.getY())).orElse(false)) {
					// Clear player target
					targetPlayer.sendMessage.accept(new String[] { "clear_target" });
					targetPlayer.sendMessage.accept(new String[] { "set_animation", String.valueOf(targetPlayer.id), "idle" });
					targetPlayer.target = Optional.empty();
				}
				targetPlayer.sendVision(game.level);
			}
		}
	}
}
