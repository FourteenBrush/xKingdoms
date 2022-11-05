package me.fourteendoggo.xkingdoms.utils;

public interface Constants {
    String ADMIN_PERMISSION_STRING = "xkingdoms.admin";
    String MODERATOR_PERMISSION_STRING = "xkingdoms.moderator";
    String[] SQLITE_INITIAL_TABLE_SETUP = new String[] {
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid BLOB,
                level INTEGER NOT NULL,
                PRIMARY KEY (uuid)
            );""",
            """
            CREATE TABLE IF NOT EXISTS homes (
                owner BLOB,
                name VARCHAR(40),
                world BLOB NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                yaw FLOAT NOT NULL,
                pitch FLOAT NOT NULL,
                PRIMARY KEY (owner, name),
                FOREIGN KEY (owner) REFERENCES players (uuid)
            );""",
            """
            CREATE TABLE IF NOT EXISTS skills (
                owner BLOB,
                type VARCHAR(40),
                level INT NOT NULL,
                xp INT NOT NULL,
                PRIMARY KEY (owner, type),
                FOREIGN KEY (owner) REFERENCES players (uuid)
            );"""
    };
}
