package com.claw.evolution.singleton;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.claw.evolution.Constants;
import com.claw.evolution.events.IEvent;
import com.claw.evolution.systems.IEventHandler;

public class EventManager {
	private static final String tag = "EventManager : ";
	private static EventManager instance;
	
	private EventManager(){}
	
	public static EventManager getInstance()
	{
		if(instance == null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "no instance found");
		}
		return instance;
	}
	
	public static void dispose()
	{
		getInstance().listenerRegistry.clear();
		getInstance().eventQueue.clear();
		instance = null;
	}

	public static void createInstance()
	{
		if(instance != null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "instance already created");
		}
		else
		{
			instance = new EventManager();
		}
		instance.listenerRegistry = new HashMap<Class<? extends IEvent>, ArrayList<IEventHandler>>();
		instance.eventQueue = new ArrayList<IEvent>();
	}
	
	private HashMap<Class<? extends IEvent>, ArrayList<IEventHandler>> listenerRegistry;
	private ArrayList<IEvent> eventQueue;
	
	public static <T extends IEvent> void registerListener(IEventHandler system, Class<T> eventType)
	{
		HashMap<Class<? extends IEvent>, ArrayList<IEventHandler>> listenerRegistry = 
				getInstance().listenerRegistry;
		ArrayList<IEventHandler> systems = listenerRegistry.get(eventType);
		if(systems == null)
		{
			systems = new ArrayList<IEventHandler>();
			systems.add(system);
			listenerRegistry.put(eventType, systems);
		}
		else
		{
			if(!systems.contains(system))
			{
				systems.add(system);
			}
		}
	}
	
	public static void unregisterListener(IEventHandler handler)
	{
		for(Class<? extends IEvent> key : getInstance().listenerRegistry.keySet())
		{
			if(getInstance().listenerRegistry.get(key).contains(handler))
			{
				getInstance().listenerRegistry.get(key).remove(handler);
			}
		}
	}
	
	public static <T extends IEvent> void unregisterListener(IEventHandler handler, Class<T> eventType)
	{
		if(getInstance().listenerRegistry.containsKey(eventType))
		{
			if(getInstance().listenerRegistry.get(eventType).contains(handler))
			{
				getInstance().listenerRegistry.get(eventType).remove(handler);
			}
		}
	}
	
	public static void update(float ms)
	{
		ArrayList<IEvent> eventQueue = getInstance().eventQueue;
		HashMap<Class<? extends IEvent>, ArrayList<IEventHandler>> listenerRegistry = getInstance().listenerRegistry;
		for(int i = 0; i < eventQueue.size(); i++)
		{
			ArrayList<IEventHandler> systems = listenerRegistry.get(eventQueue.get(i).getClass());
			if(systems == null) continue;
			for(int j = 0; j < systems.size() ; j++)
			{
				if(systems.get(j).handleEvent(eventQueue.get(i))) break;
			}
		}
		eventQueue.clear();
	}
	
	public static void pushEvent(IEvent e)
	{
		ArrayList<IEvent> eventQueue = getInstance().eventQueue;
		eventQueue.add(e);
	}
}
