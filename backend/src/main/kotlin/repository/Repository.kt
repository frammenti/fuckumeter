package dev.frammenti.fuckumeter.repository

import dev.frammenti.fuckumeter.db.Database
import dev.frammenti.fuckumeter.db.DatabaseContext

abstract class Repository(database: Database) : DatabaseContext by database
