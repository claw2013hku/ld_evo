package com.claw.evolution.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.ai.AIConditionResult;

public class CAIBehaviours extends AComponent {
	public Array<AIConditionResult> triggers;
	public AIConditionResult activatedTrigger;
	
	public CAIBehaviours()
	{
		triggers = Pools.obtain(Array.class);
	}

	@Override
	public void reset(){
		for(AIConditionResult tri : triggers)
		{
			Pools.free(tri);
		}
		triggers.clear();
		activatedTrigger = null;
	}
}
