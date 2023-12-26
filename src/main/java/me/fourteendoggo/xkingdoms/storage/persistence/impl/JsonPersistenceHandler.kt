package me.fourteendoggo.xkingdoms.storage.persistence.impl

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.player.KingdomPlayer
import me.fourteendoggo.xkingdoms.player.PlayerData
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler
import me.fourteendoggo.xkingdoms.utils.Home
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files
import java.util.*

class JsonPersistenceHandler(private val plugin: XKingdoms) : PersistenceHandler {
    private val gson = GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Location::class.java, LocationTypeAdapter())
            .registerTypeAdapter(Home::class.java, HomeDeserializer())
            .registerTypeAdapter(KingdomPlayer::class.java, KingdomPlayerDeserializer())
            .create()

    private fun getPlayerDataFile(playerId: UUID): File {
        val dataFolder = File(plugin.dataFolder, "playerdata")
        val playerFile = File(dataFolder, "$playerId.json")
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw RuntimeException("Could not create playerdata folder")
        }
        return playerFile
    }

    override fun loadPlayer(id: UUID): KingdomPlayer {
        val playerFile = getPlayerDataFile(id)
        return if (!playerFile.exists()) {
            KingdomPlayer(id)
        } else {
            val json = Files.readString(playerFile.toPath())
            gson.fromJson(json, KingdomPlayer::class.java)
        }
    }

    override fun deleteHome(home: Home) {}
    override fun savePlayer(player: KingdomPlayer) {
        val playerFile = getPlayerDataFile(player.uuid)
        val json = gson.toJson(player)
        Files.writeString(playerFile.toPath(), json)
    }

    private class LocationTypeAdapter : TypeAdapter<Location>() {
        @Throws(IOException::class)
        override fun write(out: JsonWriter, location: Location) {
            out.beginObject()
            out.name("worldID").value(location.world!!.uid.toString())
            out.name("x").value(location.x)
            out.name("y").value(location.y)
            out.name("z").value(location.z)
            out.name("yaw").value(location.yaw.toDouble())
            out.name("pitch").value(location.pitch.toDouble())
            out.endObject()
        }

        override fun read(`in`: JsonReader): Location {
            val parser = JsonParser()
            val json = parser.parse(`in`) as JsonObject
            val worldID = UUID.fromString(json["worldID"].asString)
            val x = json["x"].asDouble
            val y = json["y"].asDouble
            val z = json["z"].asDouble
            val yaw = json["yaw"].asFloat
            val pitch = json["pitch"].asFloat
            return Location(Bukkit.getWorld(worldID), x, y, z, yaw, pitch)
        }
    }

    private class HomeDeserializer : JsonDeserializer<Home> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Home {
            val obj = json as JsonObject
            val name = obj["name"].asString
            val owner = UUID.fromString(obj["owner"].asString)
            val location = context.deserialize<Location>(obj["location"], Location::class.java)
            return Home(name, owner, location)
        }
    }

    private class KingdomPlayerDeserializer : JsonDeserializer<KingdomPlayer> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): KingdomPlayer {
            val obj = json as JsonObject
            val uuid = UUID.fromString(obj["uuid"].asString)
            val playerData = context.deserialize<PlayerData>(obj, PlayerData::class.java)
            return KingdomPlayer(uuid, playerData)
        }
    }
}
