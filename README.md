# core-ui-extension

**UI扩展库**

### 外部引用

```
compile 'com.joy.support:core-ui-extension:0.2.2'
```

### 自身依赖

```
compile 'com.joy.support:core-ui:0.3.5'
compile 'com.joy.support:core-http:0.2.9'
compile 'com.facebook.fresco:fresco:1.5.0'
```

### 版本历史

- `0.2.2` 增加图片选择和大图浏览功能；

- `0.2.1` 删除getLayoutManager方法，以免和provideLayoutManager方法产生歧义；给mRefreshMode赋初始值；

- `0.2.0` invalidateContent方法改为final，不可覆写；适配core-ui:0.3.3中的更新；

- `0.1.9` 增加不为null的判断；

- `0.1.8` 回滚RvAdapter，fix上一版的bug；添加RvAdapterX相关，用来简化RecyclerView单、多布局的实现方式；更新FrescoImage方法，支持URL的各种scheme；

- `0.1.5` 更新core-ui到0.3.0；更新core-http到0.2.6；更新Fresco到1.5.0；

### 结构

- **Adapter**

    `RvAdapter` `RvViewHolder`

    `RvAdapterX` `RvEntityX`

- **mvp.presenters**

    `BaseHttpRvPresenter` `BaseHttpUiPresenter`

    `Presenter` `PresenterImpl`

    `RequestLauncher`

- **Photo**

    `preview`

    `select`

- **View**

    `banner`

    `fresco`

- **widget**

    `ExBaseHttpRvWidget` `ExBaseHttpWidget`

- **Extension**

    `BaseHttpLvActivity` `BaseHttpLvFragment`

    `BaseHttpRvActivity` `BaseHttpRvFragment`

    `BaseHttpUiActivity` `BaseHttpUiFragment`

### 用法

**此为MVP架构中的重要一环，提供了P层的支持；另外扩展了一些View、组件**

详见穷游APP、蓝莓APP

### Joy-Library中的引用体系

![](core-ui-extension.png)
