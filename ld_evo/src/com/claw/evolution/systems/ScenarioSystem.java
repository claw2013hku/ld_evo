package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.claw.evolution.events.IEvent;

/** 1. Handle Add / Remove actor events 
 *  2. Decrease HP of player
 *  3. Remove targets / add score for player 
 *  4. Change game state **/
public class ScenarioSystem extends ASystem {

	protected ScenarioSystem(Aspect aspect) {
		super(aspect);
		setPassive(true);
	}

	@Override
	protected void process(Entity e) {
		
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean handleEvent(IEvent event) {
		return false;
	}

	@Override
	public void dispose() {

	}
}
