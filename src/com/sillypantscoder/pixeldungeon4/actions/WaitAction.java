package com.sillypantscoder.pixeldungeon4.actions;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.Entity;
import com.sillypantscoder.pixeldungeon4.entities.Player;
import com.sillypantscoder.pixeldungeon4.entities.TileEntity;

public class WaitAction extends Action<Entity> {
	public WaitAction(Entity target) {
		super(target, 16);
	}
	public void execute(Game game) {
		// Update time
		this.entity.time += this.time;
		// Send idle animation to clients
		if (this.entity instanceof TileEntity tileEntity) {
			for (Player player : game.level.allPlayers()) {
				if (player == tileEntity || game.level.isLocVisible(player.x, player.y, tileEntity.x, tileEntity.y)) {
					// Set animation
					player.sendMessage.accept(new String[] {
						"set_animation",
						String.valueOf(tileEntity.id),
						"idle"
					});
				}
			}
		}
	}
}
