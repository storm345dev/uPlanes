package net.stormdev.uPlanes.presets;

import net.stormdev.uPlanes.api.uPlanesVehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public interface uPlanesVehiclePreset<T extends uPlanesVehicle> {
    public int getMaxPassengers();

    public T toVehicle();

    public void setMaxPassengers(int maxPassengers);

    public double getBoatRotationOffsetDeg();

    public void setBoatRotationOffsetDeg(double boatRotationOffsetDeg);

    public float getHitBoxX();

    public float getHitBoxZ();

    public String getHandleString(boolean b);

    public ItemStack toItemStack();

    public double getSpeed();

    public void setSpeed(double speed);

    public String getName();

    public void setName(String name);

    public double getHealth();

    public void setHealth(double health);

    public void setTurnAmountPerTick(double d);

    public double getTurnAmountPerTick();

    public double getAccelMod();

    public void setAccelMod(double accelMod);

    public String getPresetID();

    public void setPresetID(String presetID);

    public double getCost();

    public void setCost(double cost);

    public String[] getSellLore();

    public boolean hasDisplayBlock();

    public MaterialData getDisplayBlock();

    public void setDisplayBlock(MaterialData displayBlock);

    public double getDisplayOffset();

    public void setDisplayOffset(double displayOffset);
}
