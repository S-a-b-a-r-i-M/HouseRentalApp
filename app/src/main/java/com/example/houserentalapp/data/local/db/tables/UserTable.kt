package com.example.houserentalapp.data.local.db.tables

object UserTable {
    const val TABLE_NAME = "t_user"
    const val COLUMN_ID = "id"
    const val COLUMN_NAME = "name" // Min - 3, Max - 50
    const val COLUMN_EMAIL = "email"
    const val COLUMN_PHONE = "phone"
    const val COLUMN_PROFILE_IMAGE_ADDRESS = "profile_image"
    const val COLUMN_HASHED_PASSWORD = "hashed_password"
    const val COLUMN_CREATED_AT = "created_at"
//    const val COLUMN_MODIFIED_AT = "modified_at"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_EMAIL TEXT,
            $COLUMN_PHONE TEXT UNIQUE NOT NULL UNIQUE,
            $COLUMN_PROFILE_IMAGE_ADDRESS TEXT,
            $COLUMN_HASHED_PASSWORD TEXT NOT NULL,
            $COLUMN_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
        )
    """

    const val CREATE_INDEXES = """
        CREATE INDEX idx_user_email ON $TABLE_NAME($COLUMN_EMAIL);
        CREATE INDEX idx_user_phone ON $TABLE_NAME($COLUMN_PHONE);
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}