package com.wwdablu.soumya.extimageview;

public interface ResultCallBack<T> {
    void onComplete(T data);
    void onError(Throwable throwable);
}
