package me.fourteendoggo.xkingdoms.storage.persistence.impl

import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.player.KingdomPlayer
import me.fourteendoggo.xkingdoms.player.PlayerData
import me.fourteendoggo.xkingdoms.skill.SkillProgress
import me.fourteendoggo.xkingdoms.skill.SkillType
import me.fourteendoggo.xkingdoms.storage.persistence.Database
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler
import me.fourteendoggo.xkingdoms.utils.Constants
import me.fourteendoggo.xkingdoms.utils.Home
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.*

class SqlitePersistenceHandler(plugin: XKingdoms) : Database(), PersistenceHandler {

    init {
        val dbFile = File(plugin.dataFolder, "database.db")
        if (!dbFile.exists()) dbFile.createNewFile()
        // TODO: ensure Database doesn't reassign this to a connection from a null pool
        _connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absoluteFile}")
    }

    override fun connect() = executeAll(Constants.SQLITE_INITIAL_TABLE_SETUP)

    override fun loadPlayer(id: UUID) = withConnection<KingdomPlayer>("SELECT * FROM players WHERE uuid=?;", id) { conn, ps ->
        val rs = ps.executeQuery()
        if (rs.next()) {
            val level = rs.getInt("level")
            val playerData = PlayerData(level)
            insertHomes(conn, playerData, id)
            insertSkills(conn, playerData, id)
            return@withConnection KingdomPlayer(id, playerData)
        }
        return@withConnection KingdomPlayer(id)
    }

    override fun deleteHome(home: Home) {
        withConnection("DELETE FROM homes WHERE name=? AND owner=?;", home.name, uuidToBytes(home.owner)) { _, ps ->
            ps.executeUpdate()
        }
    }

    private fun insertHomes(conn: Connection, playerData: PlayerData, id: UUID) {
        withConnection("SELECT * FROM homes WHERE owner=?;", conn, uuidToBytes(id)) { ps ->
            val rs = ps.executeQuery()
            while (rs.next()) {
                val name = rs.getString("name")
                val worldUUID = uuidFromBytes(rs.getBytes("world"))
                val x = rs.getDouble("x")
                val y = rs.getDouble("y")
                val z = rs.getDouble("z")
                val yaw = rs.getFloat("yaw")
                val pitch = rs.getFloat("pitch")
                val world = Bukkit.getWorld(worldUUID)
                val location = Location(world, x, y, z, yaw, pitch)
                playerData.addHome(Home(name, id, location))
            }
        }
    }

    private fun insertSkills(conn: Connection, playerData: PlayerData, id: UUID) {
        withConnection("SELECT * FROM skills WHERE owner=?;", conn, id) { ps: PreparedStatement? ->
            val rs = ps!!.executeQuery()
            while (rs.next()) {
                val type = SkillType.valueOf(rs.getString("type"))
                val level = rs.getInt("level")
                val xp = rs.getInt("xp")
                playerData.setSkillProgress(type, SkillProgress(level, xp))
            }
        }
    }

    override fun savePlayer(player: KingdomPlayer) {
        val sql = "INSERT INTO players(uuid,level) VALUES (?,?) ON CONFLICT DO UPDATE SET level=?;"
        val playerData = player.playerData
        val level = playerData.level
        withConnection(sql, player.uuid, level, level) { conn, ps ->
            ps.executeUpdate()
            saveHomes(conn, playerData.homes.values)
            saveSkills(conn, playerData.skills, player.uuid)
        }
    }

    private fun saveHomes(conn: Connection, homes: Collection<Home>) {
        val sql = "INSERT OR IGNORE INTO homes(owner,name,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?,?);"
        withConnection(sql, conn) { ps ->
            var count = 0
            for (home in homes) {
                val loc = home.location
                if (!loc.isWorldLoaded) {
                    // world files could've been deleted, so this home must've been saved before
                    continue
                }
                ps!!.setBytes(1, uuidToBytes(home.owner))
                ps.setString(2, home.name)
                ps.setBytes(3, uuidToBytes(loc.world!!.uid))
                ps.setInt(4, loc.blockX)
                ps.setInt(5, loc.blockY)
                ps.setInt(6, loc.blockZ)
                ps.setFloat(7, loc.yaw)
                ps.setFloat(8, loc.pitch)
                ps.addBatch()
                count++

                // execute every 10 records or fewer
                if (count % 10 == 0 || count == homes.size) {
                    ps.executeBatch()
                }
            }
        }
    }

    private fun deleteHome(conn: Connection, home: Home) {
        val uuidBytes = uuidToBytes(home.owner)
        withConnection("DELETE FROM homes WHERE owner=? AND name=?;", conn, uuidBytes, home.name) { ps ->
            ps.executeUpdate()
        }
        withConnection("SELECT * FROM homes WHERE owner=?;", conn) { ps ->
            val rs = ps.executeQuery()
            while (rs.next()) {
                println("home found after deleting: " + rs.getString("name"))
            }
        }
    }

    private fun saveSkills(conn: Connection, skills: Map<SkillType, SkillProgress>, id: UUID) {
        val sql = "INSERT INTO skills(owner,type,level,xp) VALUES (?,?,?,?) ON CONFLICT DO UPDATE SET level=?, xp=?;"
        withConnection(sql, conn) { ps ->
            var count = 0
            for ((type, progress) in skills) {
                ps!!.setBytes(1, uuidToBytes(id))
                ps.setString(2, type.name)
                ps.setInt(3, progress.level)
                ps.setInt(4, progress.xp)
                ps.setInt(5, progress.level)
                ps.setInt(6, progress.xp)
                ps.addBatch()
                count++
                if (count % 10 == 0 || count == skills.size) {
                    ps.executeBatch()
                }
            }
        }
    }
}
