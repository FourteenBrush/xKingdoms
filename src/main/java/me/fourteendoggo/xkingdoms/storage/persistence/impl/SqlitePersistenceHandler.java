package me.fourteendoggo.xkingdoms.storage.persistence.impl;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.skill.SkillProgress;
import me.fourteendoggo.xkingdoms.skill.SkillType;
import me.fourteendoggo.xkingdoms.storage.persistence.Database;
import me.fourteendoggo.xkingdoms.storage.persistence.PersistenceHandler;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Home;
import me.fourteendoggo.xkingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SqlitePersistenceHandler extends Database implements PersistenceHandler {

    public SqlitePersistenceHandler(XKingdoms plugin) {
        super(new LazyConnection(() -> {
            File dbFile = Utils.getCreatedFile(new File(plugin.getDataFolder(), "database.db"));
            return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }, false));
    }

    @Override
    public void connect() {
        executeAll(Constants.SQLITE_INITIAL_TABLE_SETUP);
    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        return withConnection("SELECT * FROM players WHERE uuid=?;", (conn, ps) -> {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int level = rs.getInt("level");
                PlayerData playerData = new PlayerData(level);

                insertHomes(conn, playerData, id);
                insertSkills(conn, playerData, id);

                return new KingdomPlayer(id, playerData);
            }
            return KingdomPlayer.newFirstJoinedPlayer(id);
        }, id);
    }

    private void insertHomes(Connection conn, PlayerData playerData, UUID id) {
        withConnection("SELECT * FROM homes WHERE owner=?;", conn, ps -> {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                UUID worldUUID = uuidFromBytes(rs.getBytes("world"));

                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                World world = Bukkit.getWorld(worldUUID);
                Location location = new Location(world, x, y, z, yaw, pitch);

                playerData.addHome(new Home(name, id, location));
            }
        }, id);
    }

    private void insertSkills(Connection conn, PlayerData playerData, UUID id) {
        withConnection("SELECT * FROM skills WHERE owner=?;", conn, ps -> {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SkillType type = SkillType.valueOf(rs.getString("type"));
                int level = rs.getInt("level");
                int xp = rs.getInt("xp");
                playerData.setSkillProgress(type, new SkillProgress(level, xp));
            }
        }, id);
    }

    @Override
    public void savePlayer(KingdomPlayer player) {
        String sql = "INSERT INTO players(uuid,level) VALUES (?,?) ON CONFLICT DO UPDATE SET level=?;";
        PlayerData playerData = player.getData();
        int level = playerData.getLevel();
        withConnection(sql, (conn, ps) -> {
            ps.executeUpdate();
            saveHomes(conn, playerData.getHomes().values());
            saveSkills(conn, playerData.getSkills(), player.getUniqueId());
        }, player.getUniqueId(), level, level);
    }

    private void saveHomes(Connection conn, Collection<Home> homes) {
        String sql = "INSERT OR IGNORE INTO homes(owner,name,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?,?);";
        withConnection(sql, conn, ps -> {
            int count = 0;
            for (Home home : homes) {
                Location loc = home.location();
                assert loc.getWorld() != null;

                ps.setBytes(1, uuidToBytes(home.owner()));
                ps.setString(2, home.name());
                ps.setBytes(3, uuidToBytes(loc.getWorld().getUID()));
                ps.setInt(4, loc.getBlockX());
                ps.setInt(5, loc.getBlockY());
                ps.setInt(6, loc.getBlockZ());
                ps.setFloat(7, loc.getYaw());
                ps.setFloat(8, loc.getPitch());

                ps.addBatch();
                count++;

                // execute every 10 records or fewer
                if (count % 10 == 0 || count == homes.size()) {
                    ps.executeBatch();
                }
            }
        });
    }

    private void saveSkills(Connection conn, Map<SkillType, SkillProgress> skills, UUID id) {
        String sql = "INSERT INTO skills(owner,type,level,xp) VALUES (?,?,?,?) ON CONFLICT DO UPDATE SET level=?, xp=?;";
        withConnection(sql, conn, ps -> {
            int count = 0;
            for (Map.Entry<SkillType, SkillProgress> entry : skills.entrySet()) {
                SkillType type = entry.getKey();
                SkillProgress progress = entry.getValue();

                ps.setBytes(1, uuidToBytes(id));
                ps.setString(2, type.name());
                ps.setInt(3, progress.getLevel());
                ps.setInt(4, progress.getXp());

                ps.setInt(5, progress.getLevel());
                ps.setInt(6, progress.getXp());

                ps.addBatch();
                count++;

                if (count % 10 == 0 || count == skills.size()) {
                    ps.executeBatch();
                }
            }
        });
    }
}
