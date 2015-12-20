package net.stormdev.uPlanes.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

import com.useful.ucars.ucars;

public class PEntityMeta {
	
	private static Map<UUID, Object> entityMetaObjs = new ConcurrentHashMap<UUID, Object>(100, 0.75f, 2);
	public static boolean USING_UCARS = false;
	
	public static void cleanEntityObjs(){
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("cleanEntityObjs");
			method.invoke(null);
			return;
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		Bukkit.getScheduler().runTask(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				final List<Entity> allEntities = new ArrayList<Entity>();
				for(World w:Bukkit.getWorlds()){
					allEntities.addAll(w.getEntities());
				}
				Bukkit.getScheduler().runTaskAsynchronously(ucars.plugin, new Runnable(){

					@Override
					public void run() {
						mainLoop: for(UUID entID:new ArrayList<UUID>(entityMetaObjs.keySet())){
							for(Entity e:allEntities){
								if(e.getUniqueId().equals(entID)){
									continue mainLoop;
								}
							}
							Object o = entityMetaObjs.get(entID);
							entityMetaObjs.remove(entID);
							if(o != null){
								PMeta.removeAllMeta(o);
							}
						}
					}});
				return;
			}});
	}
	
	public static void removeAllMeta(Entity e){
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("removeAllMeta", Entity.class);
			method.invoke(null, e);
			return;
		} catch (Exception e1) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e1.printStackTrace();
			}
		}
		Object o = entityMetaObjs.get(e.getUniqueId());
		entityMetaObjs.remove(e.getUniqueId());
		if(o != null){
			PMeta.removeAllMeta(o);
		}
	}
	
	private static Object getMetaObj(Entity e){
		if(e == null){
			return null;
		}
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("getMetaObj", Entity.class);
			return method.invoke(null, e);
		} catch (Exception e1) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e1.printStackTrace();
			}
		}
		synchronized(entityMetaObjs){
			Object obj = entityMetaObjs.get(e.getUniqueId());
			if(obj == null){
				obj = new Object();
				entityMetaObjs.put(e.getUniqueId(), obj);
			}
			return obj;
		}
	}
	
	public static void setMetadata(Entity entity, String metaKey, MetadataValue value){
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("setMetadata", Entity.class, String.class, MetadataValue.class);
			method.invoke(null, entity, metaKey, value);
			return;
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		PMeta.getMeta(getMetaObj(entity), metaKey).add(value);
	}
	
	public static List<MetadataValue> getMetadata(Entity entity, String metaKey){
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("getMetadata", Entity.class, String.class);
			return (List<MetadataValue>) method.invoke(null, entity, metaKey);
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		return PMeta.getAllMeta(getMetaObj(entity)).get(metaKey);
	}
	
	public static boolean hasMetadata(Entity entity, String metaKey){
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("hasMetadata", Entity.class, String.class);
			return (Boolean) method.invoke(null, entity, metaKey);
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		return PMeta.getAllMeta(getMetaObj(entity)).containsKey(metaKey);
	}
	
	public static void removeMetadata(Entity entity, String metaKey){
		try {
			Class<?> uEntityMetaClass = Class.forName("com.useful.ucars.util.UEntityMeta");
			USING_UCARS = true;
			Method method = uEntityMetaClass.getDeclaredMethod("removeMetadata", Entity.class, String.class);
			method.invoke(null, entity, metaKey);
			return;
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		PMeta.removeMeta(getMetaObj(entity), metaKey);
	}
}
