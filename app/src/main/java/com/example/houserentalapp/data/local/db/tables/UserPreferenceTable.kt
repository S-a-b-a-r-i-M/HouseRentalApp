package com.example.houserentalapp.data.local.db.tables

object UserPreferenceTable {
    const val TABLE_NAME = "t_user_preference"
    const val COLUMN_USER_ID = "user_id"
    const val COLUMN_CITY = "city"
    const val COLUMN_LOOKING_TO = "looking_to"
    const val COLUMN_BHK = "bhk"
//    const val COLUMN_MIN_PRICE = "min_price"
//    const val COLUMN_MAX_PRICE = "max_price"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_USER_ID INTEGER PRIMARY KEY,
            $COLUMN_CITY TEXT,
            $COLUMN_LOOKING_TO TEXT,
            $COLUMN_BHK TEXT,
            FOREIGN KEY($COLUMN_USER_ID) REFERENCES ${UserTable.TABLE_NAME}(${UserTable.COLUMN_ID}) ON DELETE CASCADE
        )
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}