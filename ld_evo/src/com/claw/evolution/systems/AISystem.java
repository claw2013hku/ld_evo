package com.claw.evolution.systems;

import java.util.Random;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.Constants;
import com.claw.evolution.ai.AIConditionResult;
import com.claw.evolution.ai.AIEntityComparable;
import com.claw.evolution.ai.AIEscapeState;
import com.claw.evolution.ai.AINode;
import com.claw.evolution.ai.AIPatrolState;
import com.claw.evolution.ai.AIStoredSearch;
import com.claw.evolution.components.CAIAgent;
import com.claw.evolution.components.CAIBehaviours;
import com.claw.evolution.components.CAIMap;
import com.claw.evolution.components.CController;
import com.claw.evolution.components.CPhyBody;
import com.claw.evolution.components.CPhyWorld;
import com.claw.evolution.components.CPosition;
import com.claw.evolution.components.CWorldActor;
import com.claw.evolution.components.CWorldActor.Type;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.singleton.Assemblage;

/**
 *	1. Set the destination (CAIAgent) and controller (CController) of AI agents based on their AI State
 *	2. Change the state of AI agents based on their behaviour(s) (CAIBehaviours)
 */
public class AISystem extends ASystem {
	private float arrivalTheresholdSqu = (1 / Constants.AI_NodeDensity * 1 / Constants.AI_NodeDensity * 0.25f);
	private ComponentMapper<CAIAgent> cAIMapper;
	private ComponentMapper<CAIMap> cAIMapMapper;
	private ComponentMapper<CPosition> cPosMapper;
	private ComponentMapper<CController> cConMapper;
	private ComponentMapper<CAIBehaviours> cBeMapper;
	private ComponentMapper<CWorldActor> cActorMapper;
	private ComponentMapper<CPhyBody> cPhyBodyMapper;
	private ComponentMapper<CPhyWorld> cPhyWorldMapper;
	private ObjectMap<Type, Array<Entity>> m_worldActors;
	
	public ComponentMapper<CPosition> getPosMapper(){return cPosMapper;}
	
	public AISystem() {
		super(Aspect.getAspectForOne(CAIAgent.class, CAIBehaviours.class, CWorldActor.class));
	}

	@Override
	protected void initialize() {
		cAIMapper = world.getMapper(CAIAgent.class);
		cPosMapper = world.getMapper(CPosition.class);
		cConMapper = world.getMapper(CController.class);
		cBeMapper = world.getMapper(CAIBehaviours.class);
		cAIMapMapper = world.getMapper(CAIMap.class);
		cActorMapper = world.getMapper(CWorldActor.class);
		cPhyBodyMapper = world.getMapper(CPhyBody.class);
		cPhyWorldMapper = world.getMapper(CPhyWorld.class);
		m_worldActors = new ObjectMap<CWorldActor.Type, Array<Entity>>();
	}

	private CAIMap getCAIMap()
	{
		return cAIMapMapper.get(world.getManager(TagManager.class).getEntity(Constants.tag_AIMap));
	}
	
