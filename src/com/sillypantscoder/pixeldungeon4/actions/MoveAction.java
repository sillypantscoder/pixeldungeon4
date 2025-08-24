package com.sillypantscoder.pixeldungeon4.actions;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;

public class MoveAction extends Action<TileEntity> {
	public int targetX;
	public int targetY;
	public MoveAction(TileEntity target, int targetX, int targetY) {
		super(target, 16);
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
		for (Player player : game.level.allPlayers()) {
			if (player == this.entity || game.level.isLocVisible(player.x, player.y, previousX, previousY) || game.level.isLocVisible(player.x, player.y, this.targetX, this.targetY)) {
				// Create entity if it does not exist
				if (! player.visibleEntities.contains(this.entity)) {
					player.visibleEntities.add(this.entity);
					String[] data = new String[] {
						"create_entity",
						this.entity.serialize().toString()
					};
					player.sendMessage.accept(data);
				}
				// Move entity
				player.sendMessage.accept(new String[] {
					"move_entity",
					String.valueOf(this.entity.id),
					String.valueOf(this.entity.x),
					String.valueOf(this.entity.y)
				});
				// Set animation
				player.sendMessage.accept(new String[] {
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
		this.entity.afterMove(game.level);
	}
}
