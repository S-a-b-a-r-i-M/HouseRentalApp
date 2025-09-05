package com.example.houserentalapp.data.local.db.tables

object SearchHistoryTable {
    const val TABLE_NAME = "t_search_history"
    const val COLUMN_USER_ID = "user_id"
    const val COLUMN_QUERY = "query"
    const val COLUMN_META = "meta"
    const val COLUMN_CREATED_AT = "created_at"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_USER_ID INTEGER,
            $COLUMN_QUERY TEXT NOT NULL,
            $COLUMN_META TEXT,
            $COLUMN_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            PRIMARY KEY($COLUMN_USER_ID, $COLUMN_QUERY),
            FOREIGN KEY($COLUMN_USER_ID) REFERENCES ${UserTable.TABLE_NAME}(${UserTable.COLUMN_ID}) ON DELETE CASCADE
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}