	private void getNeighbourNodes(int x, int y, Array<AINode> neighbourNodes)
	{
		CAIMap aiMap = getCAIMap();
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				int nX = x + i;
				int nY = y + j;
				if(nX < 0 || nY < 0 || nX > aiMap.lastX || nY > aiMap.lastY)
				{
					continue;
				}
				if(nX == x && nY == y)// || i == j || i == -j)
				{
					continue;
				}
				if(!aiMap.nodes.get(nX).get(nY).valid)
				{
					continue;
				}
				AINode nNode = Pools.obtain(AINode.class);
				nNode.valid = true;
				nNode.x = nX;
				nNode.y = nY;
				neighbourNodes.add(nNode);
			}
		}
	}
	
	private void getNodesWithinRadius(
			int x, int y, float withinMeters, Array<Vector2> result)
	{
		float radiusSqu = withinMeters * withinMeters;
		CAIMap aiMap = getCAIMap();
		x = MathUtils.clamp(x, 0, aiMap.lastX);
		y = MathUtils.clamp(y, 0, aiMap.lastY);
		Vector2 beginPos = Pools.obtain(Vector2.class).set(x, y);
		Vector2 beginPosM = Assemblage.AIToMeters(beginPos);
		
		Array<Vector2> nodeQueue = Pools.obtain(Array.class);
		Array<Vector2> mark = Pools.obtain(Array.class);
		
		nodeQueue.add(beginPos);
		
		while(nodeQueue.size > 0)
		{
			Vector2 checkNode = nodeQueue.removeIndex(0);
			
			if(mark.contains(checkNode, false))
			{
				continue;
			}
			else
			{
				mark.add(Pools.obtain(Vector2.class).set(checkNode));
			}
			Vector2 checkNodeM = Assemblage.AIToMeters(checkNode);
			if(checkNodeM.dst2(beginPosM) <= radiusSqu)
			{
				Array<AINode> tempAINodeArray = Pools.obtain(Array.class);
				result.add(Pools.obtain(Vector2.class).set(checkNode));
				getNeighbourNodes((int)checkNode.x, (int)checkNode.y, tempAINodeArray);
				for(AINode aiNode : tempAINodeArray)
				{
					nodeQueue.add(Pools.obtain(Vector2.class).set(aiNode.x, aiNode.y));
				}
				Pools.free(tempAINodeArray);
				tempAINodeArray.clear();
				Pools.free((Object)tempAINodeArray);
			}
			Pools.free(checkNodeM);
		}
		
		Pools.free(beginPos);
		Pools.free(beginPosM);
		Pools.free(nodeQueue);
		nodeQueue.clear();
		Pools.free((Object)nodeQueue);
		Pools.free(mark);
		mark.clear();
		Pools.free((Object)mark);
	}
	
	private void getNodesOutOfRange(int x, int y, int outX, int outY, float range, int nodesCount, Array<Vector2> result)
	{
		float rangeSqu = range * range;
		Array<AINode> nodeQueueAI = Pools.obtain(Array.class);
		Array<Vector2> mark = Pools.obtain(Array.class);
		
		Vector2 beginPos = Pools.obtain(Vector2.class).set(x, y);
		enforceAICoordinates(beginPos);
		AINode beginNode = Pools.obtain(AINode.class);
		beginNode.x = (int)beginPos.x;
		beginNode.y = (int)beginPos.y;
		Pools.free(beginPos);
		
		Vector2 outPos = Pools.obtain(Vector2.class).set(outX, outY);
		enforceAICoordinates(outPos);
		Vector2 outPosM = Assemblage.AIToMeters(outPos);
		
		nodeQueueAI.add(beginNode);
		
		int resultSize = 0;
		int iteration = 0;
		while(nodeQueueAI.size > 0)
		{
			AINode checkNode = nodeQueueAI.removeIndex(0);
			if(iteration == 1)
			{
				mark.add(Pools.obtain(Vector2.class).set((int)outPos.x, (int)outPos.y));
			}
			
			Vector2 checkMark = Pools.obtain(Vector2.class).set(
					(int)checkNode.x,
					(int)checkNode.y);
			if(mark.contains(checkMark, false))
			{
				Pools.free(checkMark);
				continue;
			}
			else
			{
				mark.add(checkMark);
			}
			
			Vector2 checkNodeM = Assemblage.AIToMeters(checkNode.x, checkNode.y);
			if(checkNodeM.dst2(outPosM) >= rangeSqu)
			{
				resultSize++;
				result.add(Pools.obtain(Vector2.class).set(checkNode.x, checkNode.y));
				if(resultSize >= nodesCount || iteration == 0)
				{
					break;
				}
				else
				{
					Pools.free(mark);
					mark.clear();
					iteration = 0;
					nodeQueueAI.clear();
					nodeQueueAI.add(beginNode);
					for(Vector2 v : result)
					{
						mark.add(Pools.obtain(Vector2.class).set(
								(int)v.x,
								(int)v.y));
					}
					continue;
				}
			}
			
			Array<AINode> tempAINodeArray = Pools.obtain(Array.class);
			getNeighbourNodes(checkNode.x, checkNode.y, tempAINodeArray);
			for(int i = 0; i < tempAINodeArray.size; i++)
			{
				AINode queueNode = Pools.obtain(AINode.class).set(tempAINodeArray.get(i));
				queueNode.parent = checkNode;
				double heuristicsCost = 
						Assemblage.metersToAI(range) -Math.sqrt(
								(outPos.x - tempAINodeArray.get(i).x) * (outPos.x - tempAINodeArray.get(i).x) + 
								(outPos.y - tempAINodeArray.get(i).y) * (outPos.y - tempAINodeArray.get(i).y)
								);
				double realCost = 
						checkNode.realCost +
				Math.sqrt(
						(checkNode.x - tempAINodeArray.get(i).x) * (checkNode.x - tempAINodeArray.get(i).x) + 
						(checkNode.y - tempAINodeArray.get(i).y) * (checkNode.y - tempAINodeArray.get(i).y));
				queueNode.realCost = (float)realCost;
				queueNode.totalCost = (float)(realCost + heuristicsCost);
				nodeQueueAI.add(queueNode);
			}
			Pools.free(tempAINodeArray);
			tempAINodeArray.clear();
			Pools.free((Object)tempAINodeArray);
			
			nodeQueueAI.sort();
			iteration++;
		}
		Pools.free(nodeQueueAI);
		nodeQueueAI.clear();
		Pools.free((Object)nodeQueueAI);
		Pools.free(mark);
		mark.clear();
		Pools.free((Object)mark);
		Pools.free(outPos);
	}
	
	private boolean search(int fromX, int fromY, int toX, int toY, CAIAgent agent, Array<AINode> result)
	{
		Array<AINode> nodeQueueAI = Pools.obtain(Array.class);
		Array<AINode> tempResult = Pools.obtain(Array.class);
		Array<Vector2> mark = Pools.obtain(Array.class);
		
		CAIMap aiMap = getCAIMap();
		fromX = MathUtils.clamp(fromX, 0, aiMap.lastX);
		fromY = MathUtils.clamp(fromY, 0, aiMap.lastY);
		AINode beginNode = Pools.obtain(AINode.class);
		beginNode.parent = null;
		beginNode.x = aiMap.nodes.get(fromX).get(fromY).x;
		beginNode.y = aiMap.nodes.get(fromX).get(fromY).y;
		beginNode.realCost = 0;
		beginNode.totalCost = 0;
		
		result.clear();
		nodeQueueAI.clear();
		nodeQueueAI.add(beginNode);
		
		while(nodeQueueAI.size > 0)
		{
			AINode checkNode = nodeQueueAI.removeIndex(0);
			
			Vector2 checkMark = Pools.obtain(Vector2.class).set(
					(int)checkNode.x,
					(int)checkNode.y);
			if(mark.contains(checkMark, false))
			{
				Pools.free(checkMark);
				continue;
			}
			else
			{
				mark.add(checkMark);
			}

			if(checkNode.x == toX && checkNode.y == toY)
			{
				tempResult.clear();
				AINode resultNode = checkNode;
				while(resultNode != null)
				{
					tempResult.add(resultNode);
					resultNode = resultNode.parent;
				}
				result.clear();
				for(int i = tempResult.size - 1; i >= 0; i--)
				{
					AINode node = Pools.obtain(AINode.class).set(tempResult.get(i));
					result.add(node);
				}
				break;
			}
			
			Array<AINode> tempAINodeArray = Pools.obtain(Array.class);
			getNeighbourNodes(checkNode.x, checkNode.y, tempAINodeArray);
			for(int i = 0; i < tempAINodeArray.size; i++)
			{
				AINode queueNode = Pools.obtain(AINode.class).set(tempAINodeArray.get(i));
				queueNode.parent = checkNode;
				double heuristicsCost = 
						Math.sqrt(
								(toX - tempAINodeArray.get(i).x) * (toX - tempAINodeArray.get(i).x) + 
								(toY - tempAINodeArray.get(i).y) * (toY - tempAINodeArray.get(i).y)
								);
				double realCost = 
						checkNode.realCost +
				Math.sqrt(
						(checkNode.x - tempAINodeArray.get(i).x) * (checkNode.x - tempAINodeArray.get(i).x) + 
						(checkNode.y - tempAINodeArray.get(i).y) * (checkNode.y - tempAINodeArray.get(i).y));
				queueNode.realCost = (float)realCost;
				queueNode.totalCost = (float)(realCost + heuristicsCost);
				nodeQueueAI.add(queueNode);
			}
			Pools.free(tempAINodeArray);
			tempAINodeArray.clear();
			Pools.free((Object)tempAINodeArray);
			nodeQueueAI.sort();
		}
		Pools.free(nodeQueueAI);
		nodeQueueAI.clear();
		Pools.free((Object)nodeQueueAI);
		Pools.free(mark);
		mark.clear();
		Pools.free((Object)mark);
		Pools.free(tempResult);
		tempResult.clear();
		Pools.free((Object)tempResult);
		return true;
	}
	
	Random rand = new Random();
	Array<Vector2> candidates = new Array<Vector2>();
	public Vector2 getNextPatrolPt(int pX, int pY, float radius)
	{
		getNodesWithinRadius(pX, pY, radius, candidates);
		Vector2 result = null;
		if(candidates.size != 0)
		{
			result = Pools.obtain(Vector2.class).set(candidates.get(rand.nextInt(candidates.size)));
		}
		else
		{
			result = Pools.obtain(Vector2.class).set(pX, pY);
		}
		Pools.free(candidates);
		candidates.clear();
		return result;
	}
	
	public Vector2 getNextEscapePt(int x, int y, float radius, int fromX, int fromY)
	{
		Vector2 dist = Assemblage.AIToMeters(x, y);
		if(dist.dst2(Assemblage.AIToMeters(fromX), Assemblage.AIToMeters(fromY)) > radius * radius)
		{
			return dist.set(x, y);
		}
		else
		{
			Pools.free(dist);
		}
		
		getNodesOutOfRange(x, y, fromX, fromY, radius, 2, candidates);
		Vector2 result = null;
		if(candidates.size != 0)
		{
			for(int i = 0; i < candidates.size; i++)
			{
				if(rand.nextFloat() < 0.9f || i == candidates.size - 1)
				{
					result = Pools.obtain(Vector2.class).set(candidates.get(i));
					break;
				}
			}
		}
		else
		{
			result = Pools.obtain(Vector2.class).set(x, y);
		}
		Pools.free(candidates);
		candidates.clear();
		return result;
	}
	
	private boolean arrived(Vector2 pos, Vector2 dest)
	{
		double distSqu = Math.pow(pos.x - dest.x, 2) + Math.pow(pos.y - dest.y, 2);
		return distSqu < arrivalTheresholdSqu;
	}
	
	private Vector2 positionInMeters(Vector2 pos)
	{
		return Pools.obtain(Vector2.class).set(pos.x / Constants.AI_NodeDensity, pos.y / Constants.AI_NodeDensity);
	}
	
	public void enforceAICoordinates(Vector2 v)
	{
		CAIMap aiMap = getCAIMap();
		
		v.x = MathUtils.clamp(v.x, 0, aiMap.lastX);
		v.y = MathUtils.clamp(v.y, 0, aiMap.lastY);
		
		
		if(!aiMap.nodes.get((int)v.x).get((int)v.y).valid)
		{
			int range = 1;
			boolean valid = false;
			while(!valid)
			{
				int startX = (int)v.x - range;
				startX = MathUtils.clamp(startX, 0, aiMap.lastX);
				int endX = (int)v.x + range;
				endX = MathUtils.clamp(endX, 0, aiMap.lastX);
				int startY = (int)v.y - range;
				startY = MathUtils.clamp(startY, 0, aiMap.lastY);
				int endY = (int)v.y + range;
				endY = MathUtils.clamp(endY, 0, aiMap.lastY);
				
				for(int i = startX; i <= endX; i++)
				{
					for(int j = startY; j <= endY; j++)
					{
						if(aiMap.nodes.get(i).get(j).valid)
						{
							v.x = i;
							v.y = j;
							valid = true;
							break;
						}
					}
				}
				range++;
			}
		}
	}
	
	@Override
	public boolean handleEvent(IEvent event) {
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void inserted(Entity e) {
		CWorldActor worldActor = null;
		if((worldActor = cActorMapper.getSafe(e)) != null)
		{
			if(m_worldActors.containsKey(worldActor.type))
			{
				if(!m_worldActors.get(worldActor.type).contains(e, false))
				{
					m_worldActors.get(worldActor.type).add(e);
				}
			}
			else
			{
				m_worldActors.put(worldActor.type, Pools.obtain(Array.class));
				m_worldActors.get(worldActor.type).add(e);
			}
		}
	}

	@Override
	protected void removed(Entity e) {
		CWorldActor worldActor = null;
		if((worldActor = cActorMapper.getSafe(e)) != null)
		{
			if(m_worldActors.containsKey(worldActor.type) && m_worldActors.get(worldActor.type).contains(e, false))
			{
				m_worldActors.get(worldActor.type).removeValue(e, false);
			}
		}
	}

	Vector2 playerPos = new Vector2();
	@Override
	protected void begin() {
		super.begin();
		CPosition cPos = cPosMapper.get(world.getManager(TagManager.class).getEntity(Constants.tag_player));
		playerPos.set(cPos.x / Constants.pixelsPerMeter, cPos.y / Constants.pixelsPerMeter);
	}

	Array<AIEntityComparable> eds = new Array<AIEntityComparable>();
	
	@Override
	protected void process(Entity e) {
		CAIAgent cAI = cAIMapper.getSafe(e);
		if(cAI != null)
		{
			processCAI(cAI, e);
		}
	}
	
	private void processCBE(CAIBehaviours cBe, Entity e, CAIAgent cAI)
	{
		int index = 0;
		for(; index < cBe.triggers.size; index++)
		{
			AIConditionResult trigger = cBe.triggers.get(index);
			if(trigger.testCondition(e, cAI, this, world, cAI.state, cBe.activatedTrigger))
			{
				break;
			}
		}
		
		if(index < cBe.triggers.size)
		{
			cAI.state = cBe.triggers.get(index).getResult(e, cAI, this, world, cAI.state, cBe.activatedTrigger);
			cBe.activatedTrigger = cBe.triggers.get(index);
		}
	}

	private void processCAI(CAIAgent cAI, Entity e)
	{
		CPosition cPos = cPosMapper.get(e);
		Vector2 pos2 = Pools.obtain(Vector2.class).set(
				cPos.x / Constants.pixelsPerMeter, 
				cPos.y / Constants.pixelsPerMeter);
		Vector2 lastNode = null;
		if(cAI.destinations.size > 0 &&
				arrived(pos2, positionInMeters(cAI.destinations.get(0))))
		{
			lastNode = cAI.destinations.removeIndex(0);
			cAI.arrived++;
		}
		
		if (cAI.destinations.size > 0)
		{
			CController cCon = cConMapper.get(e);
			Vector2 moveDir = Pools.obtain(Vector2.class).set(positionInMeters(cAI.destinations.get(0)));
			cCon.moveDir.set(moveDir.sub(pos2).nor());
		}
		else
		{
			CController cCon = cConMapper.get(e);
			cCon.moveDir.set(0,0);
		}
		
		if(lastNode == null)
		{
			lastNode = Pools.obtain(Vector2.class).set(
					Assemblage.metersToAI(pos2.x),
					Assemblage.metersToAI(pos2.y));
			enforceAICoordinates(lastNode);
		}
		
		AIEntityComparable ed = Pools.obtain(AIEntityComparable.class);
		ed.e = e;
		ed.dest = cAI.destinations.size;
		ed.lastNode = lastNode;
		ed.pending = cAI.pending;
		ed.searching = cAI.searching;
		ed.arrived = cAI.arrived;
		ed.dist2FromPlayer = pos2.dst2(playerPos);
		eds.add(ed);
		
		Pools.free(pos2);
	}
	
	long endFrameMS = 0;
	long maxRunningDelta = 10;
	long minRunningDelta = 1;
	@Override
	protected void end() {
		maxRunningDelta = 10 - (System.currentTimeMillis() - startFrameMS);
		endFrameMS = System.currentTimeMillis();
		
		boolean processBeOverDelta = false;
		
		eds.sort(AIEntityComparable.behaviourComparator);
		for(int i = 0; i < eds.size; i++)
		{
			if(!processBeOverDelta)
			{
				processBeOverDelta = (System.currentTimeMillis() - endFrameMS) > 2;
			}
			
			if(processBeOverDelta)
			{
				break;
			}
			
			AIEntityComparable ed = eds.get(i);
			CAIAgent cAI = cAIMapper.get(ed.e);
			CAIBehaviours cBe = cBeMapper.getSafe(ed.e);
			if(cBe != null)
			{
				processCBE(cBe, ed.e, cAI);
			}
		}
		
		boolean processAIOverDelta = false;
		
		eds.sort(AIEntityComparable.searchComparator);
		for(int i = 0; i < eds.size; i++)
		{
			AIEntityComparable ed = eds.get(i);
			CAIAgent cAI = cAIMapper.get(ed.e);
			if(!processAIOverDelta)
			{
				processAIOverDelta = (System.currentTimeMillis() - startFrameMS) > maxRunningDelta;
			}
			if(processAIOverDelta)
			{
				cAI.pending++;
			}
			else
			{
				if(cAI.state == null) continue;
				
				if(cAI.state.getClass() == AIPatrolState.class)
				{
					if(cAI.destinations.size > 3)
					{
						continue;
					}
					if(behaviourSearch(cAI, ed, Behaviour.PATROL))
					{
						cAI.pending = 0;
					}
				}
				else if (cAI.state.getClass() == AIEscapeState.class)
				{
					if(cAI.arrived < 4 && cAI.destinations.size > 0)
					{
						continue;
					}
					Pools.free(cAI.destinations);
					cAI.destinations.clear();
					if(behaviourSearch(cAI, ed, Behaviour.ESCAPE))
					{
						cAI.pending = 0;
					}
				}
			}
		}
		
		for(int i = 0; i < eds.size; i++)
		{
			AIEntityComparable ed = eds.get(i);
			if(ed.lastNode != null)
			{
				Pools.free(ed.lastNode);
			}
		}
		Pools.free(eds);
		eds.clear();
		super.end();
	}

	enum Behaviour{PATROL, ESCAPE};
	private boolean behaviourSearch(CAIAgent cAI, AIEntityComparable ed, Behaviour b)
	{
		Array<AINode> searchNodes = Pools.obtain(Array.class);
		
		Vector2 continueNode = null;
		if(cAI.destinations.size > 0)
		{
			continueNode = cAI.destinations.get(cAI.destinations.size - 1);
		}
		else
		{
			continueNode = ed.lastNode;
		}
		Vector2 destination = null;
		
		switch(b)
		{
		case PATROL:
			AIPatrolState cPAI = (AIPatrolState)(cAI.state);
			destination = getNextPatrolPt(
					(int)cPAI.patrolPt.x, 
					(int)cPAI.patrolPt.y, 
					cPAI.patrolRange);
			break;
		case ESCAPE:
			AIEscapeState cEAI = (AIEscapeState)(cAI.state);
			CPosition cPos = cPosMapper.get(cEAI.escapeFrom);
			Vector2 tempPosAI = Pools.obtain(Vector2.class).set(cPos.x / Constants.pixelsPerMeter , cPos.y / Constants.pixelsPerMeter);
			Assemblage.switchMetersToAI(tempPosAI);
			enforceAICoordinates(tempPosAI);
			destination = getNextEscapePt(
					(int)continueNode.x, 
					(int)continueNode.y,  
					cEAI.escapeRange,
					(int)tempPosAI.x,
					(int)tempPosAI.y);
			Pools.free(tempPosAI);
			break;
		}
		 
		search(
				(int)(continueNode.x), 
				(int)(continueNode.y), 
				(int)destination.x, 
				(int)destination.y,
				cAI,
				searchNodes);
		Pools.free(destination);
		
		cAI.searching = false;
		for(int i = 1 ; i < searchNodes.size; i++)
		{
			cAI.destinations.add(
					Pools.get(Vector2.class).obtain().set(
							searchNodes.get(i).x,
							searchNodes.get(i).y)
					);
		}
		cAI.arrived = 0;
		
		boolean hasResult = searchNodes.size > 0;
		Pools.free(searchNodes);
		searchNodes.clear();
		Pools.free((Object)searchNodes);
		return hasResult;
	}

	@Override
	public String getName() {
		return "AI";
	}

	/** 
	 * @param maxResult no. of returned actors, -1 for all possible results
	 */
	static Body hostBody = null;
	static Body targetBody = null;
	static boolean rayCastCalledBack = false;
	static RayCastCallback rcb = new RayCastCallback()
	{
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			if(fixture.getBody() == hostBody || fixture.getBody() == targetBody)
			{
				return -1;
			}
			else
			{
				rayCastCalledBack = true;
				return 0;
			}
		}
	};
	public void actorTypeSearch(CWorldActor.Type type, float withinRadius, Entity fromEntity, Vector2 fromPosAI, Array<Entity> result, int maxResult)
	{
		if(!m_worldActors.containsKey(type)) return;
		float withinRadiusSqu = withinRadius * withinRadius;
		com.badlogic.gdx.physics.box2d.World phyWorld = null; 
		
		for(Entity e: m_worldActors.get(type))
		{
			CPosition cPos = cPosMapper.get(e);
			Vector2 testPosM = Pools.obtain(Vector2.class).set(cPos.x / Constants.pixelsPerMeter, cPos.y / Constants.pixelsPerMeter);
			Vector2 fromPosM = Assemblage.AIToMeters(fromPosAI);
			
			if(testPosM.dst2(fromPosM) > withinRadiusSqu)
			{
				Pools.free(testPosM);
				Pools.free(fromPosM);
				continue;
			}
			
			if(phyWorld == null)
			{
				phyWorld = cPhyWorldMapper.get(world.getManager(TagManager.class).getEntity(Constants.tag_phyWorld)).world;
			}
			
			targetBody = cPhyBodyMapper.get(e).body;
			hostBody = cPhyBodyMapper.get(fromEntity).body;
			rayCastCalledBack = false;
			phyWorld.rayCast(rcb, fromPosM, testPosM);
			
			if(!rayCastCalledBack)
			{
				result.add(e);
				if(maxResult != -1 && result.size >= maxResult)
				{
					break;
				}
			}
			else
			{
				rayCastCalledBack = false;
			}
			Pools.free(testPosM);
			Pools.free(fromPosM);
		}	
	}

	public boolean actorVisible(Entity e, float withinRadius, Entity fromEntity, Vector2 fromPosAI)
	{
		float withinRadiusSqu = withinRadius * withinRadius;
		CPosition cPos = cPosMapper.get(e);
		Vector2 testPosM = Pools.obtain(Vector2.class).set(cPos.x / Constants.pixelsPerMeter, cPos.y / Constants.pixelsPerMeter);
		Vector2 fromPosM = Assemblage.AIToMeters(fromPosAI);
		
		if(testPosM.dst2(fromPosM) > withinRadiusSqu)
		{
			Pools.free(testPosM);
			Pools.free(fromPosM);
			return false;
		}
		
		com.badlogic.gdx.physics.box2d.World phyWorld = cPhyWorldMapper.get(world.getManager(TagManager.class).getEntity(Constants.tag_phyWorld)).world;
	
		targetBody = cPhyBodyMapper.get(e).body;
		hostBody = cPhyBodyMapper.get(fromEntity).body;
		rayCastCalledBack = false;
		phyWorld.rayCast(rcb, fromPosM, testPosM);
		
		Pools.free(testPosM);
		Pools.free(fromPosM);
		if(!rayCastCalledBack)
		{
			return true;
		}
		else
		{
			rayCastCalledBack = false;
			return false;
		}
	}
}
