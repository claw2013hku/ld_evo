package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CDimension;
import com.claw.evolution.components.CPhyBody;
import com.claw.evolution.components.CWorldActor;
import com.claw.evolution.components.CPosition;
import com.claw.evolution.components.CWorldCamera;
import com.claw.evolution.events.IEvent;

/** 1. Accelerates camera that matches player's velocity delta
 *  2. Moves the camera so that the player (CPlayer) is within the viewport (CWorldCamera),
 * 		scroll (CMovable) when player is on the outer 1/3 of viewport
 	3. Constrains camera (CWorldCamera) within the game world **/
public class CameraSystem extends ASystem {
	private ComponentMapper<CWorldActor> cPlayerMapper;
	private ComponentMapper<CPosition> cPlayerPosMapper;
	private ComponentMapper<CPhyBody> cPlayerBdMapper;
	private Vector2 m_accel;
	private Vector2 m_vel;
	private Vector2 m_dist;
	private Vector2 m_playerLastV;
	
	@SuppressWarnings("unchecked")
	public CameraSystem() {
		super(Aspect.getAspectForAll(CWorldActor.class));
	}

	@Override
	protected void begin() {
	
	}

	@Override
	protected void end() {
		
	}

	@Override
	protected void initialize() {
		cPlayerMapper = world.getMapper(CWorldActor.class);
		cPlayerPosMapper = world.getMapper(CPosition.class);
		cPlayerBdMapper = world.getMapper(CPhyBody.class);
		m_accel = new Vector2();
		m_vel = new Vector2();
		m_dist = new Vector2();
		m_playerLastV = new Vector2();
	}

	@Override
	public boolean handleEvent(IEvent event) {
		return false;
	}

	@Override
	public void dispose() {
	}

	Vector3 playerPos3 = new Vector3();
	Vector3 camPos3 = new Vector3();
	Vector2 playerV = new Vector2();
	@Override
	protected void process(Entity e) {
		CWorldActor cPlayer = cPlayerMapper.get(e);
		if(cPlayer == null || cPlayer.type != CWorldActor.Type.PLAYER)
		{
			return;
		}
		CPosition cPos = cPlayerPosMapper.get(e);
		CWorldCamera cWC = world.getManager(TagManager.class).getEntity(Constants.tag_worldCamera).getComponent(CWorldCamera.class);
		playerPos3.set(cPos.x, cPos.y, 0);
		camPos3.set(cPos.x, cPos.y, 0);
		playerV.set(cPlayerBdMapper.get(e).body.getLinearVelocity());
		m_accel.set(playerV.sub(m_playerLastV)).mul(10);
		
		Vector2 del_vel = Pools.obtain(Vector2.class).set(m_accel).mul(world.getDelta() / 1000);
		m_vel.add(del_vel);
		Pools.free(del_vel);
		m_vel.x = MathUtils.clamp(m_vel.x, -1, 1);
		m_vel.y = MathUtils.clamp(m_vel.y, -0.3f, 0.3f);
		m_dist.add(m_vel);
		m_dist.x = MathUtils.clamp(m_dist.x, -cWC.camera.viewportWidth / 5.5f, cWC.camera.viewportWidth / 5.5f);
		m_dist.y = MathUtils.clamp(m_dist.y, -cWC.camera.viewportHeight / 10f, cWC.camera.viewportHeight / 10f);
		camPos3.add(m_dist.x, m_dist.y, 0);
		m_playerLastV.set(cPlayerBdMapper.get(e).body.getLinearVelocity());

		//Set camera within the world
		CDimension cWorldDim = world.getManager(TagManager.class).getEntity(Constants.tag_world).getComponent(CDimension.class);
		CPosition cWorldPos = world.getManager(TagManager.class).getEntity(Constants.tag_world).getComponent(CPosition.class);
		float leastX = cWorldPos.x - cWorldDim.width / 2 + cWC.camera.viewportWidth / 2;
		camPos3.x = Math.max(camPos3.x, leastX);
		float mostX = cWorldPos.x + cWorldDim.width / 2 - cWC.camera.viewportWidth / 2;
		camPos3.x = Math.min(camPos3.x, mostX);
		float leastY = cWorldPos.y - cWorldDim.height / 2 + cWC.camera.viewportHeight / 2;
		camPos3.y = Math.max(camPos3.y, leastY);
		float mostY = cWorldPos.y + cWorldDim.height / 2 - cWC.camera.viewportHeight / 2;
		camPos3.y = Math.min(camPos3.y, mostY);
		cWC.camera.position.set(camPos3);
		cWC.camera.update();
	}

	@Override
	public String getName() {
		return "Camera";
	}
}
