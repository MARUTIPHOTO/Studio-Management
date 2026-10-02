package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.CrewMember
import com.example.data.model.Customer
import com.example.data.model.EventBooking
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE mobile = :mobile LIMIT 1")
    suspend fun findCustomerByMobile(mobile: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY eventDateMillis ASC, id DESC")
    fun getAllEvents(): Flow<List<EventBooking>>

    @Query("SELECT * FROM events WHERE customerId = :customerId ORDER BY eventDateMillis ASC, id DESC")
    fun getEventsForCustomer(customerId: Long): Flow<List<EventBooking>>

    @Query("SELECT * FROM events WHERE customerId = :customerId ORDER BY eventDateMillis ASC LIMIT 1")
    suspend fun getNextEventForCustomer(customerId: Long): EventBooking?

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): EventBooking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventBooking): Long

    @Update
    suspend fun updateEvent(event: EventBooking)

    @Delete
    suspend fun deleteEvent(event: EventBooking)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM events WHERE customerId = :customerId")
    suspend fun deleteEventsForCustomer(customerId: Long)

    @Query("SELECT * FROM events WHERE customerId = :customerId AND LOWER(eventName) = LOWER(:eventName) AND eventDate = :eventDate AND eventTime = :eventTime")
    suspend fun findDuplicateEvents(customerId: Long, eventName: String, eventDate: String, eventTime: String): List<EventBooking>

    @Query("SELECT * FROM events WHERE customerId = :customerId AND LOWER(eventName) = LOWER(:eventName) AND eventDate = :eventDate AND eventTime = :eventTime AND id != :excludeEventId")
    suspend fun findDuplicateEventsExcluding(customerId: Long, eventName: String, eventDate: String, eventTime: String, excludeEventId: Long): List<EventBooking>

    @Query("SELECT * FROM events")
    suspend fun getAllEventsSync(): List<EventBooking>

    @Query("UPDATE events SET customerName = :customerName, customerMobile = :customerMobile WHERE customerId = :customerId")
    suspend fun updateCustomerInfoForEvents(customerId: Long, customerName: String, customerMobile: String)

    @Query("SELECT * FROM events WHERE eventDate = :eventDate")
    suspend fun getEventsOnDate(eventDate: String): List<EventBooking>

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventBooking>)
}

@Dao
interface CrewDao {
    @Query("SELECT * FROM crew_members ORDER BY id DESC")
    fun getAllCrew(): Flow<List<CrewMember>>

    @Query("SELECT * FROM crew_members WHERE id = :id LIMIT 1")
    suspend fun getCrewById(id: Long): CrewMember?

    @Query("SELECT * FROM crew_members WHERE mobile = :mobile LIMIT 1")
    suspend fun findCrewByMobile(mobile: String): CrewMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrew(crew: CrewMember): Long

    @Update
    suspend fun updateCrew(crew: CrewMember)

    @Delete
    suspend fun deleteCrew(crew: CrewMember)

    @Query("DELETE FROM crew_members WHERE id = :id")
    suspend fun deleteCrewById(id: Long)

    @Query("DELETE FROM crew_members")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crew: List<CrewMember>)
}

@Database(
    entities = [Customer::class, EventBooking::class, CrewMember::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun eventDao(): EventDao
    abstract fun crewDao(): CrewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maruti_studio_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
