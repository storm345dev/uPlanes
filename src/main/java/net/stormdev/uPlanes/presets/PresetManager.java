package net.stormdev.uPlanes.presets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.stormdev.uPlanes.main.ItemStackFromId;
import net.stormdev.uPlanes.main.main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

public class PresetManager {
	public static boolean usePresets = false;
	public static boolean disableItemRenaming = false;
	private List<PlanePreset> presets = new ArrayList<PlanePreset>();
	private List<BoatPreset> boatPresets = new ArrayList<>();
	private Random random = new Random();
	
	public PresetManager(){
		init();
	}

	public List<BoatPreset> getBoatPresets(){
		return new ArrayList<BoatPreset>(boatPresets);
	}

	public BoatPreset getRandomBoatPreset(){
		return boatPresets.get(random.nextInt(boatPresets.size()));
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
		if(!main.config.contains("general.planes.presets.disableItemRename")){
			main.config.set("general.planes.presets.disableItemRename", true);
		}
		usePresets = main.config.getBoolean("general.planes.presets.enable");
		disableItemRenaming = main.config.getBoolean("general.planes.presets.disableItemRename");
		ConfigurationSection presets = main.config.getConfigurationSection("general.planes.presets.types");
		ConfigurationSection boatPresets = main.config.getConfigurationSection("general.planes.presets.boatTypes");
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
		if(boatPresets == null) { //Write defaults
			boatPresets = main.config.createSection("general.planes.presets.boatTypes");
			boatPresets.set("boat1.name", "Boat");
			boatPresets.set("boat1.speed", 50);
			boatPresets.set("boat1.health", 75);
			boatPresets.set("boat1.acceleration", 20);
			boatPresets.set("boat1.handling", 15);
			boatPresets.set("boat1.cost", 200);
		}
		
		this.presets.clear();
		this.boatPresets.clear();
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
				boolean hoverMidair = sect.contains("planeHover") ? sect.getBoolean("planeHover") : false;
				if(sect.contains("helicopter")){
					hoverMidair = hover;
					hover = sect.getBoolean("helicopter");
				}
				double cost = sect.getDouble("cost");
				
				if(name == null || speed <= 0 || health <= 0 || accelMod <= 0 || turnAmountPerTick <= 0){
					throw new Exception("INVALID plane preset "+id);
				}
				
				MaterialData displayBlock = null;
				double displayOffset = 0;
				if(sect.contains("display")){
					try {
						displayBlock = ItemStackFromId.get(sect.getString("display")).getData();
					} catch (Exception e) {
						//Invalid config
					}
				}
				if(sect.contains("displayOffset")){
					displayOffset = sect.getDouble("displayOffset");
				}
				float hitBoxX = -1;
				float hitBoxZ = -1;
				if(sect.contains("hitbox.x")){
					hitBoxX = (float) sect.getDouble("hitbox.x");
				}
				if(sect.contains("hitbox.z")){
					hitBoxZ = (float) sect.getDouble("hitbox.z");
				}
				if(!sect.contains("passengers.max")){
					sect.set("passengers.max",1);
				}
				if(!sect.contains("passengers.boatRotationOffsetDeg")){
					sect.set("passengers.boatRotationOffsetDeg",0.0d);
				}
				
				PlanePreset pp = new PlanePreset(id, speed, name, health, accelMod, turnAmountPerTick, hover, cost, displayBlock, displayOffset, hitBoxX, hitBoxZ, hoverMidair);
				pp.setMaxPassengers(sect.getInt("passengers.max"));
				pp.setBoatRotationOffsetDeg(sect.getDouble("passengers.boatRotationOffsetDeg"));

				this.presets.add(pp);
			} catch (Exception e) {
				//Error loading this preset!
				e.printStackTrace();
			}
		}
		for(String id:boatPresets.getKeys(false)){
			try {
				ConfigurationSection sect = boatPresets.getConfigurationSection(id);
				String name = sect.getString("name");
				double speed = sect.getDouble("speed");
				double health = sect.getDouble("health");
				double accelMod = sect.getDouble("acceleration") / 10.0d;
				double turnAmountPerTick = sect.getDouble("handling") / 10.0d;
				double cost = sect.getDouble("cost");

				if(name == null || speed <= 0 || health <= 0 || accelMod <= 0 || turnAmountPerTick <= 0){
					throw new Exception("INVALID boat preset "+id);
				}

				MaterialData displayBlock = null;
				double displayOffset = 0;
				if(sect.contains("display")){
					try {
						displayBlock = ItemStackFromId.get(sect.getString("display")).getData();
					} catch (Exception e) {
						//Invalid config
					}
				}
				if(sect.contains("displayOffset")){
					displayOffset = sect.getDouble("displayOffset");
				}
				float hitBoxX = -1;
				float hitBoxZ = -1;
				if(sect.contains("hitbox.x")){
					hitBoxX = (float) sect.getDouble("hitbox.x");
				}
				if(sect.contains("hitbox.z")){
					hitBoxZ = (float) sect.getDouble("hitbox.z");
				}
				if(!sect.contains("passengers.max")){
					sect.set("passengers.max",1);
				}
				if(!sect.contains("passengers.boatRotationOffsetDeg")){
					sect.set("passengers.boatRotationOffsetDeg",0.0d);
				}

				BoatPreset pp = new BoatPreset(id, speed, name, health, accelMod, turnAmountPerTick, cost, displayBlock, displayOffset, hitBoxX, hitBoxZ);
				pp.setMaxPassengers(sect.getInt("passengers.max"));
				pp.setBoatRotationOffsetDeg(sect.getDouble("passengers.boatRotationOffsetDeg"));

				this.boatPresets.add(pp);
			} catch (Exception e) {
				//Error loading this preset!
				e.printStackTrace();
			}
		}
		
		if(usePresets && this.presets.size() < 1 && this.boatPresets.size() < 1){
			usePresets = false;
		}
	}
}
