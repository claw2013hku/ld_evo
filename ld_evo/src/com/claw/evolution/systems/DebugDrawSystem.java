package com.claw.evolution.systems;

import java.util.ArrayList;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CAIAgent;
import com.claw.evolution.components.CAIMap;
import com.claw.evolution.components.CUIActor;
import com.claw.evolution.components.CWorldCamera;
import com.claw.evolution.events.DebugToggleEvent;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.singleton.Assemblage;
import com.claw.evolution.singleton.EventManager;
import com.claw.evolution.singleton.FileManager;

public class DebugDrawSystem extends ASystem {
	public static final String tag = "DebugDrawSystem : ";
	private SpriteBatch m_batch;
	private Box2DDebugRenderer debugRenderer;
	private Stage m_stage;
	private Label m_fpsLabel;
	private Label m_frameLabel;
	private Label m_aiLabel;
	private ComponentMapper<CAIAgent> cAIMapper;
	private ComponentMapper<CAIMap> cAIMapMapper;
	private ComponentMapper<CUIActor> cActMapper;
	private Array<Entity> m_aiMarks;
	private boolean m_bDraw = false;
	
	@SuppressWarnings("unchecked")
	public DebugDrawSystem() {
		super(Aspect.getAspectForAll(CAIAgent.class));
		m_batch = new SpriteBatch();
		debugRenderer = new Box2DDebugRenderer();
		m_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	@Override
	protected void initialize() {
		//Assemblage.loadMapGrid();
		Skin skin = FileManager.getResource(Gdx.files.internal("data/uiskin.json"), Skin.class);
		m_fpsLabel = new Label("", skin);
		m_fpsLabel.setPosition(0, Gdx.graphics.getHeight() - m_fpsLabel.getHeight() - 10);
		m_aiLabel = new Label("", skin);
		m_aiLabel.setPosition(0, Gdx.graphics.getHeight() - m_aiLabel.getHeight() * 2 - 30);
		m_frameLabel = new Label("", skin);
		m_frameLabel.setPosition(100, Gdx.graphics.getHeight() - m_frameLabel.getHeight() * 2 - 10);
		
		m_stage.addActor(m_fpsLabel);
		m_stage.addActor(m_aiLabel);
		m_stage.addActor(m_frameLabel);
		cAIMapper = world.getMapper(CAIAgent.class);
		cActMapper = world.getMapper(CUIActor.class);
		cAIMapMapper = world.getMapper(CAIMap.class);
		m_aiMarks = new Array<Entity>();
		
		EventManager.registerListener(this, DebugToggleEvent.class);
	}

	@Override
	public boolean handleEvent(IEvent event) {
		// TODO Auto-generated method stub
		if(event.getClass() == DebugToggleEvent.class)
		{
			m_bDraw = !m_bDraw;
			return true;
		}
		return false;
	}

	@Override
	public void dispose() {
	}
	
	ArrayList<Actor> actorsToDraw = new ArrayList<Actor>();
	CAIMap cAIMap = null;
	int markCount;
	@Override
	protected void begin() {
		super.begin();
		if(!m_bDraw) return;
		markCount = 0;
		cAIMap = cAIMapMapper.getSafe(world.getManager(TagManager.class).getEntity(Constants.tag_AIMap));
	}

	
	@Override
	protected void process(Entity e) {
		if(!m_bDraw) return;
		Array<Vector2> v2 = cAIMapper.get(e).destinations;
		for(int i = 0; i < v2.size ; i++)
		{
			if(i == 7)
			{
				i = v2.size - 1;
			}
			
			Vector2 v = v2.get(i);
			Entity markE = null;
			markCount++;
			if(markCount > m_aiMarks.size)
			{
				markE = Assemblage.loadAIDebugLabel();
				m_aiMarks.add(markE);
			}
			else
			{
				markE = m_aiMarks.get(markCount - 1);
			}
			if(i == v2.size - 1)
			{
				cActMapper.get(markE).actor.setColor(Color.GREEN);
			}
			else
			{
				float y = 1 - (v2.size - 1 - i) * 0.05f;
				y = y < 0f ? 0f : y;
				cActMapper.get(markE).actor.setColor(1,y,0,0.9f);
			}
			
			if(cAIMap.nodes.get((int)v.x).get((int)v.y).rayCasted)
			{
				((Label)cActMapper.get(markE).actor).setText("C");
			}
			else
			{
				((Label)cActMapper.get(markE).actor).setText("x");
			}
			cActMapper.get(markE).actor.setPosition(
					v.x / Constants.AI_NodeDensity * Constants.pixelsPerMeter - cActMapper.get(markE).actor.getWidth() / 2, 
					v.y / Constants.AI_NodeDensity * Constants.pixelsPerMeter - cActMapper.get(markE).actor.getHeight() / 2);
		}
	}

	Vector3 pos = new Vector3();
	
	@Override
	protected void end() {
		Camera camera = world.getManager(TagManager.class).getEntity(Constants.tag_worldCamera).getComponent(CWorldCamera.class).camera;
		
		if(m_bDraw)
		{
			if(markCount < m_aiMarks.size)
			{
				for(int i = 0; i < m_aiMarks.size - markCount ; i++)
				{
					cActMapper.get(m_aiMarks.get(m_aiMarks.size - 1 - i)).actor.setColor(0, 0, 0, 0);
				}
			}
			debugRenderer.render(Assemblage.getPhyWorld().world, camera.combined.cpy().scale(
					Constants.pixelsPerMeter, 
					Constants.pixelsPerMeter, 
					1));
			
			m_batch.setProjectionMatrix(camera.combined);
			m_batch.begin();

			ImmutableBag<Entity> e2 = world.getManager(GroupManager.class).getEntities(Constants.tag_AIGrid);
			for(int i = 0; i < e2.size(); i++)
			{
				pos.set(e2.get(i).getComponent(CUIActor.class).actor.getX(), e2.get(i).getComponent(CUIActor.class).actor.getY(), 0);
				if(!e2.get(i).getComponent(CUIActor.class).actor.isVisible()) continue;
				if(camera.frustum.sphereInFrustumWithoutNearFar(
						pos, 
						e2.get(i).getComponent(CUIActor.class).actor.getWidth()))
				{
					e2.get(i).getComponent(CUIActor.class).actor.draw(m_batch, 1f);
				}
			}

			m_batch.end();
		}
		
		ImmutableBag<Entity> eAI = world.getManager(GroupManager.class).getEntities(Constants.tag_AIplayer);
		int fps = Gdx.graphics.getFramesPerSecond();
		if(fps != lastFPS)
		{
			m_fpsLabel.setText("FPS: " + fps);
			if(fps >= 55)
			{
				m_fpsLabel.setColor(Color.GREEN);
			}
			else if (fps >= 40)
			{
				m_fpsLabel.setColor(Color.YELLOW);
			}
			else
			{
				m_fpsLabel.setColor(Color.RED);
			}
		}
		lastFPS = fps;
		
		if(lastAISize != eAI.size())
		{
			m_aiLabel.setText("AIs: " + eAI.size());
		}
		lastAISize = eAI.size();
		super.end();
		
		ImmutableBag<EntitySystem> systems = world.getSystems();
		String frameTimeDisplay = new String();
		for(int i = 0; i < systems.size(); i++)
		{
			if(((ASystem)systems.get(i)).getFrameTime() == -1) continue;
			frameTimeDisplay += ((ASystem)systems.get(i)).getName() + ": " + ((ASystem)systems.get(i)).getFrameTime() + "ms   ";
			if(i > 0 && i % 4  == 0)
			{
				frameTimeDisplay += "\n";
			}
		}
		m_frameLabel.setText(frameTimeDisplay);
		
		m_stage.getSpriteBatch().setProjectionMatrix(camera.combined);
		m_stage.draw();
	}
	int lastAISize = 0;
	int lastFPS = 0;
	@Override
	public String getName() {
		return "DebugDraw";
	}
}
