package com.example.administrator.skygame.game;

import android.graphics.Bitmap;

/**
 * Enemy aircraft, bulky, anti-strike capability
 */
public class BigEnemyPlane extends EnemyPlane {

    public BigEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(10);// Enemy aircraft resistance is 10, that is, 10 bullets to destroy the enemy aircraft
        setValue(30000);// destroy a big enemy aircraft can get 30,000 points
    }

}