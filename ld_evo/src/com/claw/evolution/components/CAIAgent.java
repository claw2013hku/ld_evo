package com.claw.evolution.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.ai.AIEscapeState;
import com.claw.evolution.ai.AIPatrolState;
import com.claw.evolution.ai.AIState;

public class CAIAgent extends AComponent {
	public Array<Vector2> destinations;
	public AIState state;
	public int pending;
	public boolean searching; 
	public int arrived;
	
	public CAIAgent()
	{
		destinations = new Array<Vector2>();
		state = null;
		pending = 0;
		arrived = 0;
		searching = false;
	}
	
	@Override
	public void reset() {
		for(Vector2 v2 : destinations)
		{
			Pools.free(v2);
		}
		destinations.clear();
		if(state.getClass() == AIPatrolState.class)
		{
			Pools.free((AIPatrolState)state);
		}
		else if(state.getClass() == AIEscapeState.class)		
		{
			Pools.free((AIEscapeState)state);
		}
		pending = 0;
		arrived = 0;
		searching = false;
	}
}
