package net.stormdev.uPlanes.hover;

import net.minecraft.server.v1_12_R1.*;
import net.stormdev.uPlanes.utils.CustomEntityHandler;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.vehicle.*;

import java.util.ArrayList;
import java.util.List;

public class HoverCartEntity extends EntityArmorStand {	 //TODO Stop it falling out of the world
	public static final double OFFSET_AMOUNT = /*-1*/0;
	
	public float getHitBoxX() {
		return hitBoxX;
	}

	public void setHitBoxX(float hitBoxX) {
		this.hitBoxX = hitBoxX;
		setSize();
	}

	public float getHitBoxZ() {
		return hitBoxZ;
	}

	public void setHitBoxZ(float hitBoxZ) {
		this.hitBoxZ = hitBoxZ;
		setSize();
	}

	private CraftHoverCart ce;
	private Location loc;
	private double heightOffset = 0;
	private float hitBoxX = -1;
	private float hitBoxZ = -1;
	private double boatOffsetDeg = 0;
	private int maxPassengers = 1;

	public double getBoatOffsetDeg() {
		return boatOffsetDeg;
	}

	public void setBoatOffsetDeg(double boatOffsetDeg) {
		this.boatOffsetDeg = boatOffsetDeg;
	}

	public int getMaxPassengers() {
		return maxPassengers;
	}

	public void setMaxPassengers(int maxPassengers) {
		this.maxPassengers = maxPassengers;
	}

	//Stuff to allow multiple passengers
	private EntityBoat fakeBoat;
	private EntityArrow fakeArrow;
	private EntityArrow fakeArrow2;
	
	public static HoverCart getCart(org.bukkit.entity.Entity e){
		if(((CraftEntity)e).getHandle() instanceof HoverCartEntity){
			HoverCartEntity hce = (HoverCartEntity) ((CraftEntity)e).getHandle();
			return hce.getHoverCartEntity();
		}
		return null;
	}
	
	public static boolean isCart(org.bukkit.entity.Entity e){
		return getCart(e) != null;
	}
	
	public HoverCartEntity(World world) {
		super(world);
		this.loc = new Location(world.getWorld(), this.locX, this.locY, this.locZ);
		setSize();
		/*setSize(0.98F, 0.7F);*/
	}
	
	public HoverCartEntity(Location loc) {
		this(((CraftWorld)loc.getWorld()).getHandle());
		this.loc = loc;
		updatePosition(loc);
		this.motX = 0;
		this.motY = 0;
		this.motZ = 0;
	}
	
	/*@Override
	public boolean s() {
		if(!super.s()){
			setSize();
		}
	    return true; //Say our size is set
	}*/
	
	private void setSize(){
		setSize(hitBoxX<0?0.98F:hitBoxX, hitBoxZ<0?0.7F:hitBoxZ);
	}
	
	public Location getTrueLocation(){
		this.loc.setX(this.locX);
		this.loc.setY(this.locY-OFFSET_AMOUNT);
		this.loc.setZ(this.locZ);
		this.loc.setYaw(this.yaw);
		this.loc.setPitch(this.pitch);
		this.loc.setWorld(world.getWorld());
		return this.loc.clone();
	}
	
	public void updatePosition(Location loc){
		this.loc = loc;
		setPositionRotation(loc.getX(), loc.getY()+OFFSET_AMOUNT, loc.getZ(), loc.getYaw(), loc.getPitch());
	}
	
	public HoverCart spawn(){
		if(ce != null){
			return ce;
		}
		/*this.locX = loc.getX();
		this.locY = loc.getY();
		this.locZ = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
		this.motX = 0;
		this.motY = 0;
		this.motZ = 0;*/
		CraftWorld w = ((CraftWorld)loc.getWorld());
		w.getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.DEFAULT);
		this.world.getServer().getPluginManager().callEvent(new VehicleCreateEvent((Vehicle)ce));
		this.bukkitEntity = getHoverCartEntity();
		updatePosition(loc);

		HoverCart hc= getHoverCartEntity();

		if(hc.getMaxPassengers() == 2){
			//Allocate entity IDs to fake entities to show only to the client
			this.fakeBoat = new EntityBoat(w.getHandle());
			this.fakeArrow = new EntityArrow(w.getHandle()) {
				@Override
				protected ItemStack j() {
					return null;
				}
			};
			this.fakeArrow2 = new EntityArrow(w.getHandle()) {
				@Override
				protected ItemStack j() {
					return null;
				}
			};
		}

