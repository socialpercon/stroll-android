package com.strollimo.android.model;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mystery {
    public static final String TYPE = "mission";

    @Expose
    private String name;
    @Expose
    private String shortDesc;
    @Expose
    private String imgUrl;
    @Expose
    private String id;
    @Expose
    private boolean topLevel;
    @Expose
    private Location loc;
    @Expose
    private String type = TYPE;
    @Expose
    private List<String> children = new ArrayList<String>();

    private List<Secret> secrets = new ArrayList<Secret>();
    private String mCode;
    private Drawable mImage;
    private int mCoinValue;

    public Mystery(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.loc = new Location(lat, lon);
    }

    public Mystery(String id, String name, double lat, double lon, String code, Drawable image) {
        this.id = id;
        this.name = name;
        this.loc = new Location(lat, lon);
        mCode = code;
        mImage = image;
        mCoinValue = new Random().nextInt(3) + 1;
    }

    public Bitmap getBitmap() {
        return ((BitmapDrawable) mImage).getBitmap();
    }

    public boolean isScannedCodeValid(String scannedCode) {
        if (scannedCode == null) {
            return false;
        } else {
            return scannedCode.equals(mCode);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String mCode) {
        this.mCode = mCode;
    }

    public Drawable getImage() {
        return mImage;
    }

    public void setImage(Drawable mImage) {
        this.mImage = mImage;
    }

    public int getCoinValue() {
        return mCoinValue;
    }

    public void setCoinValue(int mCoinValue) {
        this.mCoinValue = mCoinValue;
    }

    public List<Secret> getSecrets() {
        if (secrets == null) {
            secrets = new ArrayList<Secret>();
        }
        return secrets;
    }

    public void addChild(String secretId) {
        children.add(secretId);
    }

    public List<String> getChildren() {
        return this.children;
    }

    public void addSecret(Secret secret) {
        if (secrets == null) {
            secrets = new ArrayList<Secret>();
        }
        secrets.add(secret);
        if (!children.contains(secret.getId())) {
            children.add(secret.getId());
        }
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public String getType() {
        return type;
    }
}
