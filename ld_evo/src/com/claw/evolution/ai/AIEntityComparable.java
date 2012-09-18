package com.claw.evolution.ai;

import java.util.Comparator;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class AIEntityComparable implements Poolable, Comparable<AIEntityComparable>
{
	public Entity e;
	public int dest;
	public Vector2 lastNode;
	public int pending;
	public boolean searching;
	public int arrived;
	public float dist2FromPlayer;
	
	public AIEntityComparable()
	{
		reset();
	}

	@Override
	public void reset() {
		e = null;
		dest = 0;
		pending = 0;
		arrived = 0;
		lastNode = null;
		searching = false;
	}

	@Override
	public int compareTo(AIEntityComparable o) {
		if(searching && !o.searching)
		{
			return -1;
		}
		else if (!searching && o.searching)
		{
			return 1;
		}
		
		if(pending > o.pending)
		{
			return -1;
		}
		else if (pending < o.pending)
		{
			return 1;
		}
		
		if(dest < o.dest)
		{
			return -1;
		}
		else if (dest > o.dest)
		{
			return 1;
		}
		
		if(arrived < o.arrived)
		{
			return 1;
		}
		else if (arrived > o.arrived)
		{
			return -1;
		}
		
		return 0;
	
	}
	
	public static Comparator<AIEntityComparable> searchComparator = new Comparator<AIEntityComparable>()
	{

		@Override
		public int compare(AIEntityComparable arg0, AIEntityComparable o) {
			if(arg0.searching && !o.searching)
			{
				return -1;
			}
			else if (!arg0.searching && o.searching)
			{
				return 1;
			}
			
			if(arg0.dest < o.dest)
			{
				return -1;
			}
			else if (arg0.dest > o.dest)
			{
				return 1;
			}
			
			if(arg0.pending > o.pending)
			{
				return -1;
			}
			else if (arg0.pending < o.pending)
			{
				return 1;
			}
			
			if(arg0.arrived < o.arrived)
			{
				return 1;
			}
			else if (arg0.arrived > o.arrived)
			{
				return -1;
			}
			
			return 0;
		}

	};
	
	public static Comparator<AIEntityComparable> behaviourComparator = new Comparator<AIEntityComparable>()
	{

		@Override
		public int compare(AIEntityComparable arg0, AIEntityComparable o) {
			if(arg0.dist2FromPlayer < o.dist2FromPlayer)
			{
				return -1;
			}
			else if(arg0.dist2FromPlayer > o.dist2FromPlayer)
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
	};
}
