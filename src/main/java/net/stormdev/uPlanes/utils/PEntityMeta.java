package net.stormdev.uPlanes.utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.uPlanes.main.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

public class PEntityMeta {
	
	private static volatile Map<UUID, Object> entityMetaObjs = new ConcurrentHashMap<UUID, Object>(100, 0.75f, 2);
	private static volatile Map<UUID, WeakReference<Entity>> entityObjs = new ConcurrentHashMap<UUID, WeakReference<Entity>>(100, 0.75f, 2);
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
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				for(UUID entID:new ArrayList<UUID>(entityObjs.keySet())){
					WeakReference<Entity> val = entityObjs.get(entID);
					if(val == null){
						continue;
					}
					if(val.get() == null){
						entityObjs.remove(entID);
					}
				}
				return;
			}});
		/*Bukkit.getScheduler().runTask(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				final List<Entity> allEntities = new ArrayList<Entity>();
				for(World w:Bukkit.getWorlds()){
					allEntities.addAll(w.getEntities());
				}
				Bukkit.getScheduler().runTaskAsynchronously(ucars.plugin, new Runnable(){

					@Override
					public void run() {
						for(final Entity e:new ArrayList<Entity>(entityObjs.values())){
							if(e.isDead() && !e.isValid()){
								synchronized(entityMetaObjs){
									Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

										@Override
										public void run() {
											entityObjs.remove(e.getUniqueId());
											entityMetaObjs.remove(e.getUniqueId());
											return;
										}}, 100l);
								}
							}
						}
						mainLoop: for(final UUID entID:new ArrayList<UUID>(entityMetaObjs.keySet())){
							for(Entity e:allEntities){
								if(e.getUniqueId().equals(entID)){
									continue mainLoop;
								}
							}
							Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

								@Override
								public void run() {
									Object o = entityMetaObjs.get(entID);
									entityMetaObjs.remove(entID);
									if(o != null){
										UMeta.removeAllMeta(o);
									}
									return;
								}}, 100l);
						}
					}});
				return;
			}});*/
	}
	
	private static void setEntityObj(Entity e){
		synchronized(entityObjs){
			entityObjs.put(e.getUniqueId(), new WeakReference<Entity>(e));
		}
	}
	
	private static void delEntityObj(Entity e){
		synchronized(entityObjs){
			entityObjs.remove(e.getUniqueId());
		}
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
		delEntityObj(e);
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
				setEntityObj(e);
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
		setEntityObj(entity);
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
