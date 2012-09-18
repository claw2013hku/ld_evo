package com.claw.evolution.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;

public class CController extends AComponent{
	public boolean enabled;
	public Vector2 moveDir;
	
	public CController()
	{
		enabled = true;
		moveDir = Pools.get(Vector2.class).obtain().set(0, 0);
	}
	
	@Override
	public void reset() {
		enabled = true;
		moveDir.set(0, 0);
	}
}
