package me.fourteendoggo.xkingdoms.utils;

import org.intellij.lang.annotations.Language;

public interface Constants {
    String ADMIN_PERMISSION_STRING = "xkingdoms.admin";
    String MODERATOR_PERMISSION_STRING = "xkingdoms.moderator";
    @Language("SQL") String[] SQLITE_INITIAL_TABLE_SETUP = new String[] {
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) NOT NULL,
                level INTEGER NOT NULL DEFAULT 0
            );""",
            """
            CREATE TABLE IF NOT EXISTS homes (
                id VARCHAR(36) PRIMARY KEY,
                owner VARCHAR(36) NOT NULL,
                name VARCHAR(40) NOT NULL,
                world VARCHAR(36) NOT NULL,
                x DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                z DOUBLE PRECISION NOT NULL,
                yaw FLOAT NOT NULL,
                pitch FLOAT NOT NULL
            );""",
            """
            CREATE TABLE IF NOT EXISTS skills (
                owner VARCHAR(36) NOT NULL,
                type VARCHAR(40) PRIMARY KEY,
                level INT NOT NULL DEFAULT 0,
                xp INT NOT NULL DEFAULT 0
            );"""
    };
}
