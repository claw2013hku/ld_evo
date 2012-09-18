package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CController;
import com.claw.evolution.components.CPosition;
import com.claw.evolution.components.CWorldCamera;
import com.claw.evolution.events.IEvent;

/** Receives input events from input processor and 
 * translate raw input into logical input (CController of player)
 * rejects input if player is too close to the input pointer**/
public class InputSystem extends ASystem {
	private int thereshold = 10 * 10;
	GameInputProcessor gip;
	boolean lastTouchdown = false;
	
	public InputSystem()
	{
		super(Aspect.getEmpty());
		setPassive(true);
	}
	
	@Override
	protected void end() {
	}

	@Override
	protected void initialize() {
		gip = new GameInputProcessor();
		InputProcessor cProc = Gdx.input.getInputProcessor();
		if(cProc == null)
		{
			Gdx.input.setInputProcessor(gip);
		}
		else
		{
			InputMultiplexer im = new InputMultiplexer();
			im.addProcessor(cProc);
			im.addProcessor(1, gip);
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
	protected void begin() {
		if(gip.touchingDown)
		{
			getPlayerController().moveDir.set(
					deriveDir(Gdx.input.getX(), Gdx.input.getY()));
		}
		else if(lastTouchdown == true && !gip.touchingDown)
		{
			getPlayerController().moveDir.set(0,0);
		}
		lastTouchdown = gip.touchingDown;
	}

	@Override
	protected void process(Entity e) {
		
	}
	
	private CController getPlayerController()
	{
		return world.getManager(TagManager.class).getEntity(Constants.tag_player).getComponent(CController.class);
	}
	Vector2 playerPos = new Vector2();
	private Vector2 getPlayerPosition()
	{
		CPosition cPos = world.getManager(TagManager.class).getEntity(Constants.tag_player).getComponent(CPosition.class);
		playerPos.set(cPos.x, cPos.y);
		return playerPos;
	}
	
	Vector3 worldPos3 = new Vector3();
	Vector2 worldPos2 = new Vector2();
	private Vector2 getWorldPosition(int screenX, int screenY)
	{
		CWorldCamera cWC = world.getManager(TagManager.class).getEntity(Constants.tag_worldCamera).getComponent(CWorldCamera.class);
		worldPos3.set(screenX, screenY, 0);
		cWC.camera.unproject(worldPos3);
		worldPos2.set(worldPos3.x, worldPos3.y);
		//worldPos2.set(screenX - 400, -screenY + 240);
		return worldPos2;
	}
	
	Vector2 dir = new Vector2();
	Vector2 dist = new Vector2();
	
	private Vector2 deriveDir(int screenX, int screenY)
	{
		dist.set(getWorldPosition(screenX, screenY)).sub(getPlayerPosition());
		if(dist.len2() < thereshold)
		{
			dir.set(0, 0);
		}
		else
		{
			dir.set(dist).nor();
		}
		return dir;
	}
	
	public class GameInputProcessor implements InputProcessor
	{
		public boolean touchingDown;
		public GameInputProcessor()
		{
			touchingDown = false;
		}
		
		@Override
		public boolean keyDown(int keycode) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {
			touchingDown = true;
			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			touchingDown = false;
			return true;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return true;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
				return false;
		}

		@Override
		public boolean scrolled(int amount) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	@Override
	public String getName() {
		return "InputSystem";
	}
}
