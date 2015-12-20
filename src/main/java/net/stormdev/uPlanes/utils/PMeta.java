package net.stormdev.uPlanes.utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.metadata.MetadataValue;

public class PMeta {
	private static volatile HashMap<WeakKey, Map<String, List<MetadataValue>>> metadata = new HashMap<WeakKey, Map<String, List<MetadataValue>>>();
	
	public static boolean USING_UCARS = false;
	
	public static void removeAllMeta(Object key){
		try {
			Class<?> uMetaClass = Class.forName("com.useful.ucars.util.UMeta");
			USING_UCARS = true;
			Method removeAllMeta = uMetaClass.getDeclaredMethod("removeAllMeta", Object.class);
			removeAllMeta.invoke(null, key);
			return;
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		synchronized(metadata){
			WeakKey weakKey = new WeakKey(key);
			metadata.remove(weakKey);
		}
	}
	
	public static Map<String, List<MetadataValue>> getAllMeta(Object key){
		try {
			Class<?> uMetaClass = Class.forName("com.useful.ucars.util.UMeta");
			USING_UCARS = true;
			Method getAllMeta = uMetaClass.getDeclaredMethod("getAllMeta", Object.class);
			return (Map<String, List<MetadataValue>>) getAllMeta.invoke(null, key);
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		synchronized(metadata){
			WeakKey weakKey = new WeakKey(key);
			Map<String, List<MetadataValue>> res = metadata.get(weakKey);
			if(res == null){
				res = new ConcurrentHashMap<String, List<MetadataValue>>(10, 0.75f, 2);
				metadata.put(weakKey, res);
			}
			return res;
		}
	}
	
	public static List<MetadataValue> getMeta(Object key, String metaKey){
		try {
			Class<?> uMetaClass = Class.forName("com.useful.ucars.util.UMeta");
			USING_UCARS = true;
			Method getMeta = uMetaClass.getDeclaredMethod("getMeta", Object.class, String.class);
			return (List<MetadataValue>) getMeta.invoke(null, key, metaKey);
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		Map<String, List<MetadataValue>> meta = getAllMeta(key);
		List<MetadataValue> list;
		synchronized(PSchLocks.getMonitor(key)){
			list = meta.get(metaKey);
			if(list == null){
				list = new ArrayList<MetadataValue>();
				meta.put(metaKey, list);
			}
		}
		return list;
	}
	
	public static void removeMeta(Object key, String metaKey){
		try {
			Class<?> uMetaClass = Class.forName("com.useful.ucars.util.UMeta");
			USING_UCARS = true;
			Method removeAllMeta = uMetaClass.getDeclaredMethod("removeMeta", Object.class, String.class);
			removeAllMeta.invoke(null, key, metaKey);
			return;
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		Map<String, List<MetadataValue>> meta = getAllMeta(key);
		synchronized(PSchLocks.getMonitor(key)){
			meta.remove(metaKey);
		}
	}
	
	public static void gc(){
		System.gc();
		clean();
	}
	
	public static void clean(){
		try {
			Class<?> uMetaClass = Class.forName("com.useful.ucars.util.UMeta");
			USING_UCARS = true;
			Method clean = uMetaClass.getDeclaredMethod("clean");
			clean.invoke(null);
			return;
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		synchronized(metadata){
			for(WeakKey ref:metadata.keySet()){
				try {
					if(ref.get() == null || ref == null){
						metadata.remove(ref);
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	private static class WeakKey extends WeakReference {

		private int hash;
		
		public WeakKey(Object arg0) {
			super(arg0);
			this.hash = arg0.hashCode();
		}
		
		@Override
		public int hashCode(){
			return hash;
		}
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof WeakKey)){
				return false;
			}
			Object self = get();
			Object other = ((WeakKey)o).get();
			if(self == null || other == null){
				return super.equals(o);
			}
			return self.equals(other);
		}
		
	}
	
	public static int getTotalMetaSize(){
		try {
			Class<?> uMetaClass = Class.forName("com.useful.ucars.util.UMeta");
			USING_UCARS = true;
			Method method = uMetaClass.getDeclaredMethod("getTotalMetaSize");
			return (Integer) method.invoke(null);
		} catch (Exception e) {
			if(Bukkit.getServer().getPluginManager().getPlugin("uCars") != null){
				e.printStackTrace();
			}
		}
		clean();
		return metadata.size();
	}
}
