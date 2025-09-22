package com.example.houserentalapp.data.local.db.tables

object LeadPropertyMappingTable {
    const val TABLE_NAME = "t_lead_property_mapping"
    const val COLUMN_LEAD_ID = "lead_id"
    const val COLUMN_PROPERTY_ID = "property_id"
    const val COLUMN_CREATED_AT = "created_at"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_LEAD_ID INTEGER,
            $COLUMN_PROPERTY_ID INTEGER,
            $COLUMN_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            PRIMARY KEY($COLUMN_LEAD_ID, $COLUMN_PROPERTY_ID),
            FOREIGN KEY($COLUMN_LEAD_ID) REFERENCES ${LeadTable.TABLE_NAME}(${LeadTable.COLUMN_ID}),
            FOREIGN KEY($COLUMN_PROPERTY_ID) REFERENCES ${PropertyTable.TABLE_NAME}(${PropertyTable.COLUMN_ID})
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}