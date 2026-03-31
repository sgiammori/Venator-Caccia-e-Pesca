package it.mygroup.org.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class CacciaPescaDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var Instance: CacciaPescaDatabase? = null

        fun getDatabase(context: Context): CacciaPescaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CacciaPescaDatabase::class.java, "prey_database")
                    .fallbackToDestructiveMigration()
                    .build().also { Instance = it }
            }
        }
    }
}
