package com.example.administrator.skygame.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Explosion effect class, the position is immutable, but it can show a dynamic explosion effect
 */
public class Explosion extends Sprite {

    private int segment = 14;
    private int level = 0;
    private int explodeFrequency = 2;

    public Explosion(Bitmap bitmap){
        super(bitmap);
    }

    @Override
    public float getWidth() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return bitmap.getWidth() / segment;
        }
        return 0;
    }

    @Override
    public Rect getBitmapSrcRec() {
        Rect rect = super.getBitmapSrcRec();
        int left = (int)(level * getWidth());
        rect.offsetTo(left, 0);
        return rect;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            if(getFrame() % explodeFrequency == 0){
                //Since level 1 plus, used to draw the next explosion fragment
                level++;
                if(level >= segment){
                    //Destroy explosions when all the fragments have been drawn
                    destroy();
                }
            }
        }
    }

    //Get the number of frames needed to draw a complete explosion, that is, 28 frames
    public int getExplodeDurationFrame(){
        return segment * explodeFrequency;
    }
}