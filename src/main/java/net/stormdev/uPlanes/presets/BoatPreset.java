package net.stormdev.uPlanes.presets;

import net.stormdev.uPlanes.api.Boat;
import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BoatPreset extends uPlanesVehiclePresetBase<Boat> implements Serializable {

	private static final long serialVersionUID = 1L;
	private double mass = 1000; //In kg

	public BoatPreset(String presetID, double speed, String name, double health, double accelMod, double turnAmountPerTick, double cost, MaterialData displayBlock, double offset, float hitBoxX, float hitBoxZ){
		super(presetID,speed,name,health,accelMod,turnAmountPerTick,cost,displayBlock,offset,hitBoxX,hitBoxZ);
	}
	
	public Boat toBoat(){
		Boat p =  new Boat(getSpeed(), getName(), getHealth(), getAccelMod(), getTurnAmountPerTick(), false);
		p.setMass(this.mass);
		p.setCartDisplayBlock(this.getDisplayBlock());
		p.setDisplayOffset(this.getDisplayOffset());
		return p;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	@Override
	public Boat toVehicle() {
		return toBoat();
	}

	@Override
	public ItemStack toItemStack(){
		ItemStack stack;
		if(main.config.getBoolean("general.planes.renderAsModelledBlockWhenExist") && getDisplayBlock() != null){
			MaterialData md = this.getDisplayBlock();
			stack = new ItemStack(md.getItemType());
			stack.setData(md);
		}
		else {
			stack = new ItemStack(Material.MINECART);
		}
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		lore.add(ChatColor.GRAY+"boat");
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+mutliplier);
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health);
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+accelMod*10.0d);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+turnAmount*10.0d);
		meta.setDisplayName(Colors.colorise(name));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	@Override
	public String[] getSellLore() {
		String currency = main.config.getString("general.currencySign");
		List<String> lore = new ArrayList<String>();
		lore.add(main.colors.getTitle()+"[Price:] "+main.colors.getInfo()+currency+cost);
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+mutliplier);
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health);
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+accelMod*10.0d);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+turnAmount*10.0d);
		return lore.toArray(new String[]{});
	}
}
