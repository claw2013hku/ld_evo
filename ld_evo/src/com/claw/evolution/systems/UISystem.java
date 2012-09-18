/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.claw.evolution.systems;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;

import java.util.Random;
import java.util.UUID;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CUIActor;
import com.claw.evolution.components.CUIMenu;
import com.claw.evolution.components.CUI;
import com.claw.evolution.components.CUIMenu.MenuType;
import com.claw.evolution.events.ButtonEvent;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.events.RemoveActorEvent;
import com.claw.evolution.processes.ActionPendingProcess;
import com.claw.evolution.processes.Process;
import com.claw.evolution.processes.InvokeProcess;
import com.claw.evolution.processes.WaitProcess;
import com.claw.evolution.singleton.Assemblage;
import com.claw.evolution.singleton.EventManager;
import com.claw.evolution.singleton.ProcessManager;

public class UISystem extends ASystem{	
	public static final String tag = "UISystem : ";
	SpriteBatch batch;
	UUID m_uiEntity;
	ComponentMapper<CUIActor> cAct;
	Stage m_stage; 
	
	public String getSimpleName(){
		return "MenuSystem";
	}
	
	@SuppressWarnings("unchecked")
	public UISystem(){
		super(Aspect.getAspectForAll(CUIActor.class));
		batch = new SpriteBatch();
	}

	@Override
	protected void initialize() {
		cAct = world.getMapper(CUIActor.class);
		//listen about Button events for showing / hiding in-game menus
		EventManager.registerListener(this, ButtonEvent.class);
		EventManager.registerListener(this, RemoveActorEvent.class);
		
		//make necessary entities for this system
		Assemblage.loadUIStage();
		m_stage = world.getManager(TagManager.class).getEntity(Constants.tag_UI).getComponent(CUI.class).stage;
		
		Assemblage.loadInGameMenu();
		showMenu(false, MenuType.IN_GAME_LAUNCHED);
	}
	
	/** Draw and animate UI elements**/
	@Override
	protected void begin() {
		super.begin();
		m_stage.act(world.getDelta() / 1000);
		m_stage.draw();
	}
	
	@Override
	protected void process(Entity arg0) {
	}

	@Override
	public void dispose () {
		Gdx.app.debug(Constants.debug_tag, tag + "begin dispose UI");
		m_stage.dispose();
	}
	
	public Entity getMenuId(CUIMenu.MenuType type)
	{
		boolean hasMenu = false;
		
		ImmutableBag<Entity> iMenuIds = world.getManager(GroupManager.class).getEntities("UIMenu");
		Entity menuId = null;
		CUIMenu menuC = null;
		for(int i = 0; i < iMenuIds.size(); i++)
		{
			menuId = iMenuIds.get(i);
			menuC = menuId.getComponent(CUIMenu.class);
			if(menuC.type == type)
			{
				hasMenu = true;
				break;
			}
		}
		if(hasMenu)
		{
			return menuId;
		}
		else
		{
			return null;
		}
	}
	
	// 				methods to show/hide UI elements				//
	/** set isProcess to false to show immediately on call, 
	 * else a process that will show the menu is returned to be linked with other processes
	 * @param isProcess false : show immediately
	 * @return head of Process if not shown immediately
	 */
	public Process showMenu(boolean isProcess, final CUIMenu.MenuType menuType, final float fadeInTime)
	{
		//check if in-game menu already exists in entity system
		Entity menuId = getMenuId(menuType);
		boolean hasMenu = (menuId != null);
		if(hasMenu)
		{
			//if already exists then just need to fade in it
		}
		else
		{
			//else add the menu into entity system first
			switch(menuType)
			{
			case IN_GAME_LAUNCHED:
				menuId = Assemblage.loadInGameMenu();
				break;
			//TODO: more menu types
				default:
				menuId = Assemblage.loadInGameMenu();
				break;
			}
		}
		
		Process proc = showUIElement(menuId, isProcess, fadeInTime);
		if(proc == null)
		{
			return null;
		}
		else
		{
			final Entity id = menuId;
			InvokeProcess iProc = new InvokeProcess()
			{
				public void invoke() {
					id.getComponent(CUIMenu.class).shown = true;
				}
			};
			return ProcessManager.link(
					proc,
					iProc);
		}
	}
	
