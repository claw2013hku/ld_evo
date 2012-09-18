package com.claw.evolution.components;

public class CWave extends AComponent {
	public float countDown = 0;
	public int levelId = 0;
	public int waveId = 0;
	@Override
	public void reset() {
		countDown = 0;
		levelId = 0;
		waveId = 0;
	}
}
