package net.stormdev.uPlanes.api;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

/**
 * uPlaneManager is a section of the API for managing, creating
 * and removing planes
 * 
 */

public class uPlaneManager {
	protected uPlaneManager(){
		//Only classes in package can generate
	}
	
	/**
	 * Generates a random plane
	 * 
	 * @return Returns the generated plane
	 */
	public Plane generateRandomPlane(){
		Plane plane = PlaneGenerator.gen();
		
		main.plugin.planeManager.setPlane(plane.id, plane);
		
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
		plane.id = UUID.randomUUID();
		plane.health = health;
		plane.mutliplier = speed;
		plane.name = name;
		
		if(hover){
			plane.stats.put("plane.hover", new Stat("Hover", "Yes", main.plugin, true));
		}
		
		main.plugin.planeManager.setPlane(plane.id, plane);
		
		return plane;
	}
	
	/**
	 * Makes sure the plane is saved, and therefore usable.
	 * If you are fully controlling planes, make sure to remove them from
	 * being saved later to stop resource loss.
	 * 
	 * @param plane The plane to save in the plugin's memory of valid planes
	 */
	public void saveOrUpdatePlane(Plane plane){
		main.plugin.planeManager.setPlane(plane.id, plane);
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
		main.plugin.planeManager.removePlane(plane.id);
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
		main.plugin.planeManager.removePlane(planeId);
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
		Plane item = main.plugin.planeManager.getPlane(stack);
		
		return item;
	}
	
	/**
	 * Check if an entity is a plane, by the id
	 * 
	 * @param entityId The id of the entity to check
	 * @return True if a plane
	 */
	public boolean isAPlane(UUID entityId){
		return main.plugin.planeManager.isAPlane(entityId);
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
	 */
	public boolean isPlanePlaced(Plane plane){
		return plane.isPlaced;
	}
	
	/**
	 * Place a plane at the given location and handle it all correctly
	 * 
	 * @param plane The plane to place
	 * @param loc The location to place it at
	 * @return Returns the placed entity
	 */
	public Minecart placePlane(Plane plane, Location loc){
		Minecart ent = (Minecart) loc.getWorld().spawnEntity(loc, EntityType.MINECART);
		ent.setMetadata("ucars.ignore", new StatValue(true, main.plugin));
		ent.setMetadata("plane.health", new StatValue(plane.health, main.plugin));
		if(plane.stats.containsKey("plane.hover")){
			ent.setMetadata("plane.hover", new StatValue(true, main.plugin));
		}
		
		plane.isPlaced = true;
		plane.id = ent.getUniqueId();
		
		main.plugin.planeManager.setPlane(plane.id, plane);
		return ent;
	}
	
	/**
	 * Place a plane at the given location and handle it all correctly
	 * 
	 * @param plane The plane to place
	 * @param loc The location to place it at
	 * @param planeStack The itemstack associated with the plane
	 * @return Returns the placed entity
	 */
	public Minecart placePlane(Plane plane, Location loc, ItemStack planeStack){
		planeStack.setAmount(planeStack.getAmount()-1);
		if(planeStack.getAmount() <= 0){
			planeStack.setType(Material.AIR);
		}
		
		Minecart ent = (Minecart) loc.getWorld().spawnEntity(loc, EntityType.MINECART);
		ent.setMetadata("ucars.ignore", new StatValue(true, main.plugin));
		ent.setMetadata("plane.health", new StatValue(plane.health, main.plugin));
		if(plane.stats.containsKey("plane.hover")){
			ent.setMetadata("plane.hover", new StatValue(true, main.plugin));
		}
		
		plane.isPlaced = true;
		plane.id = ent.getUniqueId();
		
		main.plugin.planeManager.setPlane(plane.id, plane);
		return ent;
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
		plane.isPlaced = false;
		main.plugin.planeManager.setPlane(id, plane);
		
		Entity top = vehicle.getPassenger();
		if(top instanceof Player){
			top.eject();
		}
		vehicle.eject();
		vehicle.remove();
		
		ItemStack i = new ItemStack(PlaneItemMethods.getItem(plane));
		
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
	public void damagePlane(Minecart m, Plane plane, double damage, Player damager, boolean breakIt){
		//Plane being punched to death
		double health = plane.health;
		if(m.hasMetadata("plane.health")){
			List<MetadataValue> ms = m.getMetadata("plane.health");
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
	 */
	public void damagePlane(Minecart m, Plane plane, double damage, Player damager){
		damagePlane(m, plane, damage, damager, true);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 */
	public void damagePlane(Minecart m, Plane plane, double damage){
		damagePlane(m, plane, damage, true);
	}
	
	/**
	 * Damage the given plane
	 * 
	 * @param m The plane entity
	 * @param plane The plane
	 * @param damage The amount to damage it by
	 * @param breakIt Whether or not to break the car if necessary
	 */
	public void damagePlane(Minecart m, Plane plane, double damage, boolean breakIt){
		double health = plane.health;
		if(m.hasMetadata("plane.health")){
			List<MetadataValue> ms = m.getMetadata("plane.health");
			health = (Double) ms.get(0).value();
		}
		String msg = Lang.get("general.damage.msg");
		Boolean die = false;
		msg = msg.replaceAll(Pattern.quote("%damage%"), damage+"HP");
		health -= damage;
		if(health <= 0){
			die = true;
			health = 0;
		}
		msg = msg.replaceAll(Pattern.quote("%remainder%"), health+"HP");
		msg = msg.replaceAll(Pattern.quote("%cause%"), "Damage");
		
		if(m.getPassenger() != null && m.getPassenger() instanceof Player){
			((Player)m.getPassenger()).sendMessage(main.colors.getInfo()+msg);
		}
		
		m.removeMetadata("plane.health", main.plugin);
		m.setMetadata("plane.health", new StatValue(health, main.plugin)); //Update the health on the vehicle
		
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
}
