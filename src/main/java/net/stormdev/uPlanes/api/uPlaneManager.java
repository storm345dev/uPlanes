package net.stormdev.uPlanes.api;

import java.util.UUID;

import net.stormdev.uPlanes.main.PlaneGenerator;
import net.stormdev.uPlanes.main.PlaneItemMethods;
import net.stormdev.uPlanes.main.main;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

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
}
