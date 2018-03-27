package com.example.administrator.skygame.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Take a straight line of Sprite class, its position can only be straight up and down
 */
public class AutoSprite extends Sprite {
    // The number of pixels moved per frame to be positive down
    private float speed = 2;

    public AutoSprite(Bitmap bitmap){
        super(bitmap);
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public float getSpeed(){
        return speed;
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            //Move the speed pixel in the y-axis direction
            move(0, speed * gameView.getDensity());
        }
    }

    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(!isDestroyed()){
            // Check if the Sprite is outside of the Canvas scope, and if it does, destroy the Sprite
            RectF canvasRecF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            RectF spriteRecF = getRectF();
            if(!RectF.intersects(canvasRecF, spriteRecF)){
                destroy();
            }
        }
    }
}