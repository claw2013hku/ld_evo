package com.claw.evolution.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;

public class AIPatrolState extends AIState {
	public Vector2 patrolPt;
	public float patrolRange;
	public boolean dynamicPt;
	
	public AIPatrolState()
	{
		patrolPt = Pools.get(Vector2.class).obtain().set(0,0);
		patrolRange = 0;
	}

	@Override
	public void reset() {
		dynamicPt = false;
		patrolPt.set(0, 0);
		patrolRange = 0;
	}
}
