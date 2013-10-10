package com.strollimo.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.graphics.Bitmap;
import android.util.Log;
import com.crittercism.app.Crittercism;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.strollimo.android.controller.*;
import com.strollimo.android.controller.ImageUploader.BitmapTypeAdapter;
import com.strollimo.android.controller.ImageUploader.ImageUploadTask;
import com.strollimo.android.controller.ImageUploader.ImageUploadTaskQueue;
import com.strollimo.android.model.PickupMode;
import com.strollimo.android.model.PickupModeTypeAdapter;
import com.strollimo.android.network.AmazonS3Controller;
import com.strollimo.android.network.StrollimoApi;

public class StrollimoApplication extends Application {
    public static String TAG = StrollimoApplication.class.getSimpleName();

    private static Context mContext;
    private static StrollimoApplication mInstance;

    private AccomplishableController mAccomplishableController;
    private UserService mUserService;
    private StrollimoPreferences mPrefs;
    private AmazonS3Controller mAmazonS3Controller;
    private PhotoUploadController mPhotoUploadController;
    private Gson mGson;
    private StrollimoApi mStrollimoApi;
    private ImageUploadTaskQueue mImageUploadTaskQueue;
    private Bus mBus;



    public static <T> T getService(Class<T> serviceClass) {
        return ((StrollimoApplication) mContext.getApplicationContext()).getServiceInstance(serviceClass);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mInstance = this;
        // Initialize Crittercism crash reporting only on release builds
        if (!BuildConfig.DEBUG) {
            Crittercism.init(getApplicationContext(), "51f80ad3558d6a58c1000002");
        }

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(PickupMode.class, new PickupModeTypeAdapter());
        builder.excludeFieldsWithoutExposeAnnotation();
        mGson = builder.create();
        mPrefs = new StrollimoPreferences(this, PreferenceManager.getDefaultSharedPreferences(this), mGson);
        mBus = new Bus();
        mPrefs = new StrollimoPreferences(this, getSharedPreferences("StrollimoPreferences", 0), mGson);
        mStrollimoApi = new StrollimoApi(mGson, mPrefs);
        mAmazonS3Controller = new AmazonS3Controller();
        mPhotoUploadController = new PhotoUploadController(this, mAmazonS3Controller);
        mAccomplishableController = new AccomplishableController(this, mPrefs, mPhotoUploadController, mStrollimoApi);
        mUserService = new UserService(mPrefs);
        mUserService.loadCapturedSecrets();
        mAccomplishableController.preloadPlaces();
        mImageUploadTaskQueue = ImageUploadTaskQueue.create(this, new GsonBuilder().registerTypeAdapter(Bitmap.class, new BitmapTypeAdapter()).create()); // start upload service if there were images to upload since the last time
        startService(new Intent(this, SecretStatusPollingService.class));

        Log.i(TAG, String.format("Device UUID: %s", mPrefs.getDeviceUUID()));
    }

    public <T> T getServiceInstance(Class<T> serviceClass) {
        if (serviceClass == AccomplishableController.class) {
            return (T) mAccomplishableController;
        } else if (serviceClass == UserService.class) {
            return (T) mUserService;
        } else if (serviceClass == StrollimoPreferences.class) {
            return (T) mPrefs;
        } else if (serviceClass == AmazonS3Controller.class) {
            return (T) mAmazonS3Controller;
        } else if (serviceClass == PhotoUploadController.class) {
            return (T) mPhotoUploadController;
        } else if (serviceClass == StrollimoApi.class) {
            return (T) mStrollimoApi;
        } else if (serviceClass == ImageUploadTaskQueue.class) {
            return (T) mImageUploadTaskQueue;
        } else if (serviceClass == Bus.class) {
            return (T) mBus;
        }
        return null;

    }


    public static Context getContext() {
        return StrollimoApplication.mContext;
    }

    public static StrollimoApplication getInstance() {
        return mInstance;
    }
}
