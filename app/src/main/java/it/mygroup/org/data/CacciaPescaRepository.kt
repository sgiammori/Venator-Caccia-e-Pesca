package it.mygroup.org.data

import kotlinx.coroutines.flow.Flow

interface CacciaPescaRepository {
    /**
     * Retrieve all the items from the given data source with the given [month] [year].
     */
    fun getAllItemsOfMonthStream(month: String, year: String): Flow<List<Item>>

    /**
     * Retrieve items from the given data source that match with the given [day] [month] [year].
     */
    fun getItemsByDayStream(day: String, month: String, year: String): Flow<List<Item>>

    /**
     * Retrieve an item from the given data source that matches with the given [preyName] [day] [month] [year].
     */
    fun getItemsByNameAndDayStream(preyName: String, day: String, month: String, year: String): Flow<Item>

    /**
     * Retrieve all items from the given data source for a specific [year].
     */
    fun getItemsByYearStream(year: String): Flow<List<Item>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getItemStream(id: Int): Flow<Item?>

    /**
     * Retrieve all distinct years from the data source.
     */
    fun getYearsStream(): Flow<List<String>>

    /**
     * Retrieve items by [ActivityType] (HUNTING or FISHING).
     */
    fun getItemsByActivityStream(type: ActivityType): Flow<List<Item>>

    /**
     * Insert item in the data source
     */
    suspend fun insertItem(item: Item)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(item: Item)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(item: Item)
}
