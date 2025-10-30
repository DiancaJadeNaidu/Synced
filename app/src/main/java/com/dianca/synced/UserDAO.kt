package com.dianca.synced

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}
