package net.stormdev.uPlanes.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.uPlanes.api.Plane;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlanesManager {
	private ConcurrentHashMap<UUID, Plane> planes = new ConcurrentHashMap<UUID, Plane>();
	public ConcurrentHashMap<UUID, Plane> cache = new ConcurrentHashMap<UUID, Plane>();
	File saveFile = null;
	public PlanesManager(File saveFile){
		this.saveFile = saveFile;
		load();
	}
	public Boolean isAPlane(UUID PlaneId){
		if(cache.containsKey(PlaneId)){
			return true;
		}
		Boolean b = planes.containsKey(PlaneId);
		if(b){
			cache.put(PlaneId, planes.get(PlaneId));
			cacheSize();
		}
		return b;
	}
	public Plane getPlane(ItemStack item){
		Material mat = item.getType();
		ItemMeta im = item.getItemMeta();
		UUID id = null;
		
		if(mat != Material.MINECART){
			return null;
		}
		
		if(im == null
				|| im.getLore() == null
				|| im.getLore().size() < 1){
			return null;
		}
		
		String rawId = im.getLore().get(0);
		id = UUID.fromString(ChatColor.stripColor(rawId));
		
		return getPlane(id);
	}
	public Boolean isAPlane(ItemStack item){
		return getPlane(item) != null;
	}
	public Plane getPlane(UUID PlaneId){
		Plane c = cache.get(PlaneId);
		if(c != null){
			return c;
		}
		return planes.get(PlaneId);
	}
	public void setPlane(UUID PlaneId, Plane Plane){
		if(!Plane.isPlaced){
			cache.remove(PlaneId);
		}
		else{
			cache.put(PlaneId, Plane);
		}
		planes.put(PlaneId, Plane);
		cacheSize();
		asyncSave();
	}
	public void removePlane(UUID PlaneId){
		cache.remove(PlaneId);
		planes.remove(PlaneId);
		asyncSave();
	}
	public void updatePlane(UUID old, Plane current){
		removePlane(old);
		setPlane(current.id, current);
		asyncSave();
	}
	public void cacheSize(){
		while(cache.size() > 20){ //Maximum Plane cache
			cache.remove(cache.keySet().toArray()[0]); //Clear it back to size
		}
		return;
	}
	public void removeFromCache(UUID PlaneId){
		cache.remove(PlaneId);
	}
	public void noLongerPlaced(UUID PlaneId){
		removeFromCache(PlaneId);
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
