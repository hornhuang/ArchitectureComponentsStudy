package com.entry.basicrxjavasample.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.entry.basicrxjavasample.Injection;
import com.entry.basicrxjavasample.R;

import org.reactivestreams.Subscriber;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = UserActivity.class.getSimpleName();

    private TextView mUserName;

    private EditText mUserNameInput;

    private Button mUpdateButton;
    // 一个 ViewModel 用于获得 Activity & Fragment 实例
    private ViewModelFactory mViewModelFactory;
    // 用于访问数据库
    private UserViewModel mViewModel;
    // disposable 是订阅事件，可以用来取消订阅。防止在 activity 或者 fragment 销毁后仍然占用着内存，无法释放。
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUserName = findViewById(R.id.user_name);
        mUserNameInput = findViewById(R.id.user_name_input);
        mUpdateButton = findViewById(R.id.update_user);

        // 实例化 ViewModelFactory 对象，准备实例化 ViewModel
        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(UserViewModel.class);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserName();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 观察者模式
        // 通过 ViewModel 从数据库中读取 UserName 显示
        // 如果读取失败，显示错误信息
        mDisposable.add(mViewModel.getUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                mUserName.setText(s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, "Unable to update username");
            }
        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消订阅。防止在 activity 或者 fragment 销毁后仍然占用着内存，无法释放。
        mDisposable.clear();
    }

    private void updateUserName() {
        String userName = mUserNameInput.getText().toString();
        // 在完成用户名更新之前禁用“更新”按钮
        mUpdateButton.setEnabled(false);
        // 开启观察者模式
        // 更新用户信息，结束后重新开启按钮
        mDisposable.add(mViewModel.updateUserName(userName)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action() {
            @Override
            public void run() throws Exception {
                mUpdateButton.setEnabled(true);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.d(TAG, "accept: Unable to update username");
            }
        }));
    }
}
