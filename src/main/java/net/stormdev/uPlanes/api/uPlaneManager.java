package net.stormdev.uPlanes.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.hover.HoverCart;
import net.stormdev.uPlanes.hover.HoverCartEntity;
import net.stormdev.uPlanes.items.ItemPlaneValidation;
import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.presets.PlanePreset;
import net.stormdev.uPlanes.utils.CartOrientationUtil;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.PEntityMeta;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

/**
 * uPlaneManager is a section of the API for managing, creating
 * and removing planes
 * 
 */

public class uPlaneManager {
	protected uPlaneManager(){
		//Only classes in package can generate
	}
	
	private volatile List<AccelerationModifier> accelMods = new ArrayList<AccelerationModifier>();
	private volatile List<AccelerationModifier> decelMods = new ArrayList<AccelerationModifier>();
	private volatile List<PlaneTurningModifier> rotMods = new ArrayList<PlaneTurningModifier>();
	
	/**
	 * Sets a turning modifier which can change how fast planes turn
	 * @param mod The mod to add
	 */
	public void addTurningModifier(PlaneTurningModifier mod){
		rotMods.add(mod);
	}
	
	/**
	 * Removes a registered turning modifer
	 * @param mod The mod to remove
	 */
	public void removeTurningModifier(PlaneTurningModifier mod){
		rotMods.remove(mod);
	}
	
	public double getAlteredRotationAmountPerTick(Player player, Vehicle cart, Plane plane){
		double current = plane.getTurnAmountPerTick();
		for(PlaneTurningModifier am:new ArrayList<PlaneTurningModifier>(rotMods)){
			current *= am.getTurnAmountPerTick(cart, current);
		}
		return current;
	}
	
	/**
	 * Sets an acceleration modifer which calculates player's different accelerations
	 * @param mod The mod to add
	 */
	public void addAccelerationModifier(AccelerationModifier mod){
		accelMods.add(mod);
	}
	
	/**
	 * Removes a registered acceleration modifer
	 * @param mod The mod to remove
	 */
	public void removeAccelerationModifier(AccelerationModifier mod){
		accelMods.remove(mod);
	}
	
	/**
	 * Sets a deceleration modifer which calculates player's different accelerations
	 * @param mod The mod to add
	 */
	public void addDecelerationModifier(AccelerationModifier mod){
		decelMods.add(mod);
	}
	
	/**
	 * Removes a registered deceleration modifer
	 * @param mod The mod to remove
	 */
	public void removeDecelerationModifier(AccelerationModifier mod){
		decelMods.remove(mod);
	}

	public double getAlteredDecelerationMod(Player player, Vehicle cart, Plane plane){
		double current = 1.0d;
		for(AccelerationModifier am:new ArrayList<AccelerationModifier>(decelMods)){
			current *= am.getAccelerationMultiplier(player, cart, plane);
		}
		return current;
	}
	
