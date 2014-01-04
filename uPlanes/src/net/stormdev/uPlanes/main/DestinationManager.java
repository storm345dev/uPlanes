package net.stormdev.uPlanes.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;

public class DestinationManager {
	private ConcurrentHashMap<String, SerializableLocation> destinations =
			new ConcurrentHashMap<String, SerializableLocation>();
	private File saveFile;
	
	public DestinationManager(File saveFile){
		this.saveFile = saveFile;
		load();
	}
	
	public Location getLocation(String destination, Server server){
		SerializableLocation sl = destinations.get(getCorrectName(destination));
		if(sl == null)
			return null;
		Location l = sl.getLocation(server);
		return l;
	}
	
	public Boolean locationExists(String destination){
		return destinations.containsKey(getCorrectName(destination));
	}
	
	public List<String> getDestinationsList(){
		return new ArrayList<String>(destinations.keySet());
	}
	
	public ConcurrentHashMap<String, SerializableLocation> getDestinations(){
		return destinations;
	}
	
	public String getCorrectName(String name){
		if(!destinations.containsKey(name)){
			for(String dest:getDestinationsList()){
				if(ChatColor.stripColor(Colors.colorise(dest)).equalsIgnoreCase(name)){
					name = dest;
				}
			}
		}
		return name;
	}
	
	public synchronized void setDestination(String name, Location loc){
		destinations.put(name, new SerializableLocation(loc));
		asyncSave();
		return;
	}
	
	public synchronized void delDestination(String name){
		if(!destinations.containsKey(name)){
			for(String dest:getDestinationsList()){
				if(ChatColor.stripColor(Colors.colorise(dest)).equalsIgnoreCase(name)){
					name = dest;
				}
			}
		}
		destinations.remove(name);
		return;
	}
	
	public void asyncSave(){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			public void run() {
				save();
				return;
			}});
		return;
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
				this.destinations = loadHashMap(this.saveFile.getAbsolutePath());
			} catch (Exception e) {
				//Old format
				this.destinations = null;
			}
		}
		if(this.destinations == null){
			this.destinations = new ConcurrentHashMap<String, SerializableLocation>();
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
		saveHashMap(destinations, this.saveFile.getAbsolutePath());
	}
	public static void saveHashMap(ConcurrentHashMap<String, SerializableLocation> map, String path)
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
	public static ConcurrentHashMap<String, SerializableLocation> loadHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			try {
				return (ConcurrentHashMap<String, SerializableLocation>) result;
			} catch (Exception e) {
				return new ConcurrentHashMap<String, SerializableLocation>();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
