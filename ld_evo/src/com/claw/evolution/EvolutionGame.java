package com.claw.evolution;

import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.claw.evolution.singleton.AIBehaviourAssemblage;
import com.claw.evolution.singleton.Assemblage;
import com.claw.evolution.singleton.EventManager;
import com.claw.evolution.singleton.FileManager;
import com.claw.evolution.singleton.ProcessManager;
import com.claw.evolution.systems.AISystem;
import com.claw.evolution.systems.ASystem;
import com.claw.evolution.systems.ActorPositioningSystem;
import com.claw.evolution.systems.CameraSystem;
import com.claw.evolution.systems.DebugDrawSystem;
import com.claw.evolution.systems.InputSystem;
import com.claw.evolution.systems.MovementSystem;
import com.claw.evolution.systems.UISystem;
import com.claw.evolution.systems.PhysicsSystem;
import com.claw.evolution.systems.Render2DSystem;

public class EvolutionGame implements ApplicationListener {
	private World m_es;
	Array<ASystem> m_systems;
	Render2DSystem m_2dDrawSystem;
	UISystem m_uiSystem;
	
	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		
		FileManager.createInstance();
		EventManager.createInstance();
		ProcessManager.createInstance();
		
		//initialize systems and database
		m_es = new World();
		m_es.setManager(new GroupManager());
		m_es.setManager(new TagManager());

		Assemblage.createInstance(m_es);
		AIBehaviourAssemblage.createInstance();
		
		m_systems = new Array<ASystem>(true, 16, ASystem.class);
		
		//add to database
		Assemblage.loadPhyWorld(new Vector2(0, -0.1f));
		  
		PhysicsSystem ps = new PhysicsSystem(true);
		m_es.setSystem(ps);
		m_systems.add(ps);
		
		InputSystem is = new InputSystem();
		m_es.setSystem(is);
		m_systems.add(is);
		
		AISystem aiSystem = new AISystem();
		m_es.setSystem(aiSystem);
		m_systems.add(aiSystem);
		
		MovementSystem ms = new MovementSystem();
		m_es.setSystem(ms);
		m_systems.add(ms);
		
		Assemblage.loadWorldCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		CameraSystem cs = new CameraSystem();
		m_es.setSystem(cs);
		m_systems.add(cs);
		
		m_2dDrawSystem = new Render2DSystem();
		m_es.setSystem(m_2dDrawSystem);
		
		m_systems.add(m_2dDrawSystem);
		DebugDrawSystem dds = new DebugDrawSystem();
		m_es.setSystem(dds);
		m_systems.add(dds);
		
		m_uiSystem = new UISystem();
		m_es.setSystem(m_uiSystem);
		m_systems.add(m_uiSystem);
		
		m_es.initialize();
		
		Assemblage.loadMap(3200, 1500);
		Assemblage.loadAINodes();
		Assemblage.loadMapGrid();
		Assemblage.loadPlayer();
	}

	@Override
	public void dispose() {
		//dispose resources
		for( ASystem system : m_systems )
		{
			system.dispose();
		}
		EventManager.dispose();
		ProcessManager.dispose();
		FileManager.dispose();
		Assemblage.dispose();
		AIBehaviourAssemblage.dispose();
	}

	@Override
	public void render() {	
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		update(Gdx.graphics.getDeltaTime() * 1000);
	}
	
	public void update(float ms)
	{
		EventManager.update(ms);
		ProcessManager.update(ms);
		m_es.setDelta(ms);
		m_es.process();
	}

	@Override
	public void resize(int width, int height) {
		m_2dDrawSystem.resize(width, height);
		m_uiSystem.resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
