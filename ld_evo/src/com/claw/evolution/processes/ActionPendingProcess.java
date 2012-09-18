package com.claw.evolution.processes;

public class ActionPendingProcess extends Process {
	boolean finished = false;
	@Override
	public boolean isDead() {
		return finished;
	}

	public void ActionFinished()
	{
		finished = true;
	}

	@Override
	public void update(float ms) {
		
	}

	@Override
	public void reset() {
		finished = false;
	}
}
