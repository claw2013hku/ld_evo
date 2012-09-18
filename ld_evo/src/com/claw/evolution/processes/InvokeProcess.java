package com.claw.evolution.processes;

public abstract class InvokeProcess extends Process {
	boolean invoked = false;
	@Override
	public void update(float ms) {
		invoke();
		invoked = true;
	}

	public abstract void invoke();
	
	@Override
	public boolean isDead() {
		return invoked;
	}

	@Override
	public void reset() {
		invoked = false;
	}
}
