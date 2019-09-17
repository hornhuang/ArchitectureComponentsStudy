package com.entry.basicrxjavasample.ui;

import androidx.lifecycle.ViewModel;

import com.entry.basicrxjavasample.UserDataSource;
import com.entry.basicrxjavasample.persistance.User;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * UserActivity 的 ViewModel
 * 作用：
 */
public class UserViewModel extends ViewModel {

    /**
     * UserDataSource 接口
     */
    private final UserDataSource mDataSource;

    private User mUser;

    public UserViewModel(UserDataSource dataSource){
        this.mDataSource = dataSource;
    }

    /**
     * 从数据库中读取
     * @return
     */
    public Flowable<String> getUserName(){
        return mDataSource.getUser()
                .map(new Function<User, String>() {
                    @Override
                    public String apply(User user) throws Exception {
                        return user.getUserName();
                    }
                });
    }

}
