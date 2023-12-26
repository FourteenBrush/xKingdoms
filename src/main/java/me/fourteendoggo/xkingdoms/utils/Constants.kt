package me.fourteendoggo.xkingdoms.utils

import org.intellij.lang.annotations.Language

object Constants {
    const val ADMIN_PERMISSION_STRING = "xkingdoms.admin"
    const val MODERATOR_PERMISSION_STRING = "xkingdoms.moderator"

    @Language("SQL")
    val SQLITE_INITIAL_TABLE_SETUP = arrayOf(
            """
            CREATE TABLE IF NOT EXISTS players (
                uuid BLOB,
                level INTEGER NOT NULL,
                PRIMARY KEY (uuid)
            );
            """.trimIndent(),
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
            );
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS skills (
                owner BLOB,
                type VARCHAR(40),
                level INT NOT NULL,
                xp INT NOT NULL,
                PRIMARY KEY (owner, type),
                FOREIGN KEY (owner) REFERENCES players (uuid)
            );
            """
                    .trimIndent()
    )
}
