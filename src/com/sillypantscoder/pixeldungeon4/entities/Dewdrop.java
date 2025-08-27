package com.sillypantscoder.pixeldungeon4.entities;

import java.util.Optional;

import com.sillypantscoder.pixeldungeon4.Game;
import com.sillypantscoder.pixeldungeon4.actions.Action;
import com.sillypantscoder.pixeldungeon4.actions.WaitAction;
import com.sillypantscoder.pixeldungeon4.level.Level;

public class Dewdrop extends TileEntity {
	public Dewdrop(int time, int x, int y) {
		super(time, x, y);
	}
	public String getTypeID() { return "dewdrop"; }
	public Optional<Action<?>> getAction(Level level) {
		return Optional.of(new WaitAction(this));
	}
	public Optional<Action<?>> collect(LivingEntity entity) {
		return Optional.of(new CollectDewdrop(entity, this));
	}
	public static class CollectDewdrop extends Action<LivingEntity> {
		public Dewdrop dewdrop;
		public CollectDewdrop(LivingEntity target, Dewdrop dewdrop) {
			super(target, 4);
			this.dewdrop = dewdrop;
		}
		public void execute(Game game) {
			// Remove dewdrop
			for (Player player : game.allPlayers()) {
				if (game.level.isLocVisible(player.x, player.y, this.dewdrop.x, this.dewdrop.y)) {
					// Send death
					player.visibleEntities.remove(this.dewdrop);
					player.sendMessage.accept(new String[] {
						"remove_entity",
						String.valueOf(this.dewdrop.id)
					});
				}
			}
			game.level.entities.remove(this.dewdrop);
			// Heal entity
			this.entity.health += 1;
			if (this.entity.health > this.entity.maxHealth) this.entity.health = this.entity.maxHealth;
			game.updateEntityHealth(this.entity);
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
