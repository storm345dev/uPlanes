package net.stormdev.uPlanes.main;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.utils.Keypress;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.Plane;
import net.stormdev.uPlanes.utils.PlaneDeathEvent;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;
import net.stormdev.uPlanes.utils.Stat;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

public class uPlanesListener implements Listener {
	private main plugin;
	private double defaultSpeed;
	private double defaultHealth;
	private double punchDamage;
	public uPlanesListener(main instance){
		this.plugin = instance;
		
		defaultSpeed = main.config.getDouble("general.planes.defaultSpeed");
		defaultHealth = main.config.getDouble("general.planes.defaultHealth");
		punchDamage = main.config.getDouble("general.planes.punchDamage");
	}
	@EventHandler
	void planeFlightControl(PlaneUpdateEvent event){
		Vehicle vehicle = event.getVehicle();
		
		if(!(vehicle instanceof Minecart)){
			return;
		}
		
		Minecart cart = (Minecart) vehicle;
		Plane plane = getPlane(cart);
		
		if(plane == null){ //Not a plane, just a Minecart
			return;
		}
		
		Location loc = vehicle.getLocation();
		Vector travel = event.getTravelVector();
		double y = 0.0;
		
		travel.multiply(defaultSpeed);
		Keypress press = event.getPressedKey();
		
		switch(press){
		case A: 
			y = 0.5; break; //Go up
		case D: 
			y = -0.5; break; //Go down
		default:
			break;
		}
		
		travel.setY(y);
		
		Vector behind = travel.clone().multiply(-1); //Behind the plane
		Location exhaust = loc.add(behind);
		exhaust.getWorld().playEffect(exhaust, Effect.SMOKE, 1);
		
		vehicle.setVelocity(event.getTravelVector());
		return;
	}
	
	@EventHandler
	void placePlane(PlayerInteractEvent event){
		Action a = event.getAction();
		if(!(a == Action.RIGHT_CLICK_BLOCK)){
			return;
		}
		
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();
		
		if(inHand.getType() != Material.MINECART){
			return;
		}
		Plane plane = main.plugin.planeManager.getPlane(inHand);
		if(plane == null){
			return; //Just a minecart
		}
		inHand.setAmount(inHand.getAmount()-1);
		if(player.getGameMode() == GameMode.CREATIVE){
			player.setItemInHand(new ItemStack(Material.AIR)); //Remove from their hand
		}
		//Now place the car
		Block b = event.getClickedBlock();
		Location toSpawn = b.getLocation().add(0,1.5,0);
		
		Minecart ent = (Minecart) toSpawn.getWorld().spawnEntity(toSpawn, EntityType.MINECART);
		ent.setMetadata("ucars.ignore", new StatValue(true, main.plugin));
		ent.setMetadata("plane.health", new StatValue(plane.health, main.plugin));
		ent.setMetadata("plane.speed", new StatValue(plane.mutliplier, main.plugin));
		
		plane.isPlaced = true;
		plane.id = ent.getUniqueId();
		
		main.plugin.planeManager.setPlane(plane.id, plane);
		return;
	}
	
	@EventHandler
	void vehicleDestroy(VehicleDestroyEvent event){
		Vehicle v = event.getVehicle();
		if(!(v instanceof Minecart)){
			return;
		}
		Minecart m = (Minecart) v;
		if(!isAPlane(m)){
			return;
		}
		event.setCancelled(true); //Don't allow, let health handle it
	}
	
	@EventHandler
	void lostPlanes(ItemDespawnEvent event){
		Item i = event.getEntity();
		ItemStack is = i.getItemStack();
		if(is.getType() != Material.MINECART){
			return;
		}
		ItemMeta im = is.getItemMeta();
		if(im.getLore() == null || im.getLore().size() < 1){
			return;
		}
		String id = ChatColor.stripColor(im.getLore().get(0));
		UUID planeId = UUID.fromString(id);
		plugin.planeManager.removePlane(planeId);
		return;
	}
	
