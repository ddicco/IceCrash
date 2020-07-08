package me.ddicco.icecrash;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class IceCrashPrepare extends WaterAbility implements AddonAbility {

	private long cooldown;
	public Location blocklocation;
	private Location location;
	private ArrayList<TempBlock> tempblocks = new ArrayList<TempBlock>();
	private Block block;
	private Block newblocklocation;
	private long duration;
	private long starttime;
	private boolean setup = false;
	private int loopcounter = 0;
	private double sourcerange;
	private Block sourceblock;

	public IceCrashPrepare(Player player) {
		super(player);
		// TODO Auto-generated constructor stub
		
		if(!bPlayer.canBend(this)) {
			remove();
			return;
		}
		setFields();
		if(WaterReturn.hasWaterBottle(player)) {
			WaterReturn.emptyWaterBottle(player);
			start();
		} else if(sourceblock != null) {
			
			block = sourceblock.getLocation().add(0, 2, 0).getBlock();
			location = block.getLocation().add(0, -2, 0);
			start();
		} else {
			remove();
			return;
		}
	}
	
	public String getConfigPath() {
		return "ExtraAbilities.ddicco.Water.IceCrash.";
	}
	
	private void setFields() {
		// TODO Auto-generated method stub
		sourcerange = 32;
		sourceblock = BlockSource.getWaterSourceBlock(player, sourcerange);
		block = player.getLocation().add(0, 2, 0).getBlock();
		location = player.getLocation();
		
		cooldown = ConfigManager.getConfig().getLong(getConfigPath() + "Passive.Cooldown");
		duration = ConfigManager.getConfig().getLong(getConfigPath() + "Passive.Duration");
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
			new WaterReturn(player, player.getLocation().getBlock());
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
			TempBlock watertblock = new TempBlock(block, Material.WATER);
			tempblocks.add(watertblock);
		}
		
		if(loopcounter == 1) {
			TempBlock watertblock2 = new TempBlock(block.getLocation().add(0, 1, 0).getBlock(), Material.WATER);
			tempblocks.add(watertblock2);
		}
		
		if(loopcounter == 2) {
			if(setup == false) {
				for(int y = 4; y < 6; y += 1) {
					newblocklocation = location.clone().add(0, y, 0).getBlock();
					TempBlock tblock = new TempBlock(newblocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
				
				for(int x = -1; x < 2; x += 2) {
					for(int y = 3; y < 6; y += 1) {
						newblocklocation = location.clone().add(x, y, 0).getBlock();
						TempBlock tblock = new TempBlock(newblocklocation, Material.ICE);
						tempblocks.add(tblock);
					}
				}
				
				for(int z = -1; z < 2; z += 2) {
					for(int y = 3; y < 6; y += 1) {
						newblocklocation = location.clone().add(0, y, z).getBlock();
						TempBlock tblock = new TempBlock(newblocklocation, Material.ICE);
						tempblocks.add(tblock);
					}
				}
				
				for(int x = -1; x < 2; x += 2) {
					for(int z = -1; z < 2; z += 2) {
						newblocklocation = location.clone().add(x, 4, z).getBlock();
						TempBlock tblock = new TempBlock(newblocklocation, Material.ICE);
						tempblocks.add(tblock);
					}
				}
				
				setup = true;
			}
		}
		
		if(starttime + duration < System.currentTimeMillis()) {
			bPlayer.addCooldown(this);
			new WaterReturn(player, player.getLocation().getBlock());
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
		return "idk either";
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void createMoveAnimation() {
		// TODO Auto-generated method stub
		if(tempblocks != null && block != null) {
			new IceCrash(player, block.getLocation(), tempblocks);
		}
	}

}
