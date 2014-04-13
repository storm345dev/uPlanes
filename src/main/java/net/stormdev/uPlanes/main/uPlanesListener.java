package net.stormdev.uPlanes.main;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.stormdev.uPlanes.api.AutopilotDestination;
import net.stormdev.uPlanes.api.Keypress;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.api.PlaneDeathEvent;
import net.stormdev.uPlanes.api.Stat;
import net.stormdev.uPlanes.utils.Lang;
import net.stormdev.uPlanes.utils.PlaneUpdateEvent;
import net.stormdev.uPlanes.utils.StatValue;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class uPlanesListener implements Listener {
	private main plugin;
	
	private double punchDamage;
	private double heightLimit;
	private boolean perms;
	private String perm;
	private boolean safeExit;
	public uPlanesListener(main instance){
		this.plugin = instance;
		
		punchDamage = main.config.getDouble("general.planes.punchDamage");
		heightLimit = main.config.getDouble("general.planes.heightLimit");
		perms = main.config.getBoolean("general.planes.perms");
		perm = main.config.getString("general.planes.flyPerm");
		safeExit = main.config.getBoolean("general.planes.safeExit");
	}
	
	@EventHandler
	void protectInPlane(EntityDamageEvent event){
		Entity e = event.getEntity();
		if(event.getCause() != DamageCause.FALL){
			return;
		}
		Entity v = e.getVehicle();
		if(v == null || !(v instanceof Minecart)){
			return;
		}
		if(isAPlane((Minecart) v)){
			event.setDamage(0d);
			event.setCancelled(true);
		}
	}
	
	 @EventHandler
	 void signWrite(SignChangeEvent event){
		 String[] lines = event.getLines();
			if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Shop]")){
				lines[0] = ChatColor.GREEN+"[Shop]";
				lines[1] = ChatColor.RED + ChatColor.stripColor(lines[1]);
				lines[2] = "Place chest";
				lines[3] = "above";
			}
		return;
	 }
	 
	 @EventHandler
		public void shopOpen(InventoryOpenEvent event){
			if(!plugin.shopsEnabled){
				//Don't do shops
				return;
			}
			if(main.economy == null){
		        Boolean installed = plugin.setupEconomy();
		        if(!installed){
		        	main.logger.info(main.colors.getError()+"[Important] Unable to find an economy plugin:"
		        			+ " shop was unable to open.");
		        	return;
		        }
			}
			Inventory inv = event.getInventory();
			if (!(inv.getHolder() instanceof Chest || inv.getHolder() instanceof DoubleChest)){
	            return;
	        }
			//They opened a chest
			Block block = null;
			if(inv.getHolder() instanceof Chest){
				block = ((Chest)inv.getHolder()).getBlock();
			}
			else{
				block = ((DoubleChest)inv.getHolder()).getLocation().getBlock();
			}
			Block underBlock = block.getRelative(BlockFace.DOWN);
			Block underunderBlock = underBlock.getRelative(BlockFace.DOWN);
			Sign sign = null;
			if((underBlock.getState() instanceof Sign)){
				sign = (Sign) underBlock.getState();
			}
			else if((underunderBlock.getState() instanceof Sign)){
				sign = (Sign) underunderBlock.getState();
			}
			else {
				return;
			}
			if(!(ChatColor.stripColor(sign.getLines()[0])).equalsIgnoreCase("[Shop]") || !(ChatColor.stripColor(sign.getLines()[1])).equalsIgnoreCase("planes")){
				return;
			}
			//A trade sign for cars
			//Create a trade inventory
			Player player = (Player) event.getPlayer(); //Get the player from the event
			event.getView().close();
			event.setCancelled(true); //Cancel the event
			plugin.planeShop.open(player);
			//Made the trade booth
			return;
		}
	
	 @EventHandler (priority = EventPriority.LOW) //Call early
	    void vehicleExit(VehicleExitEvent event){
	    	//Safe exit
	    	if(!safeExit){
	    		return; //Don't bother
	    	}
	    	Vehicle veh = event.getVehicle();
	    	final Location loc = veh.getLocation();
	        Block b = loc.getBlock();
	        final Entity exited = event.getExited();
	        if(!(exited instanceof Player) || !(veh instanceof Minecart)){
	        	return;
	        }
	        if(!plugin.planeManager.isAPlane(veh.getUniqueId())){
	        	return;
	        }
	        Player player = (Player) exited;
	        
	        if((!b.isEmpty() && !b.isLiquid()) 
	        		|| (!b.getRelative(BlockFace.UP).isEmpty() && !b.getRelative(BlockFace.UP).isLiquid())
	        		|| (!b.getRelative(BlockFace.UP, 2).isEmpty() && !b.getRelative(BlockFace.UP, 2).isLiquid())){
	        	//Not allowed to exit
	        	player.sendMessage(main.colors.getError()+Lang.get("general.noExit.msg"));
	        	event.setCancelled(true);
	        }
	        else{
	        	//Handle the exit ourselves
	        	main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new BukkitRunnable(){

					public void run() {
						exited.teleport(loc.add(0, 0.5, 0));
						return;
					}}, 2l); //Teleport back to car loc after exit
	        }
	    	return;
	    }
	
	@EventHandler
	void vehicleUpdate(VehicleUpdateEvent event){
		Vehicle veh = event.getVehicle();
		if(!(veh instanceof Minecart)){
			return;
		}
		Minecart car = (Minecart) veh;
		Location loc = car.getLocation();
		Entity passenger = car.getPassenger();
		if(passenger == null){
			return;
		}
		if(veh.hasMetadata("plane.destination")){
			//Autopilot
			List<MetadataValue> metas = veh.getMetadata("plane.destination");
			Location dest = (Location) metas.get(0).value();
			FlightControl.route(dest, loc, veh);
			return;
		}
		if(!veh.hasMetadata("plane.hover")){
			return;
		}
		//Hover
		if(veh.hasMetadata("plane.left") || veh.hasMetadata("plane.right")){
			//Going up or down
			return; //Ignore it
		}
		Block b = loc.getBlock();
		Vector vel = car.getVelocity();
		Block under = b.getRelative(BlockFace.DOWN);
		Block under2 = b.getRelative(BlockFace.DOWN,2);
		int count = 0;
		if(!b.isEmpty()){
			count++;
		}
		if(!under.isEmpty()){
			count++;
		}
		if(!under2.isEmpty()){
			count++;
		}
		switch(count){
		case 0:vel.setY(-0.3);break;
		case 1:vel.setY(2); break;
		case 2:vel.setY(1); break;
		case 3:vel.setY(0.1);break;
		}
		if((loc.getY() < heightLimit)){
			car.setVelocity(vel);
		}
		return;
	}
	
	@EventHandler
	void planeFlightControl(PlaneUpdateEvent event){
		Vehicle vehicle = event.getVehicle();
		Player player = event.getPlayer();
		
		if(!(vehicle instanceof Minecart)){
			return;
		}
		
		if(main.perms){
			if(!player.hasPermission("uplanes.fly")){
				player.sendMessage(main.colors.getError()+"You don't have the permission 'uplanes.fly' required to fly a plane!");
				return;
			}
		}
		
		Minecart cart = (Minecart) vehicle;
		Plane plane = getPlane(cart);
		
		if(plane == null){ //Not a plane, just a Minecart
			return;
		}
		
		if(plane.stats.containsKey("plane.hover")){
			vehicle.setMetadata("plane.hover", new StatValue(true, main.plugin));
		}
		
		if(cart.hasMetadata("plane.destination")){
			AutopilotDestination aData = null;
			
			if(vehicle.hasMetadata("plane.autopilotData")){
				aData = (AutopilotDestination) vehicle.getMetadata("plane.autopilotData").get(0).value();
			}
			
			boolean cont = false;
			
			if(aData != null && !aData.isAutopilotOverridenByControlInput()){
				cont = true;
			}
			
			if(!cont){
				//Disable autopilot
				if(aData != null){
					aData.autoPilotCancelled();
				}
				else{
					player.sendMessage(main.colors.getInfo()+Lang.get("general.cmd.destinations.cancel"));
				}
				cart.removeMetadata("plane.destination", main.plugin);
				cart.removeMetadata("plane.autopilotData", main.plugin);
			}
		}
		
		if(perms){
			if(!player.hasPermission(perm)){
				return;
			}
		}
		
		cart.setMaxSpeed(5); //Don't crash the server...
		
		Location loc = vehicle.getLocation();
		Vector travel = event.getTravelVector();
		double y = 0.0;
		double multiplier = plane.mutliplier;
		
		travel.multiply(multiplier);
	    Keypress press = event.getPressedKey();
		
	    switch(press){
		case A: 
			y = 0.6; break; //Go up
		case D: 
			y = -0.6; break; //Go down
		default:
			break;
		}
		
		if(loc.getY() >= heightLimit){
			y = 0;
			//Send message it's too high
			player.sendMessage(main.colors.getError()+Lang.get("general.heightLimit"));
		}
		
		travel.setY(y);
		
		Vector behind = travel.clone().multiply(-1); //Behind the plane
		Location exhaust = loc.add(behind);
		exhaust.getWorld().playEffect(exhaust, Effect.SMOKE, 1);
		
		vehicle.setVelocity(travel);
		return;
	}
	
	@EventHandler
	void placePlane(PlayerInteractEvent event){
		Action a = event.getAction();
		if(!(a == Action.RIGHT_CLICK_BLOCK)){
			return;
		}
		if(event.isCancelled()){
			return; //Worldguard says no
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
		if(main.perms){
			if(!player.hasPermission("uplanes.place")){
				player.sendMessage(main.colors.getError()+"You don't have the permission 'uplanes.place' required to place a plane!");
				return;
			}
		}
		
		//Now place the car
		Block b = event.getClickedBlock();
		Location toSpawn = b.getLocation().add(0,1.5,0);
		Block in = toSpawn.getBlock();
		if(!in.isEmpty() && !in.isLiquid()){
			return;
		}
		
		Minecart ent = (Minecart) toSpawn.getWorld().spawnEntity(toSpawn, EntityType.MINECART);
		in = ent.getLocation().getBlock();
		Block n = in.getRelative(BlockFace.NORTH);   // The directions minecraft aligns the cart to
		Block w = in.getRelative(BlockFace.WEST);
		Block nw = in.getRelative(BlockFace.NORTH_WEST);
		Block ne = in.getRelative(BlockFace.NORTH_EAST);
		Block sw = in.getRelative(BlockFace.SOUTH_WEST);
		if((!in.isEmpty() && !in.isLiquid())
				|| (!n.isEmpty() && !n.isLiquid())
				|| (!w.isEmpty() && !w.isLiquid())
				|| (!ne.isEmpty() && !ne.isLiquid())
				|| (!nw.isEmpty() && !nw.isLiquid())
				|| (!sw.isEmpty() && !sw.isLiquid())){
			ent.remove();
			event.setUseItemInHand(Result.DENY);
			return;
		}
		
		inHand.setAmount(inHand.getAmount()-1);
		if(inHand.getAmount() < 1){
			player.setItemInHand(new ItemStack(Material.AIR)); //Remove from their hand
		}
		
		ent.setMetadata("ucars.ignore", new StatValue(true, main.plugin));
		ent.setMetadata("plane.health", new StatValue(plane.health, main.plugin));
		if(plane.stats.containsKey("plane.hover")){
			ent.setMetadata("plane.hover", new StatValue(true, main.plugin));
		}
		
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
			msg = msg.replaceAll(Pattern.quote("%damage%"), damage+"HP");
			health -= damage;
			if(health <= 0){
				die = true;
				health = 0;
			}
			msg = msg.replaceAll(Pattern.quote("%remainder%"), health+"HP");
			msg = msg.replaceAll(Pattern.quote("%cause%"), "Fist");
			((Player)attacker).sendMessage(main.colors.getInfo()+msg);
		}
		else{
			msg = msg.replaceAll(Pattern.quote("%damage%"), damage+"HP");
			health -= damage;
			if(health <= 0){
				die = true;
				health = 0;
			}
			msg = msg.replaceAll(Pattern.quote("%remainder%"), health+"HP");
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
		HumanEntity e = event.getWhoClicked();
		Player player = null;
		if(e instanceof Player){
			player = (Player) e;
		}
		Boolean hover = false;
		String name = ChatColor.stripColor(recipe.getItemMeta().getDisplayName());
		if(!name.equalsIgnoreCase("plane")){
			if(name.equalsIgnoreCase("hover plane")){
				hover = true;
			}
			else{
				return;
			}
		}
		Plane plane = PlaneGenerator.gen();
		if(hover){
			plane.name = "Hover Plane";
			plane.stats.put("plane.hover", new Stat("Hover", "Yes", main.plugin, true));
		}
		if(player != null){
			if(main.perms){
				if(!player.hasPermission("uplanes.craft")){
					player.sendMessage(main.colors.getError()+"You don't have the permission 'uplanes.craft' required to craft a plane!");
					event.setCurrentItem(new ItemStack(Material.AIR));
					//event.setCancelled(true);
					return;
				}
			}
		}
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
		@SuppressWarnings("unused")
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
