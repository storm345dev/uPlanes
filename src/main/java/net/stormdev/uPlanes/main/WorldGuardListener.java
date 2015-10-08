package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.uPlanesAPI;
import net.stormdev.uPlanes.hover.HoverCart;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;

public class WorldGuardListener implements Listener {
	public WorldGuardListener(){
		main.logger.info("WorldGuard handling enabled!");
		Bukkit.getPluginManager().registerEvents(this, main.plugin);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	void entityDamage(DamageEntityEvent event){
		if(uPlanesAPI.getPlaneManager().isAPlane(event.getEntity()) && event.getEntity().getType().equals(EntityType.ARMOR_STAND)){
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void entityDamageMonitor(DamageEntityEvent event){
		if(uPlanesAPI.getPlaneManager().isAPlane(event.getEntity()) && event.getEntity().getType().equals(EntityType.ARMOR_STAND)){
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	void entityDestroy(DestroyEntityEvent event){
		if(uPlanesAPI.getPlaneManager().isAPlane(event.getEntity()) && event.getEntity().getType().equals(EntityType.ARMOR_STAND)){
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void entityDestroyMonitor(DestroyEntityEvent event){
		if(uPlanesAPI.getPlaneManager().isAPlane(event.getEntity()) && event.getEntity().getType().equals(EntityType.ARMOR_STAND)){
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	void entityUse(UseEntityEvent event){
		if(uPlanesAPI.getPlaneManager().isAPlane(event.getEntity()) && event.getEntity().getType().equals(EntityType.ARMOR_STAND)){
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void entityUseMonitor(UseEntityEvent event){
		if(uPlanesAPI.getPlaneManager().isAPlane(event.getEntity()) && event.getEntity().getType().equals(EntityType.ARMOR_STAND)){
			event.setCancelled(false);
		}
	}
}
