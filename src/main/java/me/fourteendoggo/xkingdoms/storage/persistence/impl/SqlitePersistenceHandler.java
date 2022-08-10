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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlitePersistenceHandler extends Database implements PersistenceHandler {

    public SqlitePersistenceHandler(XKingdoms plugin) {
        super(plugin);
        File dbFile = Utils.getCreatedFile(new File(plugin.getDataFolder(), "database.db"));
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }

    @Override
    public void connect() {
        try (Connection conn = getConnection()) {
            for (String query : Constants.SQLITE_INITIAL_TABLE_SETUP) {
                executeRawQuery(conn, query);
            }
        } catch (SQLException e) {
            Utils.sneakyThrow(e);
        }
    }

    @Override
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public KingdomPlayer loadPlayer(UUID id) {
        return withConnection("SELECT * FROM players WHERE uuid=?;", (conn, ps) -> {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                  int level = rs.getInt("level");
                  PlayerData playerData = new PlayerData(level);

                  List<Home> homes = fetchHomes(conn, id);
                  homes.forEach(playerData::addHome);

                  Map<SkillType, SkillProgress> skillsProgress = fetchSkills(conn, id);
                  skillsProgress.forEach(playerData::addSkillProgress);
            }
            return KingdomPlayer.newFirstJoinedPlayer(id);
        }, id);
    }

    private List<Home> fetchHomes(Connection conn, UUID owner) {
        return withConnection("SELECT * FROM homes WHERE owner=?;", conn, ps -> {
            ResultSet rs = ps.executeQuery();
            List<Home> results = new ArrayList<>();
            while (rs.next()) {
                results.add(Home.fromResultSet(rs));
            }
            return results;
        }, owner);
    }

    private Map<SkillType, SkillProgress> fetchSkills(Connection conn, UUID owner) {
        return withConnection("SELECT * FROM skills WHERE owner=?", conn, ps -> {
            ResultSet rs = ps.executeQuery();
            Map<SkillType, SkillProgress> results = new HashMap<>();
            while (rs.next()) {
                SkillType type = SkillType.valueOf(rs.getString("type"));
                int level = rs.getInt("level");
                int xp = rs.getInt("xp");
                results.put(type, new SkillProgress(level, xp));
            }
            return results;
        }, owner);
    }

    @Override
    public void savePlayer(KingdomPlayer player) {

    }
}
