package net.stormdev.uPlanes.api;

import net.stormdev.uPlanes.presets.BoatPreset;
import net.stormdev.uPlanes.presets.uPlanesVehiclePreset;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.UUID;

public interface uPlanesVehicle<P extends uPlanesVehiclePreset> {
    public static enum VehicleType {
        BOAT, PLANE, OTHER;
    }

    public VehicleType getType();

    public String getTypeName();

    public float getRollAmount(RollTarget rollTarget);

    public double getTurnAmountPerTick();
    public void setTurnAmountPerTick(double d);

    public  long getTimeSinceLastUpdateEvent();

    public  void postUpdateEvent(Vector vec);

    public  void setRoll(double i);

    public RollTarget getRollTarget();

    public void updateRoll();

    public void setRollTarget(RollTarget target);

    public float getRoll();

    public int getMaxPassengers();

    public void setMaxPassengers(int maxPassengers);

    public double[] getBoatRotationOffsetDegrees();

    public void setBoatRotationOffsetDegrees(double[] boatRotationOffsetDegrees);

    public boolean isFromPreset();

    public P getPreset();

    public ItemStack toItemStack();

    public UUID getId();

    public uPlanesVehicleBase setId(UUID id);

    public double getSpeed();

    public void setSpeed(double speed);

    public String getName();

    public void setName(String name);

    public double getHealth();

    public void setHealth(double health);

    public boolean isWrittenOff();

    public void setWrittenOff(boolean writtenOff);

    public float getCurrentPitch();

    public void setCurrentPitch(float currentPitch);

    public double getAccelMod();

    public void setAccelMod(double accelMod);

    public MaterialData getCartDisplayBlock();

    public void setCartDisplayBlock(MaterialData displayBlock);

    public double getDisplayOffset();

    public void setDisplayOffset(double offset);

    public Vector getLastUpdateEventVec();

    public void setLastUpdateEventVec(Vector lastUpdateEventVec);

    public Entity getLastDamager();

    public void setLastDamager(Entity lastDamager);

    public float getHitboxX();

    public void setHitboxX(float hitboxX);

    public float getHitboxZ();

    public void setHitboxZ(float hitboxZ);
}
