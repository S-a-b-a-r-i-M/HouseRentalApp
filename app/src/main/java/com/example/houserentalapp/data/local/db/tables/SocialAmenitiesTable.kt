package com.example.houserentalapp.data.local.db.tables

object SocialAmenitiesTable {
    const val TABLE_NAME = "t_social_amenities"
    const val COLUMN_ID = "id"
    const val COLUMN_NAME = "name"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT NOT NULL UNIQUE 
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}