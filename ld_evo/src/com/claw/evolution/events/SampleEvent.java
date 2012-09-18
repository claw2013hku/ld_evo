package com.claw.evolution.events;

//Implement new event types from IEvent and override reset(), which is called when this event is reused.
public class SampleEvent implements IEvent {
	public float sampleData = 0f;
	@Override
	public void reset() {
		sampleData = 0f;
	}
}
