package com.fwest98.fingify.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fwest98.fingify.Helpers.HelperFunctions;

import java.util.Hashtable;

public class InternetConnectionManager {
    private static Hashtable<String, InternetConnectionChangeListener> listeners;

    public static void registerListener(String name, InternetConnectionChangeListener listener) {
        if(listeners == null) {
            listeners = new Hashtable<>();
        }
        if(!listeners.containsKey(name)) {
            listeners.put(name, listener);
        } else {
            listeners.remove(name);
            listeners.put(name, listener);
        }
    }

    public static void unregisterListener(String name) {
        if(listeners == null) return;
        if(listeners.containsKey(name)) listeners.remove(name);
    }

    private static void notifyChange(boolean hasInternetConnection) {
        if(listeners == null) return;

        for(InternetConnectionChangeListener listener : listeners.values()) {
            listener.internetConnectionChanged(hasInternetConnection);
        }
    }

    public static class InternetConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean hasInternetConnection = HelperFunctions.hasInternetConnection(context);
            notifyChange(hasInternetConnection);
        }
    }

    public interface InternetConnectionChangeListener {
        public void internetConnectionChanged(boolean hasInternetConnection);
    }
}
