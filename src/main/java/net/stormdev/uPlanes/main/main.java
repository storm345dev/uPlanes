package net.stormdev.uPlanes.main;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.milkbowl.vault.economy.Economy;
import net.stormdev.uPlanes.api.uPlanesAPI;
import net.stormdev.uPlanes.commands.*;
import net.stormdev.uPlanes.presets.PresetManager;
import net.stormdev.uPlanes.shops.PlaneShop;
import net.stormdev.uPlanes.utils.Colors;
import net.stormdev.uPlanes.utils.CustomLogger;
import net.stormdev.uPlanes.utils.uCarsCompatibility;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

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
	public static boolean perms = true;
	public static boolean upgradePerms = false;
	public static double maxSpeed = 200;
	public static boolean doAcceleration = true;
	public static boolean doTurningCircles = true;
	
	public ProtocolManager protocolManager = null;
	public uPlanesListener listener = null;
	public PlanesManager planeManager = null;
	public Random random = new Random();
	public DestinationManager destinationManager = null;
	public uPlanesAPI api = null;
	
	public PlaneShop planeShop = null;
	public static Economy economy = null;
	public boolean shopsEnabled = false;
	public int cacheSize = 20;
	public PresetManager presets = null;
	
	public static HashMap<String, Double> fuel = new HashMap<String, Double>();
	
	/**
	 * Economy setup code
	 * 
	 * @return If an economy plugin was found or not
	 */
	public boolean setupEconomy() {
		try {
			RegisteredServiceProvider<Economy> economyProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			return (economy != null);
		} catch (Exception e) {
			return false;
		}
	}
	
	
	/**
	 * Startup code
	 */
	public void onEnable(){
		plugin = this;
		getDataFolder().mkdirs();
		File langFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		File planesSaveFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "Data" + File.separator + "uplanes.data");
		File destinationSaveFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "Data" + File.separator + "destinations.locationdata");
		if (!langFile.exists() || langFile.length() < 1) {
			try {
				langFile.createNewFile();
			} catch (IOException e) {
			}
		}
		try {
			lang.load(langFile);
		} catch(Exception e) {
			lang = new YamlConfiguration();
			getLogger().log(Level.WARNING, "Error creating/loading lang file! Regenerating..");
		}

		if(!lang.contains("general.buy.notEnoughMoney")){
			lang.set("general.buy.notEnoughMoney", "You cannot afford that item! You only have %balance%!");
		}
		if(!lang.contains("general.buy.success")){
			lang.set("general.buy.success", "Successfully bought %item% for %price%, you now have %balance%!");
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
			lang.set("general.noExit.msg", "You may only exit in a clear area!");
		}
		if (!lang.contains("lang.fuel.empty")) {
			lang.set("lang.fuel.empty", "You don't have any fuel left!");
		}
		if (!lang.contains("lang.fuel.disabled")) {
			lang.set("lang.fuel.disabled", "Fuel is not enabled!");
		}
		if (!lang.contains("lang.fuel.unit")) {
			lang.set("lang.fuel.unit", "litres");
		}
		if (!lang.contains("lang.fuel.isItem")) {
			lang.set("lang.fuel.isItem",
					"&9[Important:]&eItem fuel is enabled-The above is irrelevant!");
		}
		if (!lang.contains("lang.fuel.invalidAmount")) {
			lang.set("lang.fuel.invalidAmount", "Amount invalid!");
		}
		if (!lang.contains("lang.fuel.noMoney")) {
			lang.set("lang.fuel.noMoney", "You have no money!");
		}
		if (!lang.contains("lang.fuel.notEnoughMoney")) {
			lang.set("lang.fuel.notEnoughMoney",
					"That purchase costs %amount% %unit%! You only have %balance% %unit%!");
		}
		if (!lang.contains("lang.fuel.success")) {
			lang.set(
					"lang.fuel.success",
					"Successfully purchased %quantity% of fuel for %amount% %unit%! You now have %balance% %unit% left!");
		}
		if (!lang.contains("lang.fuel.sellSuccess")) {
			lang.set(
					"lang.fuel.sellSuccess",
					"Successfully sold %quantity% of fuel for %amount% %unit%! You now have %balance% %unit% left!");
		}
		
		if (!new File(getDataFolder(), "config.yml").exists()
				|| new File(getDataFolder(), "config.yml").length() < 1) {
			getDataFolder().mkdirs();
			File configFile = new File(getDataFolder(), "config.yml");
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
        	if(!config.contains("general.currencySign")){
        		config.set("general.currencySign", "$");
        	}
        	if(!config.contains("general.planes.price")){
        		config.set("general.planes.price", 70.0);
        	}
        	if (!config.contains("general.planes.heightLimit")) {
				config.set("general.planes.heightLimit", 256.0);
			}
        	if (!config.contains("general.planes.defaultHealth")) {
				config.set("general.planes.defaultHealth", 30.0);
			}
        	if (!config.contains("general.planes.maxHealth")) {
				config.set("general.planes.maxHealth", 100.0);
			}
        	if (!config.contains("general.planes.enableCrashing")) {
				config.set("general.planes.enableCrashing", true);
			}
        	if (!config.contains("general.planes.punchDamage")) {
				config.set("general.planes.punchDamage", 15.0);
			}
        	if(!config.contains("general.planes.perms")){
        		config.set("general.planes.perms", false);
        	}
        	if(!config.contains("general.planes.upgradeperms")){
        		config.set("general.planes.upgradeperms", false);
        	}
        	perms = config.getBoolean("general.planes.perms");
        	upgradePerms = config.getBoolean("general.planes.upgradeperms");
        	if(!config.contains("general.planes.flyPerm")){
        		config.set("general.planes.flyPerm", "uplanes.fly");
        	}
        	if(!config.contains("general.planes.maxSpeed")){
        		config.set("general.planes.maxSpeed", 200.0d);
        	}
        	maxSpeed = config.getDouble("general.planes.maxSpeed");
        	if(!config.contains("general.planes.doAcceleration")){
        		config.set("general.planes.doAcceleration", true);
        	}
        	doAcceleration = config.getBoolean("general.planes.doAcceleration");
        	if(!config.contains("general.planes.doTurningCircles")){
        		config.set("general.planes.doTurningCircles", true);
        	}
        	doTurningCircles = config.getBoolean("general.planes.doTurningCircles");
        	if(!config.contains("general.planes.safeExit")){
        		config.set("general.planes.safeExit", true);
        	}
        	if(!config.contains("general.planes.autopilot")){
        		config.set("general.planes.autopilot", true);
        	}
        	if(!config.contains("general.shop.enable")){
        		config.set("general.shop.enable", true);
        	}
        	if(!config.contains("general.planes.cacheSize")){
        		config.set("general.planes.cacheSize", 100);
        	}
        	if (!config.contains("general.planes.fuel.enable")) {
				config.set("general.planes.fuel.enable", false);
			}
			if (!config.contains("general.planes.fuel.price")) {
				config.set("general.planes.fuel.price", (double) 2);
			}
			if (!config.contains("general.planes.fuel.check")) {
				config.set("general.planes.fuel.check", new String[]{"FEATHER"});
			}
			if (!config.contains("general.planes.fuel.cmdPerm")) {
				config.set("general.planes.fuel.cmdPerm", "uplanes.uplanes");
			}
			if (!config.contains("general.planes.fuel.bypassPerm")) {
				config.set("general.planes.fuel.bypassPerm", "uplanes.bypassfuel");
			}
			if (!config.contains("general.planes.fuel.sellFuel")) {
				config.set("general.planes.fuel.sellFuel", true);
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
		this.presets = new PresetManager();
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		
		cacheSize = config.getInt("general.planes.cacheSize");
		
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
		hoverRecipe.shape("012", "345", "678");
		hoverRecipe.setIngredient('0', Material.REDSTONE);
		hoverRecipe.setIngredient('1', Material.LEVER);
		hoverRecipe.setIngredient('2', Material.REDSTONE);
		hoverRecipe.setIngredient('3', Material.WOOD_PLATE);
		hoverRecipe.setIngredient('4', Material.MINECART);
		hoverRecipe.setIngredient('5', Material.WOOD_PLATE);
		hoverRecipe.setIngredient('7', Material.REDSTONE_TORCH_ON);
		
		getServer().addRecipe(recipe);
		getServer().addRecipe(hoverRecipe);
		
		setupUplanesCompatibility();
		
		this.destinationManager = new DestinationManager(destinationSaveFile);
		
		getCommand("uPlanes").setExecutor(new InfoCommandExecutor());
		getCommand("plane").setExecutor(new AdminCommandExecutor());
		AutoPilotCommandExecutor apce = new AutoPilotCommandExecutor();
		getCommand("destination").setExecutor(apce);
		getCommand("destinations").setExecutor(apce);
		AutoPilotAdminCommandExecutor apace = new AutoPilotAdminCommandExecutor();
		getCommand("setDestination").setExecutor(apace);
		getCommand("delDestination").setExecutor(apace);
		getCommand("planeFuel").setExecutor(new FuelCommandExecutor());
		
		boolean economy = setupEconomy();
		shopsEnabled = main.config.getBoolean("general.shop.enable") && economy;
		if(shopsEnabled){
			planeShop = new PlaneShop(this);
		}
		fuel = new HashMap<String, Double>();
		File fuels = new File(plugin.getDataFolder(), "fuel.bin");
		if (fuels.exists() && fuels.length() > 1) {
			fuel = loadHashMapDouble(plugin.getDataFolder()
					.getAbsolutePath()
					+ File.separator
					+ "fuel.bin");
			if (fuel == null) {
				fuel = new HashMap<String, Double>();
			}
		}
		
		api = uPlanesAPI.getAPI(); //Setup the API
		
		logger.info("uPlanes v"+plugin.getDescription().getVersion()+" has been enabled!");
	}
	
	/**
	 * Shutdown code
	 */
	public void onDisable(){
		if(planeShop != null){
			planeShop.destroy();
		}
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
	private boolean setupProtocol() {
		try {
			this.protocolManager = ProtocolLibrary.getProtocolManager();
			/*
			 * ((ProtocolManager)this.protocolManager).addPacketListener(new
			 * PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE,
			 * ListenerPriority.NORMAL, 0x1b) {
			 */
			
			this.protocolManager.addPacketListener(
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
	private void setupUplanesCompatibility(){
		if(getServer().getPluginManager().getPlugin("uCars") == null){
			return;
		}
		uCarsCompatibility.run();
	}
	
	public static void saveFuel(){
		saveHashMap(fuel, plugin.getDataFolder()
						.getAbsolutePath()
						+ File.separator
						+ "fuel.bin");
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, Double> loadHashMapDouble(String path) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					path));
			Object result = ois.readObject();
			ois.close();
			// you can feel free to cast result to HashMap<String, Integer> if
			// you know there's that HashMap in the file
			return (HashMap<String, Double>) result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void saveHashMap(HashMap<String, Double> map, String path) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			// Handle I/O exceptions
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
