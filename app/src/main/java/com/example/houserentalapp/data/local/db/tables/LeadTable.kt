package com.example.houserentalapp.data.local.db.tables

object LeadTable {
    const val TABLE_NAME = "t_lead"
    const val COLUMN_ID = "id"
    const val COLUMN_TENANT_ID = "tenant_id"
    const val COLUMN_LANDLORD_ID = "landlord_id"
    const val COLUMN_NOTE = "note"
    const val COLUMN_CREATED_AT = "created_at"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TENANT_ID INTEGER,
            $COLUMN_LANDLORD_ID INTEGER,
            $COLUMN_NOTE TEXT,
            $COLUMN_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            UNIQUE($COLUMN_TENANT_ID, $COLUMN_LANDLORD_ID),
            FOREIGN KEY($COLUMN_TENANT_ID) REFERENCES ${UserTable.TABLE_NAME}(${UserTable.COLUMN_ID}),
            FOREIGN KEY($COLUMN_LANDLORD_ID) REFERENCES ${UserTable.TABLE_NAME}(${UserTable.COLUMN_ID})
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}