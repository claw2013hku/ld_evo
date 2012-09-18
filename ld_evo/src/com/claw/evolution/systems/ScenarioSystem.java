package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.claw.evolution.components.CWorldActor;
import com.claw.evolution.events.IEvent;

/** 1. Handle Add / Remove actor events 
 *  2. Decrease HP of player
 *  3. Remove targets / add score for player 
 *  4. Change game state **/
public class ScenarioSystem extends ASystem {

	protected ScenarioSystem() {
		super(Aspect.getEmpty());
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
//		if(event.getClass() == CollisionEvent.class)
//		{
//			Entity e1;
//			Entity e2;
//			if(e1.getComponent(CWorldActor.class).type == CWorldActor.Type.PLAYER)
//			{
//				
//			}
//			else if()
//			{
//				
//				
//			}
//		}
		return false;
	}

	@Override
	public void dispose() {

	}
}
