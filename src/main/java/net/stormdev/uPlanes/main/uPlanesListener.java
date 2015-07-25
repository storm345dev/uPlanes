package net.stormdev.uPlanes.main;

import net.stormdev.uPlanes.api.*;
import net.stormdev.uPlanes.items.ItemPlaneValidation;
import net.stormdev.uPlanes.presets.PlanePreset;
import net.stormdev.uPlanes.presets.PresetManager;
import net.stormdev.uPlanes.utils.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class uPlanesListener implements Listener {
	private main plugin;
	
	private double punchDamage;
	public static double heightLimit;
	private boolean perms;
	private String perm;
	private boolean safeExit;
	private boolean fuel;
	private boolean crashing = false;
	private String fuelBypassPerm;
	
	public uPlanesListener(main instance){
		this.plugin = instance;
		fuel = main.config.getBoolean("general.planes.fuel.enable");
		fuelBypassPerm = main.config.getString("general.planes.fuel.bypassPerm");
		punchDamage = main.config.getDouble("general.planes.punchDamage");
		heightLimit = main.config.getDouble("general.planes.heightLimit");
		perms = main.config.getBoolean("general.planes.perms");
		perm = main.config.getString("general.planes.flyPerm");
		safeExit = main.config.getBoolean("general.planes.safeExit");
		crashing = main.config.getBoolean("general.planes.enableCrashing");
	}
	
	@EventHandler
	void entityCrash(VehicleEntityCollisionEvent event){
		if(!crashing){
			return;
		}
		Vehicle veh = event.getVehicle();
		if(veh == null || !(veh instanceof Minecart)){
			return;
		}
		Minecart m = (Minecart) veh;
		Plane plane = getPlane(m);
		if(plane == null){ //Not a plane
			return;
		}
		
		Entity collided = event.getEntity();
		if(collided == null || (collided instanceof Item) || (collided instanceof ItemFrame)){
			return;
		}
		if(m.getPassenger() == null || (m.getPassenger() != null && collided.equals(m.getPassenger()))){
			return;
		}
		double speedSq = m.getVelocity().lengthSquared();
		if(speedSq < 0.2){ //Going v. slow
			return;
		}
		
		double damage = 20.0 * speedSq;
		damage = Math.round(damage*10.0d)/10.0d;
		if(damage < 1){
			damage = 1;
		}
		if(damage > 15){
			damage = 15;
		}
		if(collided instanceof Damageable){
			if(collided instanceof Player){
				((Player)collided).sendMessage(ChatColor.RED+"You collided with a plane!");
			}
			if(!plane.isHover()){
				collided.setVelocity(m.getVelocity().clone().setY(0.5));
			}
			((Damageable) collided).damage(damage, m);
		}
		uPlanesAPI.getPlaneManager().damagePlane(m, plane, damage, "Crash");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void despawn(EntityDeathEvent event){
		if(!plugin.planeManager.isPlaneInUse(event.getEntity().getUniqueId())){
			return;
		}
		plugin.planeManager.noLongerPlaced(event.getEntity().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	void projectileDamage(EntityDamageByEntityEvent event){
		if(event.isCancelled()){
			return;
		}
		Entity dmged = event.getEntity();
		Entity dmger = event.getDamager();
		if(dmger instanceof Player){
			Player player = (Player) dmger;
			if(player.getVehicle() != null && player.getVehicle().equals(dmged)){
				event.setDamage(-0.5);
				event.setCancelled(true);
			}
			return;
		}
		
		if(dmger instanceof Projectile){
			Projectile proj = (Projectile) dmger;
			@SuppressWarnings("deprecation")
			ProjectileSource source = proj.getShooter();
			if(source instanceof Player){
				Player player = (Player) source;
				if(player.getVehicle() != null && player.getVehicle().equals(dmged)){
					event.setDamage(-0.5);
					event.setCancelled(true);
				}
			}
		}
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
			event.setDamage(-2.5);
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
		        boolean installed = plugin.setupEconomy();
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
			//A trade sign for planes
			//Create a trade inventory
			Player player = (Player) event.getPlayer(); //Get the player from the event
			event.getView().close();
			event.setCancelled(true); //Cancel the event
			plugin.planeShop.open(player);
			//Made the trade booth
			return;
		}
	
	 @EventHandler (priority = EventPriority.HIGHEST) //Call late
	    void vehicleExit(VehicleExitEvent event){
		 	if(event.isCancelled()){
		 		return;
		 	}
	    	//Safe exit
		 	Vehicle veh = event.getVehicle();
		 	final Entity exited = event.getExited();
		 	if(!(exited instanceof Player) || !(veh instanceof Minecart)){
	        	return;
	        }
	        if(!plugin.planeManager.isPlaneInUse(veh.getUniqueId())){
	        	return;
	        }
	        veh.removeMetadata(AccelerationManager.ACCEL_META, main.plugin);
	    	if(!safeExit){
	    		/*if(Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null){
	    			final Entity exited = event.getExited();
	    	        if(!(exited instanceof Player) || !(veh instanceof Minecart)){
	    	        	return;
	    	        }
	    	        if(!plugin.planeManager.isPlaneInUse(veh.getUniqueId())){
	    	        	return;
	    	        }
	    	        Player player = (Player) exited;
	    			AntiCheatAPI.unexemptPlayer(player, CheckType.FLY);
			 	}*/
	    		return; //Don't bother
	    	}
	    	final Location loc = veh.getLocation();
	        Block b = loc.getBlock();
	        Player player = (Player) exited;
	        if(exited.isDead() || player.getHealth() < 1){
	        	return; //Allow them to exit
	        }
	        
	        //Handle the exit ourselves
	        loc.setYaw(player.getLocation().getYaw());
	        loc.setPitch(player.getLocation().getPitch());
	        final Vector vel = veh.getVelocity();
	    	main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){

				public void run() {
					exited.teleport(loc.add(0, 0.5, 0));
					exited.setVelocity(vel);
					return;
				}}, 2l); //Teleport back to car loc after exit
	        /*
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
						if(Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null){
			    	        Player player = (Player) exited;
			    			AntiCheatAPI.unexemptPlayer(player, CheckType.FLY);
					 	}
						return;
					}}, 2l); //Teleport back to car loc after exit
	        }*/
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
		if(passenger instanceof Player){
			Player player = (Player) passenger;
			/*if(Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null){
				AntiCheatAPI.exemptPlayer(player, CheckType.FLY);
			}*/
		}
		if(veh.hasMetadata("plane.destination")){
			//Autopilot
			List<MetadataValue> metas = veh.getMetadata("plane.destination");
			Location dest = (Location) metas.get(0).value();
			FlightControl.route(dest, loc, veh);
			return;
		}
		
		Plane pln = getPlane(car);
		if(pln == null){
			return;
		}
		
		if(!pln.isHover()){
			CartOrientationUtil.setPitch(car, pln.getCurrentPitch());
			if(veh.getVelocity().getX() > 0 || veh.getVelocity().getZ() > 0){
				float cYaw = (float) Math.toDegrees(Math.atan2(veh.getVelocity().getX() , -veh.getVelocity().getZ())) - 90;
				CartOrientationUtil.setYaw(car, cYaw);
			}
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
		Plane plane = event.getPlane();
		
		if(plane == null){ //Not a plane, just a Minecart
			return;
		}
		
		if(plane.isHover()){
			vehicle.setMetadata("plane.hover", new StatValue(true, main.plugin));
			if(main.perms){
				if(!player.hasPermission("uplanes.hoverplane")){
					player.sendMessage(main.colors.getError()+"You don't have the permission 'uplanes.hoverplane' required to fly a plane!");
					return;
				}
			}
		} else if(crashing){
			//Check if they crashed xD
			Block current = cart.getLocation().getBlock();
			Block b = null;
			if(current.isEmpty() || current.isLiquid()){
				Location nextHorizontal = cart.getLocation().add(new Vector(cart.getVelocity().getX(), 0, cart.getVelocity().getZ()));
				b = nextHorizontal.getBlock();
				if(!b.isEmpty() && !b.isLiquid() && b.getType().isSolid() && !b.getType().equals(Material.CARPET) && !b.getType().equals(Material.BARRIER)){ //Crashed into something
					b = b.getRelative(BlockFace.UP);
					String bt = b.getType().name().toLowerCase();
					if((!b.isEmpty() && !b.isLiquid() && b.getType().isSolid())
							|| (!bt.contains("step")
									&& !bt.contains("carpet")
									&& (!bt.contains("grass") && !b.getType().equals(Material.GRASS))
									)){ //Crashed into definitely a wall or something bad
						double speedSq = cart.getVelocity().lengthSquared();
						if(speedSq > 0.2){ //Going v. slow
							double damage = 100.0 * speedSq;
							damage = Math.round(damage*10.0d)/10.0d;
							if(damage < 1){
								damage = 1;
							}
							if(damage > 100){
								damage = 100;
							}
							
							PrePlaneCrashEvent evt = new PrePlaneCrashEvent(cart, player, event.getAcceleration(), plane, damage);
							Bukkit.getPluginManager().callEvent(evt);
							
							if(!evt.isCancelled() && evt.getDamage() > 0){
								uPlanesAPI.getPlaneManager().damagePlane(cart, plane, evt.getDamage(), "Crash");
							}
						}
					}
				}
			}
			
			
			//TODO If crash into ground with significantly less x/z speed than needed for flight
			
			
			
			/*if(cart.getVelocity().getY() < 0.5 && new Vector(cart.getVelocity().getX(), 0, cart.getVelocity().getZ()).lengthSquared() < 0.5){ //Going down fast enough to do some damage
				Location nextVertical = cart.getLocation().add(0, cart.getVelocity().getY(), 0);
				b = nextVertical.getBlock();
				if(!b.isEmpty() && !b.isLiquid() && b.getType().isSolid()){ //Crashed into something
					double damage = 20.0 * cart.getVelocity().getY();
					damage = Math.round(damage*10.0d)/10.0d;
					if(damage < 1){
						damage = 1;
					}
					if(damage > 15){
						damage = 15;
					}
					
					uPlanesAPI.getPlaneManager().damagePlane(cart, plane, damage, "Rough Landing");
				}
			}*/
		}
		
		if (fuel
				&& !player.hasPermission(fuelBypassPerm)) {
			double fuel = 0;
			if (main.fuel.containsKey(player.getName())) {
				fuel = main.fuel.get(player.getName());
			}
			if (fuel < 0.1) {
				player.sendMessage(main.colors.getError()
						+ Lang.get("lang.fuel.empty"));
				return;
			}
			int amount = 0 + (int) (Math.random() * 250);
			if (amount == 10) {
				fuel = fuel - 0.1;
				fuel = (double) Math.round(fuel * 10) / 10;
				main.fuel.put(player.getName(), fuel);
			}
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
		double multiplier = plane.getSpeed();
		
		travel.multiply(multiplier);
		
	    /*float vertAmount = (float) (0.01 * multiplier * (plane.isHover() ? 1:event.getAcceleration()));
	    
	    switch(press){
		case A: 
			y = vertAmount; break; //Go up
		case D: 
			y = -vertAmount; break; //Go down
		default:
			break;
		}*/
		
		if(loc.getY() >= heightLimit){
			travel.setY(-1);
			float pitch = plane.getCurrentPitch();
			pitch += 20;
			if(pitch > 90){
				pitch = 90;
			}
			plane.setCurrentPitch(pitch);
			//Send message it's too high
			player.sendMessage(main.colors.getError()+Lang.get("general.heightLimit"));
		}
		
		if((new Vector(travel.getX(), 0, travel.getZ()).lengthSquared() < 0.75 && event.getAcceleration() < 0.75) && !plane.isHover() && travel.getY() < 0.1){
			travel.setY(cart.getVelocity().getY() * 1.015); //Need more speed to maintain flight!
			/*float pitch = plane.getCurrentPitch();
			pitch += 1;
			if(pitch > 90){
				pitch = 90;
			}
			plane.setCurrentPitch(pitch);*/
		}
		
		if(crashing && !plane.isHover()){
			if((travel.getY() < -0.3 && plane.getCurrentPitch() > 25) || ((travel.getY() < -0.3 && new Vector(travel.getX(), 0, travel.getZ()).lengthSquared() < 0.8 && event.getAcceleration() < 0.8))){
				Location nextVertical = cart.getLocation().add(0, cart.getVelocity().getY(), 0);
				Block b = nextVertical.getBlock();
				if(!b.isEmpty() && !b.isLiquid() && b.getType().isSolid()){ //Crashed into something
					double damage = 110.0 * Math.abs(cart.getVelocity().getY());
					damage = Math.round(damage*10.0d)/10.0d;
					if(damage < 1){
						damage = 1;
					}
					if(damage > 200){
						damage = 200;
					}
					
					PrePlaneRoughLandingEvent evt = new PrePlaneRoughLandingEvent(cart, player, event.getAcceleration(), plane, damage);
					Bukkit.getPluginManager().callEvent(evt);
					
					if(!evt.isCancelled() && evt.getDamage() > 0){
						player.damage(20*(damage/200.0));
						uPlanesAPI.getPlaneManager().damagePlane(cart, plane, evt.getDamage(), "Rough Landing");
					}
				}
			}
		}
		
		if(plane.isHover() && !event.wasKeypressed(Keypress.A) && !event.wasKeypressed(Keypress.D)){
			travel.setY(cart.getVelocity().getY());
		}
		
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
		
		PlanePreset pp = plane.getPreset();
		if(pp != null && pp.hasDisplayBlock()){
			ent.setDisplayBlock(pp.getDisplayBlock());
			ent.setDisplayBlockOffset(pp.getDisplayOffset());
		}
		
		float yaw = player.getLocation().getYaw()+90;
		if(yaw < 0){
			yaw = 360 + yaw;
		}
		else if(yaw >= 360){
			yaw = yaw - 360;
		}
		CartOrientationUtil.setYaw(ent, yaw);
		
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
		ent.setMetadata("plane.health", new StatValue(plane.getHealth(), main.plugin));
		if(plane.isHover()){
			ent.setMetadata("plane.hover", new StatValue(true, main.plugin));
		}
		
		plane.setId(ent.getUniqueId());
		
		main.plugin.planeManager.nowPlaced(plane);
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
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void vehicleDamage(VehicleDamageEvent event){
		if(event.isCancelled()){
			return;
		}
		Vehicle veh = event.getVehicle();
		if(!(veh instanceof Minecart)){
			return;
		}
		Minecart m = (Minecart) veh;
		Plane plane = getPlane(m);
		if(plane == null){
			return;
		}
		
		Entity dmger = event.getAttacker();
		if(dmger instanceof Projectile){
			Projectile proj = (Projectile) dmger;
			@SuppressWarnings("deprecation")
			ProjectileSource source = proj.getShooter();
			if(source instanceof Player){
				Player player = (Player) source;
				if(player.getVehicle() != null && player.getVehicle().equals(m)){
					event.setCancelled(true); //They are shooting their own vehicle by accident...
					return;
				}
			}
		}
		else if(dmger instanceof Player){
			Player player = (Player) dmger;
			if(player.getVehicle() != null && player.getVehicle().equals(m)){
				event.setCancelled(true); //They are shooting their own vehicle by accident...
				return;
			}
		}
		
		double health = plane.getHealth();
		if(m.hasMetadata("plane.health")){
			List<MetadataValue> ms = m.getMetadata("plane.health");
			health = (double) ms.get(0).value();
		}
		double damage = event.getDamage();
		String msg = Lang.get("general.damage.msg");
		Entity attacker = dmger;
		boolean die = false;
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
		
		event.setDamage(-5.5);
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
		boolean hover = false;
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
			plane.setName("Hover Plane");
			plane.setHover(true);
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
		return;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	void planeUpgradeAnvil(final InventoryClickEvent event){
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
		
		if(event.isShiftClick() && (view.getBottomInventory() instanceof AnvilInventory || view.getTopInventory() instanceof AnvilInventory)){
			event.setCancelled(true); //Disables shift clicking stuff into anvils since it doesn't update properly with the upgrading
			return;
		}
		
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
		boolean update = true;
		boolean save = false;
		boolean pickup = false;
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
			if(!pickup && i.getItem(1) != null){ //Put down item and already an upgrade in slot 2...
				ItemStack held = event.getCursor();
				Plane plane = ItemPlaneValidation.getPlane(held);
				if(plane == null){
					return;
				}
				//They just placed the plane; revalidate upgrades next tick
				Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						if(i.getItem(0) != null){
							planeUpgradeAnvil(event);
						}
						return;
					}}, 1l);
			}
			return;
		}
		if(!(item.getType() == Material.MINECART) || 
				item.getItemMeta().getLore().size() < 2){
			return; //Not a plane
		}
		//Anvil contains a car in first slot.
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		Plane plane = ItemPlaneValidation.getPlane(item);
		if(plane == null){
			return;
		}
        if(save && slotNumber ==2){
        	if(!PresetManager.usePresets || !PresetManager.disableItemRenaming){
        		//They are renaming it
            	ItemStack result = event.getCurrentItem();
            	String name = ChatColor.stripColor(result.getItemMeta().getDisplayName());
            	plane.setName(name);
            	player.sendMessage(main.colors.getSuccess()+"+"+main.colors.getInfo()+" Renamed plane to: '"+name+"'");
            	return;
        	}
        	//Display item naming
        	event.getCurrentItem().getItemMeta().setDisplayName(plane.getName());
        	event.setCancelled(true);
			return;
		}
		InventoryAction a = event.getAction();
		ItemStack upgrade = null;
		boolean set = false;
		final ItemStack up = upgrade;
		final boolean updat = update;
		final boolean sav = save;
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
					UpgradeManager.applyUpgrades(upgrade, ca, updat, sav, player, i, ca.getId());
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
		UpgradeManager.applyUpgrades(upgrade, plane, update, save, player, i, plane.getId());
		return;
	}
	
	public void killPlane(Vehicle vehicle, Plane plane){
		//Kill plane
		UUID id = vehicle.getUniqueId();
		plugin.planeManager.noLongerPlaced(id);
		final Location loc = vehicle.getLocation();
		Entity top = vehicle.getPassenger();
		if(top instanceof Player){
			top.eject();
			top.setVelocity(vehicle.getVelocity());
			if(safeExit){
				final Player pl = (Player) top;
				final Vector vel = vehicle.getVelocity();
				main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){

					public void run() {
						pl.teleport(loc.clone().add(0, 0.5, 0));
						pl.setVelocity(vel);
						/*if(Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null){
			    			AntiCheatAPI.unexemptPlayer(pl, CheckType.FLY);
					 	}*/
						return;
					}}, 2l); //Teleport back to car loc after exit
			}
		}
		vehicle.eject();
		vehicle.remove();
		if(!plane.isWrittenOff()){
			loc.getWorld().dropItemNaturally(loc, new ItemStack(PlaneItemMethods.getItem(plane)));
		}
		//Remove plane and get back item
		return;
	}
	
	public Plane getPlane(Minecart m){
		return plugin.planeManager.getPlane(m.getUniqueId());
	}
	
	public boolean isAPlane(Minecart m){
		return plugin.planeManager.isPlaneInUse(m.getUniqueId());
	}
}
