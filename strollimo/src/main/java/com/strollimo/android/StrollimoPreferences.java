package com.strollimo.android;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.strollimo.android.model.Mystery;
import com.strollimo.android.model.PickupMode;
import com.strollimo.android.model.PickupModeTypeAdapter;
import com.strollimo.android.model.Secret;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StrollimoPreferences {
    private static final String COIN_VALUE_KEY = "COIN_VALUE_KEY";
    private static final String CAPTURE_PLACE_KEY = "CAPTURED_PLACE_";
    private static final String CAPTURED_PLACES_NUM_KEY = "CAPTURED_PLACES_NUM";
    public static final String USE_BARCODE_KEY = "USE_BARCODE";
    public static final String DEBUG_MODE_ON = "DEBUG_MODE_ON";
    public static final String MISSIONS_KEY = "mmissions_";
    public static final String SECRET_KEY = "SECRET";
    private SharedPreferences mPrefs;
    private final Gson mGson;

    public StrollimoPreferences(SharedPreferences prefs) {
        mPrefs = prefs;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(PickupMode.class, new PickupModeTypeAdapter());
        builder.excludeFieldsWithoutExposeAnnotation();
        mGson = builder.create();
    }

    public boolean isUseBarcode() {
        return mPrefs.getBoolean(USE_BARCODE_KEY, false);
    }

    public void setUseBarcode(boolean useBarcode) {
        mPrefs.edit().putBoolean(USE_BARCODE_KEY, useBarcode).apply();
    }

    public int getCoins() {
        return mPrefs.getInt(COIN_VALUE_KEY, 0);
    }

    public void saveCoins(int coins) {
        mPrefs.edit().putInt(COIN_VALUE_KEY, coins).apply();
    }

    public HashSet<String> getCapturedPlaces() {
        HashSet<String> places = new HashSet<String>();
        int maxCaptured = mPrefs.getInt(CAPTURED_PLACES_NUM_KEY, 0);
        for (int i = 1; i < maxCaptured + 1; i++) {
            String placeId = mPrefs.getString(CAPTURE_PLACE_KEY + i, "");
            if (placeId != "") {
                places.add(placeId);
            }
        }
        return places;
    }

    public void clearCapturedPlaces() {
        mPrefs.edit().putInt(CAPTURED_PLACES_NUM_KEY, 0).apply();
    }

    public void setDebugModeOn(boolean debugModeOn) {
        mPrefs.edit().putBoolean(DEBUG_MODE_ON, debugModeOn).apply();
    }

    public boolean isDebugModeOn() {
        return mPrefs.getBoolean(DEBUG_MODE_ON, false);
    }

    public void saveSecret(Secret secret) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SECRET_KEY + secret.getId(), mGson.toJson(secret));
        editor.apply();
    }

    public Secret getSecret(String id) {
        String json = mPrefs.getString(SECRET_KEY+ id, "");
        return mGson.fromJson(json, Secret.class);
    }

    public List<Mystery> getMissions() {
        String json = mPrefs.getString(MISSIONS_KEY, "");
        Type listType = new TypeToken<ArrayList<Mystery>>() {}.getType();
        List<Mystery> mysteries = mGson.fromJson(json, listType);
        for (Mystery mystery : mysteries) {
            for (String secretId : mystery.getChildren()) {
                mystery.addSecret(getSecret(secretId));
            }
        }
        return mysteries;
    }

    public void saveMissions(List<Mystery> mysteries) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(MISSIONS_KEY, mGson.toJson(mysteries));
        for (Mystery mystery : mysteries) {
            for (Secret secret : mystery.getSecrets()) {
                saveSecret(secret);
            }
        }
        editor.apply();
    }

}