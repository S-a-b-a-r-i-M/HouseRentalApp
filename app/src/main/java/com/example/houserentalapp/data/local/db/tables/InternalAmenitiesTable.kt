package com.example.houserentalapp.data.local.db.tables

object InternalAmenitiesTable {
    const val TABLE_NAME = "t_internal_amenities"
    const val COLUMN_ID = "id"
    const val COLUMN_NAME = "name"
    const val COLUMN_HAS_COUNT = "has_count"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT UNIQUE NOT NULL,
            $COLUMN_HAS_COUNT INTEGER DEFAULT 0
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}