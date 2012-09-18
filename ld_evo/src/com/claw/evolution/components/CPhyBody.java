package com.claw.evolution.components;

import com.badlogic.gdx.physics.box2d.Body;

public class CPhyBody extends AComponent {
	public Body body;
	public CPhyBody()
	{
		body = null;
	}
	
	@Override
	public void reset() {
		body = null;
	}
}