	@EventHandler
	void vehicleDamage(VehicleDamageEvent event){
		Vehicle veh = event.getVehicle();
		if(!(veh instanceof Minecart)){
			return;
		}
		Minecart m = (Minecart) veh;
		Plane plane = getPlane(m);
		if(plane == null){
			return;
		}
		double health = plane.health;
		if(m.hasMetadata("plane.health")){
			List<MetadataValue> ms = m.getMetadata("plane.health");
			health = (Double) ms.get(0).value();
		}
		double damage = event.getDamage();
		String msg = Lang.get("general.damage.msg");
		Entity attacker = event.getAttacker();
		Boolean die = false;
		if(attacker != null && attacker instanceof Player){
			//Plane being punched to death
			damage = punchDamage;
			msg = msg.replaceAll(Pattern.quote("%damage%"), damage+"");
			health -= damage;
			if(health <= 0){
				die = true;
				health = 0;
			}
			msg = msg.replaceAll(Pattern.quote("%remainder%"), health+"");
			msg = msg.replaceAll(Pattern.quote("%cause%"), "Fist");
			((Player)attacker).sendMessage(main.colors.getInfo()+msg);
		}
		else{
			msg = msg.replaceAll(Pattern.quote("%damage%"), damage+"");
			health -= damage;
			if(health <= 0){
				die = true;
				health = 0;
			}
			msg = msg.replaceAll(Pattern.quote("%remainder%"), health+"");
			msg = msg.replaceAll(Pattern.quote("%cause%"), "Damage");
		}
		if(m.getPassenger() != null && m.getPassenger() instanceof Player){
			((Player)m.getPassenger()).sendMessage(main.colors.getInfo()+msg);
		}
		
		m.removeMetadata("plane.health", main.plugin);
		m.setMetadata("plane.health", new StatValue(health, main.plugin)); //Update the health on the vehicle
		
		event.setDamage(0.0);
		event.setCancelled(true);
		if(die || health < 0.1){
			//Kill the plane
			PlaneDeathEvent evt = new PlaneDeathEvent(m, plane);
			main.plugin.getServer().getPluginManager().callEvent(evt);
			if(!evt.isCancelled()){
				//Kill the plane
				killPlane(m, plane);
			}
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void itemCraft(CraftItemEvent event){
		if(event.isCancelled()){
			return;
		}
		ItemStack recipe = event.getCurrentItem();
		if(!(recipe.getType() == Material.MINECART)){
			return;
		}
		if(!ChatColor.stripColor(recipe.getItemMeta().getDisplayName()).equalsIgnoreCase("plane")){
			return;
		}
		Plane plane = PlaneGenerator.gen();
        event.setCurrentItem(PlaneItemMethods.getItem(plane));
        main.plugin.planeManager.setPlane(plane.id, plane);
		return;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	void carUpgradeAnvil(InventoryClickEvent event){
		if(event.getAction()==InventoryAction.CLONE_STACK){
			ItemStack cloned = event.getCursor();
			if(cloned.getType() == Material.MINECART || 
					cloned.getItemMeta() == null ||
					cloned.getItemMeta().getLore() == null ||
					cloned.getItemMeta().getLore().size() < 2){
				event.setCancelled(true);
				return;
			}
			return;
		}
		final Player player = (Player) event.getWhoClicked();
		InventoryView view = event.getView();
		final Inventory i = event.getInventory();
		if(!(i instanceof AnvilInventory)){
			return;
		}
		int slotNumber = event.getRawSlot();
		if(!(slotNumber == view.convertSlot(slotNumber))){
			//Not clicking in the anvil
			return;
		}
		//AnvilInventory i = (AnvilInventory) inv;
		Boolean update = true;
		Boolean save = false;
		Boolean pickup = false;
		if(event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME){
			update = false;
			pickup = true;
			if(slotNumber == 2){ //Result slot
				save = true;
			}
		}
		ItemStack item = null;
		try {
			item = i.getItem(0);
		} catch (Exception e) {
			return;
		}
		if(item == null){
			return;
		}
		if(!(item.getType() == Material.MINECART) || 
				item.getItemMeta().getLore().size() < 2){
			return; //Not a car
		}
		//Anvil contains a car in first slot.
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		final UUID id;
		try {
			if(lore.size() < 1){
				return;
			}
			id = UUID.fromString(ChatColor.stripColor(lore.get(0)));
		} catch (Exception e) {
			return;
		}
		Plane plane = plugin.planeManager.getPlane(id);
		if(plane == null){
			return;
		}
		final ConcurrentHashMap<String, Stat> stats = plane.stats;
        if(save && slotNumber ==2){
			//They are renaming it
        	ItemStack result = event.getCurrentItem();
        	String name = ChatColor.stripColor(result.getItemMeta().getDisplayName());
        	plane.name = name;
        	plugin.planeManager.setPlane(id, plane);
        	player.sendMessage(main.colors.getSuccess()+"+"+main.colors.getInfo()+" Renamed plane to: '"+name+"'");
        	return;
		}
		InventoryAction a = event.getAction();
		ItemStack upgrade = null;
		Boolean set = false;
		final ItemStack up = upgrade;
		final Boolean updat = update;
		final Boolean sav = save;
		final Plane ca = plane;
		if(slotNumber == 1 && (a==InventoryAction.PLACE_ALL || a==InventoryAction.PLACE_ONE || a==InventoryAction.PLACE_SOME) && event.getCursor().getType()!=Material.AIR){
			//upgrade = event.getCursor().clone();
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

				public void run() {
					ItemStack upgrade = up;
					try {
						upgrade = i.getItem(1); //Upgrade slot
					} catch (Exception e) {
						return;
					}
					if(upgrade == null){
						return;
					}
					//A dirty trick to get the inventory to look correct on the client
					UpgradeManager.applyUpgrades(upgrade, ca, updat, sav, player, i, id);
					return;
				}}, 1l);
			set = true;
			return;
		}
		if(!set){
		    try {
				upgrade = i.getItem(1); //Upgrade slot
			} catch (Exception e) {
				return;
			}
		}
		if(upgrade == null){
			return;
		}
		if(pickup && slotNumber == 1){
			return; //Don't bother tracking and updating, etc...
		} 
		UpgradeManager.applyUpgrades(upgrade, plane, update, save, player, i, id);
		return;
	}
	
	public void killPlane(Vehicle vehicle, Plane plane){
		//Kill plane
		UUID id = vehicle.getUniqueId();
		plane.isPlaced = false;
		plugin.planeManager.setPlane(id, plane);
		Location loc = vehicle.getLocation();
		Entity top = vehicle.getPassenger();
		if(top instanceof Player){
			top.eject();
		}
		vehicle.eject();
		vehicle.remove();
		loc.getWorld().dropItemNaturally(loc, new ItemStack(PlaneItemMethods.getItem(plane)));
		//Remove plane and get back item
		return;
	}
	
	public Plane getPlane(Minecart m){
		return plugin.planeManager.getPlane(m.getUniqueId());
	}
	
	public Boolean isAPlane(Minecart m){
		return plugin.planeManager.isAPlane(m.getUniqueId());
	}
}
