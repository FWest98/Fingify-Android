package com.fwest98.fingify.Helpers;

import android.os.Handler;

import lombok.Setter;

public class TotpCountdown implements Runnable {
    private final int iterationTime;
    private final Handler handler = new Handler();

    private boolean shouldStop;
    @Setter private Listener listener;

    public interface Listener {
        void onTotpCountdown();
    }

    public TotpCountdown(int iterationTime) {
        this.iterationTime = iterationTime;
    }

    public void startAndNotifyListener() {
        if(shouldStop) {
            throw new IllegalStateException("Task already stopped");
        }
        run();
    }

    public void stop() { shouldStop = true; }

    @Override
    public void run() {
        if(shouldStop) return;

        fireTotpCountown();

        scheduleNext();
    }

    private void scheduleNext() {
        handler.postDelayed(this, iterationTime);
    }

    private void fireTotpCountown() {
        if(listener == null || shouldStop) return;
        listener.onTotpCountdown();
    }
}
