package com.example.administrator.skygame.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;


public class Award extends AutoSprite {
    public static int STATUS_DOWN1 = 1;
    public static int STATUS_UP2 = 2;
    public static int STATUS_DOWN3 = 3;

    private int status = STATUS_DOWN1;

    public Award(Bitmap bitmap){
        super(bitmap);
        setSpeed(7);
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        // Check if the Sprite is outside of the Canvas scope, and if it does, destroy the Sprite
        if(!isDestroyed()){
            //After drawing a certain number of times to change the direction or speed
            int canvasHeight = canvas.getHeight();
            if(status != STATUS_DOWN3){
                float maxY = getY() + getHeight();
                if(status == STATUS_DOWN1){
             // the first time down
                    if(maxY >= canvasHeight * 0.25){
                        //Change direction when up to cutoff for the first time, up
                        setSpeed(-5);
                        status = STATUS_UP2;
                    }
                }
                else if(status == STATUS_UP2){
                    if(maxY+this.getSpeed() <= 0){
                        // Change direction when going up to critical value for the second time, down
                        setSpeed(13);
                        status = STATUS_DOWN3;
                    }
                }
            }
            if(status == STATUS_DOWN3){
                if(getY() >= canvasHeight){
                    destroy();
                }
            }
        }
    }
}