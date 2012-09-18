package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CPosition;
import com.claw.evolution.components.CWorldActor;
import com.claw.evolution.components.CWorldActorPositionMap;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.singleton.Assemblage;

public class ActorPositioningSystem extends ASystem {
	private ComponentMapper<CWorldActorPositionMap> cMapper;
	private ComponentMapper<CPosition> cPosMapper;
	private IdentityMap<Entity, Vector2> m_posMap;
	private long maxFrameTimeMS = 2;
	
	@SuppressWarnings("unchecked")
	public ActorPositioningSystem() {
		super(Aspect.getAspectForAll(CWorldActor.class));
	}

	@Override
	protected void initialize() {
		cMapper = world.getMapper(CWorldActorPositionMap.class);
		cPosMapper = world.getMapper(CPosition.class);
		Assemblage.loadPositionMap();
	}

	@Override
	public String getName() {
		return "ActorPositioning";
	}

	@Override
	public boolean handleEvent(IEvent event) {
		return false;
	}

	@Override
	public void dispose() {
	}

	Array<Entity> updatedEntity = new Array<Entity>();
	boolean overDelta = false;
	@Override
	protected void process(Entity e) {
		long currentMS = System.currentTimeMillis();
		overDelta = (currentMS - startFrameMS) > maxFrameTimeMS;
		if(overDelta || updatedEntity.contains(e, false))
		{
			return;
		}
		else
		{
			CPosition cPos = cPosMapper.get(e);
			Vector2 testV = Pools.obtain(Vector2.class).set(cPos.x, cPos.y);
			if(m_posMap.containsKey(e))
			{
				if(!m_posMap.get(e).equals(testV))
				{
					m_posMap.get(e).set(testV);
				}
				Pools.free(testV);
			}
			else
			{
				m_posMap.put(e, testV);
			}
			updatedEntity.add(e);
		}
	}

	@Override
	protected void begin() {
		super.begin();
		overDelta = false;
		m_posMap = cMapper.get(world.getManager(TagManager.class).getEntity(Constants.tag_PosMap)).map;
	}

	@Override
	protected void end() {
		if(!overDelta)
		{
			//updatedEntity.clear();
		}
		super.end();
	}
}
