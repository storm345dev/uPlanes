package net.stormdev.uPlanes.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Simple serializable format for plane data
 * 
 * Do not manipulate this directly if avoidable, use the API
 *
 */
public class Plane implements Serializable {
	public static final double DEFAULT_TURN_AMOUNT = 2;
	
	private static final long serialVersionUID = 2L;
	private double mutliplier = 30;
	private String name = "Plane";
	private double health = 50;
	private double turnAmount = DEFAULT_TURN_AMOUNT;
	private double accelMod = 1;
	private boolean hover = false;
	private UUID id = UUID.randomUUID();
	private boolean writtenOff = false;
	private transient float currentPitch = 0;
	
	public Plane(){ //An empty plane
		setCurrentPitch(0);
	}
	
	public Plane(double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean hover){
		setCurrentPitch(0);
		if(speed > main.maxSpeed){
			speed = main.maxSpeed;
		}
		this.mutliplier = speed;
		this.name = name;
		this.health = health;
		this.accelMod = accelMod;
		this.turnAmount = turnAmountPerTick;
		this.hover = hover;
	}
	
	private String getHandleString(boolean b){
		if(b){
			return "Yes";
		}
		else {
			return "No";
		}
	}
	
	public ItemStack toItemStack(){
		ItemStack stack = new ItemStack(Material.MINECART);
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		lore.add(ChatColor.GRAY+"plane");
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+mutliplier);
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health);
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+accelMod*10.0d);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+turnAmount*10.0d);
		if(hover){
			lore.add(main.colors.getTitle()+"[Hover:] "+main.colors.getInfo()+getHandleString(hover));
		}
		meta.setDisplayName(Colors.colorise(name));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	public UUID getId() {
		return id;
	}

	public Plane setId(UUID id) {
		this.id = id;
		return this;
	}

	public double getSpeed() {
		if(this.mutliplier > main.maxSpeed){
			this.mutliplier = main.maxSpeed;
		}
		return mutliplier;
	}

	public void setSpeed(double speed) {
		if(speed > main.maxSpeed){
			speed = main.maxSpeed;
		}
		this.mutliplier = speed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
	}

	public boolean isWrittenOff() {
		return writtenOff;
	}

	public void setWrittenOff(boolean writtenOff) {
		this.writtenOff = writtenOff;
	}
	
	public void setTurnAmountPerTick(double d){
		this.turnAmount = d;
	}

	public double getTurnAmountPerTick() {
		if(turnAmount <= 0){
			turnAmount = DEFAULT_TURN_AMOUNT;
		}
		return turnAmount;
	}

	public float getCurrentPitch() {
		return currentPitch;
	}

	public void setCurrentPitch(float currentPitch) {
		this.currentPitch = currentPitch;
	}

	public double getAccelMod() {
		return accelMod;
	}

	public void setAccelMod(double accelMod) {
		this.accelMod = accelMod;
	}
}
