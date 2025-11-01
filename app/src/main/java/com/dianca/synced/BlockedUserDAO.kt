package com.dianca.synced

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dianca.synced.BlockedUser

@Dao
interface BlockedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: BlockedUser)

    @Query("SELECT * FROM blocked_users")
    suspend fun getAll(): List<BlockedUser>

    @Query("DELETE FROM blocked_users WHERE userId = :id")
    suspend fun delete(id: String)
}
