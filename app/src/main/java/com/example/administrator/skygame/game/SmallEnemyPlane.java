package com.example.administrator.skygame.game;

import android.graphics.Bitmap;

/**
 * Small enemy aircraft, small size, low resistance to combat
 */
public class SmallEnemyPlane extends EnemyPlane {

    public SmallEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(1);
        setValue(1000);
    }

}