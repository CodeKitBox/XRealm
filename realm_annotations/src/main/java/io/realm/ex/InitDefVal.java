package io.realm.ex;

/**
 * 列的初始化默认值，在数据库迁徙的时候使用，
 * 实现这个接口的类，只有无参数构造函数
 * @author chenxumeng
 * @param <T>
 */
public interface InitDefVal<T> {
    /**
     * 返回列的初始默认值
     * @return 返回列的初始默认值
     */
    T defVal();
}
