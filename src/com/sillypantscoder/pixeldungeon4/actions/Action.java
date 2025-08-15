package com.sillypantscoder.pixeldungeon4.actions;

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
	public static class MoveAction extends Action<TileEntity> {
		public int targetX;
		public int targetY;
		public MoveAction(TileEntity target, int time, int targetX, int targetY) {
			super(target, time);
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
			// Inform clients about this update
			for (String playerID : game.messages.keySet()) {
				Player player = game.getPlayerByID(playerID);
				if (game.level.isLocVisible(player.x, player.y, previousX, previousY) || game.level.isLocVisible(player.x, player.y, this.targetX, this.targetY) || player == this.entity) {
					game.messages.get(playerID).add(new String[] {
						"move_entity",
						String.valueOf(this.entity.id),
						String.valueOf(this.entity.x),
						String.valueOf(this.entity.y)
					});
				}
			}
			if (this.entity instanceof Player player) {
				game.sendPlayerVision(player);
			}
			// Update time
			this.entity.time += this.time;
		}
	}
}
