package me.ddicco.icecrash;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class IceCrashPrepare extends WaterAbility implements AddonAbility {

	private long cooldown;
	public static Location blocklocation;
	private Location location;
	private static ArrayList<TempBlock> tempblocks = new ArrayList<TempBlock>();
	private static Block block;
	private Block newblocklocation;
	private long duration;
	private long starttime;
	private boolean setup = false;
	private int loopcounter = 0;

	public IceCrashPrepare(Player player) {
		super(player);
		// TODO Auto-generated constructor stub
		
		if(!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		setFields();
		
		start();
	}
	
	public static ArrayList<TempBlock> getTempBlocks() {
		return tempblocks;
	}
	
	private void setFields() {
		// TODO Auto-generated method stub
		block = player.getLocation().add(0, 2, 0).getBlock();
		location = player.getLocation();
		cooldown = 1000;
		duration = 15000;
		starttime = System.currentTimeMillis();
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

	@Override
	public void progress() {
		// TODO Auto-generated method stub
		
		if(!bPlayer.canBend(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if(player.isDead() || !player.isOnline()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if(loopcounter == 0) {
			TempBlock watertblock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0);
			tempblocks.add(watertblock);
		}
		
		if(loopcounter == 1) {
			TempBlock watertblock2 = new TempBlock(block.getLocation().add(0, 1, 0).getBlock(), Material.STATIONARY_WATER, (byte) 0);
			tempblocks.add(watertblock2);
		}
		
		if(loopcounter == 2) {
			if(setup == false) {
				for(int y = 4; y < 6; y += 1) {
					newblocklocation = location.clone().add(0, y, 0).getBlock();
					TempBlock tblock = new TempBlock(newblocklocation, Material.ICE, (byte) 0);
					tempblocks.add(tblock);
				}
				
				for(int x = -1; x < 2; x += 2) {
					for(int y = 3; y < 6; y += 1) {
						newblocklocation = location.clone().add(x, y, 0).getBlock();
						TempBlock tblock = new TempBlock(newblocklocation, Material.ICE, (byte) 0);
						tempblocks.add(tblock);
					}
				}
				
				for(int z = -1; z < 2; z += 2) {
					for(int y = 3; y < 6; y += 1) {
						newblocklocation = location.clone().add(0, y, z).getBlock();
						TempBlock tblock = new TempBlock(newblocklocation, Material.ICE, (byte) 0);
						tempblocks.add(tblock);
					}
				}
				
				for(int x = -1; x < 2; x += 2) {
					for(int z = -1; z < 2; z += 2) {
						newblocklocation = location.clone().add(x, 4, z).getBlock();
						TempBlock tblock = new TempBlock(newblocklocation, Material.ICE, (byte) 0);
						tempblocks.add(tblock);
					}
				}
				
				setup = true;
			}
		}
		
		if(starttime + duration < System.currentTimeMillis()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		loopcounter += 1;
	}
	
	
	
	@Override
	public void remove() {
		for(TempBlock tb : tempblocks) {
	        tb.revertBlock();
	    }
		super.remove();
		return;
	} 

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "ddicco";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "2.0";
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public static Location getCrashLocation() {
		return blocklocation = block.getLocation();
	}

}
