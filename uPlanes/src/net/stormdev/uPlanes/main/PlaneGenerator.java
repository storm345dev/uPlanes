package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.utils.Plane;

public class PlaneGenerator {
	public static Plane gen(){
		Plane plane = new Plane();
		double maxHealth = main.config.getDouble("general.planes.maxHealth");
		double defHealth = main.config.getDouble("general.planes.defaultHealth");
		int defMult = 10;
		int healthTopBand = (int)defHealth + 10;
		int healthBottomBand = (int)defHealth - 10;
		String name = "Plane";
		
		double health = main.plugin.random.nextInt(healthTopBand-healthBottomBand)+healthBottomBand;
		double speed = (main.plugin.random.nextInt((defMult+50)-(defMult-50))+(defMult-50));
		
		if(health > maxHealth){
			health = maxHealth;
		}
		if(speed < 10){
			speed = 10;
		}
		
		plane.health = health;
		plane.mutliplier = speed;
		plane.name = name;
		
		return plane;
	}
}
