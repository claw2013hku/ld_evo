package com.claw.evolution.singleton;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.claw.evolution.Constants;

public class FileManager {
	static final String tag = "FileManager : ";
	private static FileManager instance = null;
	
	private FileManager(){};
	
	private static FileManager getInstance()
	{
		if(instance == null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "no instance found");
		}
		return instance;
	}

	public static void createInstance()
	{
		if(instance != null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "instance already created");
		}
		else
		{
			instance = new FileManager();
		}
		instance.resourceMap = new HashMap<String, Disposable>();
	}

	public static void dispose()
	{
		HashMap<String, Disposable> resourceMap = getInstance().resourceMap;
		Gdx.app.debug(Constants.debug_tag, "FileManager begin dispose resources");
		Iterator<Disposable> iterator = resourceMap.values().iterator();
		while(iterator.hasNext())
		{
			iterator.next().dispose();
		}
		resourceMap.clear();
		instance = null;
	}
	
	private HashMap<String, Disposable> resourceMap;
	
	@SuppressWarnings("unchecked")
	public static <T extends Disposable> T getResource(FileHandle f, Class<T> type)
	{
		HashMap<String, Disposable> resourceMap = getInstance().resourceMap;
		if(resourceMap.containsKey(f.name()))
		{
			return (T)resourceMap.get(f.name());
		}
		else
		{
			Gdx.app.debug(Constants.debug_tag, "no resource found in cache, creating " + f.name());
			T resource = null;
			try {
				resource = type.getConstructor(FileHandle.class).newInstance(f);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			resourceMap.put(f.name(), resource);
			return resource;
		}
	}
}
