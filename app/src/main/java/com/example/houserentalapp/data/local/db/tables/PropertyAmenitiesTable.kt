package com.example.houserentalapp.data.local.db.tables

object PropertyAmenitiesTable {
    const val TABLE_NAME = "t_property_amenities"
    const val COLUMN_ID = "id"
    const val COLUMN_PROPERTY_ID = "property_id"
    const val COLUMN_AMENITY = "amenity"
    const val COLUMN_AMENITY_TYPE = "amenity_type"
    const val COLUMN_COUNT = "count"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_PROPERTY_ID INTEGER,
            $COLUMN_AMENITY TEXT,
            $COLUMN_AMENITY_TYPE TEXT NOT NULL,
            $COLUMN_COUNT INTEGER DEFAULT NULL, -- Only used for COUNTABLE AMENITY TYPES
            UNIQUE($COLUMN_PROPERTY_ID, $COLUMN_AMENITY),
            FOREIGN KEY(${COLUMN_PROPERTY_ID}) 
            REFERENCES ${PropertyTable.TABLE_NAME}(${PropertyTable.COLUMN_ID}) ON DELETE CASCADE
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}