package net.stormdev.uPlanes.presets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;

/**
 * Simple serializable format for plane data
 * 
 * Do not manipulate this directly if avoidable, use the API
 *
 */
public class PlanePreset implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String presetID = "";
	private double mutliplier = 30;
	private String name = "Plane";
	private double health = 50;
	private double turnAmount = 2;
	private double accelMod = 1;
	private boolean hover = false;
	private double cost = 0;
	private MaterialData displayBlock;
	private double displayOffset = 0;
	private float hitBoxX = -1;
	private float hitBoxZ = -1;
	
	public PlanePreset(String presetID, double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean hover, double cost, MaterialData displayBlock, double offset, float hitBoxX, float hitBoxZ){
		this.setPresetID(presetID);
		if(speed > main.maxSpeed){
			speed = main.maxSpeed;
		}
		this.mutliplier = speed;
		this.name = name;
		this.health = health;
		this.accelMod = accelMod;
		this.turnAmount = turnAmountPerTick;
		this.hover = hover;
		this.setCost(cost);
		this.setDisplayBlock(displayBlock);
		this.setDisplayOffset(offset);
		this.hitBoxX = hitBoxX;
		this.hitBoxZ = hitBoxZ;
	}
	
	public float getHitBoxX(){
		return this.hitBoxX;
	}
	
	public float getHitBoxZ(){
		return this.hitBoxZ;
	}
	
	private String getHandleString(boolean b){
		if(b){
			return "Yes";
		}
		else {
			return "No";
		}
	}
	
	public Plane toPlane(){
		return new Plane(getSpeed(), getName(), getHealth(), getAccelMod(), getTurnAmountPerTick(), isHover());
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
	
	public void setTurnAmountPerTick(double d){
		this.turnAmount = d;
	}

	public double getTurnAmountPerTick() {
		return turnAmount;
	}

	public double getAccelMod() {
		return accelMod;
	}

	public void setAccelMod(double accelMod) {
		this.accelMod = accelMod;
	}

	public String getPresetID() {
		return presetID;
	}

	public void setPresetID(String presetID) {
		this.presetID = presetID;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public String[] getSellLore() {
		String currency = main.config.getString("general.currencySign");
		List<String> lore = new ArrayList<String>();
		lore.add(main.colors.getTitle()+"[Price:] "+main.colors.getInfo()+currency+cost);
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+mutliplier);
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health);
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+accelMod*10.0d);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+turnAmount*10.0d);
		if(hover){
			lore.add(main.colors.getTitle()+"[Hover:] "+main.colors.getInfo()+getHandleString(hover));
		}
		return lore.toArray(new String[]{});
	}
	
	public boolean hasDisplayBlock(){
		return this.displayBlock != null;
	}

	public MaterialData getDisplayBlock() {
		return displayBlock;
	}

	public void setDisplayBlock(MaterialData displayBlock) {
		this.displayBlock = displayBlock;
	}

	public double getDisplayOffset() {
		return displayOffset;
	}

	public void setDisplayOffset(double displayOffset) {
		this.displayOffset = displayOffset;
	}
}
