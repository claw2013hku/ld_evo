package com.claw.evolution.ai;

import com.artemis.Entity;

public class AIEscapeState extends AIState {
	public float escapeRange;
	public Entity escapeFrom;
	
	@Override
	public void reset() {
		escapeRange = 0;
		escapeFrom = null;
	}
}
