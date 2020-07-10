package me.ddicco.icecrash;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

import me.ddicco.icecrash.IceCrash.State;


public class IceCrashListener implements Listener {
	
	private double selectrange = 16;
	private State state;
	
	@EventHandler
	public void onShift(PlayerToggleSneakEvent e) {
		Player player = e.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (e.isCancelled() || bPlayer == null || !player.isSneaking()) {
			return;
		}
		
		IceCrash ability = CoreAbility.getAbility(player, IceCrash.class);
		if(ability == null) {
			if(bPlayer.canBend(CoreAbility.getAbility(IceCrash.class))) {
				new IceCrash(player, State.PREPARE);
				return;
			} else {
				return;
			}
		} else {
			state = ability.getState();
		}
		
		if (bPlayer.canBend(CoreAbility.getAbility(IceCrash.class))) {
			if(state == State.SOURCING){
				((IceCrash) CoreAbility.getAbility(player, IceCrash.class)).sourceShiftFunction();
			}
		}
	}
	
	@EventHandler
	public void onPlayerAnimationEvent(PlayerAnimationEvent event) {
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		IceCrash ability = CoreAbility.getAbility(player, IceCrash.class);
		
		if (event.isCancelled() || bPlayer == null) {
			return;
		}
		if(ability == null) {
			if(bPlayer.canBend(CoreAbility.getAbility(IceCrash.class)) && !CoreAbility.hasAbility(player, IceCrash.class)) {
				if(BlockSource.getWaterSourceBlock(player, selectrange, ClickType.LEFT_CLICK, true, true, bPlayer.canPlantbend()) != null) {
					new IceCrash(player, State.SOURCING);
				}
			} else {
				return;
			}
		} else {
			state = ability.getState();
		}
		
		if (bPlayer.canBend(CoreAbility.getAbility(IceCrash.class))) {
			if(CoreAbility.hasAbility(event.getPlayer(), IceCrash.class)) {
				if(state == State.MOVING) {
					((IceCrash) CoreAbility.getAbility(player, IceCrash.class)).resetDirectionFunction();
				} else if(state == State.PREPARE) {
					((IceCrash) CoreAbility.getAbility(player, IceCrash.class)).createMoveAnimation();
				}
			}
		}
	}
	
	@EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {
        if(e.getEntityType().equals(EntityType.FALLING_BLOCK) && e.getEntity().hasMetadata("icecrashshards")) {
            e.setCancelled(true);
        }
    }
}
