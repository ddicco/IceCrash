package me.ddicco.icecrash;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class IceCrash extends WaterAbility implements AddonAbility{
	
	private double damage;
	private long cooldown;
	private Vector direction;
	private Location location;
	private ArrayList<TempBlock> previoustempblocks = new ArrayList<TempBlock>();
	private ArrayList<TempBlock> tempblocks = new ArrayList<TempBlock>();
	private Block blocklocation;
	private double speed;
	private double radius;

	public IceCrash(Player player, Location loc, ArrayList<TempBlock> tempBlocks) {
		super(player);
		// TODO Auto-generated constructor stub
		location = loc;
		setFields();
		
		previoustempblocks = tempBlocks;
		
		for(TempBlock tb : previoustempblocks) {
	        tb.revertBlock();
	    }
		previoustempblocks.clear();
		
		start();
	}

	private void setFields() {
		// TODO Auto-generated method stub
		speed = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Speed");
		damage = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Damage");
		cooldown = ConfigManager.getConfig().getLong(getConfigPath() + "Smash.Cooldown");
		location.add(0, 2, 0);
		direction = player.getEyeLocation().getDirection();
		radius = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Radius");
		
	}
	
	@Override
	public void remove() {
		
		for(TempBlock tb : tempblocks) {
	        tb.revertBlock();
	    }
		super.remove();
	}

	@Override
	public long getCooldown() {
		// TODO Auto-generated method stub
		return cooldown;
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return player.getLocation();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "IceCrash";
	}

	@Override
	public boolean isHarmlessAbility() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void setBlocks() {
		for(int y = 0; y < 2; y += 1) {
			blocklocation = location.clone().add(0, y, 0).getBlock();
			if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
				TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
				tempblocks.add(tblock);
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int y = -1; y < 2; y += 1) {
				blocklocation = location.clone().add(x, y, 0).getBlock();
				if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
		
		for(int z = -1; z < 2; z += 2) {
			for(int y = -1; y < 2; y += 1) {
				blocklocation = location.clone().add(0, y, z).getBlock();
				if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int z = -1; z < 2; z += 2) {
				blocklocation = location.clone().add(x, 0, z).getBlock();
				if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
	}
	
	@Override
	public void progress() {
		// TODO Auto-generated method stub
		
		if(!bPlayer.canBend(this) || player.isDead() || !player.isOnline()) {
			new WaterReturn(player, player.getLocation().getBlock());
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if(!tempblocks.isEmpty()) {
			for(TempBlock tb : tempblocks) {
		        tb.revertBlock();
		    }
			tempblocks.clear();
		}
		
		setBlocks();
		location.add(direction.multiply(speed));
		for(Entity e : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			if(e != null && e != player) {
				new WaterReturn(player, player.getLocation().add(0, 1, 0).getBlock());
				new IceCrashShards(player, location.getBlock());
				DamageHandler.damageEntity(e, damage, this);
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
		
		Location testblocklocation = location;
		final Block block = testblocklocation.getBlock();
		
		if (!block.getType().equals(Material.AIR)) {
			if (!block.getType().equals(Material.ICE)) {
				new IceCrashShards(player, location.getBlock());
				new WaterReturn(player, player.getLocation().add(0, 1, 0).getBlock());
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "ddicco";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "2.1";
	}
	
	public String getConfigPath() {
		return "ExtraAbilities.ddicco.Water.IceCrash.";
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new IceCrashListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Successfully enabled " + getName() + " by " + getAuthor() + " version " + getVersion());
		
		ConfigManager.getConfig().addDefault(getConfigPath() + "Passive.Cooldown", 10000);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Passive.Duration", 15000);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Cooldown", 10000);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Damage", 4);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Speed", 0.8);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Radius", 3);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.Amount", 10);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.Damage", 1);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.Radius", 2);
		
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void leftClickFunction() {
		// TODO Auto-generated method stub
		direction = player.getLocation().getDirection();
	}
	
	@Override
	public String getDescription() {
		return "Good waterbenders use their powers to turn the water into ice to form a structure, they can then shoot the structure that explodes into shards on impact";
	}
	
	@Override
	public String getInstructions() {
		return "Press shift looking at a waterbendable block to create an ice structure and click to shoot it, you can then click to redirect it wherever you're looking at";
	}

}
