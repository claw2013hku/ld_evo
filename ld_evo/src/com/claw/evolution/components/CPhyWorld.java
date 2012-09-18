package com.claw.evolution.components;

import com.badlogic.gdx.physics.box2d.World;

public class CPhyWorld extends AComponent {
	public World world;
	public CPhyWorld()
	{
		world = null;
	}
	
	public void reset()
	{
		world = null;
	}
}
