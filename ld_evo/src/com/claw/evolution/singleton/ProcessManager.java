package com.claw.evolution.singleton;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.claw.evolution.Constants;
import com.claw.evolution.processes.Process;

public class ProcessManager {
	private static final String tag = "ProcessManager : ";
	private static ProcessManager instance;
	
	private ProcessManager(){};
	
	public static ProcessManager getInstance()
	{
		if(instance == null)
		{
			Gdx.app.error(Constants.debug_tag, tag + "no instance found");
		}
		return instance;
	}
	
	public static void dispose()
	{
		getInstance().processes.clear();
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
			instance = new ProcessManager();
		}
		instance.processes = new ArrayList<Process>();
	}
	
	private ArrayList<Process> processes;
	
	public static void update(float ms)
	{
		ArrayList<Process> processes = getInstance().processes;
		for(int i = 0; i < processes.size(); i++)
		{
			processes.get(i).update(ms);
			if(processes.get(i).isDead())
			{
				if(processes.get(i).next != null)
				{
					processes.add(processes.get(i).next);
				}
				processes.remove(i--);
			}
		}
	}
	
	public static void attach(Process p)
	{
		ArrayList<Process> processes = getInstance().processes;
		processes.add(p);
	}

	/** link the process linked lists, returns the head **/
	public static Process link(Process proc1, Process proc2)
	{
		getTail(proc1).next = proc2;
		return proc1;
	}
	
	/** link the process linked lists, returns the head **/
	public static Process link(Process... procs)
	{
		Process head = procs[0];
		for(int i = 1; i < procs.length; i++)
		{
			link(procs[i - 1], procs[i]);
		}
		return head;
	}
	
	/** get the tail from a process linked list **/
	public static Process getTail(Process head)
	{
		Process tail = head;
		while(tail.next != null)
		{
			tail = tail.next;
		}
		return tail;
	}
}
