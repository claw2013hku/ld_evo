package com.claw.evolution.systems;

import java.util.UUID;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Manifold.ManifoldPoint;
import com.badlogic.gdx.physics.box2d.World;
import com.claw.evolution.Constants;
import com.claw.evolution.components.*;
import com.claw.evolution.events.IEvent;

/** Update positions and dimensions from physical world to game world, 
 *  required components : CPosition, CDimension, CPhyWorld, CPhyBody
 *  Registers collisions,
 *  required components : CCollisionPairs **/
public class PhysicsSystem extends ASystem {
	private static final String tag = "PhysicsSystem : ";
	boolean b_registerCollisions;
	World m_world;
	private ComponentMapper<CPhyBody> cBodyMapper;
	private ComponentMapper<CPosition> cPosMapper;
	private ComponentMapper<CDimension> cDimMapper;
	
	@SuppressWarnings("unchecked")
	public PhysicsSystem(boolean registerCollisions)
	{
		super(Aspect.getAspectForAll(CPhyBody.class));
		b_registerCollisions = registerCollisions;
	}
	
	@Override
	protected void initialize() {
		cBodyMapper = world.getMapper(CPhyBody.class);
		cDimMapper = world.getMapper(CDimension.class);
		cPosMapper = world.getMapper(CPosition.class);
		m_world = world.getManager(TagManager.class).getEntity(Constants.tag_phyWorld).getComponent(CPhyWorld.class).world;
		//set listen to collisions
		if(b_registerCollisions) 
		{
			m_world.setContactListener(new PhysicSystemContactListener());
		}
	}

	@Override
	protected void begin() {
		super.begin();
		m_world.step(world.getDelta() / 1000, 8, 3);
	}

	@Override
	protected void process(Entity e) {
		CPhyBody cBody = cBodyMapper.get(e);
		float angle = MathUtils.radiansToDegrees * cBody.body.getAngle();
	    Vector2 position = cBody.body.getPosition();
	    CPosition cPos = cPosMapper.get(e);
	    CDimension cDim = cDimMapper.get(e);
	    cPos.x = position.x * Constants.pixelsPerMeter;
	    cPos.y = position.y * Constants.pixelsPerMeter;
	    cDim.rotation = angle;
	}
	
//	@Override
//	public void processOneGameTick(float ms) {
//		//update world
//		m_world.step(ms / 1000, 8, 3);
//		//update positions and rotations
//		Set<UUID> allBodies = m_es.getAllEntitiesPossessingComponent(CPhyBody.class);
//		for(UUID entity : allBodies)
//		{
//			CPhyBody cBody = m_es.getComponent(entity, CPhyBody.class);
//			float angle = MathUtils.radiansToDegrees * cBody.body.getAngle();
//			Vector2 position = cBody.body.getPosition();
//			//Gdx.app.debug(Constants.debug_tag, "position : " + position.x + ", " + position.y);
//			m_es.getComponent(entity, CDimension.class).rotation = angle;
//			
//			m_es.getComponent(entity, CPosition.class).x = position.x * Constants.pixelsPerMeter - m_es.getComponent(entity, CDimension.class).width / 2;
//			m_es.getComponent(entity, CPosition.class).y = position.y * Constants.pixelsPerMeter - m_es.getComponent(entity, CDimension.class).height / 2;;
//		}
//		
//		//put framePair to this frame
//		if(b_registerCollisions)
//		{
//			Collection<CCollisionPairs> cPairs = m_es.getAllComponentsOfType(CCollisionPairs.class);
//			if(cPairs.size() != 1)
//			{
//				Gdx.app.error(
//						Constants.debug_tag, 
//						"PhysicsSystem error : " + cPairs.size() + " physics world", 
//						new GdxRuntimeException("PhysicsSystem error : " + cPairs.size() + " pair container components"));
//			}
//			CCollisionPairs cPair = cPairs.iterator().next();
//			ArrayList<UUID> temp1 = cPair.lastFramePair1;
//			ArrayList<UUID> temp2 = cPair.lastFramePair2;
//			cPair.lastFramePair1 = cPair.thisFramePair1;
//			cPair.lastFramePair2 = cPair.thisFramePair2;
//			cPair.thisFramePair1 = framePair1;
//			cPair.thisFramePair2 = framePair2;
//			framePair1 = temp1;
//			framePair2 = temp2;
//			framePair1.clear();
//			framePair2.clear();
//		}
//	}

	@Override
	protected void removed(Entity e) {
		CPhyBody cBody = cBodyMapper.getSafe(e);
		if(cBody != null)
		{
			m_world.destroyBody(cBody.body);
		}
	}

	@Override
	public void dispose() {
		Gdx.app.debug(Constants.debug_tag, tag + "begin dispose physics world");
		m_world.dispose();
	}

	public class PhysicSystemContactListener implements ContactListener
	{
		@Override
		public void beginContact(Contact contact) {
			Entity idA = (Entity)contact.getFixtureA().getBody().getUserData();
			Entity idB = (Entity)contact.getFixtureB().getBody().getUserData();
			if(idA == null || idB == null || !contact.isTouching())
			{
				return;
			}
			
//			if(idA == world.getManager(TagManager.class).getEntity(Constants.tag_player))
//			{
//				world.deleteEntity(idB);
//			}
//			else if (idB == world.getManager(TagManager.class).getEntity(Constants.tag_player))
//			{
//				world.deleteEntity(idA);
//			}
//			Gdx.app.debug(Constants.debug_tag, "begin contact : " + idA + ", " + idB);
		}

		@Override
		public void endContact(Contact contact) {
//			Gdx.app.debug(Constants.debug_tag, "end contact");
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			Manifold.ManifoldType type = oldManifold.getType();
			 Vector2 localPoint = oldManifold.getLocalPoint();
			 Vector2 localNormal = oldManifold.getLocalNormal();
			 int pointCount = oldManifold.getPointCount();
			 ManifoldPoint[] points = oldManifold.getPoints();
			 Gdx.app.debug(
			Constants.debug_tag,
			"pre solve, " + type +
			 ", point: " + localPoint +
			 ", local normal: " + localNormal +
			 ", #points: " + pointCount +
			 ", [" + points[0] + ", " + points[1] + "]");
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			 float[] ni = impulse.getNormalImpulses();
			 float[] ti = impulse.getTangentImpulses();
			 Gdx.app.debug(
						Constants.debug_tag,
			"post solve, normal impulses: " + ni[0] + ", " + ni[1] + ", tangent impulses: " + ti[0] + ", " + ti[1]);
		}
	}

	@Override
	public boolean handleEvent(IEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "Physics";
	}

	
}
