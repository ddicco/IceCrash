package me.ddicco.IceCrash;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class IceCrash extends WaterAbility implements AddonAbility {
	private Location location;
	private Location loc;
	private String configprefix;
	
	public IceCrash(Player player) {
		super(player);
		// TODO Auto-generated constructor stub
			
        ConfigManager.getConfig().getInt(configprefix + "Cooldown", 1500);
        location = player.getLocation();
        
        start();
	}

	@Override
	public long getCooldown() {
		// TODO Auto-generated method stub
		return 1500;
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

		if (bPlayer.isOnCooldown(this)) {
			remove();
			return;
		}

		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		bPlayer.addCooldown(this);
		loc = location.clone().add(0, 2, 0);
		TempBlock tblock = new TempBlock(loc.getBlock(), Material.ICE, (byte) 5000);
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "ddicco";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "Alpha 0.0.1";
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new IceCrashListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}
	
}
