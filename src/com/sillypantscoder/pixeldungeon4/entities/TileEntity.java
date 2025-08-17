package com.sillypantscoder.pixeldungeon4.entities;

import com.sillypantscoder.utils.JSONObject;
import com.sillypantscoder.utils.Random;

public abstract class TileEntity extends Entity implements PathfindingTarget {
	public int id = Random.randomInt();
	public int x;
	public int y;
	public TileEntity(int time, int x, int y) {
		super(time);
		this.x = x;
		this.y = y;
	}
	public int getX() { return this.x; }
	public int getY() { return this.y; }
	public JSONObject serialize() {
		JSONObject obj = new JSONObject();
		obj.setNumber("id", this.id);
		obj.setString("type", this.getTypeID());
		obj.setNumber("x", this.x);
		obj.setNumber("y", this.y);
		return obj;
	}
}
