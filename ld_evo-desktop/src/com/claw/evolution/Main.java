package com.claw.evolution; 

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
  
public class Main {        
	public static void main(String[] args) { 
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ld_evo";      
		cfg.useGL20 = true;                  
		cfg.width = 800;                       
		cfg.height = 480;                                                       
		                                                                 
		new LwjglApplication(new EvolutionGame(), cfg);
	}         
}             

//asdfasdfhs
                         