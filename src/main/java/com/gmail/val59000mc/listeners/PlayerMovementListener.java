package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener implements Listener{

    private PlayersManager playersManager;

    public PlayerMovementListener(PlayersManager playersManager){
        this.playersManager = playersManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        handleFrozenPlayers(event);
    }

    private void handleFrozenPlayers(PlayerMoveEvent e){
        try {
            UhcPlayer uhcPlayer = playersManager.getUhcPlayer(e.getPlayer());
            if (uhcPlayer.isFrozen()){
                if (!e.getFrom().getBlock().equals(e.getTo().getBlock())){
                    e.setCancelled(true);
                    e.getPlayer().teleport(uhcPlayer.getStartingLocation());
                }
            }
        }catch (UhcPlayerDoesntExistException ex){
            // None existent players can't be frozen
        }
    }

}