//package com.claw.evolution.singleton;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import aurelienribon.bodyeditor.BodyEditorLoader;
//
//import com.artemis.Entity;
//import com.artemis.World;
//import com.artemis.managers.GroupManager;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.Texture.TextureFilter;
//import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.physics.box2d.Body;
//import com.badlogic.gdx.physics.box2d.BodyDef;
//import com.badlogic.gdx.physics.box2d.FixtureDef;
//import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
//import com.badlogic.gdx.utils.Pools;
//import com.claw.evolution.Constants;
//import com.claw.evolution.components.CDimension;
//import com.claw.evolution.components.CDrawElement;
//import com.claw.evolution.components.CPhyBody;
//import com.claw.evolution.components.CPosition;

//public class UnusedAssemblage {
//	/** requires CPhyWorld**/
//	public static Set<Entity> loadBoxes()
//	{ 	
//		Gdx.app.debug(Constants.debug_tag, "making boxes");
//		
//		World es = getES();
//		
//		HashSet<Entity> ids = new HashSet<Entity>();
//		
//		PolygonShape boxPoly = new PolygonShape();
//		float width = 1f;
//		float height = 1f;
//		boxPoly.setAsBox(width, height);
//		
//		// next we create the 50 box bodies using the PolygonShape we just
//		// defined. This process is similar to the one we used for the ground
//		// body. Note that we reuse the polygon for each body fixture.
//		com.badlogic.gdx.physics.box2d.World world = getPhyWorld().world;
//		
//		for (int i = 0; i < 20; i++) {
//			// Create the BodyDef, set a random position above the
//			// ground and create a new body
//			CPhyBody cBody = Pools.obtain(CPhyBody.class);
//			BodyDef boxBodyDef = new BodyDef();
//			boxBodyDef.type = BodyType.DynamicBody;
//			boxBodyDef.position.x = -24 + (float)(Math.random() * 48);//-6f + (float)(Math.random() * 12);//-24 + (float)(Math.random() * 48);
//			boxBodyDef.position.y = 10 + (float)(Math.random() * 100);
//			Body boxBody = world.createBody(boxBodyDef);
//			boxBody.createFixture(boxPoly, 1);
//			
//			// add the box to database
//			Entity entity = es.createEntity();
//			ids.add(entity);
//			CPhyBody cBody = new CPhyBody(boxBody);
//			CPosition cPos = new CPosition(
//					boxBodyDef.position.x * Constants.pixelsPerMeter,
//					boxBodyDef.position.y * Constants.pixelsPerMeter);
//			CDimension cDim = new CDimension(
//					width * Constants.pixelsPerMeter * 2, 
//					height * Constants.pixelsPerMeter * 2);
//			entity.addComponent(cBody);
//			entity.addComponent(cPos);
//			entity.addComponent(cDim);
//			es.addEntity(entity);
//			es.getManager(GroupManager.class).add(entity, Constants.tag_phyBody);
//			
//			Texture texture = FileManager.getResource(Gdx.files.internal("data/libgdx.png"), Texture.class);
//			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//			TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
//			CDrawElement cDraw = new CDrawElement(region);//, width * pixelsPerMeter /2, height * pixelsPerMeter /2);
//			entity.addComponent(cDraw);
//			es.addEntity(entity);
//		}
//
//		// we are done, all that's left is disposing the boxPoly
//		boxPoly.dispose();
//		return ids;
//	}
//
//	public static Set<Entity> loadFishes()
//	{ 	
//		World es = getES();
//		
//		HashSet<Entity> ids = new HashSet<Entity>();
//		Gdx.app.debug(Constants.debug_tag, "making boxes");
//		BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("data/bg.json"));
//		 
//		// next we create the 50 box bodies using the PolygonShape we just
//		// defined. This process is similar to the one we used for the ground
//		// body. Note that we reuse the polygon for each body fixture.
//		com.badlogic.gdx.physics.box2d.World world = getPhyWorld().world;
//		
//		for (int i = 0; i < 40; i++) {
//			// Create the BodyDef, set a random position above the
//			// ground and create a new body
//			BodyDef boxBodyDef = new BodyDef();
//			boxBodyDef.type = BodyType.DynamicBody;
//			boxBodyDef.position.x = -24 + (float)(Math.random() * 48);//-6f + (float)(Math.random() * 12);//-24 + (float)(Math.random() * 48);
//			boxBodyDef.position.y = 10 + (float)(Math.random() * 100);
//			Body boxBody = world.createBody(boxBodyDef);
//			FixtureDef fd = new FixtureDef();
//		    fd.density = 1;
//		    fd.friction = 0.5f;
//		    fd.restitution = 0.3f;
//			//boxBody.createFixture(boxPoly, 1);
//		    
//		    Texture texture = null; 
//		    //FileManager.getResource(Gdx.files.internal("data/characters/fish.png"), Texture.class);
//			switch(i % 4)
//		    {
//		    case 0:
//		    	texture = FileManager.getResource(Gdx.files.internal("data/characters/small.png"), Texture.class);
//		    	break;
//		    case 1:
//		    	texture = FileManager.getResource(Gdx.files.internal("data/characters/tadpole.png"), Texture.class);
//		    	break;
//		    case 2:
//		    	texture = FileManager.getResource(Gdx.files.internal("data/characters/fish.png"), Texture.class);
//		    	break;
//		    case 3:
//		    	texture = FileManager.getResource(Gdx.files.internal("data/characters/fishleg.png"), Texture.class);
//		    	break;
//		    default:
//		    	texture = FileManager.getResource(Gdx.files.internal("data/characters/tadpole.png"), Texture.class);
//		    	break;
//		    }
//			
//		    switch(i % 4)
//		    {
//		    case 0:
//		    	loader.attachFixture(boxBody, "char-small", fd, texture.getWidth() / Constants.pixelsPerMeter);
//		    	break;
//		    case 1:
//		    	loader.attachFixture(boxBody, "char-tadpole", fd, texture.getWidth() / Constants.pixelsPerMeter);
//		    	break;
//		    case 2:
//		    	loader.attachFixture(boxBody, "char-fish", fd, texture.getWidth() / Constants.pixelsPerMeter);
//		    	break;
//		    case 3:
//		    	loader.attachFixture(boxBody, "char-legfish", fd, texture.getWidth() / Constants.pixelsPerMeter);
//		    	break;
//		    default:
//		    	loader.attachFixture(boxBody, "char-fish", fd, texture.getWidth() / Constants.pixelsPerMeter);
//		    	break;	
//		    }
//			// add the box to database
//			Entity entity = es.createEntity();
//			ids.add(entity);
//			CPhyBody cBody = Pools.obtain(CPhyBody.class);
//			cBody.body = boxBody;
//			
//			CPosition cPos = Pools.obtain(CPosition.class);
//			CPosition cPos = new CPosition(
//					boxBodyDef.position.x * Constants.pixelsPerMeter,
//					boxBodyDef.position.y * Constants.pixelsPerMeter);
//			CDimension cDim = new CDimension(
//					texture.getWidth(), 
//					texture.getHeight());
//			entity.addComponent(cBody);
//			entity.addComponent(cPos);
//			entity.addComponent(cDim);
//			es.addEntity(entity);
//			es.getManager(GroupManager.class).add(entity, Constants.tag_phyBody);
//			
//			
//			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//			TextureRegion region = new TextureRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
//			CDrawElement cDraw = new CDrawElement(region);//, width * pixelsPerMeter /2, height * pixelsPerMeter /2);
//			entity.addComponent(cDraw);
//		}
//
//		// we are done, all that's left is disposing the boxPoly
//		return ids;
//	} 
//	//	public static void unloadAndKillEntity(UUID entity)
//	{
//		checkEntityManagerInstance();
//		EntityManager es = getInstance().es;
//		Gdx.app.debug(Constants.debug_tag, tag + "removing entity : " + entity);
//		if(es.hasComponent(entity, CUIActor.class))
//		{
//			EventManager.pushEvent(new RemoveActorEvent(es.getComponent(entity, CUIActor.class).actor));
//		}
//		
//		if(es.hasComponent(entity, CPhyBody.class))
//		{
//			EventManager.pushEvent(new RemoveBodyEvent(es.getComponent(entity, CPhyBody.class).body));
//		}
//		es.killEntity(entity);
//	}
//}
