package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.presets.PresetManager;

public class PlaneGenerator {
	public static Plane gen(){
		if(PresetManager.usePresets){
			return main.plugin.presets.getRandomPreset().toPlane();
		}
		
		Plane plane = new Plane();
		double maxHealth = main.config.getDouble("general.planes.maxHealth");
		double defHealth = main.config.getDouble("general.planes.defaultHealth");
		int defMult = 10;
		int healthTopBand = (int)defHealth + 10;
		int healthBottomBand = (int)defHealth - 10;
		String name = "Plane";
		
		double health = main.plugin.random.nextInt(healthTopBand-healthBottomBand)+healthBottomBand;
		int si = (defMult+50)-(defMult-50)+(defMult-50);
		if(si < 0){
			si = 0;
		}
		int i = (int) (si-main.maxSpeed);
		if(i<=0){
			i = 1;
		}
		double speed = (main.plugin.random.nextInt((int)(i)))+si;
		
		if(health > maxHealth){
			health = maxHealth;
		}
		if(speed < 10){
			speed = 10;
		}
		
		plane.setHealth(health);
		plane.setSpeed(speed);
		plane.setName(name);
		
		return plane;
	}
}
