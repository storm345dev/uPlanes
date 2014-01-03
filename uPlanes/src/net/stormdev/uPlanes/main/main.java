package net.stormdev.uPlanes.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.CustomLogger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

/**
 * Entry point class for the application
 * 
 * @author storm345
 *
 */
public class main extends JavaPlugin {
	public static YamlConfiguration lang = new YamlConfiguration();
	public static main plugin;
	public static FileConfiguration config = new YamlConfiguration();
	public static Colors colors; 
	public static CustomLogger logger = null;
	
	public ProtocolManager protocolManager = null;
	public uPlanesListener listener = null;
	public PlanesManager planeManager = null;
	
	/**
	 * Startup code
	 */
	public void onEnable(){
		plugin = this;
		getDataFolder().mkdirs();
		File langFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		File planesSaveFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "Data" + File.separator + "planes.data");
		if (langFile.exists() == false
				|| langFile.length() < 1) {
			try {
				langFile.createNewFile();
			} catch (IOException e) {
			}
			
		}
		try {
			lang.load(langFile);
		} catch(Exception e){
			getLogger().log(Level.WARNING, "Error creating/loading lang file! Regenerating..");
		}
		if (new File(getDataFolder().getAbsolutePath() + File.separator
				+ "config.yml").exists() == false
				|| new File(getDataFolder().getAbsolutePath() + File.separator
						+ "config.yml").length() < 1) {
			getDataFolder().mkdirs();
			File configFile = new File(getDataFolder().getAbsolutePath()
					+ File.separator + "config.yml");
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
			copy(getResource("uplanesConfigHeader.yml"), configFile);
		}
		config = getConfig();
		logger = new CustomLogger(getServer().getConsoleSender(), getLogger());
		//Setup the config
		try {
        	if (!config.contains("general.logger.colour")) {
				config.set("general.logger.colour", true);
			}
        	if (!config.contains("general.planes.heightLimit")) {
				config.set("general.planes.heightLimit", 256.0);
			}
        	if (!config.contains("general.planes.defaultSpeed")) {
				config.set("general.planes.defaultSpeed", 30.0);
			}
        	if (!config.contains("colorScheme.success")) {
				config.set("colorScheme.success", "&a");
			}
			if (!config.contains("colorScheme.error")) {
				config.set("colorScheme.error", "&c");
			}
			if (!config.contains("colorScheme.info")) {
				config.set("colorScheme.info", "&e");
			}
			if (!config.contains("colorScheme.title")) {
				config.set("colorScheme.title", "&9");
			}
			if (!config.contains("colorScheme.tp")) {
				config.set("colorScheme.tp", "&5");
			}
        } catch(Exception e){
        }
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		//Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		//Actually do something productive
		if(!setupProtocol()){
			logger.info(colors.getError()+"An error occurred in setting up"
					+ " with ProtocolLib, please check for a plugin update.");
			try {
				Thread.sleep(1000); //Tell them the message for 1000ms
			} catch (InterruptedException e) {} 
			getServer().getPluginManager().disablePlugin(this); //Disable
			return;
		}
		
		listener = new uPlanesListener(this);
		this.planeManager = new PlanesManager(planesSaveFile);
		getServer().getPluginManager().registerEvents(listener, this);
		logger.info("uPlanes v"+plugin.getDescription().getVersion()+" has been enabled!");
	}
	
	/**
	 * Shutdown code
	 */
	public void onDisable(){
		logger.info("uPlanes v"+plugin.getDescription().getVersion()+" has been disabled!");
	}
	
	/**
	 * Copy/Save resources
	 * 
	 * @param in The inputstream/resource to copy
	 * @param file The file to save it to
	 */
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				// System.out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Code to setup use of ProtocolLib
	 */
	private Boolean setupProtocol() {
		try {
			this.protocolManager = ProtocolLibrary.getProtocolManager();
			/*
			 * ((ProtocolManager)this.protocolManager).addPacketListener(new
			 * PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE,
			 * ListenerPriority.NORMAL, 0x1b) {
			 */
			
			((ProtocolManager) this.protocolManager).addPacketListener(
					  new PacketAdapter(this, PacketType.Play.Client.STEER_VEHICLE) {
						  @Override
						  public void onPacketReceiving(PacketEvent event) {
								PacketContainer packet = event.getPacket();
								float sideways = packet.getFloat().read(0);
								float forwards = packet.getFloat().read(1);
								MotionManager.move(event.getPlayer(), forwards,
										sideways);
						  }
					});
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
