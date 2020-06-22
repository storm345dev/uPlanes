package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.items.ItemBoatValidation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoatsManager {
	private ConcurrentHashMap<UUID, Boat> boats = new ConcurrentHashMap<UUID, Boat>();
	public ConcurrentHashMap<UUID, Boat> cache = new ConcurrentHashMap<UUID, Boat>();
	File saveFile = null;
	public BoatsManager(File saveFile){
		this.saveFile = saveFile;
		load();
		Bukkit.getScheduler().runTaskTimer(main.plugin, new Runnable(){

			@Override
			public void run() {
				//Remove removed entities from inUse
				final List<Entity> entities = new ArrayList<Entity>();
				for(World w:Bukkit.getWorlds()){
					entities.addAll(w.getEntities());
				}
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						mainLoop: for(UUID id:new ArrayList<UUID>(boats.keySet())){
							for(Entity e:entities){
								if(e.getUniqueId().equals(id)){
									continue mainLoop;
								}
							}
							//No entity matched it!
							boats.remove(id);
							cache.remove(id);
						}
						return;
					}});
				return;
			}}, 260*20l, 260*20l);
	}
	public Boolean isBoatInUse(UUID PlaneId){
		if(cache.containsKey(PlaneId)){
			return true;
		}
		Boolean b = boats.containsKey(PlaneId);
		if(b){
			cache.put(PlaneId, boats.get(PlaneId));
			cacheSize();
		}
		return b;
	}
	public Boolean isBoatInUse(Boat plane){
		UUID PlaneId = plane.getId();
		if(cache.containsKey(PlaneId)){
			return true;
		}
		Boolean b = boats.containsKey(PlaneId);
		if(b){
			cache.put(PlaneId, boats.get(PlaneId));
			cacheSize();
		}
		return b;
	}
	public Boat getBoat(ItemStack item){
		return ItemBoatValidation.getBoat(item);
	}
	public Boolean isABoat(ItemStack item){
		return getBoat(item) != null;
	}
	public Boat getBoat(UUID PlaneId){
		Boat c = cache.get(PlaneId);
		if(c != null){
			return c;
		}
		return boats.get(PlaneId);
	}
	public void cacheSize(){
		while(cache.size() > main.plugin.cacheSize){ //Maximum Plane cache
			cache.remove(cache.keySet().toArray()[0]); //Clear it back to size
		}
		return;
	}
	public void removeFromCache(UUID PlaneId){
		cache.remove(PlaneId);
	}
	public void noLongerPlaced(UUID PlaneId){
		cache.remove(PlaneId);
		boats.remove(PlaneId);
		asyncSave();
	}
	public void nowPlaced(Boat Plane){
		UUID PlaneId = Plane.getId();
		cache.put(PlaneId, Plane);
		boats.put(PlaneId, Plane);
		cacheSize();
		asyncSave();
	}
	public void updateUsedBoat(UUID old, Boat current){
		noLongerPlaced(old);
		nowPlaced(current);
		asyncSave();
	}
	public void asyncSave(){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			public void run() {
				save();
				return;
			}});
	}
	public void load(){
		this.saveFile.getParentFile().mkdirs();
		if(!this.saveFile.exists() || this.saveFile.length() < 1){
			try {
				this.saveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		else{
			try {
				this.boats = loadHashMap(this.saveFile.getAbsolutePath());
			} catch (Exception e) {
				//Old format
				this.boats = null;
			}
		}
		if(this.boats == null){
			this.boats = new ConcurrentHashMap<UUID, Boat>();
			save(); //Make sure it overrides old format	
		}
	}
	private void save(){
		this.saveFile.getParentFile().mkdirs();
		if(!this.saveFile.exists() || this.saveFile.length() < 1){
			try {
				this.saveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		saveHashMap(boats, this.saveFile.getAbsolutePath());
	}
	public static void saveHashMap(ConcurrentHashMap<UUID, Boat> map, String path)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			//Handle I/O exceptions
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public static ConcurrentHashMap<UUID, Boat> loadHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			try {
				return (ConcurrentHashMap<UUID, Boat>) result;
			} catch (Exception e) {
				return new ConcurrentHashMap<UUID, Boat>();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
