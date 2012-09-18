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
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.Constants;
import com.claw.evolution.components.CUIActor;
import com.claw.evolution.components.CUIMenu;
import com.claw.evolution.components.CUI;
import com.claw.evolution.components.CUIMenu.MenuType;
import com.claw.evolution.events.ButtonEvent;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.events.RemoveActorEvent;
import com.claw.evolution.events.SampleEvent;
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
		//RegisterListener() for all interested events for this system, otherwise handleEvent() will not be called for unregisted event types
		EventManager.registerListener(this, SampleEvent.class);
		
		//make necessary entities for this system
		Assemblage.loadUIStage();
		m_stage = world.getManager(TagManager.class).getEntity(Constants.tag_UI).getComponent(CUI.class).stage;
		
		//shows a menu based on its type; showMenu() also has many override for more control e.g. fade in time
		showMenu(MenuType.SAMPLE);
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
			//What to load based on menu type when a showMenu() is called but menu is not loaded
			case SAMPLE:
				menuId = Assemblage.loadSampleUIMenu();
				break;
			default:
				menuId = Assemblage.loadSampleUIMenu();
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
	 */
	public Process showMenu(boolean isProcess, final CUIMenu.MenuType menuType)
	{
		return showMenu(isProcess, menuType, 750f);
	}
	
	public Process showMenu(final CUIMenu.MenuType menuType)
	{
		return showMenu(false, menuType, 750f);
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
										world.deleteEntity(entity);
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
		if(event.getClass() == ButtonEvent.class)
		{
			ButtonEvent be = (ButtonEvent)event;

			if(be.type == ButtonEvent.ButtonType.SAMPLE)
			{
				startSampleAction();
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
		// add / modify for more type of events, refer the events package
		else if (event.getClass() == SampleEvent.class)
		{
			
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
	
	private Array<Process> tempProcesses = Pools.obtain(Array.class);
	
	public void startAction()
	{
		
	}
	
	public void endAction()
	{
		if(tempProcesses.size < 1) return;
		Process head = tempProcesses.first();
		for(int i = 1; i < tempProcesses.size; i++)
		{
			ProcessManager.link(head, tempProcesses.get(i));
		}
		ProcessManager.attach(head);
		tempProcesses.clear();
	}
	
	public void act(Entity e, Action... actions)
	{
		for(int i = 0; i < actions.length; i++)
		{
			tempProcesses.add(UIAction(e, actions[i]));
		}
	}
	
	public void show(Entity e)
	{
		tempProcesses.add(showUIElement(e, true, 0f));
	}
	
	public void remove(Entity e)
	{
		tempProcesses.add(destroyUIElement(e, true));
	}
	
	public void fireEvent(final IEvent ev)
	{
		tempProcesses.add(new InvokeProcess()
			{
				@Override
				public void invoke() {
					EventManager.pushEvent(ev);
				}
			});	
	}
	
	/**A sample series of actons of every kind**/
	public void startSampleAction()
	{
		//begin every sequence of action of startAction() and endAction()
		startAction();
		
		//get entity of UI actor (or menu for the 2nd line, though not recommended) (and load the actor just beforehand)
		Entity sampleEntity = Assemblage.loadSampleUIActor();
		//Entity sampleEntity = getMenuId(MenuType.SAMPLE);
		
		//show() should be called right before it should be visible to the player
		show(sampleEntity);
		/*act() performs libgdx Actions on the UI actor, 
			for more kinds of libgdx Actions Ctrl+Click "sequence" below,
			then import the new kind of action on the top of the source file for use, 
			e.g. import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sample;
		*/
		act(sampleEntity, sequence(fadeOut(0.0f), fadeIn(0.3f), moveBy(30f, -90f, 0.5f)));
		act(sampleEntity, fadeOut(1f));
		act(sampleEntity, parallel(fadeIn(0.3f), color(Color.YELLOW, 0.3f)));
		act(sampleEntity, parallel(fadeOut(0.7f), color(Color.RED, 0.4f)));
		//remove() deletes the entity (and frees the memory of the bitmap, unimplemented yet)
		remove(sampleEntity);
		
		//fireEvent() fires an event
		SampleEvent sampleEvt = Pools.obtain(SampleEvent.class);
		sampleEvt.sampleData = 1f;
		fireEvent(sampleEvt);
		
		endAction();
	}
}
