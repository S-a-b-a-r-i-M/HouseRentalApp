package com.example.houserentalapp.data.local.db.tables

object PropertySocialAmenitiesTable {
    const val TABLE_NAME = "t_property_social_amenities"
    const val COLUMN_ID = "id"
    const val COLUMN_PROPERTY_ID = "property_id"
    const val COLUMN_AMENITY = "amenity_id"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_PROPERTY_ID INTEGER,
            $COLUMN_AMENITY TEXT,
            UNIQUE($COLUMN_PROPERTY_ID,  $COLUMN_AMENITY),
            FOREIGN KEY($COLUMN_PROPERTY_ID) REFERENCES ${PropertyTable.TABLE_NAME}(${PropertyTable.COLUMN_ID})
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}