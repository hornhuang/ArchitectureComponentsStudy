package com.entry.basicrxjavasample.ui;

import androidx.lifecycle.ViewModel;

import com.entry.basicrxjavasample.UserDataSource;
import com.entry.basicrxjavasample.persistance.User;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * UserActivity 的 ViewModel
 * 作用：结合 RxJava ，实现对数据库的各类操作
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
     * 从数据库中读取所有 user 名称
     * @return 背压形式发出所有 User 的名字
     *
     * 由于数据库中 User 量可能很大，可能会因为背压导致内存溢出
     * 故采用 Flowable 模式，取代 Observable
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

    /**
     * 更新/添加 数据
     *
     * 判断是否为空，若为空则创建新 User 进行存储
     * 若不为空，说明该 User 存在，这获得其主键 'getId()' 和传入的新 Name 拼接，生成新 User 存储
     * 通过 insertOrUpdateUser 接口，返回 Comparable 对象，监听是否存储成功
     * @param userName
     * @return
     */
    public Completable updateUserName(String userName) {
        mUser = mUser == null
                ? new User(userName)
                : new User(mUser.getId(), userName);
        return mDataSource.insertOrUpdateUser(mUser);
    }
}