		return hc;
	}

	public boolean hasFakeBoat() {
		return this.fakeBoat != null;
	}

	public EntityBoat getFakeBoat() {
		return fakeBoat;
	}

	public EntityArrow getFakeArrow() {
		return fakeArrow;
	}

	public EntityArrow getFakeArrow2() {
		return fakeArrow2;
	}

	private List<String> playersKnowAboutFakeEntities = new ArrayList<>();

	public boolean doesKnowAboutFakeEntities(Player p){
		return playersKnowAboutFakeEntities.contains(p.getName());
	}

	public void setKnowAboutFakeEntities(Player p, boolean b){
		if(b) {
			playersKnowAboutFakeEntities.add(p.getName());
		}
		else {
			playersKnowAboutFakeEntities.remove(p.getName());
		}
	}

	//Interaction with entity from EntityArmorStand
	@Override
	public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand hand)
	  {
		if(ce.getPassengers().size() < ce.getMaxPassengers()) {
			ce.addPassenger(entityhuman.getBukkitEntity());
		}
		return EnumInteractionResult.FAIL;
	  }
	
	/*@Override
	 protected void h() {
		this.datawatcher.a(18, new Integer(1));
	    this.datawatcher.a(19, new Float(0.0F));
		super.h();
	 }
	
	public int r() {
	    return this.datawatcher.getInt(18);
	}
	
	public void setDamage(float f)
	{
	   this.datawatcher.watch(19, Float.valueOf(f));
	}

	public float getDamage() {
	   return this.datawatcher.getFloat(19);
	}*/
	
	@Override
	public void collide(Entity entity){
		if(this.passengers.size() >0 && entity.equals(this.passengers.get(0))){
			return;
		}
		
		Vehicle vehicle = (Vehicle)getHoverCartEntity();
	    org.bukkit.entity.Entity hitEntity = entity == null ? null : entity.getBukkitEntity();

	    VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, hitEntity);
	    this.world.getServer().getPluginManager().callEvent(collisionEvent);

	    if (collisionEvent.isCancelled()) {
	      return;
	    }
	    super.collide(entity);
	}
	
	@Override
	public void Y(){ //Living entity base tick from EntityLiving
		double prevX = this.locX;
	    double prevY = this.locY;
	    double prevZ = this.locZ;
	    float prevYaw = this.yaw;
	    float prevPitch = this.pitch;
	    super.Y();
	    
	    org.bukkit.World bworld = this.world.getWorld();
	    Location from = new Location(bworld, prevX, prevY, prevZ, prevYaw, prevPitch);
	    Location to = new Location(bworld, this.locX, this.locY, this.locZ, this.yaw, this.pitch);
	    Vehicle vehicle = (Vehicle)getHoverCartEntity();

	    this.world.getServer().getPluginManager().callEvent(new VehicleUpdateEvent(vehicle));

	    if (!from.equals(to)) {
	      this.world.getServer().getPluginManager().callEvent(new VehicleMoveEvent(vehicle, from, to));
	    }
	    
	    setSize();
	}
	
	private DamageSource damagesource = null;
	
	@Override
	public void die(){
		Vehicle vehicle = (Vehicle)getHoverCartEntity();
	      org.bukkit.entity.Entity passenger = damagesource == null || damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity();
		VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
        this.world.getServer().getPluginManager().callEvent(destroyEvent);
		super.die();
	}
	
	@Override
	public boolean damageEntity(DamageSource damagesource, float f)
	  {
		this.damagesource = damagesource;
		Vehicle vehicle = (Vehicle)getHoverCartEntity();
	      org.bukkit.entity.Entity passenger = damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity();

	      VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, f);/*{
	      	@Override
			  public void setCancelled(boolean b){
	      		new Exception().printStackTrace();
	      		super.setCancelled(b);
			}
		  };*/
	      this.world.getServer().getPluginManager().callEvent(event);

	      if (event.isCancelled()) {
	        return false;
	      }

	    return super.damageEntity(damagesource, f);
	  }
	
	/*@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
	    if ((!this.world.isClientSide) && (!this.dead)) {
	      if (isInvulnerable(damagesource)) {
	        return false;
	      }

	      Vehicle vehicle = (Vehicle)getBukkitEntity();
	      org.bukkit.entity.Entity passenger = damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity();

	      VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, f);
	      this.world.getServer().getPluginManager().callEvent(event);

	      if (event.isCancelled()) {
	        return true;
	      }

	      f = (float)event.getDamage();

	      k(-r());
	      j(10);
	      ac();
	      setDamage(getDamage() + f * 10.0F);
	      boolean flag = ((damagesource.getEntity() instanceof EntityHuman)) && (((EntityHuman)damagesource.getEntity()).abilities.canInstantlyBuild);

	      if ((flag) || (getDamage() > 40.0F))
	      {
	        VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
	        this.world.getServer().getPluginManager().callEvent(destroyEvent);

	        if (destroyEvent.isCancelled()) {
	          setDamage(40.0F);
	          return true;
	        }

	        if (this.passenger != null) {
	          this.passenger.mount(null);
	        }

	        if ((flag) && (!hasCustomName()))
	          die();
	        else {
	          die();
	        }
	      }

	      return true;
	    }

	    return true;
	  }*/
	
	@Override
	public CraftHoverCart getBukkitEntity(){
		return getHoverCartEntity();
	}
	
	public void setYaw(float yaw){
		this.setYawPitch(yaw, this.pitch);
	}
	
	public void setPitch(float pitch){
		this.setYawPitch(this.yaw, pitch);
	}
	
	@Override
	public void setYawPitch(float yaw, float pitch){
		super.setYawPitch(yaw, pitch);
	}
	
	public CraftHoverCart getHoverCartEntity(){
		if(this.ce == null){
			this.ce = new CraftHoverCart(world.getServer(), this);/*(CraftHoverCart) CraftHoverCart.getEntity((CraftServer) world.getServer(), this)*/;
		}
		return this.ce;
	}
	
	public static void register(){
		CustomEntityHandler.registerEntity("ArmorStand", 30, EntityArmorStand.class, HoverCartEntity.class);
	}
	
	@Override
	 protected void cB() { //Bounding box collisions in EntityArmorStand
	    List<?> list = this.world.getEntities(this, getBoundingBox());

	    if ((list != null) && (!list.isEmpty()))
	      for (int i = 0; i < list.size(); i++) {
	        Entity entity = (Entity)list.get(i);

	        if (entity instanceof EntityMinecartAbstract || entity instanceof HoverCartEntity && (h(entity) <= 0.2D))
	          entity.collide(this);
	      }
	  }

	public double getHeightOffset() {
		return heightOffset;
	}

	public void setHeightOffset(double heightOffset) {
		this.heightOffset = heightOffset;
	}
}
