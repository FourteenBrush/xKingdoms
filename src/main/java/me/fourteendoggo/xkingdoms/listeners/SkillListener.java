package me.fourteendoggo.xkingdoms.listeners;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.skill.SkillData;
import me.fourteendoggo.xkingdoms.skill.SkillType;
import me.fourteendoggo.xkingdoms.skill.SkillsManager;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SkillListener implements Listener {
    private final XKingdoms plugin;
    private final SkillsManager skillsManager;

    public SkillListener(XKingdoms plugin) {
        this.plugin = plugin;
        this.skillsManager = plugin.getSkillsManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        Player player = event.getPlayer();

        if (Tag.CROPS.isTagged(material)) {
            handleXPGain(SkillType.FARMING, player);
        } else if (Tag.LOGS_THAT_BURN.isTagged(material)) {
            handleXPGain(SkillType.WOODCUTTING, player);
        }
    }

    private void handleXPGain(SkillType type, Player player) {
        KingdomPlayer kPlayer = plugin.getUserRepository().get(player.getUniqueId());
        SkillData skillData = kPlayer.getData().getSkillData();

        skillData.incrementXP(type, 5); // TODO: change this to a proper values for each material or smth
        skillsManager.checkProgress(type, kPlayer);
    }
}
