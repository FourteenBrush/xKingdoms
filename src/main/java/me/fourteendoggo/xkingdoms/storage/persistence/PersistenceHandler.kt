package me.fourteendoggo.xkingdoms.storage.persistence

import me.fourteendoggo.xkingdoms.utils.Home
import java.util.*

interface PersistenceHandler {
    fun loadPlayer(id: UUID): KingdomPlayer?
    fun deleteHome(home: Home)
    fun savePlayer(player: KingdomPlayer)
}
