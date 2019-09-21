package com.entry.basicrxjavasample;

import com.entry.basicrxjavasample.persistance.User;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface UserDataSource {

    /**
     * 从数据库中读取信息
     * 由于读取速率可能 远大于 观察者处理速率，故使用背压 Flowable 模式
     * Flowable：https://www.jianshu.com/p/ff8167c1d191/
     */
    Flowable<User> getUser();


    /**
     * 将数据写入数据库中
     * 如果数据已经存在则进行更新
     * Completable 可以看作是 RxJava 的 Runnale 接口
     * 但他只能调用 onComplete 和 onError 方法，不能进行 map、flatMap 等操作
     * Completable：https://www.jianshu.com/p/45309538ad94
     */
    Completable insertOrUpdateUser(User user);


    /**
     * 删除所有表中所有 User 对象
     */
    void  deleteAllUsers();

}