	public double getAlteredAccelerationMod(Player player, Vehicle cart, Plane plane){
		double current = 1.0d;
		for(AccelerationModifier am:new ArrayList<AccelerationModifier>(accelMods)){
			try {
				current *= am.getAccelerationMultiplier(player, cart, plane);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return current;
	}
	
	/**
	 * Generates a random plane
	 * 
	 * @return Returns the generated plane
	 */
	public Plane generateRandomPlane(){
		Plane plane = PlaneGenerator.gen();
		
		return plane;
	}
	
	/**
	 * Generates a random plane
	 * 
	 * @return Returns the generated plane
	 */
	public Plane generateRandomPlane(boolean hover){
		Plane plane = PlaneGenerator.gen();
		
		if(hover){
			plane.setName("Hover Plane");
			plane.setHover(true);
		}
		
		return plane;
	}
	
	/**
	 * Returns a plane that meets the desired specification
	 *  
	 * @param health The plane's health
	 * @param speed The plane's speed
	 * @param name The name of the plane
	 * @param hover If the plane is a hover plane or not
	 * @return Returns the generated plane
	 */
	public Plane generatePlane(double health, double speed, String name, boolean hover){
		Plane plane = PlaneGenerator.gen();
		plane.setId(UUID.randomUUID());
		plane.setHealth(health);
		plane.setSpeed(speed);
		plane.setName(name);
		
		if(hover){
			plane.setHover(true);
		}
		
		return plane;
	}
	
	/**
	 * Makes sure the plane is saved, and therefore usable.
	 * If you are fully controlling planes, make sure to remove them from
	 * being saved later to stop resource loss.
	 * 
	 * @param plane The plane to save in the plugin's memory of valid planes
	 * 
	 * @deprecated Planes are no longer stored to file, unless in use
	 */
	@Deprecated //No longer needed
	public void saveOrUpdatePlane(Plane plane){
		main.plugin.planeManager.updateUsedPlane(plane.getId(), plane);
	}
	
	/**
	 * Very important method to remove planes from the plugin's list
	 * of valid planes. Failure to call this method at the end of the
	 * Plane's life will result in it permanently taking up space
	 * in the plane-save-file.
	 * 
	 * @param plane The plane to remove
	 */
	public void removePlane(Plane plane){
		main.plugin.planeManager.noLongerPlaced(plane.getId());
	}
	
	/**
	 * Very important method to remove planes from the plugin's list
	 * of valid planes. Failure to call this method at the end of the
	 * Plane's life will result in it permanently taking up space
	 * in the plane-save-file.
	 * 
	 * @param planeId The id of the plane to remove
	 */
	public void removePlane(UUID planeId){
		main.plugin.planeManager.noLongerPlaced(planeId);
	}
	
	/**
	 * Will grab a plane of the requested id, if exists.
	 * 
	 * @param planeId The id of the plane to search for 
	 * @return Returns the plane, or null if it doesnt exist
	 */
	public Plane getPlaneById(UUID planeId){
		return main.plugin.planeManager.getPlane(planeId);
	}
	
	/**
	 * Gets the item stack for a plane
	 * 
	 * @param plane The plane to get the item for
	 * @return The item used to place the plane (Can change at any time)
	 */
	public ItemStack getPlaneItem(Plane plane){
		return PlaneItemMethods.getItem(plane);
	}
	
	/**
	 * Get the plane associated with a valid plane item.
	 * 
	 * @param stack The item to get the plane from 
	 * @return The plane associated with the item, or null if
	 * the item is not a plane
	 */
	public Plane getPlaneFromItem(ItemStack stack){
		Plane item = ItemPlaneValidation.getPlane(stack);
		
		return item;
	}
	
	/**
	 * Check if an entity is a plane, by the id
	 * 
	 * @param entityId The id of the entity to check
	 * @return True if a plane
	 */
	public boolean isAPlane(UUID entityId){
		return main.plugin.planeManager.isPlaneInUse(entityId);
	}
	
	/**
	 * Check if an entity is a plane
	 * 
	 * @param entity The entity to check
	 * @return True if a plane
	 */
	public boolean isAPlane(Entity entity){
		return isAPlane(entity.getUniqueId());
	}
	
	/**
	 * Check if a plane has been placed, or if it's an item still
	 *  
	 * @param plane The plane to check
	 * @return True if the plane is placed (An entity),
	 * False if not (An itemstack)
	 * @deprecated No longer necessary or possible, always returns true now
	 */
	@Deprecated
	public boolean isPlanePlaced(Plane plane){
		return true;
	}
	
	/**
	 * Place a plane at the given location and handle it all correctly
	 * 
	 * @param plane The plane to place
	 * @param loc The location to place it at
	 * @param directionToFace The direction for the plane to rotate to face
	 * @return Returns the placed entity
	 */
	public Vehicle placePlane(Plane plane, Location loc, Vector directionToFace){
		synchronized (uPlaneManager.class){
			PlanePreset pp = plane.getPreset();
			Vehicle ent;
			
			MaterialData display = null;
			double offset = 0;
			if(plane.getCartDisplayBlock() != null){
				display = plane.getCartDisplayBlock();
				offset = plane.getDisplayOffset();
			}
			else if(pp != null && pp.hasDisplayBlock()){
				display = pp.getDisplayBlock();
				offset = pp.getDisplayOffset();
			}
			
			if(display == null){
				ent = (Vehicle) loc.getWorld().spawnEntity(loc, EntityType.MINECART);
			}
			else {
				HoverCartEntity hce = new HoverCartEntity(loc.clone().add(0, 0.3, 0));
				if(pp != null){
					hce.setHitBoxX(pp.getHitBoxX());
					hce.setHitBoxZ(pp.getHitBoxZ());
				}
				HoverCart hc = hce.spawn();
				ent = hc;
				hc.setDisplay(new ItemStack(display.getItemType(), 1, display.getData()), offset);
			}
			
			PEntityMeta.setMetadata(ent, "ucars.ignore", new StatValue(true, main.plugin));
			PEntityMeta.setMetadata(ent, "plane.health", new StatValue(plane.getHealth(), main.plugin));
			if(plane.isHover()){
				PEntityMeta.setMetadata(ent, "plane.hover", new StatValue(true, main.plugin));
				plane.setHover(true);
			}
			PEntityMeta.setMetadata(ent, "plane.direction", new StatValue(directionToFace.clone(), main.plugin));
			plane.setId(ent.getUniqueId());
			plane.setRoll(0);
			float vYaw = (float) Math.toDegrees(Math.atan2(directionToFace.getX() , -directionToFace.getZ()));
			vYaw -= 90;
			CartOrientationUtil.setYaw(ent, vYaw);
			
			main.plugin.planeManager.nowPlaced(plane);
			return ent;
		}
	}
	
	/**
	 * Place a plane at the given location and handle it all correctly
	 * 
	 * @param plane The plane to place
	 * @param loc The location to place it at
	 * @return Returns the placed entity
	 */
	public Vehicle placePlane(Plane plane, Location loc){
		return placePlane(plane, loc, loc.getDirection());
	}
	
	/**
	 * Place a plane at the given location and handle it all correctly
	 * 
	 * @param plane The plane to place
	 * @param loc The location to place it at
	 * @param planeStack The itemstack associated with the plane
	 * @return Returns the placed entity
	 */
	public Vehicle placePlane(Plane plane, Location loc, ItemStack planeStack){
		planeStack.setAmount(planeStack.getAmount()-1);
		if(planeStack.getAmount() <= 0){
			planeStack.setType(Material.AIR);
		}
		
		return placePlane(plane, loc);
	}
	
	/**
	 * Destroy the given plane safely and correctly
	 * 
	 * @param vehicle The plane's vehicle entity to remove
	 * @param plane The plane associated with the entity
	 * @return Returns the plane's associated itemstack
	 */
	public ItemStack destroyPlane(Vehicle vehicle, Plane plane){
		UUID id = vehicle.getUniqueId();
		main.plugin.planeManager.noLongerPlaced(id);
		
		Entity top = vehicle.getPassenger();
		if(top instanceof Player){
			top.eject();
			top.setVelocity(vehicle.getVelocity());
		}
		vehicle.eject();
		vehicle.remove();
		
		ItemStack i = plane.toItemStack();
		
		return i;
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param damager The damager
	 * @param breakIt Whether or not to break the car if necessary
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, Player damager, boolean breakIt){
		if(m.hasMetadata("invincible") || PEntityMeta.hasMetadata(m, "invincible")){
			return;
		}
		//Plane being punched to death
		double health = plane.getHealth();
		if(PEntityMeta.hasMetadata(m, "plane.health")){
			List<MetadataValue> ms = PEntityMeta.getMetadata(m, "plane.health");
			health = (Double) ms.get(0).value();
		}
		String msg = Lang.get("general.damage.msg");
		msg = msg.replaceAll(Pattern.quote("%damage%"), damage+"HP");
		health -= damage;
		if(health <= 0){
			health = 0;
		}
		msg = msg.replaceAll(Pattern.quote("%remainder%"), health+"HP");
		msg = msg.replaceAll(Pattern.quote("%cause%"), "Fist");
		((Player)damager).sendMessage(main.colors.getInfo()+msg);
		
		damagePlane(m, plane, damage, breakIt);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param damager The damager
	 * @param cause The cause of the damage
	 * @param breakIt Whether or not to break the car if necessary
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, Player damager, String cause, boolean breakIt){
		if(m.hasMetadata("invincible") || PEntityMeta.hasMetadata(m, "invincible")){
			return;
		}
		//Plane being punched to death
		double health = plane.getHealth();
		if(PEntityMeta.hasMetadata(m, "plane.health")){
			List<MetadataValue> ms = PEntityMeta.getMetadata(m, "plane.health");
			health = (Double) ms.get(0).value();
		}
		String msg = Lang.get("general.damage.msg");
		msg = msg.replaceAll(Pattern.quote("%damage%"), (damage)+"HP");
		health -= damage;
		if(health <= 0){
			health = 0;
		}
		msg = msg.replaceAll(Pattern.quote("%remainder%"), (health)+"HP");
		msg = msg.replaceAll(Pattern.quote("%cause%"), cause);
		((Player)damager).sendMessage(main.colors.getInfo()+msg);
		
		damagePlane(m, plane, damage, breakIt);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param damager The damager
	 * @param cause The cause of the damage
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, Player damager, String cause){
		damagePlane(m, plane, damage, damager, cause, true);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param damager The damager
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, Player damager){
		damagePlane(m, plane, damage, damager, true);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param cause The cause of the damage
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, String cause){
		damagePlane(m, plane, damage, cause, true);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage){
		damagePlane(m, plane, damage, true);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param breakIt Whether or not to break the car if necessary
	 * @param cause The cause of the damage
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, String cause, boolean breakIt){
		if(m.hasMetadata("invincible") || PEntityMeta.hasMetadata(m, "invincible")){
			return;
		}
		double health = plane.getHealth();
		if(PEntityMeta.hasMetadata(m, "plane.health")){
			List<MetadataValue> ms = PEntityMeta.getMetadata(m, "plane.health");
			health = (Double) ms.get(0).value();
		}
		String msg = Lang.get("general.damage.msg");
		Boolean die = false;
		msg = msg.replaceAll(Pattern.quote("%damage%"), (damage)+"HP");
		health -= damage;
		if(health <= 0){
			die = true;
			health = 0;
		}
		health = Math.round(health*10.0d)/10.0d;
		msg = msg.replaceAll(Pattern.quote("%remainder%"), (health)+"HP");
		msg = msg.replaceAll(Pattern.quote("%cause%"), cause);
		
		if(m.getPassenger() != null && m.getPassenger() instanceof Player){
			((Player)m.getPassenger()).sendMessage(main.colors.getInfo()+msg);
		}
		
		PEntityMeta.removeMetadata(m, "plane.health");
		PEntityMeta.setMetadata(m, "plane.health", new StatValue(health, main.plugin));
		
		if(die || health < 0.1 && breakIt){
			//Kill the plane
			PlaneDeathEvent evt = new PlaneDeathEvent(m, plane);
			main.plugin.getServer().getPluginManager().callEvent(evt);
			if(!evt.isCancelled()){
				//Kill the plane
				main.plugin.listener.killPlane(m, plane);
			}
		}
		return;
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 */
	public void healPlane(Vehicle m, Plane plane){
		double health = plane.getHealth();
		
		PEntityMeta.removeMetadata(m, "plane.health");
		PEntityMeta.setMetadata(m, "plane.health", new StatValue(health, main.plugin));
		return;
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param breakIt Whether or not to break the car if necessary
	 */
	public void damagePlane(Vehicle m, Plane plane, double damage, boolean breakIt){
		damagePlane(m, plane, damage, "Damage", breakIt);
	}
}
