package com.claw.evolution.components;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.claw.evolution.ai.AINode;

public class CAIMap extends AComponent {
	public Array<Array<AINode>> nodes;
	public int lastX;
	public int lastY;
	
	public CAIMap()
	{
		nodes = new Array<Array<AINode>>();
	}
	
	@Override
	public void reset() {
		Iterator<Array<AINode>> iter = nodes.iterator();
		while(iter.hasNext())
		{
			Array<AINode> arr = iter.next();
			Pools.free(arr);
			arr.clear();
		}
		nodes.clear();
	}
}
