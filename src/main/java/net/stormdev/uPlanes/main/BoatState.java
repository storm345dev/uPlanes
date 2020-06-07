package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.uPlanesAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class BoatState {
    public static final double MAX_STEERING_ANGLE = 10; //Degrees
    private Boat boat;
    private Vector vel = new Vector(0,0,0); //In blocks per sec NOT per tick
    private double throttleAmt = 0; //Between -1 and 1
    private double dragConstant = 250; //0.5*rho*Cd*S in aerodynamics... Arbitrary since thrust force will scale with config defined max velocity
    //private double sideslipDragConstant = 150; //Normalized by vehicle length
    private double thrustYawOffsetAngleDeg = 0; //0 is inline with forwards direction, positive CLOCKWISE
    private double yawRateDeg = 0; //Current rate yawing, positive CLOCKWISE. PER SECOND, not per tick
    private double planingSpeed = 1; //Blocks/tick
    private double curYaw = 0;//Matches bukkit convention, in degrees
    private double errorY = 0;

    public BoatState(Boat b){
        this.boat = b;
        this.dragConstant = this.dragConstant*b.getAccelMod(); //Drag is sized from this for speed so increase of larger forces rel to mass and therefore more accel
    }

    public void updateVelocitiesYawForThisGameTick(Vehicle vehicle){
        double maxTurnRatePerTick = boat.getTurnAmountPerTick();
        Player pl = null;
        if(vehicle.getPassenger() != null && vehicle.getPassenger() instanceof Player){
            pl = (Player) vehicle.getPassenger();
            this.dragConstant = 250*uPlanesAPI.getBoatManager().getAlteredAccelerationMod(pl,vehicle,boat);
            maxTurnRatePerTick = uPlanesAPI.getBoatManager().getAlteredRotationAmountPerTick(pl,vehicle,boat);
        }
        else {
            this.dragConstant = 250*boat.getAccelMod();
        }

        boolean floating = false;
        double yCoord = vehicle.getLocation().getY();
        Block under = vehicle.getLocation().getBlock().getRelative(BlockFace.DOWN);
        double targetY = -1;
        if(under.isLiquid()){ //Make it float
            floating = true;
            targetY = under.getY()+1.05; //Height above the water = 0.2 blocks
            if(vehicle.getLocation().getBlock().isLiquid()){
                targetY = targetY + 1; //Boat float upwards as in water
            }
        }
        else if(!under.isEmpty()){
            targetY = under.getY()+1.05;
        }

        //Update x-z velocity
        //Bukkit.broadcastMessage("Yaw: "+curYaw);
        Vector directionVector = new Vector(-Math.sin(Math.toRadians(this.curYaw)),0,Math.cos(Math.toRadians(this.curYaw)));
        //Bukkit.broadcastMessage(this.curYaw+" "+directionVector.getX()+" "+directionVector.getZ());
        Vector travelDir = directionVector.clone();
        Vector thrustForce = travelDir.clone().multiply(getThrustForce()*(floating?1:0));
        Vector toTheRightOfDir = travelDir.crossProduct(new Vector(0,1,0));
        double forwardVel = vel.clone().setY(0).dot(directionVector);
        double sideSlipVel = toTheRightOfDir.dot(vel.clone().setY(0));

        //Update yaw and yaw rate
        // I*wdot = M -> wdot = M/I
        //Bukkit.broadcastMessage("ROT: "+this.yawRateDeg+" "+getDragYawingMoment(maxTurnRatePerTick)+" "+getThrustYawingMoment()+" "+yawMomentOfInertia()+" "+getRudderYawingMoment(forwardVel));
        double totalMoment = getDragYawingMoment(maxTurnRatePerTick)+getThrustYawingMoment()+getRudderYawingMoment(forwardVel);

        double wdot = totalMoment / yawMomentOfInertia(); //rad/sec
        this.yawRateDeg = this.yawRateDeg+Math.toDegrees(wdot);
        if(this.yawRateDeg > 80){
            this.yawRateDeg = 80;
        }
        if(this.yawRateDeg < -80){
            this.yawRateDeg = -80;
        }
        this.curYaw = this.curYaw + this.yawRateDeg*(1/20.0); //Update vehicle yaw, 20 ticks per sec
        while(this.curYaw > 180){
            this.curYaw -= 360;
        }
        while(this.curYaw < -180){
            this.curYaw += 360;
        }

        //Exactly nullify sideslip...
        //Vector sideslipDragForce = toTheRightOfDir.clone().multiply(-sideSlipVel*boa);
        //Vector sideslipDragForce = toTheRightOfDir.clone().multiply(getSideslipDragForce(sideSlipVel));
        Vector sideslipDragForce = toTheRightOfDir.clone().multiply(-0.2*sideSlipVel*boat.getMass());
        /*if(forwardVel < 0.001 || sideSlipVel < 0.001 || true){
            //Exactly cancel sideslip
            sideslipDragForce = toTheRightOfDir.clone().multiply(-0.8*sideSlipVel*boat.getMass());
        }*/
        double mass = boat.getMass();
        if(mass < 1){
            mass = 1000;
        }
        mass = mass*3.5; //Give it more inertia
        Vector forwardDragForce = vel.clone().multiply(-Math.abs(getDragForce(forwardVel,boat.getCurrentPitch()/*getNominalPitch(forwardVel)*/)));
        if(Math.abs(forwardVel)<0.00001){
            forwardDragForce = vel.clone().setY(0).multiply(-mass);
        }
       /* Bukkit.broadcastMessage("T: "+thrustForce);
        Bukkit.broadcastMessage("D: "+forwardDragForce);
        *//*Bukkit.broadcastMessage("Side: "+sideslipDragForce);
        Bukkit.broadcastMessage("Side speed: "+sideSlipVel);*//*
        Bukkit.broadcastMessage("Side dir: "+toTheRightOfDir);
        Bukkit.broadcastMessage("Speed: "+forwardVel);
        Bukkit.broadcastMessage("Dir: "+travelDir);*/

        double errorY = yCoord-targetY;

        //Attempt to make it bob about...
        Vector verticalPerturbForce = new Vector(0,-0.5+main.plugin.random.nextDouble()*mass*4+(-0.5+main.plugin.random.nextDouble()*mass*40)*forwardVel,0);
        if(!floating || Math.abs(errorY) > 0.1){
            verticalPerturbForce = verticalPerturbForce.multiply(0);
        }
        Vector totalForce = thrustForce.clone().add(forwardDragForce).add(sideslipDragForce).add(verticalPerturbForce);

        Vector accel = totalForce.multiply(1/mass); //F = ma
        vel = vel.add(accel.multiply(1/20.0)); //20 ticks per sec
        if(vel.lengthSquared() > 1000000){
            Bukkit.broadcastMessage("Error in vel magnitude");
            vel = vel.normalize().multiply(3);
        }

        //Update y velocity

        if(targetY < 0){ //Falling
            double fallYVel = vel.getY()-9.81*(1/2.0);
            if(fallYVel < -3){ //Terminal velocity 3 blocks a sec
                fallYVel = -3;
            }
            vel.setY(fallYVel);
        }
        else { //Make boat float
            double errorDy = vel.getY();
            if(errorDy > 2){ //If velocity error huge, don't destabilize
                errorDy = 2; //Make it length 1
            }
            if(errorDy < -2){ //If velocity error huge, don't destabilize
                errorDy = -2; //Make it length 1
            }
            double kP = 0.7; //Spring constant of vertical bobbing motion
            double kD = 0.3; //Damping of vertical bobbing motion
            double kI = 0.7;
            double errorYI= this.errorY;
            this.errorY = this.errorY+errorY*(1/20.0);
            if(this.errorY > 1){
                this.errorY = 1;
            }
            if(this.errorY < -1){
                this.errorY = -1;
            }
            double yV = vel.getY() - kP*errorY; /*- kD*errorDy - kI*errorYI;*/
            if(yV > 0.2){
                yV = 0.2;
            }
            if(yV < -0.2){
                yV = -0.2;
            }
            vel.setY(yV);
        }

        float targetPitch = (float) ((float) ((float) getNominalPitch(forwardVel))+ (-0.5+main.plugin.random.nextDouble())*forwardVel);
        float curPitch = boat.getCurrentPitch();
        //Bukkit.broadcastMessage(forwardVel+" "+targetPitch+" throttle: "+this.throttleAmt);
        boat.setCurrentPitch((float) ((float) curPitch-0.2*(curPitch-targetPitch)));

        double targetRoll = getNominalRoll(forwardVel, this.yawRateDeg,maxTurnRatePerTick) + (-0.5+main.plugin.random.nextDouble())*2;
        double currentRoll = boat.getRoll();
        double errorRoll = currentRoll-targetRoll;
        boat.setRoll((float) (currentRoll-0.095*errorRoll));
        //Bukkit.broadcastMessage(this.throttleAmt+" "+this.thrustYawOffsetAngleDeg);
    }

    public double getDragYawingMoment(double maxTurnRatePerTick){
        double sign = 1;
        if(yawRateDeg > 0){
            sign = -1;
        }
        double maxYawRate = maxTurnRatePerTick*20;
        double equilibriumVal = getMaxThrustYawingMoment()+getMaxRudderYawingMoment();
        double k = equilibriumVal / (Math.abs(Math.pow(maxYawRate,2))*Math.cos(Math.toRadians(getNominalPitch(Math.pow(getBoatMaxRealSpeed(),2)))));
        double dragMoment = sign * k * Math.abs(Math.pow(yawRateDeg,2))*Math.cos(Math.toRadians(boat.getCurrentPitch()));
        if(Math.abs(dragMoment) > Math.abs(yawMomentOfInertia()*yawRateDeg)){ //Prevent overshoot
            dragMoment = sign*yawMomentOfInertia()*Math.abs(yawRateDeg);
        }
        return dragMoment;
        /*double l = 2*boat.getHitboxX(); //Boat length approx
        double linearVel = 0.5*l*Math.toRadians(yawRateDeg); //Linear velocity at end of boat assuming rotating around it's center
        double dragFTip = getDragForce(linearVel,0); //Really bad approximation since drag constant different completely...
        //Bad simple approximation of how corresponds to the resistance to angular motion
        double dragF = (2/3.0)*dragFTip; //Assume parabolic distribution of drag force along vehicle length
        //As boat pitches up reduce drag resistance to rotation
        return Math.cos(Math.toRadians(boat.getCurrentPitch()))*0.5*l*(2/3.0)*dragF; //Assume average acts 2/3 of distance from center to end of boat*/
    }

  /*  public boolean isTravellingBackwards(){
        Vector directionVector = new Vector(-Math.sin(Math.toRadians(this.curYaw)),0,Math.cos(Math.toRadians(this.curYaw)));
        double cosTheta = vel.clone().normalize().dot(directionVector);
        return cosTheta < 0;
    }*/

    public double getCurYaw() { //Matches bukkit convention
        return curYaw;
    }

    public void setCurYaw(double curYaw) {
        this.curYaw = curYaw-90;
        this.errorY = 0;
    }

    public double getYawRateDeg() {
        return yawRateDeg;
    }

    public void setYawRateDeg(double yawRate) {
        this.yawRateDeg = yawRate;
    }

    public double getThrustYawOffsetAngleDeg() {
        return thrustYawOffsetAngleDeg;
    }

    public void setThrustYawOffsetAngleDeg(double thrustYawOffsetAngleDeg) {
        this.thrustYawOffsetAngleDeg = thrustYawOffsetAngleDeg;
    }

    public double getRudderYawingMoment(double forwardVel){
        double speedMultiplier = Math.abs(forwardVel)/getBoatMaxRealSpeed();//Math.log(1+Math.pow(Math.abs(forwardVel)/getBoatMaxRealSpeed(),1)) / Math.log(2);
        if(speedMultiplier < 0.3){
            speedMultiplier = Math.abs(forwardVel) / 0.5;
            if(speedMultiplier > 0.3){
                speedMultiplier = 0.3;
            }
        }
        return speedMultiplier*0.1*boat.getHitboxX() * Math.sin(Math.toRadians(thrustYawOffsetAngleDeg)) * getMaxThrustForce();/*Math.cos(Math.toRadians(thrustYawOffsetAngleDeg))*/
    }

    public double getMaxRudderYawingMoment(){
        return 0.1*boat.getHitboxX() * Math.sin(Math.toRadians(MAX_STEERING_ANGLE)) * getMaxThrustForce()*Math.cos(Math.toRadians(MAX_STEERING_ANGLE));
    }

    public double getThrustYawingMoment(){
        //Assume thrust force acting at half of boat length from center of rotation at vehicle center
        /*double speedMultiplier = Math.log(1+Math.pow(getThrustMultiplier(),0.5)) / Math.log(2);*/
        return 0.9*boat.getHitboxX() * Math.sin(Math.toRadians(thrustYawOffsetAngleDeg)) * getMaxThrustForce()*getThrustMultiplier();
    }

    public double getMaxThrustYawingMoment(){
        //Assume thrust force acting at half of boat length from center of rotation at vehicle center
        return 0.9*boat.getHitboxX() * Math.sin(Math.toRadians(MAX_STEERING_ANGLE)) * getMaxThrustForce();
    }

    public double yawMomentOfInertia(){ //Calculate an approx moment of inertia in yaw
        double l = 2*boat.getHitboxX(); //Boat length approx

        //Approximate as rod rotating about it's center, with a fudge factor
        return (10*(1/12.0) * boat.getMass() * Math.pow(l,2));
    }

    public double getThrustMultiplier(){
        return (Math.exp(this.throttleAmt)-1) / (Math.exp(1)-1);
    }

    public double getThrustForce(){
        return getMaxThrustForce()*getThrustMultiplier()*Math.cos(Math.toRadians(this.thrustYawOffsetAngleDeg));
    }

    public double getBoatMaxRealSpeed(){
        return boat.getSpeed();
    }

    public double getMaxThrustForce(){ //Thrust = Drag at max speed
        return Math.abs(getDragForce(getBoatMaxRealSpeed(),getNominalPitch(getBoatMaxRealSpeed())));
    }

    /*public double getNominalPitch(){
        return getNominalPitch(vel.clone().setY(0).length());
    }*/

    public double getNominalRoll(double speed, double yawRateDeg, double maxTurnRatePerTick){
        if(speed < planingSpeed){
            return 0;
        }
        double maxRoll = 45;
        return -maxRoll*yawRateDeg/(maxTurnRatePerTick*20);
    }

    public double getNominalPitch(double speed){
        /*if(Math.abs(speed) < planingSpeed){
            return 0; //No pitch required
        }*/
        double pitch = speed*(20/Math.pow(getBoatMaxRealSpeed()*0.7,1)); //At 2 blocks per sec above planing speed should be 10 degrees pitch
        if(pitch > 20){ //Max 15 degrees pitch
            pitch = 20;
        }
        if(pitch < -5){ //Min -5 degrees pitch
            pitch = -5;
        }
        return -pitch;
    }

   /* public double getSideslipDragForce(double sideslipVel){
        double sign = 1;
        if(sideslipVel > 0){
            sign = -1;
        }
        return sign * this.sideslipDragConstant*boat.getHitboxX()*2*Math.pow(sideslipVel,2);
    }*/

    public double getDragForce(){
        return getDragForce(vel.clone().setY(0).length(), boat.getCurrentPitch());
    }

    public double getDragForce(double vel, double pitchDeg){
        double sign = 1;
        if(vel > 0){
            sign = -1;
        }
        double dragMultiplier = 1;
        double velRatio = vel/1.2;
        if(velRatio > 1){
            dragMultiplier*=(1/velRatio);
        }
        if(velRatio < 0.75){
            dragMultiplier*=(1/(velRatio+0.25));
        }
        if(dragMultiplier < 0.2){
            dragMultiplier = 0.2;
        }
        //Bukkit.broadcastMessage(dragMultiplier+"");
        return sign * Math.abs(Math.cos(Math.toRadians(pitchDeg))) * dragMultiplier*this.dragConstant * Math.pow(vel,2);
    }

    public float getBukkitVelVectorYaw(){
        return getVelVectorYaw()-90; //Needs a 90 degrees offset
    }

    public float getVelVectorYaw(){
        return (float) Math.toDegrees(Math.atan2(vel.getX() , -vel.getZ()));
    }

    public Vector getVelBlocksPerSec() {
        return vel;
    }

    public void setVelBlocksPerSec(Vector vel) {
        this.vel = vel;
    }

    public double getThrottleAmt() {
        return throttleAmt;
    }

    public void setThrottleAmt(double throttleAmt) {
        this.throttleAmt = throttleAmt;
    }
}
