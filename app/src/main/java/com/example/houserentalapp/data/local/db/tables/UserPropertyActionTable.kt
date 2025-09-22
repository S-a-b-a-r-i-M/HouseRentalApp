package com.example.houserentalapp.data.local.db.tables

object UserPropertyActionTable {
    const val TABLE_NAME = "t_user_shortlisted_property"
    const val COLUMN_ID = "id"
    const val COLUMN_TENANT_ID = "tenant_id"
    const val COLUMN_PROPERTY_ID = "property_id"
    const val COLUMN_ACTION = "action" // VIEW, FAVOURITES, INTERESTED
    const val COLUMN_CREATED_AT = "created_at"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TENANT_ID INTEGER,
            $COLUMN_PROPERTY_ID INTEGER,
            $COLUMN_ACTION TEXT NOT NULL,
            $COLUMN_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            UNIQUE($COLUMN_TENANT_ID, $COLUMN_PROPERTY_ID, $COLUMN_ACTION),
            FOREIGN KEY($COLUMN_TENANT_ID) REFERENCES ${UserTable.TABLE_NAME}(${UserTable.COLUMN_ID}),
            FOREIGN KEY($COLUMN_PROPERTY_ID) REFERENCES ${PropertyTable.TABLE_NAME}(${PropertyTable.COLUMN_ID})
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}