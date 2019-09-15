package com.entry.basicrxjavasample.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 *
 * @ViewModelProvider.Factory: ViewModelProvider 中，用于创建 ViewModel 实例的接口
 */

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final UserDataSource mDataSource;

    public ViewModelFactory(UserDataSource dataSource) {
        mDataSource = dataSource;
    }

    // 你需要通过 ViewModelProvider.Factory 的 create 方法来创建(自定义的) ViewModel
    // 参考文档：https://medium.com/koderlabs/viewmodel-with-viewmodelprovider-factory-the-creator-of-viewmodel-8fabfec1aa4f
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // 为什么这里用 isAssignableFrom 来判断传入的 modelClass 类的类型， 而不直接用 isInstance 判断？
        // 答：二者功能一样，但如果传入值（modelClass 为空）则 isInstance 会报错奔溃，而 isAssignableFrom 不会
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(mDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
