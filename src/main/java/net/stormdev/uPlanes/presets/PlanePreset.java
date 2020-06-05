package net.stormdev.uPlanes.presets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import net.stormdev.uPlanes.api.Plane;
import net.stormdev.uPlanes.main.main;
import net.stormdev.uPlanes.utils.Colors;

/**
 * Simple serializable format for plane data
 * 
 * Do not manipulate this directly if avoidable, use the API
 *
 */
public class PlanePreset extends uPlanesVehiclePresetBase<Plane> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private boolean hover = false;
	private boolean canPlaneHoverMidair = false;

	public PlanePreset(String presetID, double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean hover, double cost, MaterialData displayBlock, double offset, float hitBoxX, float hitBoxZ){
		this(presetID, speed, name, health, accelMod, turnAmountPerTick, hover, cost, displayBlock, offset, hitBoxX, hitBoxZ, hover);
	}

	public PlanePreset(String presetID, double speed, String name, double health, double accelMod, double turnAmountPerTick, boolean hover, double cost, MaterialData displayBlock, double offset, float hitBoxX, float hitBoxZ, boolean canPlaneHoverMidair){
		super(presetID,speed,name,health,accelMod,turnAmountPerTick,cost,displayBlock,offset,hitBoxX,hitBoxZ);
		this.hover = hover;
		this.canPlaneHoverMidair = canPlaneHoverMidair;
	}

	public void setCanPlaneHoverMidair(boolean b){
		this.canPlaneHoverMidair = b;
	}

	public boolean canPlaneHoverMidair(){
		return this.canPlaneHoverMidair;
	}
	
	public Plane toPlane(){
		Plane p =  new Plane(getSpeed(), getName(), getHealth(), getAccelMod(), getTurnAmountPerTick(), isHover(), canPlaneHoverMidair());
		p.setCartDisplayBlock(this.getDisplayBlock());
		p.setDisplayOffset(this.getDisplayOffset());
		return p;
	}

	@Override
	public Plane toVehicle() {
		return toPlane();
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
		lore.add(ChatColor.GRAY+(hover?"helicopter":"plane"));
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+mutliplier);
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health);
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+accelMod*10.0d);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+turnAmount*10.0d);
		if(hover){
			lore.add(main.colors.getTitle()+"[Hover:] "+main.colors.getInfo()+getHandleString(hover||canPlaneHoverMidair));
		}
		meta.setDisplayName(Colors.colorise(name));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
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
		if(hover){
			lore.add(main.colors.getTitle()+"[Hover:] "+main.colors.getInfo()+getHandleString(hover));
		}
		return lore.toArray(new String[]{});
	}
}
