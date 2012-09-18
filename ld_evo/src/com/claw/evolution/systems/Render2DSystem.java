package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CDimension;
import com.claw.evolution.components.CDrawElement;
import com.claw.evolution.components.CPosition;
import com.claw.evolution.components.CWorldCamera;
import com.claw.evolution.events.IEvent;

/** Draws 2D elements, 
 * required components : CDrawElement, CPosition, CDimension **/
public class Render2DSystem extends ASystem{
	private SpriteBatch m_batch;
	
	private ComponentMapper<CPosition> cPosMapper;
	private ComponentMapper<CDimension> cDimMapper;
	private ComponentMapper<CDrawElement> cDrawMapper;
	
	@SuppressWarnings("unchecked")
	public Render2DSystem() {
		super(Aspect.getAspectForAll(CPosition.class, CDimension.class, CDrawElement.class));
		m_batch = new SpriteBatch();
	}
	
	@Override
	protected void initialize() {
		cPosMapper = world.getMapper(CPosition.class);
		cDimMapper = world.getMapper(CDimension.class);
		cDrawMapper = world.getMapper(CDrawElement.class);
	}  

	@Override
	protected void begin() {
		super.begin();
		Camera camera = world.getManager(TagManager.class).getEntity(Constants.tag_worldCamera).getComponent(CWorldCamera.class).camera;
		m_batch.setProjectionMatrix(camera.combined);
		m_batch.begin();
	}
	
	Vector3 minb = new Vector3();
	Vector3 minb2 = new Vector3();
	Vector3 maxb = new Vector3();
	Vector3 maxb2 = new Vector3();
	Vector3 pos3 = new Vector3();
	@Override
	protected void process(Entity e) {
		Camera camera = world.getManager(TagManager.class).getEntity(Constants.tag_worldCamera).getComponent(CWorldCamera.class).camera;
		
		CPosition cPos = cPosMapper.get(e);
		CDimension cDim = cDimMapper.get(e);
		CDrawElement cDraw = cDrawMapper.get(e);
		Color color = Color.WHITE;
		Color.rgba8888ToColor(color, Color.rgba8888(1, 1, 1, 0.5f));
		m_batch.setColor(color);
		pos3.set(cPos.x, cPos.y, 0);
		float radius = cDim.width > cDim.height ? cDim.width : cDim.height;
		radius /= 2;
		if(camera.frustum.sphereInFrustumWithoutNearFar(pos3, radius))
		{
			m_batch.draw(
					cDraw.textureRegion, 
					cPos.x - cDim.width / 2, 
					cPos.y - cDim.height / 2, 
					cDraw.originX, 
					cDraw.originY, 
					cDim.width, 
					cDim.height, 
					cDraw.scaleX, 
					cDraw.scaleY, 
					cDim.rotation);
		}
	}
	
	@Override
	protected void end() {
		m_batch.end();
		super.end();
	}

	@Override
	public void dispose() {
		Gdx.app.debug(Constants.debug_tag, "Render2DSystem begin dispose batch");
		m_batch.dispose();
	}
	
	public void resize(int w, int h)
	{
		Camera camera = world.getManager(TagManager.class).getEntity(Constants.tag_worldCamera).getComponent(CWorldCamera.class).camera;
		camera.viewportWidth = w;
		camera.viewportHeight = h;
		camera.update();
		Gdx.app.debug(Constants.debug_tag, "Render2DSystem begin resize viewport w : " + w + " h : " + h);
	}

	@Override
	public boolean handleEvent(IEvent event) {
		return false;
	}

	@Override
	public String getName() {
		return "Render2D";
	}
}
