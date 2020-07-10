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
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class IceCrash extends WaterAbility implements AddonAbility{
	
	public enum State {
		START, SOURCING, PREPARE, MOVING, SHARDS, ENDED
	}
	
	private double selectrange = 16;
	private double damage;
	private long cooldown;
	private boolean sourced;
	private Vector direction;
	private Location location;
	private ArrayList<TempBlock> tempblocks = new ArrayList<TempBlock>();
	private Block blocklocation;
	private double speed;
	private double radius;
	private State state;
	private Block block;
	private Block sourceblock;
	private Location sourcelocation;
	private int selectparticles;
	private int sourcerange;
	private long duration;
	private long starttime;
	private int loopcounter;
	private boolean setup;
	private Block newblocklocation;

	public IceCrash(Player player, State s) {
		super(player);
		// TODO Auto-generated constructor stub
		state = s;
		setFields();
		
		if(state == State.PREPARE) {
			if(WaterReturn.hasWaterBottle(player)) {
				WaterReturn.emptyWaterBottle(player);
				state = State.PREPARE;
				sourced = false;
				start();
			}
		}
		if(state == State.SOURCING) {
			if(sourceblock != null) {
				state = State.SOURCING;
				sourced = true;
			}
		}
		
		start();
	}
	
	private void setFields() {
		// TODO Auto-generated method stub
		speed = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Speed");
		damage = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Damage");
		cooldown = ConfigManager.getConfig().getLong(getConfigPath() + "Smash.Cooldown");
		location = player.getLocation();
		location.add(0, 2, 0);
		direction = player.getEyeLocation().getDirection();
		radius = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Radius");
		
		sourcerange = 16;
		sourceblock = BlockSource.getWaterSourceBlock(player, sourcerange, ClickType.SHIFT_DOWN, true, true, true);
		if(sourceblock != null) {
			sourcelocation = sourceblock.getLocation();
		}
		block = player.getLocation().add(0, 2, 0).getBlock();
		location = player.getLocation();
		
		cooldown = ConfigManager.getConfig().getLong(getConfigPath() + "Passive.Cooldown");
		duration = ConfigManager.getConfig().getLong(getConfigPath() + "Passive.Duration");
		starttime = System.currentTimeMillis();
	}
	
	@Override
	public void progress() {
		// TODO Auto-generated method stub
		
		if(player.isDead() || !player.isOnline()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if(!bPlayer.canBend(this)) {
			if(state != State.SHARDS) {
				new WaterReturn(player, player.getLocation().getBlock());
				remove();
				return;
			}
		}
		
		if(state == State.SOURCING) {
			sourceAnimation();
		}
		
		if(state == State.PREPARE) {
			if(sourced == false) {
				createPrepareBlocks();
			} else {
				block = sourceblock.getLocation().add(0, 2, 0).getBlock();
				location = block.getLocation().add(0, -2, 0);
				createPrepareBlocks();
			}
		}
	}
	
	private void createPrepareBlocks() {
		// TODO Auto-generated method stub
		if(loopcounter == 0) {
			testPrepareStartBlocks();
			if(block.getType() != Material.AIR && block.getType() != Material.SNOW 
					&& block.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR
					&& block.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) {
				remove();
				return;
			} else {
				TempBlock watertblock = new TempBlock(block, Material.WATER);
				tempblocks.add(watertblock);
			}
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
	private void testPrepareStartBlocks() {
		// TODO Auto-generated method stub
		for(int y = 4; y < 6; y += 1) {
			newblocklocation = location.clone().add(0, y, 0).getBlock();
			if(newblocklocation.getType() != Material.AIR && newblocklocation.getType() != Material.SNOW) {
				remove();
				return;
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int y = 3; y < 6; y += 1) {
				newblocklocation = location.clone().add(x, y, 0).getBlock();
				if(newblocklocation.getType() != Material.AIR && newblocklocation.getType() != Material.SNOW) {
					remove();
					return;
				}
			}
		}
		
		for(int z = -1; z < 2; z += 2) {
			for(int y = 3; y < 6; y += 1) {
				newblocklocation = location.clone().add(0, y, z).getBlock();
				if(newblocklocation.getType() != Material.AIR && newblocklocation.getType() != Material.SNOW) {
					remove();
					return;
				}
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int z = -1; z < 2; z += 2) {
				newblocklocation = location.clone().add(x, 4, z).getBlock();
				if(newblocklocation.getType() != Material.AIR && newblocklocation.getType() != Material.SNOW) {
					remove();
					return;
				}
			}
		}
	}
	public State getState() {
		return state;
	}
	
	public void changeState(State s) {
		state = s;
	}
	public void sourceShiftFunction() {
		// TODO Auto-generated method stub
		
		state = State.PREPARE;
	}
	public void sourceAnimation() {
		ParticleEffect.SMOKE.display(sourcelocation, selectparticles, 0F, 0F, 0F);
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
	
	private void moveIceSetBlocks() {
		
	}
	
	public void moveIce() {
		
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
	
	@Override
	public String getDescription() {
		return "Good waterbenders use their powers to turn the water into ice to form a structure, they can then shoot the structure that explodes into shards on impact";
	}
	
	@Override
	public String getInstructions() {
		return "Press shift looking at a waterbendable block to create an ice structure and click to shoot it, you can then click to redirect it wherever you're looking at";
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
	public void resetDirectionFunction() {
		// TODO Auto-generated method stub
	}
	public void createMoveAnimation() {
		// TODO Auto-generated method stub
		
	}
}
