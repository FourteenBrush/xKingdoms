package me.fourteendoggo.xkingdoms.utils;

import org.intellij.lang.annotations.Language;

public interface Constants {
    String MODERATOR_PERMISSION_STRING = "xkingdoms.moderator";
    @Language("SQL") String[] H2_INITIAL_TABLE_SETUP = new String[] {
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid UUID PRIMARY KEY,
                level INT NOT NULL DEFAULT 0
            );""",
            """
            CREATE TABLE IF NOT EXISTS homes (
                id UUID PRIMARY KEY,
                owner UUID NOT NULL,
                name VARCHAR(40) NOT NULL,
                world UUID NOT NULL,
                x DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                yaw FLOAT NOT NULL,
                pitch FLOAT NOT NULL
            );"""
    };
    @Language("SQL") String[] SQLITE_INITIAL_TABLE_SETUP = new String[] {
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid UUID NOT NULL,
                level INTEGER NOT NULL DEFAULT 0
            );""",
            """
            CREATE TABLE IF NOT EXIST homes (
                id UUID PRIMARY KEY,
                owner UUID NOT NULL,
                name VARCHAR(40) NOT NULL,
                world UUID NOT NULL,
                x DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                y DOUBLE PRECISION NOT NULL,
                yaw FLOAT NOT NULL,
                pitch FLOAT NOT NULL
            );
            """
    };
}
