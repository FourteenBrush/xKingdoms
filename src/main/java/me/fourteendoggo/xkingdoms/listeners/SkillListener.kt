package me.fourteendoggo.xkingdoms.listeners

import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.skill.SkillType
import me.fourteendoggo.xkingdoms.skill.SkillsManager
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.function.Predicate

@Suppress("UNUSED")
class SkillListener(private val plugin: XKingdoms, private val skillsManager: SkillsManager) : Listener {
    @EventHandler
    fun onCropTrample(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL) return
        val clickedBlock = event.clickedBlock
        if (clickedBlock == null || clickedBlock.type != Material.FARMLAND) return
        event.player.sendMessage(plugin.getLang(LangKey.FARMING_CANNOT_BREAK_FARMLAND))
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) return
        val material = event.block.type

        when {
            Tag.CROPS.isTagged(material) -> {
                val crop = event.block.blockData as Ageable
                if (crop.age < crop.maximumAge) {
                    player.sendMessage(plugin.getLang(LangKey.FARMING_CROP_NOT_READY_YET, crop.age, crop.maximumAge))
                    event.isCancelled = true
                    return
                }
                handleProgress(SkillType.FARMING, HOE_TYPES::contains, player, event)
            }
            Tag.LOGS_THAT_BURN.isTagged(material) -> {
                handleProgress(SkillType.WOODCUTTING, AXE_TYPES::contains, player, event)
            }
            Tag.STONE_ORE_REPLACEABLES.isTagged(material) -> {
                handleProgress(SkillType.MINING, PICKAXE_TYPES::contains, player, event)
            }
        }
    }

    private fun handleProgress(type: SkillType, toolPredicate: Predicate<Material>, player: Player, event: BlockBreakEvent) {
        val mainHandItem = player.inventory.itemInMainHand
        if (mainHandItem.type.isAir) return

        if (!toolPredicate.test(mainHandItem.type)) {
            player.sendMessage(plugin.getLang(LangKey.INCORRECT_TOOL))
            event.isCancelled = true
            return
        }
        val kPlayer = plugin.userManager.getUser(player.uniqueId)
        kPlayer!!.playerData.updateSkillXp(type, 1) // TODO: change this to a proper values for each material or smth
        skillsManager.handleLevelling(type, kPlayer)
    }

    companion object {
        private val HOE_TYPES: Set<Material> = EnumSet.of(
                Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
                Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
        )
        private val AXE_TYPES: Set<Material> = EnumSet.of(
                Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
        )
        private val PICKAXE_TYPES: Set<Material> = EnumSet.of(
                Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
                Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
        )
    }
}
