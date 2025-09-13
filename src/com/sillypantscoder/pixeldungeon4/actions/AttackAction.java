package com.sillypantscoder.pixeldungeon4.actions;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.entities.LivingEntity;
import com.sillypantscoder.pixeldungeon4.entities.Player;

public class AttackAction extends Action<LivingEntity> {
	public LivingEntity target;
	public AttackAction(LivingEntity self, LivingEntity target) {
		super(self, 8);
		this.target = target;
	}
	public void execute(Game game) {
		// Validate
		if (! game.level.entities.contains(this.target)) {
			return;
		}
		// Decrease entity health
		this.target.health -= this.entity.getDamage(game.level);
		// Update time
		this.entity.time += this.time;
		// Inform clients about this update
		for (Player player : game.allPlayers()) {
			if (player == this.entity || game.level.isLocVisible(player.x, player.y, this.entity.x, this.entity.y)) {
				// Set animation
				player.sendMessage.accept(new String[] {
					"set_animation",
					String.valueOf(this.entity.id),
					"action"
				});
				// Create attacking particles
				player.sendMessage.accept(new String[] {
					"create_particle",
					"attack",
					String.valueOf(this.entity.id),
					String.valueOf(this.target.id)
				});
			}
		}
		// Entity death / health update
		game.updateEntityHealth(this.target);
	}
}
