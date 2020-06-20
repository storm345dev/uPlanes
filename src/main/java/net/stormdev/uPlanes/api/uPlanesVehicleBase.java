package net.stormdev.uPlanes.api;

import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.presets.BoatPreset;
import net.stormdev.uPlanes.presets.uPlanesVehiclePreset;
import org.bukkit.entity.Entity;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.UUID;

public abstract class uPlanesVehicleBase<P extends uPlanesVehiclePreset> implements uPlanesVehicle<P>, Serializable {
    private static final long serialVersionUID = 2L;

    protected double mutliplier = 30;
    protected String name = "Plane";
    protected double health = 50;
    protected double accelMod = 1;
    protected UUID id = UUID.randomUUID();
    protected boolean writtenOff = false;
    protected float hitboxX = -1;
    protected float hitboxZ = -1;
    protected int maxPassengers = -1;
    protected double[] boatsRotationOffsetDegrees = new double[]{0};

    protected transient float currentPitch = 0;
    protected transient float roll = 0;
    protected transient RollTarget rollTarget = RollTarget.NONE;
    protected transient MaterialData displayBlock;
    protected transient double offset;
    protected transient long lastUpdateEventTime = System.currentTimeMillis();
    protected transient Vector lastUpdateEventVec = null;
    protected transient Entity lastDamager;

    @Override
    public long getTimeSinceLastUpdateEvent(){
        return System.currentTimeMillis() - this.lastUpdateEventTime;
    }

    @Override
    public void postUpdateEvent(Vector vec){
        this.lastUpdateEventTime = System.currentTimeMillis();
        this.setLastUpdateEventVec(vec);
    }

    public uPlanesVehicleBase(){ //An empty boat
        setCurrentPitch(0);
        setRoll(0);
    }

    @Override
    public void setRoll(double i) {
        this.rollTarget = RollTarget.NONE;
        this.roll = (float) i;
    }

    @Override
    public RollTarget getRollTarget(){
        return this.rollTarget;
    }

    @Override
    public void updateRoll(){
        RollTarget t = this.rollTarget;
        if(t == null){
            t = RollTarget.NONE;
        }
        if(this.roll == getRollAmount(t)){
            return;
        }

        float diff = (float) getRollAmount(t) - this.roll;
        if(diff > getTurnAmountPerTick()){
            diff = (float) getTurnAmountPerTick();
        }
        if(diff < -getTurnAmountPerTick()){
            diff = (float) -getTurnAmountPerTick();
        }

        this.roll += diff;
    }

    @Override
    public void setRollTarget(RollTarget target){
        this.rollTarget = target;
    }

    @Override
    public float getRoll(){
        return this.roll;
    }

    public uPlanesVehicleBase(double speed, String name, double health, double accelMod){
        setCurrentPitch(0);
        if(speed > main.maxSpeed){
            speed = main.maxSpeed;
        }
        this.mutliplier = speed;
        this.name = name;
        this.health = health;
        this.accelMod = accelMod;
    }

    public uPlanesVehicleBase(double speed, double health, double accelMod){
        this(speed,"vehicle name",health,accelMod);
    }

    @Override
    public int getMaxPassengers() {
        if(this.maxPassengers < 0){
            if(isFromPreset()) {
                return getPreset().getMaxPassengers();
            }
            else {
                return 1;
            }
        }
        return maxPassengers;
    }

    @Override
    public void setMaxPassengers(int maxPassengers) {
        this.maxPassengers = maxPassengers;
    }

    @Override
    public double[] getBoatRotationOffsetDegrees() {
        if(this.maxPassengers < 0 && isFromPreset()) {
            return getPreset().getBoatRotationOffsetDeg();
        }
        return boatsRotationOffsetDegrees;
    }

    @Override
    public void setBoatRotationOffsetDegrees(double[] boatRotationOffsetDegrees) {
        this.boatsRotationOffsetDegrees = boatRotationOffsetDegrees;
    }

    protected String getHandleString(boolean b){
        if(b){
            return "Yes";
        }
        else {
            return "No";
        }
    }

    @Override
    public boolean isFromPreset(){
        return getPreset() != null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public uPlanesVehicleBase<P> setId(UUID id) {
        this.id = id;
        return this;
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
    public boolean isWrittenOff() {
        return writtenOff;
    }

    @Override
    public void setWrittenOff(boolean writtenOff) {
        this.writtenOff = writtenOff;
    }

    @Override
    public float getCurrentPitch() {
        return currentPitch;
    }

    @Override
    public void setCurrentPitch(float currentPitch) {
        this.currentPitch = currentPitch;
    }

    @Override
    public double getAccelMod() {
        return accelMod;
    }

    @Override
    public void setAccelMod(double accelMod) {
        this.accelMod = accelMod;
    }

    /**
     * WON'T work after plane has been broken and placed back down! Use presets for that!
     * @return
     */
    @Override
    public MaterialData getCartDisplayBlock() {
        return displayBlock;
    }

    /**
     * WON'T work after plane has been broken and placed back down! Use presets for that!
     * @return
     */
    @Override
    public void setCartDisplayBlock(MaterialData displayBlock) {
        this.displayBlock = displayBlock;
    }

    /**
     * WON'T work after plane has been broken and placed back down! Use presets for that!
     * @return
     */
    @Override
    public double getDisplayOffset() {
        return offset;
    }

    /**
     * WON'T work after plane has been broken and placed back down! Use presets for that!
     * @return
     */
    @Override
    public void setDisplayOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public Vector getLastUpdateEventVec() {
        return lastUpdateEventVec;
    }

    @Override
    public void setLastUpdateEventVec(Vector lastUpdateEventVec) {
        this.lastUpdateEventVec = lastUpdateEventVec;
    }

    @Override
    public Entity getLastDamager() {
        return lastDamager;
    }

    @Override
    public void setLastDamager(Entity lastDamager) {
        this.lastDamager = lastDamager;
    }

    @Override
    public float getHitboxX() {
        P pp = getPreset();
        if(hitboxX < 0 && pp != null){
            return pp.getHitBoxX();
        }
        return hitboxX;
    }

    /**
     * Isn't saved when plane is destroyed and replaced
     */
    @Override
    public void setHitboxX(float hitboxX) {
        this.hitboxX = hitboxX;
    }

    @Override
    public float getHitboxZ() {
        P pp = getPreset();
        if(hitboxZ < 0 && pp != null){
            return pp.getHitBoxZ();
        }
        return hitboxZ;
    }

    /**
     * Isn't saved when plane is destroyed and replaced
     */
    @Override
    public void setHitboxZ(float hitboxZ) {
        this.hitboxZ = hitboxZ;
    }
}
