package me.fourteendoggo.xkingdoms.chat

import me.fourteendoggo.xkingdoms.player.KingdomPlayer

// TODO
class ChatChannel(val name: String, val joinPermission: String) {
    private val joinedPlayers: MutableSet<KingdomPlayer>

    init {
        joinedPlayers = HashSet()
    }

    fun join(player: KingdomPlayer) {
        joinedPlayers.add(player)
        player.playerData.setJoinedChannel(this)
    }

    fun leave(player: KingdomPlayer) {
        joinedPlayers.remove(player)
        player.playerData.setJoinedChannel(null)
    }
}
