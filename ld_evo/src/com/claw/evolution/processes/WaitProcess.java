package com.claw.evolution.processes;

public class WaitProcess extends Process{
	private float timer;
	
	public WaitProcess()
	{
	}
	
	public WaitProcess setTimer(float t)
	{
		timer = t;
		return this;
	}
	
	@Override
	public void update(float ms) {
		// TODO Auto-generated method stub
		timer -= ms;
	}

	@Override
	public boolean isDead() {
		// TODO Auto-generated method stub
		return timer < 0;
	}

	@Override
	public void reset() {
		timer = 0;
	}
}
