# 什么是 Room ？
----
谷歌为了帮助开发者解决 Android 架构设计问题，在 Google I/O 2017 发布一套帮助开发者解决 Android 架构设计的方案：Android Architecture Components，而我们的 Room 正是这套方案的两大模块之一。

- 定义：数据库解决方案
- 组成：Database、Entity、DAO

# 为什么本文叫谷歌范例？
------

为了方便开发者进行学习和理解，Google 在 GitHub 上上传了一系列的 Android Architecture Components 开源代码：[googlesamples/android-architecture-components](https://github.com/googlesamples/android-architecture-components) 本文就是通过解析这套范例的第一部分：[BasicRxJavaSample](https://github.com/googlesamples/android-architecture-components/tree/master/BasicRxJavaSample) 来对 Room 的使用进行分析。

关于本文中的代码以及后续文章中的代码，我已经上传至我的 GitHub 欢迎大家围观、star 
详见-> [FishInWater-1999/ArchitectureComponentsStudy](https://github.com/FishInWater-1999/ArchitectureComponentsStudy)

# 开始之前

----

> 首先我们需要了解下 `Room` 的基本组成

前面我们已经说过 Room 的使用，主要由 Database、Entity、DAO 三大部分组成，那么这三大组成部分又分别是什么呢？

- Database：创建一个由 Room 管理的数据库，并在其中自定义所需要操作的数据库表

###### 要求：

       1. 必须是abstract类而且的extends RoomDatabase。

       2. 必须在类头的注释中包含与数据库关联的实体列表(Entity对应的类)。

       3. 包含一个具有0个参数的抽象方法，并返回用@Dao注解的类。

###### 使用：

通过单例模式实现，你可以通过静态 getInstance(...) 方法，获取数据库实例：

`public static UsersDatabase getInstance(Context context)`

- Entity：数据库中，某个表的实体类，如：
`@Entity(tableName = "users")`
`public class User {...}`

- DAO：具体访问数据库的方法的接口
`@Dao`
`public interface UserDao {...}`

# BasicRxJavaSample 源码解析
------------------------------------

由于是源码解析，那我就以：从基础的类开始，一层层向上，抽丝剥茧，最后融为一体的方式，给大家进行解析。那么现在就让我们开始吧。

#### 表的搭建

Room 作为一个 Android 数据库操作的注解集合，最基本操作就是对我们数据库进行的。所以，先让我们试着建立一张名为 “users” 的数据表

```java
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
```

首先在表头部分，我们就见到了之前说过的 `@Entity(...)` 标签，之前说过该标签表示数据库中某个表的实体类，我们查看它的源码：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Entity {...}
```

从中我们可以知道该注解实在编译注解所在的类时触发的，这是我们注意到 Google 对该类的介绍是：

```xml
Marks a class as an entity. This class will have a mapping SQLite table in the database.
```

由此可知当注解所在的类，比如我们的这个 `User` 类编译时，相应的注解处理器就会调用其内部相应的代码，建立一个名为 `users` （在 `@Entity(tableName = "users")` 中传入的数据表 ）

**我们再往下看：**

 - @ColumnInfo(name = "userid") ：该注解注解的数据成员，将会在表中生成相应的名为：`userid` 的列
 - @PrimaryKey ：顾名思义该注解与`@ColumnInfo(name = "...")` 注解一起使用，表示表中的主键，这里要注意一点，在 `@Entity` 的源码中强调：Each entity must have at least 1 field annotated with {@link PrimaryKey}. 也就是说一个被 `@Entity(...)`  标注的数据表类中至少要有一个主键
 - @Ignore ：被该注解注释的数据成员、方法，将会被注解处理器忽略，不进行处理

这里我们发现，代码中有存在两个构造方法，为什么 GoogleSample 中会存在这种看似多此一举的情况呢？我们再仔细观察就会发想，上方的构造方法标记了 `@Ignore` 标签，而下方的构造方法却没有。由于在 `@Entity` 标注的类中，构造方法和列属性的 `get()` 方法都会被注解处理器自动识别处理。我们就不难想到，Google 之所以这样设计，是因为我们于是需要创建临时的 `User` 对象，但我们又不希望 `@Entity` 在我们调用构造方法时，就将其存入数据库。所以我们就有了这个被 `@Ignore` 的构造方法，用于创建不被自动存入数据库的临时对象，等到我们想将这个对象存入数据库时，调用`User(String id, String userName)` 即可。

#### UserDao

上面我们通过 `@Entity` 建立了一张 `users` 表，下面就让我们用 `@Dao` 注解来变写 `UserDao` 接口。

```java
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
```

按照我们正常编写的习惯，我们会在该类中，编写相应的数据库操作代码。但与之不同的是采用 `Room` 之后，我们将其变为一个接口类，并且只需要编写和设定相应的标签即可，不用再去关心存储操作的具体实现。

```java
    /**
     * 为了简便，我们只在表中存入1个用户信息
     * 这个查询语句可以获得 所有 User 但我们只需要第一个即可
     * @return
     */
    @Query("SELECT * FROM Users LIMIT 1")
    Flowable<User> getUser();
```
 这里我们看到，该查询方法使用的是 `@Query` 注解，那么这个注解的具体功能是什么呢？Google 官方对它的解释是：在一个被标注了 `@Dao` 标签的类中，用于查询的方法。顾名思义被该注解标注的方法，会被 `Room` 的注解处理器识别，当作一个数据查询方法，至于具体的查询逻辑并不需要我们关心，我们只需要将 `SQL 语句` 作为参数，传入 `@Query(...)` 中即可。之后我们发现，该方法返回的是一个背压 `Flowable<...>` 类型的对象，这是为了防止表中数据过多，读取速率远大于接收数据，从而导致内存溢出的问题，具体详见 `RxJava` 的教程，这里我就不赘述了。
 
```java
    /**
     * 想数据库中插入一条 User 对象
     * 若数据库中已存在，则将其替换
     * @param user
     * @return
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(User user);
```

我们看到，上述方法被 `@Insert` 注解所标注，从名字就能看出，这将会是一个插入方法。顾名思义被 `@Insert` 标注的方法，会用于向数据库中插入数据，唯一让我们迷茫的是括号中的这个 `onConflict`  参数，`onConflict`  意为“冲突”，再联想下我们日常生活中的数据库操作，就不难想到：这是用来设定，当插入数据库中的数据，与原数据发生冲突时的处理方法。这里我们传入的是 `OnConflictStrategy.REPLACE` ，意为“如果数据发生冲突，则用其替换掉原数据”，除此之外还有很多相应操作的参数，比如`ROLLBACK` `ABORT` 等，篇幅原因就不详细说明了，大家可以自行查阅官方文档。还有一点值得说的是这个 `Completable` ，该返回值是 `RxJava` 的基本类型，它只处理 `onComplete` `onError` 事件，可以看成是Rx的Runnable。

```java
    /**
     * 清空所有数据
     */
    @Query("DELETE FROM Users")
    void deleteAllUsers();
```
最后这个方法就是清空 `users` 表中的所有内容，很简单，这里就不做说明了。唯一需要注意的是，这里使用了 `DELETE FROM 表名` 的形式，而不是 `truncate table 表名` ，区别就在于：效率上`truncate`比`delete`快，但`truncate` 相当于保留表的结构，重新创建了这个表，所以删除后不记录日志，不可以恢复数据。

#### UsersDatabase

有关于 `Room` 的三大组成我们已经讲完了两个，现在就让我们看看最后一个 `@Database` 注解：

```java
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
```

老样子， `Google` 定义中是这么写的：将一个类标记为 `Room` 数据库。顾名思义，我们需要在标记了该标签的类里，做具体的数据库操作，比如数据库的建立、版本更新等等。我们看到，我们向其中传入了多个参数，包括：`entities` 以数组结构，标记一系列数据库中的表，这个例子中我们只有一个 `User` 表，所以只传入一个； `version` 数据库版本；`exportSchema` 用于历史版本库的导出

```java
    /**
     * 单例模式
     * volatile 确保线程安全
     * 线程安全意味着改对象会被许多线程使用
     * 可以被看作是一种 “程度较轻的 synchronized”
     */
    private static volatile UsersDatabase INSTANCE;
```

 可以看出这是一个单例模式，用于创建一个全局可获得的 UsersDatabase 对象。

```java
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
```

这是单例模式对象 INSTANCE 的获得方法，不明白的同学可以去看我这篇 [单例模式-全局可用的 context 对象，这一篇就够了](https://blog.csdn.net/qq_43377749/article/details/96324909)

#### UserDataSource

我们可以看到：绝大多数的数据库操作方法，都定义在了 `UserDao` 中，虽然一般注解类的方法不会被继承，但是有些被特殊标记的方法可能会被继承，但是我们之后要建立的很多功能类中，都需要去调用 `UserDao` 里的方法。所以我们这里定义 `UserDataSource` 接口：

```java
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
```

该接口很简单，就是一个工具，方法和  `UserDao`  一摸一样，这里我们就不赘述了。

#### LocalUserDataSource

```java
public class LocalUserDataSource implements UserDataSource {

    private final UserDao mUserDao;

    public LocalUserDataSource(UserDao userDao) {
        this.mUserDao = userDao;
    }

    @Override
    public Flowable<User> getUser() {
        return mUserDao.getUser();
    }

    @Override
    public Completable insertOrUpdateUser(User user) {
        return mUserDao.insertUser(user);
    }

    @Override
    public void deleteAllUsers() {
        mUserDao.deleteAllUsers();
    }
}
```

我们先看看官方的解析：“使用 `Room` 数据库作为一个数据源。”即通过该类的对象所持有的 `UserDao` 对象，进行数据库的增删改查操作。

- 到此为止，有关于 Room 对数据库的操作部分就讲完了，接下来我们进行视图层搭建的解析。

-----------

#### UserViewModel

首先我们先实现 `ViewModel` 类，那什么是 `ViewModel` 类呢？从字面上理解的话，它肯定是跟视图 `View` 以及数据 `Model` 相关的。其实正像它字面意思一样，它是负责准备和管理和UI组件 `Fragment/Activity` 相关的数据类，也就是说 `ViewModel` 是用来管理UI相关的数据的，同时 `ViewModel` 还可以用来负责UI组件间的通信。那么现在就来看看他的具体实现：

```java
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
```

代码结构非常简单，`mDataSource` 就是我们前面建立的 `UserDataSource` 接口对象，由于我们的数据库操作控制类：`LocalUserDataSource` 是通过是实现该接口的，所以我们就可以在外部将 `LocalUserDataSource` 对象传入，从而对他的方法进行相应的回调，也就是先实现了所需的数据库操作。每个方法的功能，我已经在注释中给出，这里就不再赘述

#### ViewModelFactory

有上面我们可以看到，我们已经有了进行数据处理的 `ViewModel` 类，那么我们这里的 `ViewModelFactory` 类又有什么作用呢？让我们先看下范例中的实现：

```java
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
```

`ViewModelFactory` 继承自 `ViewModelProvider.Factory` ，它负责帮你创建 `ViewModel` 实例。但你也许会问，我们不是已经有了 `ViewModel` 的构造方法了吗？在用 `ViewModelFactory` 不是多此一举？如果还不熟悉 `ViewModelFactory` 有关内容的，可以看下这篇：[ViewModel 和 ViewModelProvider.Factory：ViewModel 的创建者](https://blog.csdn.net/qq_43377749/article/details/100856599)

#### Injection

关于 `Injection` ，这是个帮助类，它和 Room 的逻辑功能并没有关系。`Sample` 中将其独立出来用于各个对象、类型的注入，先让我们看下该类的实现：

```java
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
```

该类有两个方法组成，实现了各个类型数据相互间的转换，想再让我们先看下第一个方法：

```java
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
```

在该方法中，我们首先接到了我们的 `context` 对象，通过 `UsersDatabase.getInstance(context)` 方法，让 `database` 持有 `context` ，实现数据库的链接和初始化。同时放回一个 `LocalUserDataSource` 对象，这样一来我们就可以对数据表中的内容惊醒相应的操作。

```java
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
```

该方法的功能非常明确，就是为我们实例化出一个 `ViewModelFactory` 对象，为我们往后创建 `ViewModel` 作准备。可以看到，这里我们调用了前面的 `provideUserDateSource` 方法，通过该方法获得了对数据库操作的 `LocalUserDataSource` 对象，这里我们就看到了单例模式使用的先见性，使得数据库不会被反复的创建、连接。

- 好了，至此所有准备工作都已经完成，让我们开始视图层 UserActivity 的调用
- 由于 `UserActivity` 的内容较多我就不贴完整的代码，我们逐步进行讲解

---------------------

#### 准备数据成员

首先我们准备了所需的给类数据成员：

```java
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
```

- 首先界面操作的各个控件
- 接这就是 `mViewModelFactory` 、 `mViewModel` 两个数据成员，用于负责数据源的操作
- 再就是一个 `CompositeDisposable` 对象，用于管理订阅事件，防止 Activity 结束后，订阅仍在进行的情况

#### onCreate

控件、数据源层、数据库等的初始化

```java
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
```

- 首先是各类控件的初始化
- 接着是 `ViewModel` 的初始化，在这过程中，也就实现了数据库的链接
- 用户信息按钮监听器绑定，点击执行 `updateUserName` 方法如下

#### updateUserName

修改数据库中用户信息

```java
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
```

- 获得新的用户名
- 将按钮设为不可点击
- 在 `io` 线程中访问数据库进行修改
- 切换到主线程进行相应处理，比如让按钮恢复到可点击状态

#### onStart

初始化用户信息，修改 `UI` 界面内容

```java
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
```

- 在 `io` 线程中进行数据库访问
- 切换到主线程，修改 `UI` 信息

#### onStop

取消订阅

```java
    @Override
    protected void onStop() {
        super.onStop();
        // 取消订阅。防止在 activity 或者 fragment 销毁后仍然占用着内存，无法释放。
        mDisposable.clear();
    }
```

-  通过我们之前实例化的 `CompositeDisposable` 对象，解除订阅关系

---------------------------------

# 总结

学会使用 `Android Architecture Components` 提供的组件简化我们的开发，能够使我们开发的应用模块更解耦更稳定，视图与数据持久层分离，以及更好的扩展性与灵活性。最后，码字不易，别忘了点个赞哦
