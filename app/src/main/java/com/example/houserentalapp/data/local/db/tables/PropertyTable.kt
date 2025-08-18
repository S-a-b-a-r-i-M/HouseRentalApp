package com.example.houserentalapp.data.local.db.tables

// Note: Initially focus on minimal data for rental.
object PropertyTable {
    const val TABLE_NAME = "t_property"
    const val COLUMN_ID = "id"
    const val COLUMN_LANDLORD_ID = "landlord_id" // FK
    const val COLUMN_NAME = "name" // MIN - 3, MAX - 50
    const val COLUMN_DESCRIPTION = "description" // Nullable
    const val COLUMN_KIND = "kind"
    const val COLUMN_TYPE = "type"
    const val COLUMN_LOOKING_TO = "looking_to"
    const val COLUMN_TRANSACTION_TYPE = "transaction_type" // Specific to Sell
    const val COLUMN_AGE_OF_PROPERTY = "age_of_property" // Based on transaction_type
    const val COLUMN_FURNISHING_TYPE = "furnishing_type"
    const val COLUMN_COVERED_PARKING = "covered_parking"
    const val COLUMN_OPEN_PARKING = "open_parking"
    const val COLUMN_PREFERRED_TENANT_TYPE = "preferred_tenant_type"
    const val COLUMN_PREFERRED_BACHELOR_TYPE = "preferred_bachelor_type"
    const val COLUMN_IS_PET_ALLOWED = "is_pet_allowed" // ONLY 0 AND 1 (bool)
    const val COLUMN_AVAILABLE_FROM = "available_from"
    const val COLUMN_BHK = "bhk"
    const val COLUMN_BUILT_UP_AREA = "built_up_area" // In Sq. ft.
    const val COLUMN_BATHROOM_COUNT = "bathroom_count"
    const val COLUMN_TOTAL_FLOORS = "total_floors" // ONLY FOR APARTMENT
    const val COLUMN_FLOOR_NUMBER = "floor_number"
    const val COLUMN_IS_AVAILABLE = "is_available" // ONLY 0 AND 1 (bool) // LANDLORD CAN MAKE IT VISIBLE/INVISIBLE TO OTHER USERS
    const val COLUMN_VIEW_COUNT = "view_count" // Unique view count for fast access
    // ADDRESS ---------->
    const val COLUMN_STREET_NAME = "street_name"
    const val COLUMN_LOCALITY = "locality"
    const val COLUMN_CITY = "city"
    // PRICE ---------->
    const val COLUMN_PRICE = "price" // It can be rent or budget(sell).
    const val COLUMN_IS_MAINTENANCE_SEPARATE = "is_maintenance_separate" // ONLY 0 AND 1 (bool)
    const val COLUMN_MAINTENANCE_CHARGES = "maintenance_charges"
    const val COLUMN_SECURITY_DEPOSIT = "security_deposit" // none or number of months
    // TIMELINE ---------->
    const val COLUMN_CREATED_AT = "created_at"
    const val COLUMN_MODIFIED_AT = "modified_at"

    const val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_LANDLORD_ID INTEGER,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_DESCRIPTION TEXT,
            $COLUMN_STREET_NAME TEXT,
            $COLUMN_LOCALITY TEXT NOT NULL,
            $COLUMN_CITY TEXT NOT NULL,
            $COLUMN_LOOKING_TO TEXT NOT NULL,
            $COLUMN_KIND TEXT NOT NULL,
            $COLUMN_TYPE TEXT NOT NULL,
            $COLUMN_TRANSACTION_TYPE TEXT,
            $COLUMN_AGE_OF_PROPERTY INTEGER,
            $COLUMN_FURNISHING_TYPE TEXT NOT NULL,
            $COLUMN_COVERED_PARKING INTEGER NOT NULL,
            $COLUMN_OPEN_PARKING INTEGER NOT NULL,
            $COLUMN_PREFERRED_TENANT_TYPE TEXT DEFAULT 'ALL',
            $COLUMN_PREFERRED_BACHELOR_TYPE TEXT DEFAULT NULL,
            $COLUMN_IS_PET_ALLOWED INTEGER,
            $COLUMN_AVAILABLE_FROM INTEGER NOT NULL,
            $COLUMN_BHK TEXT NOT NULL,
            $COLUMN_BUILT_UP_AREA INTEGER NOT NULL,
            $COLUMN_BATHROOM_COUNT INTEGER,
            $COLUMN_PRICE INTEGER NOT NULL,
            $COLUMN_IS_MAINTENANCE_SEPARATE INTEGER,
            $COLUMN_MAINTENANCE_CHARGES INTEGER,
            $COLUMN_SECURITY_DEPOSIT INTEGER,
            $COLUMN_TOTAL_FLOORS INTEGER,
            $COLUMN_FLOOR_NUMBER INTEGER,
            $COLUMN_IS_AVAILABLE INTEGER DEFAULT 1,
            $COLUMN_VIEW_COUNT INTEGER DEFAULT 0,
            $COLUMN_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            $COLUMN_MODIFIED_AT INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
            FOREIGN KEY($COLUMN_LANDLORD_ID) REFERENCES ${UserTable.TABLE_NAME}(${UserTable.COLUMN_ID})
        )
    """

    const val CREATE_INDEX = """
        CREATE INDEX idx_city ON $TABLE_NAME($COLUMN_CITY)
        CREATE INDEX idx_locality ON $TABLE_NAME($COLUMN_LOCALITY)
        CREATE INDEX idx_furnishing_type ON $TABLE_NAME($COLUMN_FURNISHING_TYPE)
        CREATE INDEX idx_bhk ON $TABLE_NAME($COLUMN_BHK)
        CREATE INDEX idx_price ON $TABLE_NAME($COLUMN_PRICE)
    """

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}