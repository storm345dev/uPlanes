package net.stormdev.uPlanes.api;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.presets.BoatPreset;
import net.stormdev.uPlanes.presets.PlanePreset;
import net.stormdev.uPlanes.presets.PresetManager;
import net.stormdev.uPlanes.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple serializable format for plane data
 * 
 * Do not manipulate this directly if avoidable, use the API
 *
 */
public class Boat extends uPlanesVehicleBase<BoatPreset> implements Serializable {
	public static final double DEFAULT_TURN_AMOUNT = 2;
	private static final long serialVersionUID = 2L;
	private double turnAmount = DEFAULT_TURN_AMOUNT;
	private transient boolean steeringKeyboard = false;
	private transient long speedLockTime = 0;

	public void postBoatUpdateEvent(Vector vec) {
		this.lastUpdateEventTime = System.currentTimeMillis();
		this.setLastUpdateEventVec(vec);
	}

	public Boat() { //An empty boat
		super();
		setCurrentPitch(0);
		setRoll(0);
	}

	public Boat(double speed, String name, double health, double accelMod, double turnAmountPerTick) {
		this(speed, name, health, accelMod, turnAmountPerTick, false);
	}

	public Boat(double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean steeringKeyboard) {
		super(speed, name, health, accelMod);
		this.turnAmount = turnAmountPerTick;
		this.steeringKeyboard = steeringKeyboard;
	}

	public int getMaxPassengers() {
		if (this.maxPassengers < 0) {
			if (isFromPreset()) {
				return getPreset().getMaxPassengers();
			} else {
				return 1;
			}
		}
		return maxPassengers;
	}

	public void setMaxPassengers(int maxPassengers) {
		this.maxPassengers = maxPassengers;
	}

	public double getBoatRotationOffsetDegrees() {
		if (this.maxPassengers < 0 && isFromPreset()) {
			return getPreset().getBoatRotationOffsetDeg();
		}
		return boatRotationOffsetDegrees;
	}

	public void setBoatRotationOffsetDegrees(double boatRotationOffsetDegrees) {
		this.boatRotationOffsetDegrees = boatRotationOffsetDegrees;
	}

	public boolean isFromPreset() {
		return getPreset() != null;
	}

	public BoatPreset getPreset() {
		if (!PresetManager.usePresets) {
			return null;
		}

		List<BoatPreset> pps = main.plugin.presets.getBoatPresets();
		for (BoatPreset pp : new ArrayList<BoatPreset>(pps)) {
			if (pp.getName().equals(getName())) {
				return pp;
			}
		}
		return null;
	}

	public ItemStack toItemStack() {
		ItemStack stack;
		MaterialData displayBlock = getCartDisplayBlock();
		if (getPreset() != null) {
			displayBlock = getPreset().getDisplayBlock();
		}
		if (main.config.getBoolean("general.planes.renderAsModelledBlockWhenExist") && displayBlock != null) {
			stack = new ItemStack(displayBlock.getItemType());
			stack.setData(displayBlock);
		} else {
			stack = new ItemStack(Material.MINECART);
		}
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		lore.add(ChatColor.GRAY + "boat");
		lore.add(main.colors.getTitle() + "[Speed:] " + main.colors.getInfo() + mutliplier);
		lore.add(main.colors.getTitle() + "[Health:] " + main.colors.getInfo() + health);
		lore.add(main.colors.getTitle() + "[Acceleration:] " + main.colors.getInfo() + accelMod * 10.0d);
		lore.add(main.colors.getTitle() + "[Handling:] " + main.colors.getInfo() + turnAmount * 10.0d);
		if (getMaxPassengers() > 1) {
			lore.add(main.colors.getTitle() + "[Passengers:] " + main.colors.getInfo() + getMaxPassengers());
		}
		meta.setDisplayName(Colors.colorise(name));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	public void setTurnAmountPerTick(double d) {
		this.turnAmount = d;
	}

	@Override
	public Boat setId(UUID id) {
		return (Boat) super.setId(id);
	}

	@Override
	public VehicleType getType() {
		return VehicleType.BOAT;
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
	public String getTypeName() {
		return "boat";
	}

	public double getTurnAmountPerTick() {
		if (turnAmount <= 0) {
			turnAmount = DEFAULT_TURN_AMOUNT;
		}
		return turnAmount;
	}

	public boolean isSteeringKeyboard() {
		return steeringKeyboard;
	}

	public void setSteeringKeyboard(boolean steeringKeyboard) {
		this.steeringKeyboard = steeringKeyboard;
	}

	public long getSpeedLockTime() {
		return speedLockTime;
	}

	public void setSpeedLockTime(long speedLockTime) {
		this.speedLockTime = speedLockTime;
	}

}
