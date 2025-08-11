package com.example.houserentalapp.data.local.db.tables

object PropertyInternalAmenitiesTable {
    const val TABLE_NAME = "t_property_internal_amenities"
    const val COLUMN_PROPERTY_ID = "property_id"
    const val COLUMN_AMENITY_ID = "amenty_id"
    const val COLUMN_COUNT = "count"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_PROPERTY_ID INTEGER,
            $COLUMN_AMENITY_ID INTEGER,
            $COLUMN_COUNT INTEGER DEFAULT NULL,
            FOREIGN KEY(${COLUMN_PROPERTY_ID}) REFERENCES ${PropertyTable.TABLE_NAME}(${PropertyTable.COLUMN_ID}),
            FOREIGN KEY(${COLUMN_AMENITY_ID}) REFERENCES ${InternalAmenitiesTable.TABLE_NAME}(${InternalAmenitiesTable.COLUMN_ID})
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}