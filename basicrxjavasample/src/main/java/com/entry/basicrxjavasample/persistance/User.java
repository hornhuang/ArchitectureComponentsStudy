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
     * 这样以来，这个注解方法就不会被传入 Room 中，做相应处理
     * @param mUserName
     */
    @Ignore
    public User(String mUserName){
        this.mId    = UUID.randomUUID().toString();
        this.mUserName = mUserName;
    }

    /**
     * 我们发现与上个方法不同，该方法没有标记 @Ignore 标签
     *
     * 所以编译时该方法会被传入 Room 中相应的注解处理器，做相应处理
     * 这里的处理应该是 add 新数据
     * @param id
     * @param userName
     */
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
