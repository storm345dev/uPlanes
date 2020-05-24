package net.stormdev.uPlanes.protocolMagic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_12_R1.Entity;
import net.stormdev.uPlanes.hover.CraftHoverCart;
import net.stormdev.uPlanes.hover.HoverCart;
import net.stormdev.uPlanes.hover.HoverCartEntity;
import net.stormdev.uPlanes.main.main;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolManipulator implements Listener {
    private ProtocolManager protocolManager;

    public ProtocolManipulator(ProtocolManager manager){
        this.protocolManager = manager;
        Bukkit.getServer().getPluginManager().registerEvents(this, main.plugin);
    }

    @EventHandler
    void logout(PlayerQuitEvent event){
        for(World w: Bukkit.getServer().getWorlds()) {
            for(net.minecraft.server.v1_12_R1.Entity e:new ArrayList<>(((CraftWorld)w).getHandle().entityList)){
                HoverCart hc = HoverCartEntity.getCart(e.getBukkitEntity());
                if (hc == null) {
                    continue;
                }
                ((HoverCartEntity)e).setKnowAboutFakeEntities(event.getPlayer(),false);
            }
        }
    }

    @EventHandler
    void entityDestroy(VehicleDestroyEvent event){
        if(event.getVehicle()instanceof HoverCart){
            HoverCartEntity hce = ((CraftHoverCart)event.getVehicle()).getHandle();
            sendFakeBoatAndArrowDie(hce);
        }
    }

    public void updateBoatRotationAngle(HoverCart entity){
        for(Player pl:Bukkit.getOnlinePlayers()){
            if(((CraftHoverCart)entity).getHandle().doesKnowAboutFakeEntities(pl)){
                updateBoatRotationAngle(pl,entity);
            }
        }
    }

    public void updateBoatRotationAngle(Player player, HoverCart entity){
        int entityID = ((CraftHoverCart)entity).getHandle().getFakeBoat().getId();
        PacketContainer rotatePacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        rotatePacket.getIntegers().write(0,entityID);
        double yawDegrees = ((CraftHoverCart)entity).getHandle().getBukkitYaw()+entity.getBoatRotationOffsetDegrees();
        double pitchRad = entity.getHeadPose().getX();
        int yaw = getCompressedAngle(yawDegrees*(Math.PI/180));
        int pitch = getCompressedAngle(pitchRad);
        rotatePacket.getBytes().write(0,(byte)yaw);
        rotatePacket.getBytes().write(1,(byte)pitch);
        try {
            this.protocolManager.sendServerPacket(player,rotatePacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected int getCompressedAngle(double angleRad){
        double compAngle = angleRad* (256/(2*Math.PI));
        while(compAngle < 0){
            compAngle = 256+compAngle;
        }
        while(compAngle > 256){
            compAngle = compAngle-256;
        }
        return ((int)compAngle);
    }

    protected void sendFakeBoatAndArrowDie(HoverCartEntity hce){
        if(!hce.hasFakeBoat()){
            return;
        }
        for(Player pl:Bukkit.getOnlinePlayers()){
            if(hce.doesKnowAboutFakeEntities(pl)){
                sendFakeBoatAndArrowDie(hce,pl);
            }
        }
    }

    protected void sendFakeBoatAndArrowDie(HoverCartEntity hce, Player pl){
        sendFakeEntityRemove(pl,hce.getFakeBoat().getId());
        sendFakeEntityRemove(pl,hce.getFakeArrow().getId());
        sendFakeEntityRemove(pl,hce.getFakeArrow2().getId());
    }

    protected void sendFakeEntityRemove(Player pl, int... entityid){
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0,entityid);
        try {
            this.protocolManager.sendServerPacket(pl,packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void sendFakeBoatAndArrowSpawns(HoverCartEntity hce, HoverCart hc, Player player){
        if(!hce.hasFakeBoat()){
            return;
        }
        Entity toSend = hce.getFakeBoat();
        int entityType = 1; //1 is a boat, 78 is armor stand
        //Write the fake spawn packet
        double yawRad = (hce.getBukkitYaw()+hc.getBoatRotationOffsetDegrees())*(Math.PI/180);
        double pitchRad = hc.getHeadPose().getX();
        if(!hce.doesKnowAboutFakeEntities(player)) {
            sendFakeEntitySpawn(player, hce.getFakeBoat(), hc.getLocation(), yawRad, pitchRad, 1); //Type 1 is BOAT, 78 is armor stand, 60 is arrow
            sendFakeEntitySpawn(player, hce.getFakeArrow(), hc.getLocation(), yawRad, pitchRad, 60); //60 is arrow, 71 is item frame
            sendFakeEntitySpawn(player, hce.getFakeArrow2(), hc.getLocation(), yawRad, pitchRad, 60);sendFakeEntityPassengers(player,hce,hce.getFakeBoat().getId());
            sendFakeEntityPassengers(player,hce.getFakeBoat(),hce.getFakeArrow().getId(),hce.getFakeArrow2().getId());
            hce.setKnowAboutFakeEntities(player,true);
        }
    }

    protected void sendFakeEntityPassengers(Player player, Entity vehicle, int... passengers){
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0,vehicle.getId());
        packet.getIntegerArrays().write(0,passengers);

        try {
            this.protocolManager.sendServerPacket(player,packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void sendFakeEntitySpawn(Player player, Entity toSend, Location l, double yawRad, double pitchRad,int entityType){
        //Write the fake spawn packet
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getIntegers().write(0,toSend.getId()); //Entity ID
        packet.getUUIDs().write(0, toSend.getUniqueID());
        packet.getIntegers().write(1,0); //Velocity
        packet.getIntegers().write(2,0); //Velocity
        packet.getIntegers().write(3,0); //Velocity
        packet.getDoubles().write(0,l.getX());
        packet.getDoubles().write(1,l.getY());
        packet.getDoubles().write(2,l.getZ());
        packet.getIntegers().write(4,getCompressedAngle(pitchRad)); //Pitch
        packet.getIntegers().write(5,getCompressedAngle(yawRad)); //Pitch
        packet.getIntegers().write(6,entityType); //Type??
        packet.getIntegers().write(7,0); //Data

        try {
            this.protocolManager.sendServerPacket(player,packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void registerManipulations(){
        try { //These 2 translate our custom entity for rendering display blocks to look in the right place!
            ((ProtocolManager) this.protocolManager).addPacketListener(new PacketAdapter(main.plugin, PacketType.Play.Server.ENTITY_TELEPORT){
                @Override
                public void onPacketSending(PacketEvent event){
                    int entityId = event.getPacket().getIntegers().read(0);

                    HoverCart hce = null;
                    HoverCartEntity nmsEntity = null;
                    for(World w: Bukkit.getServer().getWorlds()){
                        for(net.minecraft.server.v1_12_R1.Entity e:((CraftWorld)w).getHandle().entityList){
                            if(entityId == e.getId()){
                                HoverCart hc = HoverCartEntity.getCart(e.getBukkitEntity());
                                if(hc == null){
                                    return;
                                }
                                nmsEntity = (HoverCartEntity) e;
                                hce = hc;
                            }
                        }
                    }
                    if(hce == null){
                        return;
                    }

                    double y = hce.getLocation().getY()/*(double)event.getPacket().getIntegers().read(2) / 32.0*/;
                    /*Block b = hce.getLocation().getBlock();*/
                    y+= hce.getDisplayOffset()-0.9;
                    event.getPacket().getDoubles().write(1, y);
                    sendFakeBoatAndArrowSpawns(nmsEntity,hce,event.getPlayer());
                }

            });
            this.protocolManager.addPacketListener(new PacketAdapter(main.plugin,PacketType.Play.Client.USE_ENTITY) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    int entityId = event.getPacket().getIntegers().read(0);
                    //Translate interacting with fake boat or arrows into interacting with real armor stand
                    for(World w:Bukkit.getServer().getWorlds()){
                        for(net.minecraft.server.v1_12_R1.Entity e:new ArrayList<>(((CraftWorld)w).getHandle().entityList)){
                            if(e instanceof HoverCartEntity){
                                if(((HoverCartEntity) e).hasFakeBoat()){
                                    if(entityId == ((HoverCartEntity) e).getFakeBoat().getId()
                                       || entityId == ((HoverCartEntity) e).getFakeArrow().getId()
                                        || entityId == ((HoverCartEntity) e).getFakeArrow2().getId()){
                                        event.getPacket().getIntegers().write(0,e.getId());
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            });
            this.protocolManager.addPacketListener(new PacketAdapter(main.plugin,PacketType.Play.Server.MOUNT) {
                @Override
                public void onPacketSending(PacketEvent event){
                    //Make sure passengers are set correct
                    int entityId = event.getPacket().getIntegers().read(0);
                    //Bukkit.broadcastMessage("send passengers for entity "+entityId+" to "+event.getPlayer().getName());

                    HoverCart hce = null;
                    HoverCartEntity nmsEntity = null;
                    for(World w:Bukkit.getServer().getWorlds()){
                        for(net.minecraft.server.v1_12_R1.Entity e:new ArrayList<>(((CraftWorld)w).getHandle().entityList)){
                            if(entityId == e.getId()){
                                //Bukkit.broadcastMessage("(name: "+e.getName()+")");
                                HoverCart hc = HoverCartEntity.getCart(e.getBukkitEntity());
                                if(hc == null){
                                    return;
                                }
                                nmsEntity = (HoverCartEntity) e;
                                hce = hc;
                            }
                        }
                    }
                    if(hce == null){
                        return;
                    }
                    if(!nmsEntity.hasFakeBoat()){
                        return;
                    }
                    int[] passengers = event.getPacket().getIntegerArrays().read(0);
                    //Needs passenger list correcting
                    int[] fixedPassengers = new int[]{nmsEntity.getFakeBoat().getId()};
                    event.getPacket().getIntegerArrays().write(0,fixedPassengers);
                    List<org.bukkit.entity.Entity> correctPassengers = hce.getPassengers();
                    passengers = new int[correctPassengers.size()];
                    for (int i=0;i<correctPassengers.size();i++){
                        passengers[i] = ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity)correctPassengers.get(i)).getHandle().getId();
                    }

                    final int[] passenger1 = passengers.length > 0 ? new int[]{passengers[0]} : new int[0];
                    final int[] passenger2 = passengers.length > 1 ? ArrayUtils.subarray(passengers,1,passengers.length) : new int[0];

                    final HoverCartEntity nm = nmsEntity;
                    //Bukkit.broadcastMessage("Sending mounted passengers to "+event.getPlayer().getName());
                    //sendFakeEntityPassengers(event.getPlayer(),nm.getFakeArrow(),passenger1);
                    sendFakeEntityPassengers(event.getPlayer(),nm.getFakeArrow2(),passenger2);
                   Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable() {
                        @Override
                        public void run() {
                            if(!nm.isAlive()){
                                return;
                            }
                            //Bukkit.broadcastMessage("Sending mounted passengers to "+event.getPlayer().getName());
                            sendFakeEntityPassengers(event.getPlayer(),nm.getFakeArrow(),passenger1);
                            //sendFakeEntityPassengers(event.getPlayer(),nm.getFakeArrow2(),passenger2);
                        }
                    },1L);
                }
            });
            ((ProtocolManager) this.protocolManager).addPacketListener(new PacketAdapter(main.plugin, PacketType.Play.Server.SPAWN_ENTITY){
                @Override
                public void onPacketSending(PacketEvent event){
                    int entityId = event.getPacket().getIntegers().read(0);

                    HoverCart hce = null;
                    HoverCartEntity nmsEntity = null;
                    for(World w:Bukkit.getServer().getWorlds()){
                        for(net.minecraft.server.v1_12_R1.Entity e:new ArrayList<>(((CraftWorld)w).getHandle().entityList)){
                            if(entityId == e.getId()){
                                HoverCart hc = HoverCartEntity.getCart(e.getBukkitEntity());
                                if(hc == null){
                                    return;
                                }
                                nmsEntity = (HoverCartEntity) e;
                                hce = hc;
                            }
                        }
                    }
                    if(hce == null){
                        return;
                    }

                    double y = (double)event.getPacket().getDoubles().read(2) /*/ 32.0*/;
                    Block b = hce.getLocation().getBlock();
                    y+= hce.getDisplayOffset()-0.9;
                    event.getPacket().getDoubles().write(2, /*(int) (*/y/* * 32)*/);
                    sendFakeBoatAndArrowSpawns(nmsEntity,hce,event.getPlayer());
                    /*System.out.println("Packet integers:");
                    for(Integer i : event.getPacket().getIntegers().getValues()){
                        System.out.println(i);
                    }
                    System.out.println("Packet bytes:");
                    for(byte i : event.getPacket().getBytes().getValues()){
                        System.out.println(i);
                    }
                    System.out.println("Packet doubles:");
                    for(Double i : event.getPacket().getDoubles().getValues()){
                        System.out.println(i);
                    }*/
                }

            });

				/*((ProtocolManager) this.protocolManager).addPacketListener(new PacketAdapter(this, PacketType.Play.Server.SPAWN_ENTITY_LIVING){
					@Override
					public void onPacketSending(PacketEvent event){
						int entityId = event.getPacket().getIntegers().read(0);

						HoverCart hce = null;
						for(World w:Bukkit.getServer().getWorlds()){
							for(net.minecraft.server.v1_8_R3.Entity e:((CraftWorld)w).getHandle().entityList){
								if(entityId == e.getId()){
									HoverCart hc = HoverCartEntity.getCart(e.getBukkitEntity());
									if(hc == null){
										return;
									}
									hce = hc;
								}
							}
						}
						if(hce == null){
							return;
						}

						double y = (double)event.getPacket().getIntegers().read(2) / 32.0;
						Block b = hce.getLocation().getBlock();
						if(b.isEmpty() || b.isLiquid()){
							y+= hce.getDisplayOffset()-0.9;
						}
						event.getPacket().getIntegers().write(2, (int) (y * 32));
					}

		});*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
