package net.stormdev.uPlanes.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.presets.PlanePreset;
import net.stormdev.uPlanes.presets.PresetManager;
import net.stormdev.uPlanes.utils.Colors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * Simple serializable format for plane data
 * 
 * Do not manipulate this directly if avoidable, use the API
 *
 */
public class Plane extends uPlanesVehicleBase<PlanePreset> implements Serializable {
	public static final double DEFAULT_TURN_AMOUNT = 2;
	
	private static final long serialVersionUID = 2L;
	private double turnAmount = DEFAULT_TURN_AMOUNT;
	private boolean hover = false; //If heli
	private boolean canPlaneHover = false; //If plane that can hover in midair
	private transient long speedLockTime = 0;
	private UUID id = UUID.randomUUID();

	private transient boolean speedLocked = false;
	
	public Plane(){ //An empty plane
		super();
		setCurrentPitch(0);
		setRoll(0);
	}

	public void postPlaneUpdateEvent(Vector vec){
		this.lastUpdateEventTime = System.currentTimeMillis();
		this.setLastUpdateEventVec(vec);
	}

	public boolean canPlaneHoverMidair(){
		return this.canPlaneHover;
	}

	public void setCanPlaneHover(boolean b){
		this.canPlaneHover = b;
	}

	public Plane(double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean hover){
		this(speed, name, health, accelMod, turnAmountPerTick, hover, hover);
	}

	public Plane(double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean hover, boolean canPlaneHoverMidair){
		super(speed,name,health,accelMod);
		this.turnAmount = turnAmountPerTick;
		this.hover = hover;
		this.canPlaneHover = canPlaneHoverMidair;
	}

	@Override
	public Plane setId(UUID id) {
		return (Plane) super.setId(id);
	}
	
	public PlanePreset getPreset(){
		if(!PresetManager.usePresets){
			return null;
		}
		
		List<PlanePreset> pps = main.plugin.presets.getPresets();
		for(PlanePreset pp:new ArrayList<PlanePreset>(pps)){
			if(pp.getName().equals(getName())){
				return pp;
			}
		}
		return null;
	}
	
	public ItemStack toItemStack(){
		ItemStack stack;
		MaterialData displayBlock = getCartDisplayBlock();
		if(getPreset() != null){
			displayBlock = getPreset().getDisplayBlock();
		}
		if(main.config.getBoolean("general.planes.renderAsModelledBlockWhenExist") && displayBlock != null){
			stack = new ItemStack(displayBlock.getItemType());
			stack.setData(displayBlock);
		}
		else {
			stack = new ItemStack(Material.MINECART);
		}
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		lore.add(ChatColor.GRAY+(isHover()?"helicopter":"plane"));
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+mutliplier);
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health);
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+accelMod*10.0d);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+turnAmount*10.0d);
		if(hover||canPlaneHover){
			lore.add(main.colors.getTitle()+"[Hover:] "+main.colors.getInfo()+getHandleString(hover||canPlaneHover));
		}
		if(getMaxPassengers() > 1){
			lore.add(main.colors.getTitle()+"[Passengers:] "+main.colors.getInfo()+getMaxPassengers());
		}
		meta.setDisplayName(Colors.colorise(name));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	@Override
	public void setTurnAmountPerTick(double d){
		this.turnAmount = d;
	}

	@Override
	public VehicleType getType() {
		return VehicleType.PLANE;
	}

	@Override
	public String getTypeName() {
		return "plane";
	}

	@Override
	public float getRollAmount(RollTarget rollTarget) {
		switch (rollTarget){
			case LEFT:
				return 25;
			case NONE:
				return 0;
			case RIGHT:
				return -25;
		}
		return 0;
	}

	@Override
	public double getTurnAmountPerTick() {
		if(turnAmount <= 0){
			turnAmount = DEFAULT_TURN_AMOUNT;
		}
		return turnAmount;
	}

	public boolean canFloat(){
		return isHover() || canPlaneHoverMidair();
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
	}

	public boolean isSpeedLocked() {
		return speedLocked;
	}

	public void setSpeedLocked(boolean speedLocked) {
		this.speedLocked = speedLocked;
	}

	public long getSpeedLockTime() {
		return speedLockTime;
	}

	public void setSpeedLockTime(long speedLockTime) {
		this.speedLockTime = speedLockTime;
	}
}
