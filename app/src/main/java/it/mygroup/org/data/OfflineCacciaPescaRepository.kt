package it.mygroup.org.data

import kotlinx.coroutines.flow.Flow

class OfflineCacciaPescaRepository(private val itemDao: ItemDao) : CacciaPescaRepository {

    override fun getAllItemsOfMonthStream(
        month: String,
        year: String
    ): Flow<List<Item>> = if (month != "0") itemDao.getAllItemsOfMonth(month, year)
    else itemDao.getItemsByYear(year)

    override fun getItemsByDayStream(
        day: String,
        month: String,
        year: String
    ): Flow<List<Item>> =
        itemDao.getItemsByDay(day, month, year)

    override fun getItemsByNameAndDayStream(
        preyName: String,
        day: String,
        month: String,
        year: String
    ): Flow<Item> =
        itemDao.getItemByNameAndDay(preyName, day, month, year)

    override fun getItemsByYearStream(year: String): Flow<List<Item>> =
        itemDao.getItemsByYear(year)

    override fun getYearsStream(): Flow<List<String>> = itemDao.getYears()

    override fun getItemStream(id: Int): Flow<Item?> = itemDao.getItem(id)

    override fun getItemsByActivityStream(type: ActivityType): Flow<List<Item>> =
        itemDao.getItemsByActivity(type)

    override suspend fun insertItem(item: Item) = itemDao.insert(item)

    override suspend fun deleteItem(item: Item) = itemDao.delete(item)

    override suspend fun updateItem(item: Item) = itemDao.update(item)
}
