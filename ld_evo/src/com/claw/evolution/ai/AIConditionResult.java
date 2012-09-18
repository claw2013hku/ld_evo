package com.claw.evolution.ai;

import com.artemis.Entity;
import com.artemis.World;
import com.claw.evolution.components.CAIAgent;
import com.claw.evolution.systems.AISystem;

public abstract class AIConditionResult {
	public abstract boolean testCondition(Entity e, CAIAgent cAI, AISystem aiSystem, World es, AIState lastState, AIConditionResult lastCondition);
	public abstract AIState getResult(Entity e, CAIAgent cAI, AISystem aiSystem, World es, AIState lastState, AIConditionResult lastCondition);
	public abstract void dispose();
}
