package com.fwest98.fingify.Helpers;

import android.support.annotation.NonNull;

public interface AsyncActionCallback<T> {
    public void onCallback(@NonNull T data);
}
