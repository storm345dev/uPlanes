package net.stormdev.uPlanes.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Level;

import net.stormdev.uPlanes.commands.AdminCommandExecutor;
import net.stormdev.uPlanes.commands.AutoPilotAdminCommandExecutor;
import net.stormdev.uPlanes.commands.AutoPilotCommandExecutor;
import net.stormdev.uPlanes.commands.InfoCommandExecutor;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.CustomLogger;
import net.stormdev.uPlanes.utils.uCarsCompatibility;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
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
	public Random random = new Random();
	public DestinationManager destinationManager = null;
	
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
		File destinationSaveFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "Data" + File.separator + "destinations.locationdata");
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
			lang = new YamlConfiguration();
			getLogger().log(Level.WARNING, "Error creating/loading lang file! Regenerating..");
		}
		if(!lang.contains("general.cmd.destinations.set")){
			lang.set("general.cmd.destinations.set", "Successfully set destination to where you're standing!");
		}
		if(!lang.contains("general.cmd.destinations.del")){
			lang.set("general.cmd.destinations.del", "Successfully deleted destination!");
		}
		if(!lang.contains("general.cmd.destinations.notInPlane")){
			lang.set("general.cmd.destinations.notInPlane", "Cannot engague autopilot when not in a plane!");
		}
		if(!lang.contains("general.cmd.destinations.go")){
			lang.set("general.cmd.destinations.go", "Destination set! Lets go!");
		}
		if(!lang.contains("general.cmd.destinations.arrvie")){
			lang.set("general.cmd.destinations.arrive", "You have arrived at your destination!");
		}
		if(!lang.contains("general.cmd.destinations.invalid")){
			lang.set("general.cmd.destinations.invalid", "That destination is doesn't exist! Do /destinations for a list.");
		}
		if(!lang.contains("general.cmd.destinations.wrongWorld")){
			lang.set("general.cmd.destinations.wrongWorld", "That destination is not in this dimension!");
		}
		if(!lang.contains("general.cmd.destinations.cancel")){
			lang.set("general.cmd.destinations.cancel", "Disabled Autopilot");
		}
		if(!lang.contains("general.info.msg")){
			lang.set("general.info.msg", "uPlanes %version%, by storm345, is working!");
		}
		if(!lang.contains("general.disabled.msg")){
			lang.set("general.disabled.msg", "Feature disabled");
		}
		if(!lang.contains("general.playersOnly")){
			lang.set("general.playersOnly", "Players only!");
		}
		if(!lang.contains("general.damage.msg")){
			lang.set("general.damage.msg", "&c-%damage%&6 (%remainder%) - [&b%cause%&6]");
		}
		if(!lang.contains("general.upgrade.msg")){
			lang.set("general.upgrade.msg", "&a+%amount% &e%stat%. Value: %value%");
		}
		if(!lang.contains("general.spawn.msg")){
			lang.set("general.spawn.msg", "Given you a plane!");
		}
		if(!lang.contains("general.playersOnly")){
			lang.set("general.playersOnly", "Players only!");
		}
		if(!lang.contains("general.heightLimit")){
			lang.set("general.heightLimit", "You may not fly above this height!");
		}
		if(!lang.contains("general.noExit.msg")){
			lang.set("general.noExit.msg", "You may only exit in a 3x3 clear area!");
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
        	/*
        	if (!config.contains("general.planes.defaultSpeed")) {
				config.set("general.planes.defaultSpeed", 30.0);
			}
			*/
        	if (!config.contains("general.planes.defaultHealth")) {
				config.set("general.planes.defaultHealth", 30.0);
			}
        	if (!config.contains("general.planes.maxHealth")) {
				config.set("general.planes.maxHealth", 100.0);
			}
        	if (!config.contains("general.planes.punchDamage")) {
				config.set("general.planes.punchDamage", 15.0);
			}
        	if(!config.contains("general.planes.perms")){
        		config.set("general.planes.perms", false);
        	}
        	if(!config.contains("general.planes.flyPerm")){
        		config.set("general.planes.flyPerm", "uplanes.fly");
        	}
        	if(!config.contains("general.planes.safeExit")){
        		config.set("general.planes.safeExit", true);
        	}
        	if(!config.contains("general.planes.autopilot")){
        		config.set("general.planes.autopilot", true);
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
		
		//Create a blank plane item
		ItemStack plane = new ItemStack(Material.MINECART);
		ItemMeta im = plane.getItemMeta();
		im.setDisplayName("Plane");
		plane.setItemMeta(im);
		
		ShapedRecipe recipe = new ShapedRecipe(plane);
		recipe.shape("012","345","678");
		recipe.setIngredient('0', Material.REDSTONE);
		recipe.setIngredient('1', Material.LEVER);
		recipe.setIngredient('2', Material.REDSTONE);
		recipe.setIngredient('3', Material.WOOD_PLATE);
		recipe.setIngredient('4', Material.MINECART);
		recipe.setIngredient('5', Material.WOOD_PLATE);
		
		//Create a blank hoverplane item
		ItemStack hplane = new ItemStack(Material.MINECART);
		ItemMeta him = plane.getItemMeta();
		him.setDisplayName("Hover Plane");
		hplane.setItemMeta(him);
		
		ShapedRecipe hoverRecipe = new ShapedRecipe(hplane);
		hoverRecipe.shape("012","345","678");
		hoverRecipe.setIngredient('0', Material.REDSTONE);
		hoverRecipe.setIngredient('1', Material.LEVER);
		hoverRecipe.setIngredient('2', Material.REDSTONE);
		hoverRecipe.setIngredient('3', Material.WOOD_PLATE);
		hoverRecipe.setIngredient('4', Material.MINECART);
		hoverRecipe.setIngredient('5', Material.WOOD_PLATE);
		hoverRecipe.setIngredient('7', Material.REDSTONE_TORCH_ON);
		
		getServer().addRecipe(recipe);
		getServer().addRecipe(hoverRecipe);
		
		setupUCarsCompatibility();
		
		this.destinationManager = new DestinationManager(destinationSaveFile);
		
		getCommand("uPlanes").setExecutor(new InfoCommandExecutor());
		getCommand("plane").setExecutor(new AdminCommandExecutor());
		AutoPilotCommandExecutor apce = new AutoPilotCommandExecutor();
		getCommand("destination").setExecutor(apce);
		getCommand("destinations").setExecutor(apce);
		AutoPilotAdminCommandExecutor apace = new AutoPilotAdminCommandExecutor();
		getCommand("setDestination").setExecutor(apace);
		getCommand("delDestination").setExecutor(apace);
		
		
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
	private void setupUCarsCompatibility(){
		if(getServer().getPluginManager().getPlugin("uCars") == null){
			return;
		}
		uCarsCompatibility.run();
	}
}
