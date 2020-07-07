package me.ddicco.icecrash;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;


public class IceCrashListener implements Listener {
	
	@EventHandler
	public void onClick(PlayerToggleSneakEvent e) {
		Player player = e.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (e.isCancelled() || bPlayer == null) {
			return;
		} else if (bPlayer.canBend(CoreAbility.getAbility(IceCrashPrepare.class)) && !CoreAbility.hasAbility(player, IceCrashPrepare.class) && !CoreAbility.hasAbility(player, IceCrash.class)) {
			new IceCrashPrepare(player);
		}
	}
	
	@EventHandler
	public void onPlayerAnimationEvent(PlayerAnimationEvent event) {
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (event.isCancelled() || bPlayer == null) {
			return;
		} else if (bPlayer.canBend(CoreAbility.getAbility(IceCrashPrepare.class))) {
			if(CoreAbility.hasAbility(event.getPlayer(), IceCrashPrepare.class)) {
				if(CoreAbility.hasAbility(event.getPlayer(), IceCrash.class)) {
					((IceCrash) CoreAbility.getAbility(player, IceCrash.class)).leftClickFunction();
				} else {
					new IceCrash(player);
				}
			}
		}
	}
}
