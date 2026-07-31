package fixtures

import dev.frammenti.fuckumeter.db.Database

object TestDatabase {
    val database = Database()

    fun truncate() {
        database.session {
            update(
                database.sql(
                    """
                TRUNCATE TABLE
                    users,
                    groups,
                    entries,
                    invites
                RESTART IDENTITY CASCADE;
                """
                )
            )
        }
    }
}
