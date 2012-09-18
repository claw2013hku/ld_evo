package com.claw.evolution.singleton;

import java.util.Random;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.Constants;
import com.claw.evolution.ai.AIConditionResult;
import com.claw.evolution.ai.AIEscapeState;
import com.claw.evolution.ai.AIPatrolState;
import com.claw.evolution.ai.AIState;
import com.claw.evolution.components.CAIAgent;
import com.claw.evolution.components.CPosition;
import com.claw.evolution.components.CWorldActor;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.events.RemoveEntityEvent;
import com.claw.evolution.systems.AISystem;
import com.claw.evolution.systems.IEventHandler;

public class AIBehaviourAssemblage {
	static final String tag = "AIBehaviourAssemblage : ";
	private static AIBehaviourAssemblage instance;
	
	private AIBehaviourAssemblage(){};
	 
	public static AIBehaviourAssemblage getInstance()
	{
		if(instance == null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "no instance found");
		}
		return instance;
	}
	
	public static void dispose()
	{
		instance = null;
	}
	 
	public static void createInstance()
	{
		if(instance != null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "instance already created");
		}
		else
		{
			instance = new AIBehaviourAssemblage();
		}
	}
	
	public static AIConditionResult getSimpleEscapeCR(CWorldActor.Type _escapeFromType, float _escapeRadius, float _detectRadius)
	{
		return getInstance().new AIEscapeCR(_escapeFromType, _escapeRadius, _detectRadius);
	}
	
	static Random rand = new Random();
	public class AIEscapeCR extends AIConditionResult implements IEventHandler
	{
		Vector2 lastAIPos = Pools.obtain(Vector2.class);
		Vector2 lastTargetAIPos = Pools.obtain(Vector2.class);
		Entity selectedEntity = null;
		boolean targetChanged = false;
		float detectRadius;
		float escapeRadius;
		CWorldActor.Type type;
		AIEscapeState resultState = Pools.obtain(AIEscapeState.class);
		
		public AIEscapeCR(CWorldActor.Type _escapeFromType, float _escapeRadius, float _detectRadius)
		{
			EventManager.registerListener(this, RemoveEntityEvent.class);
			type = _escapeFromType;
			escapeRadius = _escapeRadius;
			detectRadius = _detectRadius;
		}
		
		@Override
		public boolean handleEvent(IEvent event) {
			if(event.getClass() == RemoveEntityEvent.class)
			{
				if(selectedEntity == ((RemoveEntityEvent)event).entity)
				{
					selectedEntity = null;
				}
			}
			return false;
		}

		@Override
		public boolean testCondition(Entity e, CAIAgent cAI, AISystem aiSystem,
				World es, AIState lastState, AIConditionResult lastCondition) {
			CPosition cPos = aiSystem.getPosMapper().get(e);
			Vector2 tempAI = Pools.obtain(Vector2.class).set(cPos.x / Constants.pixelsPerMeter, cPos.y / Constants.pixelsPerMeter);
			Assemblage.switchMetersToAI(tempAI);
			aiSystem.enforceAICoordinates(tempAI);
			
			if(selectedEntity != null)
			{
				CPosition cTargetPos = aiSystem.getPosMapper().get(e);
				Vector2 targetAIPos = Assemblage.CPosToAI(cTargetPos);
				boolean quickResult = false;
				if(lastAIPos.equals(tempAI) && lastTargetAIPos.equals(targetAIPos) && lastCondition == this)
				{
					quickResult = true;
				}
				lastAIPos.set(tempAI);
				lastTargetAIPos.set(targetAIPos);
				Pools.free(targetAIPos);
				if(quickResult)
				{
					Pools.free(tempAI);
					targetChanged = false;
					return true;
				}
				else if(aiSystem.actorVisible(
						selectedEntity, 
						detectRadius, 
						e, 
						tempAI))
				{
					Pools.free(tempAI);
					targetChanged = false;
					return true;
				}
				else
				{
					targetChanged = true;
				}
			}
			
			Array<Entity> tempEntities = Pools.obtain(Array.class);
			aiSystem.actorTypeSearch(
					type, 
					detectRadius, 
					e, 
					tempAI, 
					tempEntities, 
					1);
			
			boolean result = tempEntities.size > 0;
			if(tempEntities.size > 0)
			{
//				if(selectedEntity != null && tempEntities.contains(selectedEntity, false))
//				{
//					targetChanged = false;
//				}
//				else
//				{
					targetChanged = true;
					selectedEntity = tempEntities.get(rand.nextInt(tempEntities.size));
//				}
			}
			
			Pools.free(tempAI);
			tempEntities.clear();
			Pools.free((Object)tempEntities);
			return result;
		}

		@Override
		public AIState getResult(Entity e, CAIAgent cAI, AISystem aiSystem,
				World es, AIState lastState, AIConditionResult lastCondition) {
			resultState.escapeFrom = selectedEntity;
			resultState.escapeRange = escapeRadius;
			if(targetChanged || lastCondition != this)
			{
				Pools.free(cAI.destinations);
				cAI.destinations.clear();
			}
			return resultState;
		}

		@Override
		public void dispose() {
			EventManager.unregisterListener(this);
		}
	}

	public static AIConditionResult getSimplePatrol(final float range)
	{
		AIConditionResult con2 = new AIConditionResult()
		{
			AIPatrolState aiState = Pools.obtain(AIPatrolState.class);
			
			@Override
			public AIState getResult(Entity e, CAIAgent cAI, AISystem aiSystem,
					World es, AIState lastState, AIConditionResult lastCondition) {
				if(lastCondition != this)
				{
					if(cAI.destinations.size > 0)
					{
						aiState.patrolPt.set(cAI.destinations.get(cAI.destinations.size - 1));
						aiState.patrolRange = range;
					}
					else
					{
						CPosition cPos = aiSystem.getPosMapper().get(e);
						Vector2 tempAI = Pools.obtain(Vector2.class).set(cPos.x / Constants.pixelsPerMeter, cPos.y / Constants.pixelsPerMeter);
						Assemblage.switchMetersToAI(tempAI);
						aiSystem.enforceAICoordinates(tempAI);
						aiState.patrolPt.set(tempAI);
						aiState.patrolRange = range;
						Pools.free(tempAI);
					}
				}
				return aiState;
			}
			
			@Override
			public void dispose() {
			}

			@Override
			public boolean testCondition(Entity e, CAIAgent cAI,
					AISystem aiSystem, World es, AIState lastState,
					AIConditionResult lastCondition) {
				return true;
			}
		};
		return con2;
	}
}
