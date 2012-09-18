package com.claw.evolution.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;

public class CAIPatrolAgent extends AComponent {
	public Vector2 patrolPt;
	public float patrolRange;
	
	public CAIPatrolAgent()
	{
		patrolPt = Pools.get(Vector2.class).obtain().set(0,0);
		patrolRange = 0;
	}

	@Override
	public void reset() {
		patrolPt.set(0, 0);
		patrolRange = 0;
	}
}
