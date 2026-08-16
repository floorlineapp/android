package com.thefloor.app.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

/**
 * Offline read cache. NEVER authoritative — every row is a snapshot with
 * fetchedAt, and balances/referral numbers always re-render from the API
 * when connectivity returns.
 */

@Entity(tableName = "cached_communities")
data class CachedCommunityEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val name: String,
    val description: String,
    val kind: String,
    val memberCount: Int,
    val isRestricted: Boolean,
    val membershipState: String,
    val fetchedAt: Long,
)

@Entity(tableName = "cached_posts")
data class CachedPostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorLevel: String?,
    val categoryId: String,
    val categoryName: String,
    val communityId: String?,
    val body: String,
    val commentCount: Int,
    val reactionCount: Int,
    val myReaction: String?,
    val saved: Boolean,
    val createdAt: String,
    val fetchedAt: Long,
)

@Entity(tableName = "cached_notifications")
data class CachedNotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val deepLink: String?,
    val read: Boolean,
    val createdAt: String,
    val fetchedAt: Long,
)

@Dao
interface CommunityDao {
    @Query("SELECT * FROM cached_communities ORDER BY memberCount DESC")
    fun observeAll(): Flow<List<CachedCommunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedCommunityEntity>)

    @Query("DELETE FROM cached_communities")
    suspend fun clear()
}

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts ORDER BY createdAt DESC LIMIT 50")
    fun observeFeed(): Flow<List<CachedPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedPostEntity>)

    @Query("DELETE FROM cached_posts")
    suspend fun clear()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM cached_notifications ORDER BY createdAt DESC LIMIT 100")
    fun observeAll(): Flow<List<CachedNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedNotificationEntity>)

    @Query("DELETE FROM cached_notifications")
    suspend fun clear()
}

@Database(
    entities = [CachedCommunityEntity::class, CachedPostEntity::class, CachedNotificationEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FloorDatabase : RoomDatabase() {
    abstract fun communityDao(): CommunityDao
    abstract fun postDao(): PostDao
    abstract fun notificationDao(): NotificationDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): FloorDatabase =
        Room.databaseBuilder(context, FloorDatabase::class.java, "floor.db")
            .fallbackToDestructiveMigration() // cache-only DB: destructive is correct
            .build()

    @Provides fun communityDao(db: FloorDatabase): CommunityDao = db.communityDao()
    @Provides fun postDao(db: FloorDatabase): PostDao = db.postDao()
    @Provides fun notificationDao(db: FloorDatabase): NotificationDao = db.notificationDao()
}
