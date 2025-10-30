package com.dianca.synced

import androidx.room.*

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests WHERE synced = 0")
    suspend fun getPendingRequests(): List<RequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestEntity)

    @Update
    suspend fun updateRequest(request: RequestEntity)
}
