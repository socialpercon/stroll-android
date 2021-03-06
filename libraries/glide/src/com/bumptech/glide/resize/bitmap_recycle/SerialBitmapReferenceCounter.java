package com.bumptech.glide.resize.bitmap_recycle;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

public class SerialBitmapReferenceCounter implements BitmapReferenceCounter {

    private static class InnerTrackerPool {
        private LinkedList<InnerTracker> pool = new LinkedList<InnerTracker>();

        public InnerTracker get() {
            InnerTracker result = pool.poll();
            if (result == null) {
                result = new InnerTracker();
            }

            return result;
        }

        public void release(InnerTracker innerTracker) {
            pool.offer(innerTracker);
        }
    }

    private static class InnerTracker {
        private int refs = 0;
        private boolean pending = false;

        public void acquire() {
            pending = false;
            refs++;
        }

        public boolean release() {
            refs--;

            return refs == 0 && !pending;
        }

        public boolean reject() {
            pending = false;
            return refs == 0;
        }

        public void markPending() {
            pending = true;
        }
    }

    private final Map<Bitmap, InnerTracker> counter = new WeakHashMap<Bitmap, InnerTracker>();
    private final BitmapPool target;
    private final InnerTrackerPool pool = new InnerTrackerPool();

    public SerialBitmapReferenceCounter(BitmapPool target) {
        this.target = target;
    }

    @Override
    public void initBitmap(Bitmap toInit) {
        final InnerTracker tracker = counter.get(toInit);
        if (tracker == null) {
            counter.put(toInit, pool.get());
        }
    }

    @Override
    public void acquireBitmap(Bitmap bitmap) {
        counter.get(bitmap).acquire();
    }

    @Override
    public void releaseBitmap(Bitmap bitmap) {
        final InnerTracker tracker = counter.get(bitmap);
        if (tracker.release()) {
            recycle(tracker, bitmap);
        }
    }

    @Override
    public void rejectBitmap(Bitmap bitmap) {
        final InnerTracker tracker = counter.get(bitmap);
        if (tracker.reject()) {
            recycle(tracker, bitmap);
        }
    }

    @Override
    public void markPending(Bitmap bitmap) {
        counter.get(bitmap).markPending();
    }

    private void recycle(InnerTracker tracker, Bitmap bitmap) {
        target.put(bitmap);
        counter.remove(bitmap);
        pool.release(tracker);
    }
}
