package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SkillListener implements Listener {
    private final XKingdoms plugin;

    public SkillListener(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        Player player = event.getPlayer();
        KingdomPlayer kPlayer = plugin.getUserRepository().get(player.getUniqueId());
        if (Tag.CROPS.isTagged(material)) {

            // handle breaking crops
        } else if (Tag.LOGS_THAT_BURN.isTagged(material)) {
            // handle breaking logs
        }
    }
}
