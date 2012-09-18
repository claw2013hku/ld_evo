package com.claw.evolution.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

public class AIStoredSearch implements Poolable
{
	public Array<AINode> queue = new Array<AINode>();
	public Array<Vector2> marked = new Array<Vector2>();
	public int toX;
	public int toY;
	public int fromX;
	public int fromY;
	@Override
	public void reset() {
		Pools.free(queue);
		queue.clear();
		Pools.free(marked);
		marked.clear();
	}
}
