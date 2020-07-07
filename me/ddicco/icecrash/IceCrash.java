package me.ddicco.icecrash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class IceCrash extends WaterAbility implements AddonAbility{
	
	private int numberofshards;
	private Random random = new Random();
	private double damage;
	private double sharddamage;
	private double shardinterval;
	private long cooldown;
	private Vector direction;
	private Location location;
	private Location startlocation;
	private ArrayList<TempBlock> previoustempblocks = new ArrayList<TempBlock>();
	private ArrayList<TempBlock> tempblocks = new ArrayList<TempBlock>();
	private Block blocklocation;
	private double speed;
	private double radius;
	private double range;
	private Set<FallingBlock> tracker;

	public IceCrash(Player player) {
		super(player);
		// TODO Auto-generated constructor stub
		
		setFields();
		
		previoustempblocks = IceCrashPrepare.getTempBlocks();
		
		for(TempBlock tb : previoustempblocks) {
	        tb.revertBlock();
	    }
		previoustempblocks.clear();
		
		start();
	}

	private void setFields() {
		// TODO Auto-generated method stub
		speed = 1;
		damage = 4;
		sharddamage = 1;
		shardinterval = 500;
		cooldown = 10000;
		startlocation = IceCrashPrepare.getCrashLocation();
		location = IceCrashPrepare.getCrashLocation();
		location.add(0, 2, 0);
		direction = player.getEyeLocation().getDirection();
		
		tracker = new HashSet<FallingBlock>();
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
				TempBlock tblock = new TempBlock(blocklocation, Material.ICE, (byte) 0);
				tempblocks.add(tblock);
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int y = -1; y < 2; y += 1) {
				blocklocation = location.clone().add(x, y, 0).getBlock();
				if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE, (byte) 0);
					tempblocks.add(tblock);
				}
			}
		}
		
		for(int z = -1; z < 2; z += 2) {
			for(int y = -1; y < 2; y += 1) {
				blocklocation = location.clone().add(0, y, z).getBlock();
				if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE, (byte) 0);
					tempblocks.add(tblock);
				}
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int z = -1; z < 2; z += 2) {
				blocklocation = location.clone().add(x, 0, z).getBlock();
				if(blocklocation.getType() == Material.AIR || blocklocation.getType() == Material.ICE) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE, (byte) 0);
					tempblocks.add(tblock);
				}
			}
		}
	}
	
	@Override
	public void progress() {
		// TODO Auto-generated method stub
		
		if(!bPlayer.canBend(this) || player.isDead() || !player.isOnline()) {
			bPlayer.addCooldown(this);
			player.sendMessage("you cant bend this");
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
		location.add(direction).multiply(speed);
		for(Entity e : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			if(e != null) {
				player.sendMessage("damage");
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
				player.sendMessage("" + block.getType() + " " + block.getLocation());
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
		return "1.0";
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new IceCrashListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Successfully enabled " + getName() + " by " + getAuthor() + " version " + getVersion());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void leftClickFunction() {
		// TODO Auto-generated method stub
		direction = player.getLocation().getDirection();
	}

}
