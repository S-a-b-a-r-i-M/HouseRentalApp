package com.example.houserentalapp.data.local.db.tables

object PropertyImagesTable {
    const val TABLE_NAME = "t_property_images"
    const val COLUMN_ID = "id"
    const val COLUMN_PROPERTY_ID = "property_id"
    const val COLUMN_IMAGE_ADDRESS = "image_address"
    const val COLUMN_IS_PRIMARY = "is_primary"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_PROPERTY_ID INTEGER,
            $COLUMN_IMAGE_ADDRESS TEXT NOT NULL,
            $COLUMN_IS_PRIMARY INTEGER DEFAULT 0,
            FOREIGN KEY($COLUMN_PROPERTY_ID) REFERENCES ${PropertyTable.TABLE_NAME}(${PropertyTable.COLUMN_ID})
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}