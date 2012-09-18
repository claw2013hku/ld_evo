package com.claw.evolution.events;

import com.badlogic.gdx.physics.box2d.Body;

public class RemoveBodyEvent implements IEvent {
	public Body body = null;
	
	@Override
	public void reset() {
		body = null;
	}
}
