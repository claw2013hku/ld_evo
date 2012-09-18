package com.claw.evolution.singleton;

import java.util.Comparator;
import java.util.Random;

import aurelienribon.bodyeditor.BodyEditorLoader;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Scaling;
import com.claw.evolution.Constants;
import com.claw.evolution.ai.AIConditionResult;
import com.claw.evolution.ai.AIEscapeState;
import com.claw.evolution.ai.AINode;
import com.claw.evolution.ai.AIPatrolState;
import com.claw.evolution.ai.AIState;
import com.claw.evolution.components.*;
import com.claw.evolution.components.CUIMenu.MenuType;
import com.claw.evolution.components.CWorldActor.Type;
import com.claw.evolution.events.*;
import com.claw.evolution.systems.AISystem;

/**puts components into and gets unique components from the entity system, cleanup entities **/
public class Assemblage {
	static final String tag = "Assemblage : ";
	private static Assemblage instance;
	
	private Assemblage(){};
	 
	public static Assemblage getInstance()
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
	 
	public static void createInstance(World es)
	{
		if(instance != null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "instance already created");
		}
		else
		{
			instance = new Assemblage();
		}
		instance.es = es;
	}
	
	private World es;
	
	public static void setEntityManagerInstance(World es)
	{
		getInstance().es = es;
	}
	
	private static void checkEntityManagerInstance()
	{
		if(getInstance().es == null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "not set to an entity manager instance");
		}
	}
	
	public static World getES()
	{
		checkEntityManagerInstance();
		return getInstance().es;
	}

	public static Entity loadTitle()
	{
		World es = getES();
		
		Gdx.app.debug(Constants.debug_tag, tag + "making title screen");
		Texture texture = FileManager.getResource(Gdx.files.internal("data/libgdx.png"), Texture.class);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		CDrawElement cDraw = Pools.obtain(CDrawElement.class);
		TextureRegion region = Pools.obtain(TextureRegion.class);
		region.setRegion(texture);
		region.setRegion(0, 0, 512, 275);
		cDraw.textureRegion = region;
		cDraw.setOriginFromTextureRegion();
		
		CDimension cDim = Pools.obtain(CDimension.class);
		cDim.width = 480f;
		cDim.height = 480f * region.getRegionHeight() / region.getRegionWidth();
		
		CPosition cPos = Pools.obtain(CPosition.class);
		cPos.x = -cDim.width / 2;
		cPos.y = -cDim.height / 2;
		
		Entity entity = es.createEntity();
		entity.addComponent(cDim);
		entity.addComponent(cPos);
		entity.addComponent(cDraw);
		es.addEntity(entity);
		return entity;
	}
	
	public static Entity loadPhyWorld(Vector2 gravity)
	{
		World es = getES();
		
		Gdx.app.debug(Constants.debug_tag, tag + "making physical world");
		CPhyWorld cPhy = Pools.obtain(CPhyWorld.class);
		com.badlogic.gdx.physics.box2d.World world = new com.badlogic.gdx.physics.box2d.World(gravity, true);
		cPhy.world = world;
		
		Entity entity = es.createEntity();
		entity.addComponent(cPhy);
		es.addEntity(entity);
		
		es.getManager(TagManager.class).register(Constants.tag_phyWorld, entity);
		return entity;
	}
	
	public static CPhyWorld getPhyWorld()
	{
		return getES().getManager(TagManager.class).getEntity(Constants.tag_phyWorld).getComponent(CPhyWorld.class);
	}
	
	public static Entity loadMap(int width, int height)
	{
		World es = getES();
		Texture texture = FileManager.getResource(Gdx.files.internal("data/backgrounds/small-main.jpg"), Texture.class);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		CPosition cWorldPos = Pools.obtain(CPosition.class);
		cWorldPos.x = width / 2;
		cWorldPos.y = height / 2;
		
		CDimension cWorldDim = Pools.obtain(CDimension.class);
		cWorldDim.width = width;
		cWorldDim.height = height;
		
		CPhyBody cBody = loadBasicBody("bg", "bg", BodyType.StaticBody, cWorldPos, cWorldDim);
		
	    Entity entity = es.createEntity();
		entity.addComponent(cBody);
		entity.addComponent(cWorldPos);
		entity.addComponent(cWorldDim);
		es.addEntity(entity);
		
		es.getManager(GroupManager.class).add(entity, Constants.tag_phyBody);
		es.getManager(TagManager.class).register(Constants.tag_world, entity);
		
		float texelPerPixel = (float)width / (float)texture.getWidth();
		int pixel = 256;
		int lasti = texture.getWidth() / pixel + (texture.getWidth() % pixel == 0 ? 0 : 1) - 1;
		int lastj = texture.getHeight() / pixel + (texture.getHeight() % pixel == 0 ? 0 : 1) - 1;
		for(int i = 0; i <= lasti; i++)
		{
			for(int j = 0; j <= lastj; j++)
			{
				float posx = i == lasti ? i * (float)pixel + texture.getWidth() % pixel / 2f : i * pixel + pixel / 2;
				float posy = j == lastj ? j * (float)pixel + texture.getHeight() % pixel / 2f : j * pixel + pixel / 2;
				
				CPosition cPos = Pools.obtain(CPosition.class);
				cPos.x = posx * texelPerPixel;
				cPos.y = posy * texelPerPixel;
				
				float dimx = i == lasti ? texture.getWidth() % pixel : pixel;
				float dimy = j == lastj ? texture.getHeight() % pixel : pixel;
				
				CDimension cDim = Pools.obtain(CDimension.class);
				cDim.width = dimx * texelPerPixel;
				cDim.height = dimy * texelPerPixel;
				
				TextureRegion region = Pools.obtain(TextureRegion.class);
				region.setRegion(texture);
				region.setRegion(
						i * pixel, 
						j == lastj ? 0 : texture.getHeight() - ((j + 1) * pixel), 
						i == lasti ? texture.getWidth() % pixel  : pixel,
						j == lastj ? texture.getHeight() % pixel : pixel);
				
				CDrawElement cDraw = Pools.obtain(CDrawElement.class);
				cDraw.textureRegion = region;
				cDraw.setOriginFromTextureRegion();
				
				Entity subE = es.createEntity();
				subE.addComponent(cPos);
				subE.addComponent(cDim);
				subE.addComponent(cDraw);
				es.addEntity(subE);
			}
		}
		return entity;
	}

	private static CPhyBody loadBasicBody(String json, String name, BodyType type, CPosition cPos, CDimension cDim)
	{
		FixtureDef fd = new FixtureDef();
	    fd.density = 1;
	    fd.friction = 0.005f;
	    fd.restitution = 0.01f;
	    return loadBasicBody(json, name, type, cPos, cDim, fd);
	}
	
	static BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("data/bg.json"));
	
	private static CPhyBody loadBasicBody(String json, String name, BodyType type, CPosition cPos, CDimension cDim, FixtureDef fd)
	{
		//BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("data/"+json+".json"));
		
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = type;
		boxBodyDef.position.x = cPos.x / Constants.pixelsPerMeter;
		boxBodyDef.position.y = cPos.y / Constants.pixelsPerMeter;
		
		com.badlogic.gdx.physics.box2d.World world = getPhyWorld().world;
		Body boxBody = world.createBody(boxBodyDef);
		
	    loader.attachFixture(boxBody, name, fd, ((float)cDim.width) / Constants.pixelsPerMeter);
    	
	    CPhyBody cBody = Pools.obtain(CPhyBody.class);
	    cBody.body = boxBody;
	    return cBody;
	}
	
	public static void loadMapGrid()
	{
		Skin skin = FileManager.getResource(Gdx.files.internal("data/uiskin.json"), Skin.class);
		World es = getInstance().es;
		CDimension cDim = es.getManager(TagManager.class).getEntity(Constants.tag_world).getComponent(CDimension.class);
		
		for(int i = 0; i <= cDim.width; i += 200)
		{
			for(int j = 0; j <= cDim.height; j += 200)
			{
				Label gridLabel = new Label(".(" + i + "," + j + ")", skin);
				gridLabel.setPosition(
						i - gridLabel.getWidth() / 2, 
						j - gridLabel.getHeight() / 2);
				
				Entity entity = es.createEntity();
				CUIActor cUI = Pools.obtain(CUIActor.class);
				cUI.actor = gridLabel;
				cUI.zOrder = Constants.z_mapGrid;
				es.getManager(GroupManager.class).add(entity, Constants.tag_mapGrid);
			}
		}
	}
	
	
	// AI-related
	public static int metersToAI(float m)
	{
		return (int)(m * Constants.AI_NodeDensity + 0.5f);
	}
	
	public static Vector2 metersToAI(Vector2 v)
	{
		Vector2 v2 = Pools.obtain(Vector2.class);
		v2.set(metersToAI(v.x), metersToAI(v.y));
		return v2;
	}
	
	public static Vector2 switchMetersToAI(Vector2 v)
	{
		v.set(metersToAI(v.x), metersToAI(v.y));
		return v;
	}
	
	public static Vector2 AIToMeters(Vector2 v)
	{
		Vector2 v2 = Pools.obtain(Vector2.class);
		v2.set(v.x / Constants.AI_NodeDensity, v.y / Constants.AI_NodeDensity);
		return v2;
	}
	
	public static Vector2 switchAIToMeters(Vector2 v)
	{
		v.set(v.x / Constants.AI_NodeDensity, v.y / Constants.AI_NodeDensity);
		return v;
	}
	
	public static Vector2 AIToMeters(int x, int y)
	{
		Vector2 v2 = Pools.obtain(Vector2.class);
		v2.set(x / Constants.AI_NodeDensity, y / Constants.AI_NodeDensity);
		return v2;
	}
	
	public static float AIToMeters(int x)
	{
		return x / Constants.AI_NodeDensity;
	}
	
	public static Vector2 CPosToMeters(CPosition cPos)
	{
		return Pools.obtain(Vector2.class).set(cPos.x / Constants.pixelsPerMeter, cPos.y / Constants.pixelsPerMeter);
	}
	
	public static Vector2 CPosToAI(CPosition cPos)
	{
		return Pools.obtain(Vector2.class).set(metersToAI(cPos.x / Constants.pixelsPerMeter), metersToAI(cPos.y / Constants.pixelsPerMeter));
	}
	
	static Vector2 testPt = new Vector2();
	static boolean calledBack = false;
	static QueryCallback qcb = new QueryCallback()
	{
		@Override
		public boolean reportFixture(Fixture fixture) {
			calledBack = true;
			return false;
		}
	};
	static Array<Vector2> resPts = new Array<Vector2>();
	static Array<Vector2> resAIPts = new Array<Vector2>();
	static RayCastCallback rcb = new RayCastCallback()
	{
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction) {
			Vector2 pt = Pools.obtain(Vector2.class);
			pt.set(point);
			resPts.add(pt);
			resAIPts.add(metersToAI(point));
			return -1;
		}
	};
	
	static Comparator<Vector2> vectorYComp = new Comparator<Vector2>()
		{
			public int compare(Vector2 arg0, Vector2 arg1) {
				return Float.compare(arg0.y, arg1.y);
		}
	};
		
	public static void loadAINodes()
	{
		Gdx.app.debug(Constants.debug_tag, "loading AI nodes");
		World es = getES();
		Skin skin = FileManager.getResource(Gdx.files.internal("data/uiskin.json"), Skin.class);
		
		CDimension cDim = es.getManager(TagManager.class).getEntity(Constants.tag_world).getComponent(CDimension.class);
		//1 node per 5 meter square
		float widthM = cDim.width / Constants.pixelsPerMeter;
		float heightM = cDim.height / Constants.pixelsPerMeter;
		int totalX = (int)(widthM * Constants.AI_NodeDensity);
		int totalY = (int)(heightM * Constants.AI_NodeDensity);
		
		CAIMap cAIMap = Pools.obtain(CAIMap.class);
		Array<Array<AINode>> nodes = cAIMap.nodes;
		
		for(int k = 0; k < totalX; k++)
		{
			nodes.add(new Array<AINode>());
		}
		
		CPosition cTestPos = Pools.obtain(CPosition.class);
		CDimension cTestDim = Pools.obtain(CDimension.class);
		cTestDim.width = ((1 / Constants.AI_NodeDensity) / 3f) * Constants.pixelsPerMeter;
		CPhyBody cTestBody = 
				loadBasicBody(
						"bg", 
						"char-small", 
						BodyType.DynamicBody, 
						cTestPos, 
						cTestDim);

		//Cast a straight ray for each X
		for(int i = 0; i < totalX; i++)
		{
			Vector2 testPt1 = Pools.obtain(Vector2.class);
			Vector2 testPt2 = Pools.obtain(Vector2.class);
			testPt1.set(
					i / Constants.AI_NodeDensity, 
					0 / Constants.AI_NodeDensity);
			testPt2.set(
					i / Constants.AI_NodeDensity, 
					(totalY - 1) / Constants.AI_NodeDensity);
			getPhyWorld().world.rayCast(rcb, testPt1, testPt2);
			getPhyWorld().world.rayCast(rcb, testPt2, testPt1);
			
			boolean startInside = true;
			BooleanArray containsPts = new BooleanArray();
			for(int c = 0; c < totalY; c++)
			{
				containsPts.add(false);
			}
			resPts.sort(vectorYComp);
			resAIPts.sort(vectorYComp);
			//validate / invalidate non-ray-casted points using ray-casted points
			for(int a = -1; a < resAIPts.size; a++)
			{
				int startB = (a == -1) ? 0 : (int)resAIPts.get(a).y;
				int endB = ((a == resAIPts.size - 1) ? totalY - 1 : (int)(resAIPts.get(a + 1).y));
				for(int b = startB; b <= endB; b++)
				{
					containsPts.set(b, startInside);
				}
				startInside = !startInside;
			}
			
			//validate / invalidate ray-casted points using bodies
			int lastY = -1;
			for(Vector2 v : resAIPts)
			{
				if(lastY == (int)(v.y))
				{
					continue;
				}
				lastY = (int)(v.y);
				cTestBody.body.setTransform(
						new Vector2(
								(int)(v.x) / Constants.AI_NodeDensity, 
								(int)(v.y) / Constants.AI_NodeDensity), 0);
				getPhyWorld().world.step(16, 0, 0);
				boolean containsPt = false;
				for(Contact c : getPhyWorld().world.getContactList())
				{
					if(c.isTouching())
					{
						containsPt = true;
						break;
					}
				}
				containsPts.set((int)(v.y), containsPt);
			}
			
			for(int j = 0; j < totalY; j++)
			{
				boolean containsPt = containsPts.get(j);
				Label nodeLabel = new Label(containsPt ? "o" : "x", skin);
				if(containsPt)
				{
					nodeLabel.setColor(0,0,0,1f);
				}
				else
				{
					nodeLabel.setColor(1,1,1,0.5f);
				}
				Vector2 testAIPt = Pools.obtain(Vector2.class).set(i, j);
				boolean rayCasted = false;
				if(resAIPts.contains(testAIPt, false))
				{
					nodeLabel.setText("C");
					rayCasted = true;
				}
				Pools.free(testAIPt);
				
				nodeLabel.setPosition(
						i / Constants.AI_NodeDensity * Constants.pixelsPerMeter - nodeLabel.getWidth() / 2, 
						j / Constants.AI_NodeDensity * Constants.pixelsPerMeter - nodeLabel.getHeight() / 2);
				
				CUIActor cUIActor = Pools.obtain(CUIActor.class);
				cUIActor.actor = nodeLabel;
				cUIActor.zOrder = Constants.z_AIGrid;
				
				Entity entity = es.createEntity();
				entity.addComponent(cUIActor);
				es.addEntity(entity);
				
				es.getManager(GroupManager.class).add(entity, Constants.tag_AIGrid);
				
				AINode newNode = Pools.obtain(AINode.class);
				newNode.valid = !containsPt;
				newNode.x = i;
				newNode.y = j;
				newNode.rayCasted = rayCasted;
				nodes.get(i).add(newNode);
			}
			Pools.free(resPts);
			resPts.clear();
			Pools.free(resAIPts);
			resAIPts.clear();
		}
		getPhyWorld().world.destroyBody(cTestBody.body);
		getPhyWorld().world.step(16 / 1000, 0, 0);
		Pools.free(cTestPos);
		Pools.free(cTestDim);
		cAIMap.lastX = totalX - 1;
		cAIMap.lastY = totalY - 1;
		
		Entity e = es.createEntity();
		e.addComponent(cAIMap);
		es.addEntity(e);
		
		es.getManager(TagManager.class).register(Constants.tag_AIMap, e);
		Gdx.app.debug(Constants.debug_tag, "finished loading AI nodes");
	}
	
	public static Entity loadAIDebugLabel()
	{
		World es = getES();
		Skin skin = FileManager.getResource(Gdx.files.internal("data/uiskin.json"), Skin.class);
		Label nodeLabel = new Label("x", skin);
		nodeLabel.setColor(Color.RED);
//		nodeLabel.setPosition(
//				i / Constants.AI_NodeDensity * Constants.pixelsPerMeter - nodeLabel.getWidth() / 2, 
//				j / Constants.AI_NodeDensity * Constants.pixelsPerMeter - nodeLabel.getHeight() / 2);
		
		CUIActor cUIActor = Pools.obtain(CUIActor.class);
		cUIActor.actor = nodeLabel;
		cUIActor.zOrder = Constants.z_AIGrid;
		
		Entity entity = es.createEntity();
		entity.addComponent(cUIActor);
		es.addEntity(entity);
		
		es.getManager(GroupManager.class).add(entity, Constants.tag_AIGrid);
		return entity;
	}

	public static void loadWorldCamera(float m_screenWidth, float m_screenHeight)
	{
		Camera cam = new OrthographicCamera(m_screenWidth, m_screenHeight);
		cam.position.x = 800;
		cam.position.y = 400;
		cam.update();
		
		CWorldCamera cWC = Pools.obtain(CWorldCamera.class);
		cWC.camera = cam;
		
		Entity entity = getES().createEntity();
		entity.addComponent(cWC);
		getInstance().es.addEntity(entity);
		
		getInstance().es.getManager(TagManager.class).register(Constants.tag_worldCamera, entity);
	}
	
	public static void loadPlayer()
	{
		Gdx.app.debug(Constants.debug_tag, "making player");
		
		CWorldActor cWorldActor = Pools.obtain(CWorldActor.class);
		cWorldActor.type = CWorldActor.Type.PLAYER;
		
		CDimension cWorldDim = getES().getManager(TagManager.class).getEntity(Constants.tag_world).getComponent(CDimension.class);
		
		CPosition cPos = Pools.obtain(CPosition.class);
		cPos.x = cWorldDim.width / 2;
		cPos.y = cWorldDim.height / 2;
		
		Texture texture = FileManager.getResource(Gdx.files.internal("data/characters/small.png"), Texture.class);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		CDimension cDim = Pools.obtain(CDimension.class);
		cDim.width = texture.getWidth();
		cDim.height = texture.getHeight();
		
		FixtureDef fd = new FixtureDef();
	    fd.density = 1;
	    fd.friction = 0.5f;
	    fd.restitution = 0.75f;
	    
		CPhyBody cBody = loadBasicBody("bg", "char-small", BodyType.DynamicBody, cPos, cDim, fd);
    	cBody.body.setLinearDamping(3f);
		cBody.body.setFixedRotation(true);
		
    	TextureRegion region = Pools.obtain(TextureRegion.class);
    	region.setRegion(texture);
    	
    	CDrawElement cDraw = Pools.obtain(CDrawElement.class);
    	cDraw.textureRegion = region;
    	cDraw.setOriginFromTextureRegion();
    	
		CController cControl = Pools.obtain(CController.class);
		
		AIPatrolState aiState = Pools.obtain(AIPatrolState.class);
		aiState.patrolPt.set(
				(cWorldDim.width / 2) / Constants.pixelsPerMeter * Constants.AI_NodeDensity, 
				(cWorldDim.height / 2) / Constants.pixelsPerMeter * Constants.AI_NodeDensity);
		aiState.patrolRange = 50;
		
		CAIAgent cAI = Pools.obtain(CAIAgent.class);
		cAI.state = aiState;
		
		Entity entity = getES().createEntity();
		entity.addComponent(cWorldActor);
		entity.addComponent(cDim);
    	entity.addComponent(cPos);
    	cBody.body.setUserData(entity);
		entity.addComponent(cBody);
		entity.addComponent(cDraw);
		entity.addComponent(cControl);
		//entity.addComponent(cAI);
		
		getInstance().es.addEntity(entity);
		
		getInstance().es.getManager(TagManager.class).register(Constants.tag_player, entity);
	}
	
	public static void loadTestPlayer(int x, int y)
	{
		Gdx.app.debug(Constants.debug_tag, "making player");
		
		CWorldActor cWorldActor = Pools.obtain(CWorldActor.class);
		cWorldActor.type = CWorldActor.Type.OTHERS;
		
		CPosition cPos = Pools.obtain(CPosition.class);
		cPos.x = x;
		cPos.y = y;
		
		Texture texture = FileManager.getResource(Gdx.files.internal("data/characters/small.png"), Texture.class);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		CDimension cDim = Pools.obtain(CDimension.class);
		cDim.width = texture.getWidth() * 0.75f;
		cDim.height = texture.getHeight() * 0.75f;
		
		CPhyBody cBody = loadBasicBody("bg", "char-small", BodyType.DynamicBody, cPos, cDim);
    	cBody.body.setLinearDamping(9f);
		cBody.body.setFixedRotation(true);
		
    	TextureRegion region = Pools.obtain(TextureRegion.class);
    	region.setRegion(texture);
    	
    	CDrawElement cDraw = Pools.obtain(CDrawElement.class);
    	cDraw.textureRegion = region;
    	cDraw.setOriginFromTextureRegion();
    	
		CController cControl = Pools.obtain(CController.class);
		
//		AIEscapeState aiState = Pools.obtain(AIEscapeState.class);
//		aiState.escapeRange = 6f / Constants.AI_NodeDensity;
		
		CAIAgent cAI = Pools.obtain(CAIAgent.class);
		cAI.state = null;
		
		AIConditionResult con1 = AIBehaviourAssemblage.getSimpleEscapeCR(
				Type.PLAYER,
				6f / Constants.AI_NodeDensity,
				3f / Constants.AI_NodeDensity);
		AIConditionResult con2 = AIBehaviourAssemblage.getSimplePatrol(2f / Constants.AI_NodeDensity);
		
//		AIActorWithinRadiusCondition con1 = Pools.obtain(AIActorWithinRadiusCondition.class);
//		con1.radius = 3f / Constants.AI_NodeDensity;
//		con1.triggerState = aiState;
//		con1.retain = false;
//		
//		AIAlwaysTrueCondition con2 = Pools.obtain(AIAlwaysTrueCondition.class);
//		con2.triggerState = aiState2;
//		con2.retain = true;
		
		CAIBehaviours cAIB = Pools.obtain(CAIBehaviours.class);
		cAIB.triggers.add(con1);
		cAIB.triggers.add(con2);
		
		Entity entity = getES().createEntity();
		entity.addComponent(cWorldActor);
		entity.addComponent(cDim);
    	entity.addComponent(cPos);
    	cBody.body.setUserData(entity);
    	entity.addComponent(cBody);
		entity.addComponent(cDraw);
		entity.addComponent(cControl);
		entity.addComponent(cAI);
		entity.addComponent(cAIB);
		
		getES().addEntity(entity);
		
		getES().getManager(GroupManager.class).add(entity, Constants.tag_AIplayer);
	}
	
	public static void loadPositionMap()
	{
		CWorldActorPositionMap cMap = Pools.obtain(CWorldActorPositionMap.class);
		
		Entity e = getES().createEntity();
		e.addComponent(cMap);
		getES().addEntity(e);
		
		getES().getManager(TagManager.class).register(Constants.tag_PosMap, e);
	}
	
	//				UI elements, actions should not be set here but inside the UISystem			  //
	//				getCUI().stage.addActor() should be called at the end of every make UI methods//
	//	  			preferably set them to invisible (later set visible by UISystem				  //
	//				Z-order index must be set after stage.add()									  //
	public static Entity loadUIStage()
	{
		World es = getES();
		
		Stage stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		
		InputMultiplexer i = new InputMultiplexer();
		if(Gdx.input.getInputProcessor() != null)
		{
			i.addProcessor(Gdx.input.getInputProcessor());
		}
		i.addProcessor(0, stage);
		Gdx.input.setInputProcessor(i);
		
		CUI cUI = Pools.obtain(CUI.class);
		cUI.stage = stage;
		
		Entity entity = es.createEntity();
		entity.addComponent(cUI);
		es.addEntity(entity);
		
		es.getManager(TagManager.class).register(Constants.tag_UI, entity);
		
		return entity;
	}
	
	public static CUI getUIStage()
	{
		World es = getES();
		return es.getManager(TagManager.class).getEntity(Constants.tag_UI).getComponent(CUI.class);
	}
	
	static String[] listEntries = {"are you on 9?", "you are really on99", "The meaning of life", "Is hard to come by",
			"This is a list entry", "And another one", "The meaning of life", "Is hard to come by", "This is a list entry",
			"And another one", "The meaning of life", "Is hard to come by", "This is a list entry", "And another one",
			"The meaning of life", "Is hard to come by", "This is a list entry", "And another one", "The meaning of life",
			"Is hard to come by"};
	/**requires CUI**/
	public static Entity loadInGameMenu()
	{
		World es = getES();
		
		Skin skin = FileManager.getResource(Gdx.files.internal("data/uiskin.json"), Skin.class);
		Texture texture1 = FileManager.getResource(Gdx.files.internal("data/badlogicsmall.jpg"), Texture.class);
//		Texture texture2 = FileManager.getResource(Gdx.files.internal("data/badlogic.jpg"), Texture.class);
//		Texture texture3 = FileManager.getResource(Gdx.files.internal("data/characters/tadpole.png"), Texture.class);
		TextureRegion image = new TextureRegion(texture1);
		TextureRegion imageFlipped = new TextureRegion(image);
		imageFlipped.flip(true, true);
//		TextureRegion image2 = new TextureRegion(texture2);
//		TextureRegion image3 = new TextureRegion(texture3);

		ImageButtonStyle style = new ImageButtonStyle(skin.get(ButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(image);
		style.imageDown = new TextureRegionDrawable(imageFlipped);
		ImageButton iconButton = new ImageButton(style);
		iconButton.addListener(
				new ClickListener()
				{
					@Override
					public void clicked(InputEvent event, float x, float y) {
						//ButtonEvent evt = Pools.obtain(ButtonEvent.class);
						//evt.type = ButtonEvent.ButtonType.DEBUG;
						EventManager.pushEvent(Pools.obtain(DebugToggleEvent.class));
					}
				}
		);
		Button buttonMulti = new TextButton("Multi\nLine\nToggle", skin, "toggle");
		buttonMulti.addListener(
				new ClickListener()
				{
					@Override
					public void clicked(InputEvent event, float x, float y) {
						ButtonEvent evt = Pools.obtain(ButtonEvent.class);
						evt.type = ButtonEvent.ButtonType.ADD_PLAYER;
						EventManager.pushEvent(evt);
					}
				}
		);
		Button imgButton = new Button(new Image(image), skin);
		imgButton.addListener(
				new ClickListener()
				{
					@Override
					public void clicked(InputEvent event, float x, float y) {
						ButtonEvent evt = Pools.obtain(ButtonEvent.class);
						evt.type = ButtonEvent.ButtonType.REMOVE_PLAYER;
						EventManager.pushEvent(evt);
					}
				}
		);
		Button imgToggleButton = new Button(new Image(image), skin, "toggle");
		
		CheckBox checkBox = new CheckBox("Check me", skin);
		TextField textfield = new TextField("", skin);
		textfield.setMessageText("Click here!");
		SelectBox dropdown = new SelectBox(new String[] {"Android", "Windows", "Linux", "OSX"}, skin);
//		Image imageActor = new Image(image2);
//		ScrollPane scrollPane = new ScrollPane(imageActor);
		List list = new List(listEntries, skin);
		ScrollPane scrollPane2 = new ScrollPane(list, skin);
		scrollPane2.setFlickScroll(false);
		//new image
//		Image imageActor2 = new Image(image3);
//		ScrollPane scrollPane3 = new ScrollPane(imageActor2);
		//new image end
		
//		SplitPane splitPane = new SplitPane(scrollPane, scrollPane2, false, skin, "default-horizontal");
//		SplitPane splitPane2 = new SplitPane(splitPane, scrollPane3, false, skin, "default-horizontal");
		
		Label fpsLabel = new Label("fps:", skin);
		
		// configures an example of a TextField in password mode.
		final Label passwordLabel = new Label("Textfield in password mode: ", skin);
		final TextField passwordTextField = new TextField("", skin);
		passwordTextField.setMessageText("password");
		passwordTextField.setPasswordCharacter('*');
		passwordTextField.setPasswordMode(true);

		// window.debug();
		Window window = new Window("Dialog", skin);
		window.setPosition(0, 0);
		window.defaults().spaceBottom(10);
		window.row().fill().expandX();
		window.add(iconButton);
		window.add(buttonMulti);
		window.add(imgButton);
		window.add(imgToggleButton);
		window.row();
		window.add(checkBox);
		//window.add(slider).minWidth(100).fillX().colspan(3);
		window.row();
		window.add(dropdown);
		window.add(textfield).minWidth(100).expandX().fillX().colspan(3);
		window.row();
//		window.add(splitPane2).fill().expand().colspan(4).maxHeight(200);
		window.row();
		window.add(passwordLabel).colspan(2);
		window.add(passwordTextField).minWidth(100).expandX().fillX().colspan(2);
		window.row();
		window.add(fpsLabel).colspan(4);
		window.pack();
		textfield.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});
		window.setColor(Color.RED);
		window.setVisible(false);
		getUIStage().stage.addActor(window);
		
		CUIActor cUIActor = Pools.obtain(CUIActor.class);
		cUIActor.actor = window;
		cUIActor.zOrder = Constants.z_uiMenu;
		
		CUIMenu cUIMenu = Pools.obtain(CUIMenu.class);
		cUIMenu.shown = false;
		cUIMenu.type = CUIMenu.MenuType.IN_GAME_LAUNCHED;
		
		Entity entity = es.createEntity();
		entity.addComponent(cUIActor);
		entity.addComponent(cUIMenu);
		es.addEntity(entity);
		
		es.getManager(GroupManager.class).add(entity, Constants.tag_UIMenu);
		es.getManager(GroupManager.class).add(entity, Constants.tag_UIActor);
		
		setActorZ(entity, Constants.z_uiMenu);
		
		return entity;
	}
	
	/**requires CUI**/
	public static Entity loadSplash()
	{
		checkEntityManagerInstance();
		World es = getInstance().es;
		
		Texture texture = FileManager.getResource(Gdx.files.internal("data/characters/tadpole.png"), Texture.class);
        TextureRegion image = new TextureRegion(texture);
        Drawable splashDrawable = new TextureRegionDrawable( image );
					
        Image splashImage = new Image( splashDrawable, Scaling.stretch );
        splashImage.setFillParent(true);
        splashImage.setVisible(false);
        getUIStage().stage.addActor(splashImage);
		
        CUIActor cUIActor = Pools.obtain(CUIActor.class);
        cUIActor.actor = splashImage;
        cUIActor.zOrder = Constants.z_splash;
        
        Entity entity = es.createEntity();
        entity.addComponent(cUIActor);
		es.addEntity(entity);
		
		es.getManager(GroupManager.class).add(entity, Constants.tag_UIActor);
		
		setActorZ(entity, Constants.z_splash);
		
		return entity;
	}
	
	private static void setActorZ(Entity e, int z)
	{
		checkEntityManagerInstance();
		World es = getInstance().es;
		ImmutableBag<Entity> actors = es.getManager(GroupManager.class).getEntities(Constants.tag_UIActor);
		int actorsWithHigherZ = 0;
		for(int i = 0; i < actors.size(); i++)
		{
			if(e == actors.get(i)) continue;
			if(actors.get(i).getComponent(CUIActor.class).zOrder > z)
			{
				actorsWithHigherZ++;
			}
		}
		Actor actor = e.getComponent(CUIActor.class).actor;
		actor.setZIndex(actors.size() - 1 - actorsWithHigherZ);
	}

	private static Entity newUIEntity(Actor actor, int zOrder, boolean isMenu, CUIMenu.MenuType type)
	{
		actor.setVisible(false);
		getUIStage().stage.addActor(actor);
		
		CUIActor cUIActor = Pools.obtain(CUIActor.class);
		cUIActor.actor = actor;
		cUIActor.zOrder = Constants.z_uiMenu;
		
		Entity entity = getES().createEntity();
		entity.addComponent(cUIActor);
		
		getES().getManager(GroupManager.class).add(entity, Constants.tag_UIActor);
		
		if(isMenu)
		{
			CUIMenu cUIMenu = Pools.obtain(CUIMenu.class);
			cUIMenu.shown = false;
			cUIMenu.type = CUIMenu.MenuType.IN_GAME_LAUNCHED;
			entity.addComponent(cUIMenu);
			getES().getManager(GroupManager.class).add(entity, Constants.tag_UIMenu);
		}
		
		getES().addEntity(entity);

		setActorZ(entity, zOrder);
		return entity;
	}
	
	private static Entity newUIActorEntity(Actor actor, int zOrder)
	{
		return newUIEntity(actor, zOrder, false, MenuType.MAIN);
	}
	
	private static Entity newUIMenuEntity(Actor actor, int zOrder, MenuType type)
	{
		return newUIEntity(actor, zOrder, true, type);
	}

	
	//modify or add similar methods for loading UI actor / menu
	public static Entity loadSampleUIActor()
	{
		Texture texture = FileManager.getResource(Gdx.files.internal("data/characters/tadpole.png"), Texture.class);
        TextureRegion textureRegion = new TextureRegion(texture);
        Drawable splashDrawable = new TextureRegionDrawable( textureRegion );
       
        Image sampleImage = new Image( splashDrawable, Scaling.stretch );
        sampleImage.setFillParent(true);
        
        //newUIActorEntity() adds required components to the entity system for a UI actor e.g. image
        return newUIActorEntity(sampleImage, Constants.z_splash);
	}
	
	public static Entity loadSampleUIMenu()
	{
		Skin sampleSkin = FileManager.getResource(Gdx.files.internal("data/uiskin.json"), Skin.class);
		Texture texture1 = FileManager.getResource(Gdx.files.internal("data/badlogicsmall.jpg"), Texture.class);
		TextureRegion image = new TextureRegion(texture1);
		TextureRegion imageFlipped = new TextureRegion(image);
		imageFlipped.flip(true, true);
		
		ImageButtonStyle style = new ImageButtonStyle(sampleSkin.get(ButtonStyle.class));
		style.imageUp = new TextureRegionDrawable(image);
		style.imageDown = new TextureRegionDrawable(imageFlipped);
		ImageButton iconButton = new ImageButton(style);
		
		/*
		 * clicked() is called when clicked, 
		 * the only listener I found working so far is click listener,
		 * other kinds of UI elements supports more kinds of listener e.g. textChangedListener,
		 * refer to libgdx test codes
		 */
		iconButton.addListener(
				new ClickListener()
				{
					@Override
					public void clicked(InputEvent event, float x, float y) {
						ButtonEvent evt = Pools.obtain(ButtonEvent.class);
						evt.type = ButtonEvent.ButtonType.SAMPLE;
						EventManager.pushEvent(evt);
					}
				}
		);
		
		Window sampleWindow = new Window("Dialog", sampleSkin);
		sampleWindow.setPosition(0, 0);
		sampleWindow.defaults().spaceBottom(10);
		sampleWindow.row().fill().expandX();
		sampleWindow.add(iconButton);
		
		//newUIMenuEntity() adds required components to the entity system for a UI Menu
		//the first parameter refers to the topmost actor (actor without parent), 
		//window instead of iconButton in this case.
		return newUIMenuEntity(sampleWindow, Constants.z_sample, MenuType.SAMPLE);
	}
}
