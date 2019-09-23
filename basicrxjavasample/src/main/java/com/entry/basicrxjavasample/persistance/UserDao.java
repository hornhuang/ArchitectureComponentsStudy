package com.entry.basicrxjavasample.persistance;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface UserDao {

    /**
     * 为了简便，我们只在表中存入1个用户信息
     * 这个查询语句可以获得 所有 User 但我们只需要第一个即可
     * @return
     */
    @Query("SELECT * FROM Users LIMIT 1")
    Flowable<User> getUser();

    /**
     * 想数据库中插入一条 User 对象
     * 若数据库中已存在，则将其替换
     * @param user
     * @return
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(User user);

    /**
     * 清空所有数据
     */
    @Query("DELETE FROM Users")
    void deleteAllUsers();

}
