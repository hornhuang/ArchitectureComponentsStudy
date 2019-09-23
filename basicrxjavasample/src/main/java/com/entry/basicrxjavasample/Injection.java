package com.entry.basicrxjavasample;

import android.content.Context;

import com.entry.basicrxjavasample.persistance.LocalUserDataSource;
import com.entry.basicrxjavasample.persistance.UsersDatabase;
import com.entry.basicrxjavasample.ui.ViewModelFactory;

/**
 * Injection: 注入
 *
 */
public class Injection {

    /**
     * 通过该方法实例化出能操作数据库的 LocalUserDataSource 对象
     * @param context
     * @return
     */
    public static UserDataSource provideUserDateSource(Context context) {
        // 获得 RoomDatabase
        UsersDatabase database = UsersDatabase.getInstance(context);
        // 将可操作 UserDao 传入
        // 实例化出可操作 LocalUserDataSource 对象方便对数据库进行操作
        return new LocalUserDataSource(database.userDao());
    }

    /**
     * 获得 ViewModelFactory 对象
     * 为 ViewModel 实例化作准备
     * @param context
     * @return
     */
    public static ViewModelFactory provideViewModelFactory(Context context) {
        UserDataSource dataSource = provideUserDateSource(context);
        return new ViewModelFactory(dataSource);
    }

}
