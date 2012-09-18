package com.claw.evolution.events;

import com.artemis.Entity;

public class RemoveEntityEvent implements IEvent {
	public Entity entity = null;
	@Override
	public void reset() {
		entity = null;
	}
}
