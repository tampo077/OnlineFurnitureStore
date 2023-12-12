package com.test.onlinefurniturestore

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteDb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CartDatabase"
        private const val TABLE_CART = "cart"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PRODUCT_ID = "productId"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_IMAGE_URL = "imageUrl"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCartTable = ("CREATE TABLE " + TABLE_CART + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PRODUCT_ID + " TEXT,"
                + COLUMN_NAME + " TEXT," + COLUMN_PRICE + " REAL,"
                + COLUMN_QUANTITY + " INTEGER," + COLUMN_IMAGE_URL + " TEXT" + ")")
        db.execSQL(createCartTable)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        onCreate(db)
    }

    fun add_to_cart(productId: String, name: String, price: Double, imageUrl: String): Boolean {
        val db = this.writableDatabase
        val cursor = db.query(TABLE_CART, arrayOf(COLUMN_QUANTITY), "$COLUMN_PRODUCT_ID=?", arrayOf(productId), null, null, null)
        var isNewProduct = false

        if (cursor != null && cursor.moveToFirst()) {
            val currentQuantity = cursor.getInt(0)
            update_qty(productId, currentQuantity + 1)
            cursor.close()
        } else {
            val values = ContentValues()
            values.put(COLUMN_PRODUCT_ID, productId)
            values.put(COLUMN_NAME, name)
            values.put(COLUMN_PRICE, price)
            values.put(COLUMN_QUANTITY, 1)
            values.put(COLUMN_IMAGE_URL, imageUrl)

            db.insert(TABLE_CART, null, values)
            isNewProduct = true
        }

        db.close()
        return isNewProduct
    }


    fun update_qty(productId: String, newQuantity: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_QUANTITY, newQuantity)

        db.update(TABLE_CART, values, "$COLUMN_PRODUCT_ID=?", arrayOf(productId))
        db.close()
    }

    fun get_all_cart_items(): List<CartProduct> {
        val productList = ArrayList<CartProduct>()
        val selectQuery = "SELECT  * FROM $TABLE_CART"

        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val product = CartProduct(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getInt(4),
                    cursor.getString(5)
                )
                productList.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return productList
    }

    fun delete_cart_item(pid: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CART, "$COLUMN_PRODUCT_ID=?", arrayOf(pid))
        db.close()
    }

    fun empty_cart() {
        val db = this.writableDatabase
        db.delete(TABLE_CART, null, null)
        db.close()
    }

    fun get_cart_total(): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COLUMN_PRICE * $COLUMN_QUANTITY) FROM $TABLE_CART", null)
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    fun get_count(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CART", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }
}


data class CartProduct(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = ""
)