	/** set isProcess to false to show immediately on call, 
	 * else a process that will show the menu is returned to be linked with other processes
	 * @param isProcess false : show immediately
	 * @return head of Process if not shown immediately
	 */public Process showMenu(boolean isProcess, final CUIMenu.MenuType menuType)
	{
		return showMenu(isProcess, menuType, 750f);
	}
	
	public Process hideMenu(boolean isProcess, final CUIMenu.MenuType menuType, final float fadeOutTime)
	{
		Entity menuId = getMenuId(menuType);
		boolean hasMenu = (menuId != null);
		
		if(!hasMenu)
		{
			if(isProcess)
			{
				WaitProcess wP = Pools.obtain(WaitProcess.class);
				wP.setTimer(0);
				return wP;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return hideUIElement(menuId, isProcess, false, fadeOutTime);
		}
	}
	
	public Process hideMenu(boolean isProcess, final CUIMenu.MenuType menuType)
	{
		return hideMenu(isProcess, menuType, 750f);
	}
	
	/** show UI elements belonging in an entity (they should be invisible before calling this)
	 * @param isProcess false : show immediately
	 * @return head of Process if not shown immediately
	 */
	public Process showUIElement(final Entity entity, boolean isProcess, final float fadeInTime)
	{
		if(!isProcess)
		{
			CUIActor actorC = entity.getComponent(CUIActor.class);
			actorC.actor.setVisible(true);
			actorC.actor.getColor().a = 0;
			actorC.actor.addAction(fadeIn(fadeInTime / 1000));
			return null;
		}
		else
		{
			InvokeProcess process = new InvokeProcess()
			{
				@Override
				public void invoke() {
					// TODO Auto-generated method stub
					showUIElement(entity, false, fadeInTime);
				}
			};
			
			WaitProcess waitProcess = Pools.obtain(WaitProcess.class);
			waitProcess.setTimer(fadeInTime);
			return ProcessManager.link(process, waitProcess);
		}
	}
	
	/** show UI elements belonging in an entity (they should be invisible before calling this)
	 * @param isProcess false : show immediately
	 * @return head of Process if not shown immediately
	 */public Process showUIElement(final Entity entity, boolean isProcess)
	{
		return showUIElement(entity, isProcess, 750f);
	}
	 
	/** hide / destory UI elements
	 * 
	 * @param destroy false : retain entity, only put false if there are further actions for the element
	 * @return
	 */
	public Process hideUIElement(final Entity entity, boolean isProcess, final boolean destroy, final float fadeOutTime)
	{
		if(!isProcess)
		{
			final CUIActor actorC = entity.getComponent(CUIActor.class);
			if(destroy)
			{
				actorC.actor.addAction(
						sequence(
								fadeOut(fadeOutTime / 1000),
								new Action()
								{
									@Override
									public boolean act(float delta) {
										actorC.actor.setVisible(false);
										//Assemblage.unloadAndKillEntity(entity);
										return false;
									}
								}));
			}
			else
			{
				actorC.actor.addAction(fadeOut(fadeOutTime / 1000));
			}
			return null;
		}
		else
		{
			InvokeProcess process = new InvokeProcess()
			{
				@Override
				public void invoke() {
					// TODO Auto-generated method stub
					hideUIElement(entity, false, destroy, fadeOutTime);
				}
			};
			WaitProcess waitProcess = Pools.obtain(WaitProcess.class);
			waitProcess.setTimer(fadeOutTime);		
			return ProcessManager.link(process, waitProcess);
		}
	}
	 
	/**
	 * Fade and destory elements
	 */
	public Process hideUIElement(final Entity entity, boolean isProcess)
	{
		return hideUIElement(entity, isProcess, true, 750f);
	}
	
	public Process destroyUIElement(final Entity entity, boolean isProcess)
	{
		return hideUIElement(entity, isProcess, true, 0);
	}
	
	public Process UIAction(final Entity entity, final Action action)
	{
		final ActionPendingProcess pendProc = new ActionPendingProcess();
		final Action actions = sequence(action, new Action()
		{
			@Override
			public boolean act(float delta) {
				// TODO Auto-generated method stub
				pendProc.ActionFinished();
				return false;
			}
		}
		);
		InvokeProcess actProc = new InvokeProcess()
		{
			@Override
			public void invoke() {
				entity.getComponent(CUIActor.class).actor.addAction(actions);
			}	
		};
		return ProcessManager.link(actProc, pendProc);
	}
	
	public Process UIGroupAction(final Entity entity, final Action action)
	{
		final ActionPendingProcess pendProc = new ActionPendingProcess();
		final Action actions = sequence(action, new Action()
		{
			@Override
			public boolean act(float delta) {
				// TODO Auto-generated method stub
				pendProc.ActionFinished();
				return false;
			}
		}
		);
		InvokeProcess actProc = new InvokeProcess()
		{
			@Override
			public void invoke() {
				Group group = (Group)entity.getComponent(CUIActor.class).actor;
				
				for(int i = 0; i != group.getChildren().size; i++)
				{
					group.getChildren().get(i).addAction(actions);
				}
				entity.getComponent(CUIActor.class).actor.addAction(actions);
			}
		};
		return ProcessManager.link(actProc, pendProc);
	}
	
	//handle registered events (registered in constructor, events are defined to be fired in makeMenu() etc. in Assemblage)
	int newPlayerX = 400;
	@Override
	public boolean handleEvent(IEvent event) {
		//animate the menu until disappear when resume is clicked
		// ++ test: add splash screen afterwards
		if(event.getClass() == ButtonEvent.class)
		{
			ButtonEvent be = (ButtonEvent)event;
			//hide in-game-menu
			if(be.type == ButtonEvent.ButtonType.RESUME)
			{
				Entity splashEntity = Assemblage.loadSplash();
				showUIElement(splashEntity, false, 0f);
				ProcessManager.attach(
						ProcessManager.link(
								UIAction(splashEntity, 
										sequence(
												fadeOut(0.0f), 
												fadeIn(0.3f), 
												moveBy(30f, -90f, 0.5f))),
								UIAction(getMenuId(MenuType.IN_GAME_LAUNCHED), 
										sequence(
												fadeOut(0.5f), 
												fadeIn(0.3f), 
												color(Color.BLUE, 0.5f))),
								UIAction(splashEntity, 
										sequence(
												fadeOut(0.5f), 
												fadeIn(1f), 
												color(Color.YELLOW, 0.3f),
												fadeOut(0.5f))),
								destroyUIElement(splashEntity, true))
								);
			}
			//inflate the in-game menu when menu is clicked
			else if(be.type == ButtonEvent.ButtonType.IN_GAME_MENU)
			{
				 showMenu(false, MenuType.IN_GAME_LAUNCHED);
			}
			else if(be.type == ButtonEvent.ButtonType.ADD_PLAYER)
			{
				for(int i = 0; i < 1; i++)
				{
					Assemblage.loadTestPlayer(newPlayerX, 1330);
					 newPlayerX += 20;
				}
			}
			else if(be.type == ButtonEvent.ButtonType.REMOVE_PLAYER)
			{
				ImmutableBag<Entity> e = world.getManager(GroupManager.class).getEntities(Constants.tag_AIplayer);
				if(e.size() > 0)
				{
					Random rand = new Random();
					int i = rand.nextInt(e.size());
					world.deleteEntity(e.get(i));
				}
			}
		}
		else if(event.getClass() == RemoveActorEvent.class)
		{
			RemoveActorEvent rae = (RemoveActorEvent)event;
			Gdx.app.debug(Constants.debug_tag, tag + "removing actor");
			rae.actor.remove();
		}
		return false;
	}

	
	public void resize(int width, int height) {
		//Stage stage = m_es.getComponent(m_uiEntity, CUI.class).stage;
		//stage.setViewport(width, height, true);
	}

	@Override
	public String getName() {
		return "UI";
	}
}
