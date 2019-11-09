package me.ddicco.IceCrash;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class IceCrashListener implements Listener
{
        @EventHandler(priority = EventPriority.NORMAL)
        public void Sneak(PlayerToggleSneakEvent event)
        {
        	Player player = event.getPlayer();
        	BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        	
        	if(event.isCancelled() || bPlayer == null) {
        		return;
        	} else if (bPlayer.canBend(CoreAbility.getAbility(IceCrash.class)) && !CoreAbility.hasAbility(event.getPlayer(), IceCrash.class)) {
    			new IceCrash (player);
        	}
        	
        }
 
}
