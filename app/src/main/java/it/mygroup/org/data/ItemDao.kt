package it.mygroup.org.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * from prey_items WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    @Query("SELECT DISTINCT year from prey_items ORDER BY CAST(year AS INTEGER) ASC")
    fun getYears(): Flow<List<String>>

    @Query("SELECT * from prey_items WHERE preyName = :name AND day = :day AND month = :month AND year = :year")
    fun getItemByNameAndDay(name: String, day: String, month: String, year: String): Flow<Item>

    @Query("SELECT * from prey_items WHERE day = :day AND month = :month AND year = :year")
    fun getItemsByDay(day: String, month: String, year: String): Flow<List<Item>>

    @Query("SELECT * from prey_items WHERE year = :year ORDER BY CAST(month AS INTEGER) ASC, CAST(day AS INTEGER) ASC")
    fun getItemsByYear(year: String): Flow<List<Item>>

    @Query("SELECT * from prey_items WHERE month = :month AND year = :year ORDER BY CAST(day AS INTEGER) ASC")
    fun getAllItemsOfMonth(month: String, year: String): Flow<List<Item>>

    @Query("SELECT * from prey_items WHERE activityType = :type ORDER BY timestamp DESC")
    fun getItemsByActivity(type: ActivityType): Flow<List<Item>>
}
