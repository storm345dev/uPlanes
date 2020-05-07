package net.stormdev.uPlanes.hover;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class CraftHoverCart extends CraftArmorStand implements HoverCart {

	public CraftHoverCart(CraftServer server, HoverCartEntity entity) {
		super(server, entity);
		this.setVisible(false);
		this.setGravity(true);
	}
	
	@Override
	public Arrow shootArrow(){
		return null;
	}
	
	@Override
	public Snowball throwSnowball(){
		return null;
	}
	
	@Override
	public Egg throwEgg(){
		return null;
	}
	
	@Override
	public HoverCartEntity getHandle(){
		return (HoverCartEntity) super.getHandle();
	}
	
	@Override
	public Location getLocation(){
		return getHandle().getTrueLocation();
	}
	
	@Override
	 public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
		    if ((this.entity.passengers.size() > 0) || (this.entity.dead)) {
		      return false;
		    }

		    /*this.entity.mount(null);*/

		    if (!location.getWorld().equals(getWorld())) {
		      this.entity.teleportTo(location, cause.equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL));
		      return true;
		    }

		    getHandle().updatePosition(location);
		    return true;
		  }

	@Override
	public void setDisplay(ItemStack stack, double offset) {
		this.setHelmet(stack.clone());
		getHandle().setHeightOffset(offset);
	}

	@Override
	public double getDisplayOffset() {
		return getHandle().getHeightOffset();
	}

	@Override
	public void setYaw(float yaw) {
/*		Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0, yaw, 0);
		Vector v = loc.getDirection();*/
		double y = yaw - 90;
		while(y < 0){
			y = 360 + y;
		}
		/*
		double radians = Math.toRadians(y);
		if(radians > Math.PI){
			radians -= (Math.PI * 2);
		}
		if(radians < -Math.PI){
			radians += (Math.PI * 2);
		}*/
		
		/*setHeadPose(new EulerAngle(getHeadPose().getX(), radians, getHeadPose().getZ()));
		setBodyPose(new EulerAngle(getBodyPose().getX(), radians, getBodyPose().getZ()));*/
		getHandle().setYaw((float) y);
		/*setBodyPose(new EulerAngle(getBodyPose().getX(), 0, getBodyPose().getZ()));
		setHeadPose(new EulerAngle(getHeadPose().getX(), 0, getHeadPose().getZ()));*/
		/*Entity passenger = getPassenger();
		if(passenger instanceof Player){
			Player pl = (Player) passenger;
			WrapperPlayServerEntityLook p = new WrapperPlayServerEntityLook();
			p.setEntityID(getHandle().getId());
			p.setPitch(getHandle().pitch);
			p.setYaw((float) y);
			p.sendPacket(pl);
		}*/
	}

	@Override
	public void setPitch(float pitch) {
		double y = pitch;
		while(y < 0){
			y = 360 + y;
		}
		
		setHeadPose(new EulerAngle(Math.toRadians(y), getHeadPose().getY(), getHeadPose().getZ())); 
	}

	@Override
	public void setYawPitch(float yaw, float pitch) {
		setYaw(yaw);
		setPitch(pitch);
	}

	@Override
	public void setRoll(float roll) {
		double y = roll;
		setHeadPose(new EulerAngle(getHeadPose().getX(), getHeadPose().getY(), Math.toRadians(y))); 
	}

	@Override
	public float getHitBoxX() {
		return getHandle().getHitBoxX();
	}

	@Override
	public float getHitBoxZ() {
		return getHandle().getHitBoxZ();
	}

	@Override
	public void setHitBoxX(float x) {
		getHandle().setHitBoxX(x);
	}

	@Override
	public void setHitBoxZ(float z) {
		getHandle().setHitBoxZ(z);
	}

}
