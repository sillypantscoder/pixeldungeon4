package com.sillypantscoder.pixeldungeon4.entities;

import com.sillypantscoder.utils.JSON;
import com.sillypantscoder.utils.Random;

public abstract class TileEntity extends Entity {
	public int id = Random.randomInt();
	public int x;
	public int y;
	public TileEntity(int time, int x, int y) {
		super(time);
		this.x = x;
		this.y = y;
	}
	public abstract JSON.JObject<?> serialize();
}
