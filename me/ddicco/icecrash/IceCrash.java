package me.ddicco.icecrash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
	
	private ArrayList<FallingBlock> fallenblocks = new ArrayList<FallingBlock>();
	private Map<Entity, Integer> hits= new HashMap<Entity, Integer>();
	private Location primaryblocklocation;
	private boolean shardsinitiated = false;
	private int maxshardshits;
	private double damage;
	private long cooldown;
	private Vector direction;
	private Location preparelocation;
	private ArrayList<TempBlock> tempblocks = new ArrayList<TempBlock>();
	private Block blocklocation;
	private double speed;
	private double radius;
	private State state;
	private Block sourceblock;
	private Location sourcelocation;
	private int selectparticles;
	private double sourcerange;
	private long duration;
	private long preparestarttime;
	private int loopcounter;
	private int preparestateloopcounter;
	private int numberofshards;
	private double sharddamage;
	private boolean frombottle = false;
	private ArrayList<FallingBlock> fblocks = new ArrayList<FallingBlock>();
	private double shardsradius;
	private long shardsstarttime;
	private long safeduration;
	private int slowduration;
	private int slowamplifier;
	private int movingstateloopcounter;

	public IceCrash(Player player, State s) {
		super(player);
		// TODO Auto-generated constructor stub
		state = s;
		setFields();
		
		if(state == State.PREPARE) {
			frombottle = true;
			start();
		}
		if(state == State.SOURCING) {
			if(sourceblock != null) {
				state = State.SOURCING;
				preparelocation = sourceblock.getLocation().clone().add(0, 1, 0);
				start();
			}
		}
	}
	
	private void setFields() {
		// TODO Auto-generated method stub
		speed = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Speed");
		damage = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Damage");
		cooldown = ConfigManager.getConfig().getLong(getConfigPath() + "Smash.Cooldown");
		preparelocation = player.getLocation();
		direction = player.getEyeLocation().getDirection();
		radius = ConfigManager.getConfig().getDouble(getConfigPath() + "Smash.Radius");
		
		loopcounter = 0;
		preparestateloopcounter = 0;
		movingstateloopcounter = 0;
		
		sourcerange = ConfigManager.getConfig().getDouble(getConfigPath() + "SourceRange");
		sourceblock = BlockSource.getWaterSourceBlock(player, sourcerange, ClickType.LEFT_CLICK, true, true, true);
		if(sourceblock != null) {
			sourcelocation = sourceblock.getLocation();
		}
		
		cooldown = ConfigManager.getConfig().getLong(getConfigPath() + "Passive.Cooldown");
		duration = ConfigManager.getConfig().getLong(getConfigPath() + "Passive.Duration");
		
		safeduration = 15000;
		shardsradius = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.Radius");
		numberofshards = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.Amount");
		sharddamage = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.Damage");
		maxshardshits = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.MaxHits");
		slowduration = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.SlowDuration");
		slowamplifier = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.SlowAmplifier");
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
			if(state != State.SHARDS && state != State.SOURCING) {
				new WaterReturn(player, player.getLocation().getBlock());
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
		
		if(state == State.SOURCING) {
			sourceAnimation();
		}
		
		if(state == State.PREPARE) {
			if(preparestateloopcounter == 0) {
				testPrepareStartBlocks();
				if(frombottle) {
					if(WaterReturn.hasWaterBottle(player)) {
						WaterReturn.emptyWaterBottle(player);
					} else {
						remove();
						return;
					}
				} else {
					preparelocation.subtract(0, 2, 0);
				}
				preparestarttime = System.currentTimeMillis();
			}
			createPrepareBlocks();
			if(preparestarttime + duration < System.currentTimeMillis()) {
				new WaterReturn(player, player.getLocation().getBlock());
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			preparestateloopcounter += 1;
		}
		
		if(state == State.MOVING) {
			
			if(movingstateloopcounter == 0) {
				primaryblocklocation = preparelocation.add(0, 4, 0);
				direction = player.getEyeLocation().getDirection();
			}
			if(GeneralMethods.isRegionProtectedFromBuild(player, primaryblocklocation)) {
				if(!tempblocks.isEmpty()) {
					for(TempBlock tb : tempblocks) {
				        tb.revertBlock();
				    }
					tempblocks.clear();
				}
				bPlayer.addCooldown(this);
			}
			moveIce();
			movingstateloopcounter += 1;
		}
		if(state == State.SHARDS) {
			if(!shardsinitiated) {
				shardsstarttime = System.currentTimeMillis();
				createShards();
			}
			
			for(FallingBlock fallblock : fblocks) {
				for(Entity e : GeneralMethods.getEntitiesAroundPoint(fallblock.getLocation(), shardsradius)) {
					if(e != null && e instanceof LivingEntity) {
						if(hits.get(e) < maxshardshits) {
							((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowduration, slowamplifier, true, false));
							DamageHandler.damageEntity(e, sharddamage, this);
							if(hits.get(e) == null) {
								hits.put(e, 1);
							} else {
								hits.put(e, hits.get(e) + 1);
							}
						}
					}
				}
				
				if(fallblock.isOnGround()) {
					fallenblocks.add(fallblock);
					fallblock.remove();
				}
			}
			
			if(fallenblocks.size() == fblocks.size()) {
				remove();
				return;
			}
			
			if(shardsstarttime + safeduration < System.currentTimeMillis()) {
				remove();
				return;
			}
		}
	}
	
	public int getFallingBlocksSize() {
		return fblocks.size();
	}
	
	private void createPrepareBlocks() {
		// TODO Auto-generated method stub
		if(loopcounter == 1) {
			TempBlock watertblock = new TempBlock(preparelocation.clone().add(0, 2, 0).getBlock(), Material.WATER);
			tempblocks.add(watertblock);
		}
		
		if(loopcounter == 2) {
			TempBlock watertblock2 = new TempBlock(preparelocation.clone().add(0, 3, 0).getBlock(), Material.WATER);
			tempblocks.add(watertblock2);
		}
		
		if(loopcounter == 3) {
			for(int y = 4; y < 6; y += 1) {
				TempBlock tblock = new TempBlock(preparelocation.clone().add(0, y, 0).getBlock(), Material.ICE);
				tempblocks.add(tblock);
			}
				
			for(int x = -1; x < 2; x += 2) {
				for(int y = 3; y < 6; y += 1) {
					TempBlock tblock = new TempBlock(preparelocation.clone().add(x, y, 0).getBlock(), Material.ICE);
					tempblocks.add(tblock);
				}
			}
			
			for(int z = -1; z < 2; z += 2) {
				for(int y = 3; y < 6; y += 1) {
					TempBlock tblock = new TempBlock(preparelocation.clone().add(0, y, z).getBlock(), Material.ICE);
					tempblocks.add(tblock);
				}
			}
				
			for(int x = -1; x < 2; x += 2) {
				for(int z = -1; z < 2; z += 2) {
					TempBlock tblock = new TempBlock(preparelocation.clone().add(x, 4, z).getBlock(), Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
		
		loopcounter += 1;
	}
	
	private void createShards() {
		for(int i = 0; i < numberofshards; i += 1) {
			FallingBlock fb = GeneralMethods.spawnFallingBlock(primaryblocklocation, Material.ICE);
			Random random = new Random();
			double x = random.nextInt(100);
			double z = random.nextInt(100);
			x = (x-50)/100;
			z = (z-50)/100;
			fb.setVelocity(new Vector(x, 0.2, z));
			fb.setMetadata("icecrashshards", new FixedMetadataValue(ProjectKorra.plugin, this));
			fb.setHurtEntities(false);
			fb.setDropItem(false);
			
			fblocks.add(fb);
		}
		shardsinitiated = true;
	}
	
	private void testPrepareStartBlocks() {
		// TODO Auto-generated method stub
		if(preparelocation.clone().add(0, 3, 0).getBlock().getType() != Material.AIR 
				&& preparelocation.clone().add(0, 3, 0).getBlock().getType() != Material.CAVE_AIR 
				&& preparelocation.clone().add(0, 3, 0).getBlock().getType() != Material.VOID_AIR) {
			remove();
			return;
		}
		if(preparelocation.clone().add(0, 4, 0).getBlock().getType() != Material.AIR 
				&& preparelocation.clone().add(0, 4, 0).getBlock().getType() != Material.CAVE_AIR 
				&& preparelocation.clone().add(0, 4, 0).getBlock().getType() != Material.VOID_AIR) {
			remove();
			return;
		}
		
		for(int y = 5; y < 7; y += 1) {
			if(preparelocation.clone().add(0, y, 0).getBlock().getType() != Material.AIR 
					&& preparelocation.clone().add(0, y, 0).getBlock().getType() != Material.CAVE_AIR 
					&& preparelocation.clone().add(0, y, 0).getBlock().getType() != Material.VOID_AIR) {
				remove();
				return;
			}
		}
				
		for(int x = -1; x < 2; x += 2) {
			for(int y = 3; y < 6; y += 1) {
				if(preparelocation.clone().add(x, y, 0).getBlock().getType() != Material.AIR 
						&& preparelocation.clone().add(x, y, 0).getBlock().getType() != Material.CAVE_AIR 
						&& preparelocation.clone().add(x, y, 0).getBlock().getType() != Material.VOID_AIR) {
					remove();
					return;
				}
			}
		}
				
		for(int z = -1; z < 2; z += 2) {
			for(int y = 3; y < 6; y += 1) {
				if(preparelocation.clone().add(0, y, z).getBlock().getType() != Material.AIR 
						&& preparelocation.clone().add(0, y, z).getBlock().getType() != Material.CAVE_AIR 
						&& preparelocation.clone().add(0, y, z).getBlock().getType() != Material.VOID_AIR) {
					remove();
					return;
				}
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int z = -1; z < 2; z += 2) {
				if(preparelocation.clone().add(x, 4, z).getBlock().getType() != Material.AIR 
						&& preparelocation.clone().add(x, 4, z).getBlock().getType() != Material.CAVE_AIR 
						&& preparelocation.clone().add(x, 4, z).getBlock().getType() != Material.VOID_AIR) {
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
		for(int y = 0; y < 2; y += 1) {
			blocklocation = primaryblocklocation.clone().add(0, y, 0).getBlock();
			if(blocklocation.getType() == Material.AIR 
					|| blocklocation.getType() == Material.ICE 
					|| blocklocation.getType().equals(Material.CAVE_AIR) 
					|| blocklocation.getType().equals(Material.VOID_AIR)) {
				TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
				tempblocks.add(tblock);
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int y = -1; y < 2; y += 1) {
				blocklocation = primaryblocklocation.clone().add(x, y, 0).getBlock();
				if(blocklocation.getType() == Material.AIR 
						|| blocklocation.getType() == Material.ICE 
						|| blocklocation.getType().equals(Material.CAVE_AIR) 
						|| blocklocation.getType().equals(Material.VOID_AIR)) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
		
		for(int z = -1; z < 2; z += 2) {
			for(int y = -1; y < 2; y += 1) {
				blocklocation = primaryblocklocation.clone().add(0, y, z).getBlock();
				if(blocklocation.getType() == Material.AIR 
						|| blocklocation.getType() == Material.ICE 
						|| blocklocation.getType().equals(Material.CAVE_AIR) 
						|| blocklocation.getType().equals(Material.VOID_AIR)) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
		
		for(int x = -1; x < 2; x += 2) {
			for(int z = -1; z < 2; z += 2) {
				blocklocation = primaryblocklocation.clone().add(x, 0, z).getBlock();
				if(blocklocation.getType() == Material.AIR 
						|| blocklocation.getType() == Material.ICE 
						|| blocklocation.getType().equals(Material.CAVE_AIR) 
						|| blocklocation.getType().equals(Material.VOID_AIR)) {
					TempBlock tblock = new TempBlock(blocklocation, Material.ICE);
					tempblocks.add(tblock);
				}
			}
		}
	}
	
	public void moveIce() {
		
		if(!tempblocks.isEmpty()) {
			for(TempBlock tb : tempblocks) {
		        tb.revertBlock();
		    }
			tempblocks.clear();
		}
		
		moveIceSetBlocks();
		
		primaryblocklocation.add(direction.multiply(speed));
		
		for(Entity e : GeneralMethods.getEntitiesAroundPoint(primaryblocklocation, radius)) {
			if(e != null && e != player) {
				new WaterReturn(player, player.getLocation().add(0, 1, 0).getBlock());
				if(!tempblocks.isEmpty()) {
					for(TempBlock tb : tempblocks) {
				        tb.revertBlock();
				    }
					tempblocks.clear();
				}
				state = State.SHARDS;
				DamageHandler.damageEntity(e, damage, this);
				bPlayer.addCooldown(this);
			}
		}
		
		Location testblocklocation = primaryblocklocation;
		final Block block = testblocklocation.getBlock();
		
		if (!block.getType().equals(Material.AIR) && !block.getType().equals(Material.CAVE_AIR) && !block.getType().equals(Material.VOID_AIR)) {
			if (!block.getType().equals(Material.ICE)) {
				bPlayer.addCooldown(this);
				if(!tempblocks.isEmpty()) {
					for(TempBlock tb : tempblocks) {
				        tb.revertBlock();
				    }
					tempblocks.clear();
				}
				state = State.SHARDS;
				new WaterReturn(player, player.getLocation().add(0, 1, 0).getBlock());
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
	
	@Override
	public String getDescription() {
		return "Some waterbenders have discovered how to use their powers to turn the water into ice to form a structure, they can then shoot the structure that explodes into shards on impact.";
	}
	
	@Override
	public String getInstructions() {
		return "Click while looking at a waterbendable block then shift to create an ice structure, click to shoot it. You can then click again to redirect it wherever you're looking at. Alternatively, you can press shift with a waterbottle in your inventory to activate the ability.";
	}
	
	public String getConfigPath() {
		return "ExtraAbilities.ddicco.Water.IceCrash.";
	}
	@Override
	public void load() {
		// TODO Auto-generated method stub
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new IceCrashListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Successfully enabled " + getName() + " by " + getAuthor() + " version " + getVersion());
		
		if (ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.icecrash") == null) {
            Permission perm = new Permission("bending.ability.icecrash");
            perm.setDefault(PermissionDefault.TRUE);
            ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        }
		
		ConfigManager.getConfig().addDefault(getConfigPath() + "SourceRange", 16);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Passive.Cooldown", 10000);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Passive.Duration", 15000);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Cooldown", 10000);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Damage", 4);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Speed", 1);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Smash.Radius", 3);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.Amount", 10);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.Damage", 1);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.MaxHits", 2);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.Radius", 2);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.SlowDuration", 60);
		ConfigManager.getConfig().addDefault(getConfigPath() + "Shards.SlowAmplifier", 2);
		ConfigManager.defaultConfig.save();
	}
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	public void resetDirectionFunction() {
		// TODO Auto-generated method stub
		direction = player.getEyeLocation().getDirection();
	}
	public void createMoveAnimation() {
		// TODO Auto-generated method stub
		state = State.MOVING;
	}
}
