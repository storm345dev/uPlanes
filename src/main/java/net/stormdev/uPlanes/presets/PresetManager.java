package net.stormdev.uPlanes.presets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.stormdev.uPlanes.main.main;

import org.bukkit.configuration.ConfigurationSection;

public class PresetManager {
	public static boolean usePresets = false;
	private List<PlanePreset> presets = new ArrayList<PlanePreset>();
	private Random random = new Random();
	
	public PresetManager(){
		init();
	}
	
	public List<PlanePreset> getPresets(){
		return new ArrayList<PlanePreset>(presets);
	}
	
	public PlanePreset getRandomPreset(){
		return presets.get(random.nextInt(presets.size()));
	}
	
	private void init(){
		if(!main.config.contains("general.planes.presets.enable")){
			main.config.set("general.planes.presets.enable", true);
		}
		usePresets = main.config.getBoolean("general.planes.presets.enable");
		ConfigurationSection presets = main.config.getConfigurationSection("general.planes.presets.types");
		if(presets == null){ //Write defaults
			presets = main.config.createSection("general.planes.presets.types");
			presets.set("superjet.name", "Fast Jet");
			presets.set("superjet.speed", 50);
			presets.set("superjet.health", 75);
			presets.set("superjet.acceleration", 20);
			presets.set("superjet.handling", 15);
			presets.set("superjet.cost", 200);
			
			presets.set("jet.name", "Jet");
			presets.set("jet.speed", 40);
			presets.set("jet.health", 75);
			presets.set("jet.acceleration", 15);
			presets.set("jet.handling", 10);
			presets.set("jet.cost", 150);
			
			presets.set("lightaircraft.name", "Piper");
			presets.set("lightaircraft.speed", 25);
			presets.set("lightaircraft.health", 40);
			presets.set("lightaircraft.acceleration", 7);
			presets.set("lightaircraft.handling", 5);
			presets.set("lightaircraft.cost", 60);
			
			presets.set("attackchopper.name", "Attack Chopper");
			presets.set("attackchopper.speed", 35);
			presets.set("attackchopper.health", 100);
			presets.set("attackchopper.acceleration", 10);
			presets.set("attackchopper.handling", 10);
			presets.set("attackchopper.hover", true);
			presets.set("attackchopper.cost", 150);
		}
		
		this.presets.clear();
		Set<String> presetIDs = presets.getKeys(false);
		
		for(String id:presetIDs){
			try {
				ConfigurationSection sect = presets.getConfigurationSection(id);
				String name = sect.getString("name");
				double speed = sect.getDouble("speed");
				double health = sect.getDouble("health");
				double accelMod = sect.getDouble("acceleration") / 10.0d;
				double turnAmountPerTick = sect.getDouble("handling") / 10.0d;
				boolean hover = sect.contains("hover") ? sect.getBoolean("hover") : false;
				double cost = sect.getDouble("cost");
				
				if(name == null || speed <= 0 || health <= 0 || accelMod <= 0 || turnAmountPerTick <= 0){
					throw new Exception("INVALID plane preset "+id);
				}
				
				PlanePreset pp = new PlanePreset(id, speed, name, health, accelMod, turnAmountPerTick, hover, cost);
				this.presets.add(pp);
			} catch (Exception e) {
				//Error loading this preset!
				e.printStackTrace();
			}
		}
		
		if(usePresets && this.presets.size() < 1){
			usePresets = false;
		}
	}
}
