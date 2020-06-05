package net.stormdev.uPlanes.presets;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.uPlanesVehicle;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class uPlanesVehiclePresetBase<T extends uPlanesVehicle> implements uPlanesVehiclePreset<T>,Serializable {

	private static final long serialVersionUID = 1L;
	protected String presetID = "";
	protected double mutliplier = 30;
	protected String name = "Boat";
	protected double health = 50;
	protected double turnAmount = 2;
	protected double accelMod = 1;
	protected double cost = 0;
	protected MaterialData displayBlock;
	protected double displayOffset = 0;
	protected float hitBoxX = -1;
	protected float hitBoxZ = -1;
	protected int maxPassengers = 1;
	protected double boatRotationOffsetDeg = 0;

	public uPlanesVehiclePresetBase(String presetID, double speed, String name, double health, double accelMod, double turnAmountPerTick, double cost, MaterialData displayBlock, double offset, float hitBoxX, float hitBoxZ){
		this.setPresetID(presetID);
		if(speed > main.maxSpeed){
			speed = main.maxSpeed;
		}
		this.mutliplier = speed;
		this.name = name;
		this.health = health;
		this.accelMod = accelMod;
		this.turnAmount = turnAmountPerTick;
		this.setCost(cost);
		this.setDisplayBlock(displayBlock);
		this.setDisplayOffset(offset);
		this.hitBoxX = hitBoxX;
		this.hitBoxZ = hitBoxZ;
	}

	@Override
	public int getMaxPassengers() {
		return maxPassengers;
	}

	@Override
	public void setMaxPassengers(int maxPassengers) {
		this.maxPassengers = maxPassengers;
	}

	@Override
	public double getBoatRotationOffsetDeg() {
		return boatRotationOffsetDeg;
	}

	@Override
	public void setBoatRotationOffsetDeg(double boatRotationOffsetDeg) {
		this.boatRotationOffsetDeg = boatRotationOffsetDeg;
	}

	@Override
	public float getHitBoxX(){
		return this.hitBoxX;
	}

	@Override
	public float getHitBoxZ(){
		return this.hitBoxZ;
	}
	
	@Override
	public String getHandleString(boolean b){
		if(b){
			return "Yes";
		}
		else {
			return "No";
		}
	}

	@Override
	public double getSpeed() {
		if(this.mutliplier > main.maxSpeed){
			this.mutliplier = main.maxSpeed;
		}
		return mutliplier;
	}

	@Override
	public void setSpeed(double speed) {
		if(speed > main.maxSpeed){
			speed = main.maxSpeed;
		}
		this.mutliplier = speed;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public double getHealth() {
		return health;
	}

	@Override
	public void setHealth(double health) {
		this.health = health;
	}

	@Override
	public void setTurnAmountPerTick(double d){
		this.turnAmount = d;
	}

	@Override
	public double getTurnAmountPerTick() {
		return turnAmount;
	}

	@Override
	public double getAccelMod() {
		return accelMod;
	}

	@Override
	public void setAccelMod(double accelMod) {
		this.accelMod = accelMod;
	}

	@Override
	public String getPresetID() {
		return presetID;
	}

	@Override
	public void setPresetID(String presetID) {
		this.presetID = presetID;
	}

	@Override
	public double getCost() {
		return cost;
	}

	@Override
	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public boolean hasDisplayBlock(){
		return this.displayBlock != null;
	}

	@Override
	public MaterialData getDisplayBlock() {
		return displayBlock;
	}

	@Override
	public void setDisplayBlock(MaterialData displayBlock) {
		this.displayBlock = displayBlock;
	}

	@Override
	public double getDisplayOffset() {
		return displayOffset;
	}

	@Override
	public void setDisplayOffset(double displayOffset) {
		this.displayOffset = displayOffset;
	}
}
