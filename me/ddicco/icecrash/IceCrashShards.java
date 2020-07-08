package me.ddicco.icecrash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;

public class IceCrashShards extends WaterAbility implements AddonAbility {

	private long cooldown;
	private int numberofshards;
	private double sharddamage;
	private Location location;
	private ArrayList<FallingBlock> fblocks = new ArrayList<FallingBlock>();
	private boolean shardprepared;
	private double shardsradius;
	private Block oldblock;

	public IceCrashShards(Player player, Block block) {
		super(player);
		// TODO Auto-generated constructor stub
		oldblock = block;
		setFields();
		start();
	}

	private void setFields() {
		// TODO Auto-generated method stub
		shardsradius = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.Radius");
		location = oldblock.getLocation();
		cooldown = 10000;
		numberofshards = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.Amount");
		sharddamage = ConfigManager.getConfig().getInt(getConfigPath() + "Shards.Damage");
	}
	
	public String getConfigPath() {
		return "ExtraAbilities.ddicco.Water.IceCrash.";
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
		ArrayList<FallingBlock> fallenblocks = new ArrayList<FallingBlock>();
		if(shardprepared == false) {
			for(int i = 0; i < numberofshards; i += 1) {
				FallingBlock fb = GeneralMethods.spawnFallingBlock(location, Material.ICE);
				Random random = new Random();
				double x = random.nextInt(150);
				double z = random.nextInt(150);
				x = (x-75)/100;
				z = (z-75)/100;
				fb.setVelocity(new Vector(x, 0, z));
				fb.setMetadata("icecrashshards", new FixedMetadataValue(ProjectKorra.plugin, this));
				fb.setHurtEntities(false);
				fb.setDropItem(false);
				
				fblocks.add(fb);
			}
			shardprepared = true;
		}
		
		for(FallingBlock fallblock : fblocks) {
			for(Entity e : GeneralMethods.getEntitiesAroundPoint(fallblock.getLocation(), shardsradius)) {
				if(e != null && e != player) {
					DamageHandler.damageEntity(e, sharddamage, this);
					
				}
			}
		}
		
		Iterator<FallingBlock> iterator = fblocks.iterator();
		while (iterator.hasNext()) {
			FallingBlock fallblock = iterator.next();
			if (fallblock.isDead()) {
				iterator.remove();
				fallblock.remove();
			}
		}
		
		if(fallenblocks.size() == numberofshards) {
			remove();
			return;
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
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
