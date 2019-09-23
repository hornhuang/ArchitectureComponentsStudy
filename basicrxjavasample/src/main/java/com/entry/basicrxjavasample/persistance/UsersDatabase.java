package com.entry.basicrxjavasample.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 由 Room 控制的数据库中的 User 表格
 *
 * 抽象类不能实例化对象，其它功能依然存在，成员变量、成员方法和构造方法的访问方式和普通类一样。
 * 由于抽象类不能实例化对象，所以抽象类必须被继承，才能被使用。
 */
@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UsersDatabase extends RoomDatabase {
    /**
     * 单例模式
     * volatile 确保线程安全
     * 线程安全意味着改对象会被许多线程使用
     * 可以被看作是一种 “程度较轻的 synchronized”
     */
    private static volatile UsersDatabase INSTANCE;

    /**
     * 该方法由于获得 DataBase 对象
     * abstract
     * @return
     */
    public abstract UserDao userDao();

    public static UsersDatabase getInstance(Context context) {
        // 若为空则进行实例化
        // 否则直接返回
        if (INSTANCE == null) {
            synchronized (UsersDatabase.class) {
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UsersDatabase.class, "Sample.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
