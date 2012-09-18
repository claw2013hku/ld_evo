package com.claw.evolution;

public class Constants {
	public static final String debug_tag = "ld_evo";
	public static final float pixelsPerMeter = 10f;
	public static final String tag_phyWorld = "PhyWorld";
	public static final String tag_UI = "UI";
	public static final String tag_UIMenu = "UIMenu";
	public static final String tag_UIActor = "UIActor";
	public static final String tag_mapGrid = "mapGrid";
	public static final String tag_AIGrid = "AIGrid";
	public static final String tag_phyBody = "phyBody";
	public static final String tag_world = "world";
	public static final String tag_worldCamera = "worldCamera";
	public static final String tag_player = "player";
	public static final String tag_AIplayer = "AIplayer";
	public static final String tag_AIMap = "AIMap";
	public static final String tag_PosMap = "PosMap";
	
	public static final float AI_NodeDensity = 0.14f;
	
	//the z-order of UI elements, higher Z brings UI element to the front, add / modify.
	public static int z_mapGrid = 0;
	public static int z_sample = 1;
	public static int z_AIGrid = 2;
	public static int z_uiMenu = 3;
	public static int z_splash = 4;
}
