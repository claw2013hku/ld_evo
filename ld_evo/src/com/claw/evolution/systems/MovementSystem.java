package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.claw.evolution.components.CController;
import com.claw.evolution.components.CPhyBody;
import com.claw.evolution.components.CWorldActor;
import com.claw.evolution.events.IEvent;

/** Get the controller state (CController) and apply forces to the owner (CPhyBody)
 * 	depending on the type of the owner (CWorldActor)**/
public class MovementSystem extends ASystem {
	private ComponentMapper<CController> cConMapper;
	private ComponentMapper<CPhyBody> cPhyBdMapper;
	private ComponentMapper<CWorldActor> cWldActMapper;
	
	@SuppressWarnings("unchecked")
	public MovementSystem()
	{
		super(Aspect.getAspectForAll(CController.class));
	}
	
	public void initialize()
	{
		cConMapper = world.getMapper(CController.class);
		cPhyBdMapper = world.getMapper(CPhyBody.class);
		cWldActMapper = world.getMapper(CWorldActor.class);
	}
	
	@Override
	public boolean handleEvent(IEvent event) {
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void process(Entity e) {
		CController cCon = cConMapper.get(e);
		if(cCon == null || !cCon.enabled) return;
		CPhyBody cBd = cPhyBdMapper.get(e);
		float forceScale = 0;
		switch(cWldActMapper.get(e).type)
		{
		case PLAYER:
			forceScale = 800;
			break;
		default:
			forceScale = 1300;
			break;
		}
		//cBd.body.setLinearVelocity(cCon.moveDir.x * 500, cCon.moveDir.y * 500);
		cBd.body.applyForceToCenter(forceScale * cCon.moveDir.x, forceScale * cCon.moveDir.y);
	}

	@Override
	public String getName() {
		return "Movement";
	}
}
