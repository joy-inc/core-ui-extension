package com.joy.ui.extension.mvp.presenters;

/**
 * Created by KEVIN.DAI on 16/1/18.
 */
public interface Presenter<V> {

    void attachView(V v);

    void detachView();
}
