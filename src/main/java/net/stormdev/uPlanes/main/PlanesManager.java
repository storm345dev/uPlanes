package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.items.ItemPlaneValidation;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlanesManager {
	private ConcurrentHashMap<UUID, Plane> planes = new ConcurrentHashMap<UUID, Plane>();
	public ConcurrentHashMap<UUID, Plane> cache = new ConcurrentHashMap<UUID, Plane>();
	File saveFile = null;
	public PlanesManager(File saveFile){
		this.saveFile = saveFile;
		load();
	}
	public boolean isPlaneInUse(UUID PlaneId){
		if(cache.containsKey(PlaneId)){
			return true;
		}
		boolean b = planes.containsKey(PlaneId);
		if(b){
			cache.put(PlaneId, planes.get(PlaneId));
			cacheSize();
		}
		return b;
	}
	public boolean isPlaneInUse(Plane plane){
		UUID PlaneId = plane.getId();
		if(cache.containsKey(PlaneId)){
			return true;
		}
		boolean b = planes.containsKey(PlaneId);
		if(b){
			cache.put(PlaneId, planes.get(PlaneId));
			cacheSize();
		}
		return b;
	}
	public Plane getPlane(ItemStack item){
		return ItemPlaneValidation.getPlane(item);
	}
	public boolean isAPlane(ItemStack item){
		return getPlane(item) != null;
	}
	public Plane getPlane(UUID PlaneId){
		Plane c = cache.get(PlaneId);
		if(c != null){
			return c;
		}
		return planes.get(PlaneId);
	}
	/*
	@Deprecated
	public void setPlane(UUID PlaneId, Plane Plane){
		cache.put(PlaneId, Plane);
		planes.put(PlaneId, Plane);
		cacheSize();
		asyncSave();
	}
	@Deprecated
	public void removePlane(UUID PlaneId){
		cache.remove(PlaneId);
		planes.remove(PlaneId);
		asyncSave();
	}
	@Deprecated
	public void updatePlane(UUID old, Plane current){
		removePlane(old);
		setPlane(current.getId(), current);
		asyncSave();
	}
	*/
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
		planes.remove(PlaneId);
		asyncSave();
	}
	public void nowPlaced(Plane Plane){
		UUID PlaneId = Plane.getId();
		cache.put(PlaneId, Plane);
		planes.put(PlaneId, Plane);
		cacheSize();
		asyncSave();
	}
	public void updateUsedPlane(UUID old, Plane current){
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
				this.planes = loadHashMap(this.saveFile.getAbsolutePath());
			} catch (Exception e) {
				//Old format
				this.planes = null;
			}
		}
		if(this.planes == null){
			this.planes = new ConcurrentHashMap<UUID, Plane>();
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
		saveHashMap(planes, this.saveFile.getAbsolutePath());
	}
	public static void saveHashMap(ConcurrentHashMap<UUID, Plane> map, String path)
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
	public static ConcurrentHashMap<UUID, Plane> loadHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			try {
				return (ConcurrentHashMap<UUID, Plane>) result;
			} catch (Exception e) {
				return new ConcurrentHashMap<UUID, Plane>();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
