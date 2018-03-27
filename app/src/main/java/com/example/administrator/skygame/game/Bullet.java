package com.example.administrator.skygame.game;

import android.graphics.Bitmap;

/**
 * Bullet class, moving in a straight line from bottom to top
 */
public class Bullet extends AutoSprite {

    public Bullet(Bitmap bitmap){
        super(bitmap);
        setSpeed(-10);// Negative numbers indicate that the bullets fly up
    }

}