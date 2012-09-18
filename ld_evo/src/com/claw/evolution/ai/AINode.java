package com.claw.evolution.ai;

import java.util.Comparator;

import com.badlogic.gdx.utils.Pool.Poolable;

public class AINode implements Comparable<AINode>, Poolable{
	public boolean valid;
	public int x;
	public int y;
	public float totalCost;
	public float realCost;
	public boolean rayCasted;
	public AINode parent;
	
	public AINode()
	{
		reset();
	}
	
	public AINode set(boolean _valid, int _x, int _y)
	{
		valid = _valid;
		x = _x;
		y = _y;
		parent = null;
		totalCost = 0;
		realCost = 0;
		return this;
	}
	
	public AINode set(AINode node)
	{
		valid = node.valid;
		x = node.x;
		y = node.y;
		totalCost = node.totalCost;
		realCost = node.realCost;
		rayCasted = node.rayCasted;
		parent = node.parent;
		return this;
	}
	
	@Override
	public void reset() {
		valid = false;
		x = 0;
		y = 0;
		totalCost = 0;
		realCost = 0;
		parent = null;
		rayCasted = false;
	}

	@Override
	public int compareTo(AINode arg0) {
		return Comparator.compare(this, arg0);
	}

	public static AINodeComparator Comparator = new AINodeComparator();
	public static class AINodeComparator implements Comparator<AINode>
	{
		@Override
		public int compare(AINode arg0, AINode arg1) {
			// TODO Auto-generated method stub
			return Float.compare(arg0.totalCost, arg1.totalCost);
		}
	}
}
