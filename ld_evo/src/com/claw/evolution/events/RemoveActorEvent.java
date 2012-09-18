package com.claw.evolution.events;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class RemoveActorEvent implements IEvent {
	public Actor actor = null;
	
	@Override
	public void reset() {
		actor = null;
	}
}
