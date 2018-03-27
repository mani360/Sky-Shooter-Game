package com.example.administrator.skygame.game;

import android.graphics.Bitmap;

/**
 * In the enemy aircraft, medium volume, anti-strike ability and medium
 */
public class MiddleEnemyPlane extends EnemyPlane {

    public MiddleEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(4);
        setValue(6000);
    }

}