package com.entry.basicrxjavasample.persistance;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * 应用测试的表结构模型
 */
@Entity(tableName = "users")// 表名注解
public class User {

    /**
     * 主键
     * 由于主键不能为空，所以需要 @NonNull 注解
     */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "userid")// Room 列注解
    private String mId;

    /**
     * 用户名
     * 普通列
     */
    @ColumnInfo(name = "username")
    private String mUserName;

    /**
     * 构造方法
     * 设置为 @Ignore 将其忽视
     * 忽视该方法原因未知
     * @param mUserName
     */
    @Ignore
    public User(String mUserName){
        this.mId    = UUID.randomUUID().toString();
        this.mUserName = mUserName;
    }

    public User(String id, String userName) {
        this.mId = id;
        this.mUserName = userName;
    }

    public String getId() {
        return mId;
    }

    public String getUserName() {
        return mUserName;
    }
}
