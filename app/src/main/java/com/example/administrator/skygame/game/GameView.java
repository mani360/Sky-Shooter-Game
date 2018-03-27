package com.example.administrator.skygame.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;


import com.example.administrator.skygame.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public  class  GameView  extends  View {

    private  Paint paint;
    private  Paint textPaint;
    private  CombatAircraft combatAircraft =  null ;
    private  List< Sprite > sprites =  new  ArrayList< Sprite > ();
    private  List< Sprite > spritesNeedAdded =  new  ArrayList< Sprite > ();
    // 0:combatAircraft
    // 1: explosion
    // 2: yellowBullet
    // 3:blueBullet
    // 4:smallEnemyPlane
    // 5: middleEnemyPlane
    // 6: bigEnemyPlane
    // 7:bombAward
    // 8: bulletAward
    // 9: pause1
    // 10: pause2
    // 11:bomb
    private  List< Bitmap > bitmaps =  new  ArrayList< Bitmap > ();
    private  float density = getResources() . getDisplayMetrics() . density; // screen density
    public  static  final  int  STATUS_GAME_STARTED  =  1 ; // game start
    public  static  final  int  STATUS_GAME_PAUSED  =  2 ; // game pause
    public  static  final  int  STATUS_GAME_OVER  =  3 ; // end of game
    public  static  final  int  STATUS_GAME_DESTROYED  =  4 ; // game destruction
    private  int status =  STATUS_GAME_DESTROYED ; // is initially destroyed
    private  long frame =  0 ; // total number of frames drawn
    private  long score =  0 ; // total score
    private  float fontSize =  12 ; // The default font size used to draw the upper-left text
    private  float fontSize2 =  20 ; // used to draw text in the Dialog when the Game Over
    private  float borderSize =  2 ; // Game Over Dialog's border
    private  Rect continueRect =  new  Rect (); // "continue", "restart" button of Rect

    // Touch event-related variables
    private  static  final  int  TOUCH_MOVE  =  1 ; // move
    private  static  final  int  TOUCH_SINGLE_CLICK  =  2 ; // click
    private  static  final  int  TOUCH_DOUBLE_CLICK  =  3 ; // double click
    // One click event is composed of two events, DOWN and UP. Assuming that the interval from down to up is less than 200 milliseconds, we think that a click event has occurred.
    private  static  final  int singleClickDurationTime =  200 ;
    // A double-click event is synthesized by two click events. Less than 300 milliseconds between two click events, we think that a double-click event has occurred.
    private  static  final  int doubleClickDurationTime =  300 ;
    private  long lastSingleClickTime =  - 1 ; // When the last click occurred
    private  long touchDownTime =  - 1 ; // When the contact is pressed
    private  long touchUpTime =  - 1 ; // The moment when the contact bounces
    private  float touchX =  - 1 ; // the x coordinate of the contact
    private  float touchY =  - 1 ; // the y coordinate of the contact



    public GameView ( Context  context ) {
        super (context);
        init( null , 0 );
    }

    public  GameView ( Context  context , AttributeSet  attrs ) {
        super (context, attrs);
        init(attrs, 0 );
    }

    public  GameView ( Context  context , AttributeSet  attrs , int  defStyle ) {
        super (context, attrs, defStyle);
        init(attrs, defStyle);
    }






    private  void  init ( AttributeSet  attrs , int  defStyle ) {
        final  TypedArray a = getContext() . obtainStyledAttributes(
                attrs                 , R . styleable . GameView , defStyle, 0 );
        a . recycle();
        // Initialize paint
        paint =  new  Paint ();
        paint.setStyle( Paint . Style . FILL );
        // Set textPaint, set to anti-aliasing, and is bold
        textPaint =  new  TextPaint ( Paint . ANTI_ALIAS_FLAG  |  Paint . FAKE_BOLD_TEXT_FLAG );
        textPaint . setColor( 0xff000000 );
        fontSize = textPaint . getTextSize();
        fontSize *= density;
        fontSize2 *= density;
        textPaint . setTextSize(fontSize);
        borderSize *= density;
    }

    public  void  start ( int [] bitmapIds ){
        destroy();
        for ( int bitmapId : bitmapIds){
            Bitmap bitmap =  BitmapFactory . decodeResource(getResources(), bitmapId);
            bitmaps . add(bitmap);
        }
        startWhenBitmapsReady();
    }

    private  void  startWhenBitmapsReady (){
        combatAircraft =  new  CombatAircraft (bitmaps . get( 0 ));
        // Set the game to start
        status =  STATUS_GAME_STARTED ;
        postInvalidate();
    }

    private  void  restart (){
        destroyNotRecyleBitmaps();
        startWhenBitmapsReady();
    }

    public  void  pause (){
        // Set the game to pause
        status =  STATUS_GAME_PAUSED ;

    }

    private  void  resume (){
        // Set the game to running
        status =  STATUS_GAME_STARTED ;
        postInvalidate();


    }

    private  long  getScore (){
        // Get game score
        return score;
    }


    /* -------------------------------draw---------------- --------------------- */

    @Override
    protected  void  onDraw ( Canvas  canvas ) {
        // We check at each frame whether the conditions for delaying the click event are met
        if (isSingleClick()){
            onSingleClick(touchX, touchY);
        }

        super . onDraw(canvas);

        if (status ==  STATUS_GAME_STARTED ){
            drawGameStarted(canvas);
        } else  if (status ==  STATUS_GAME_PAUSED ){
            drawGamePaused(canvas);
        } else  if (status ==  STATUS_GAME_OVER ){
            drawGameOver(canvas);
        }
    }

    // Draw a running game
    private  void  drawGameStarted ( Canvas  canvas ){

        drawScoreAndBombs(canvas);

        // At the first draw, the fighter is moved to the bottom of the Canvas, in the center of the horizontal direction
        if (frame ==  0 ){
            float centerX = canvas . getWidth() /  2 ;
            float centerY = canvas . getHeight() - combatAircraft . getHeight() /  2 ;
            combatAircraft . centerTo(centerX, centerY);
        }

        // Add spritesNeedAdded to sprites
        if (spritesNeedAdded . size() >  0 ){
            sprites . addAll(spritesNeedAdded);
            spritesNeedAdded . clear();
        }

        // Check the situation where the fighter went in front of the bullet
        destroyBulletsFrontOfCombatAircraft();

        // Remove sprites that have been destroyed prior to drawing
        removeDestroyedsprites();

        // randomly add Sprite every 30 frames
        if (frame %  30  ==  0 ){
            createRandomsprites(canvas . getWidth());
        }
        frame ++ ;

        // Traverse sprites to draw enemy planes, bullets, bonuses, explosion effects
        Iterator< Sprite > iterator = sprites . iterator();
        while (iterator . hasNext()){
            Sprite s = iterator . next();

            if ( ! s . isDestroyed()){
                //The destroy method may be called inside Sprite's draw method
                s . draw(canvas, paint, this );
            }

            // We need to determine if Sprite was destroyed after executing the draw method.
            if (s . isDestroyed()){
                // if Sprite is destroyed, remove it from sprites
                iterator . remove();
            }
        }

        if (combatAircraft !=  null ){
            // finally draw fighter
            combatAircraft . draw(canvas, paint, this );
            if (combatAircraft . isDestroyed()){
                // if the fighter is hit and destroyed, then the game is over
                status =  STATUS_GAME_OVER ;
            }
            // Call the postInvalidate () method to make the View continue to render, achieve dynamic effects
            postInvalidate();
        }
    }

    // Draw a paused game
    private  void  drawGamePaused ( Canvas  canvas ){
        drawScoreAndBombs(canvas);

        // Call the Sprite's onDraw method instead of the draw method so that the static Sprite can be rendered without changing the Sprite's position
        for ( Sprite s : sprites){
            s . onDraw(canvas, paint, this );
        }
        if (combatAircraft !=  null ){
            combatAircraft . onDraw(canvas, paint, this );
        }

        // Draw Dialog, show score
        drawScoreDialog(canvas, " continue " );

        if (lastSingleClickTime >  0 ){
            postInvalidate();
        }
    }

    // draw the game in the end state
    private  void  drawGameOver ( Canvas  canvas ){
        // Only draws popups after Game Over to show the final score
        drawScoreDialog(canvas, " restart " );

        if (lastSingleClickTime >  0 ){
            postInvalidate();
        }
    }

    private  void  drawScoreDialog ( Canvas  canvas , String  operation ){
        int canvasWidth = canvas . getWidth();
        int canvasHeight = canvas . getHeight();
        // store the original value
        float originalFontSize = textPaint . getTextSize();
        Paint . Align originalFontAlign = textPaint . getTextAlign();
        int originalColor = paint . getColor();
        Paint . Style originalStyle = paint . getStyle();
        /*
        W = 360
        W1 = 20
        W2 = 320
        buttonWidth = 140
        buttonHeight = 42
        H = 558
        H1 = 150
        H2 = 60
        H3 = 124
        H4 = 76
        */
        int w1 = ( int )( 20.0  /  360.0  * canvasWidth);
        int w2 = canvasWidth -  2  * w1;
        int buttonWidth = ( int )( 140.0  /  360.0  * canvasWidth);

        int  h1 = ( int )( 150.0  /  558.0  * canvasHeight);
        int h2 = ( int )( 60.0  /  558.0  * canvasHeight);
        int h3 = ( int )( 124.0  /  558.0  * canvasHeight);
        int h4 = ( int )( 76.0  /  558.0  * canvasHeight);
        int buttonHeight = ( int )( 42.0  /  558.0  * canvasHeight);

        canvas . translate(w1, h1);
        // draw the background color
        paint . setStyle( Paint . Style . FILL );
        paint . setColor( 0xFF4D0B35 );
        Rect rect1 =  new  Rect ( 0 , 0 , w2, canvasHeight -  2  * h1);
        canvas . drawRect(rect1, paint);
        // draw the border
        paint . setStyle( Paint . Style . STROKE );
        paint . setColor( 0xFF515151 );
        paint . setStrokeWidth(borderSize);
        // paint.setStrokeCap(Paint.Cap.ROUND);
        paint . setStrokeJoin( Paint . Join . ROUND );
        canvas . drawRect(rect1, paint);
        // Draw text "Your Score"
        textPaint . setTextSize(fontSize2);
        textPaint . setTextAlign( Paint . Align . CENTER );
        textPaint.setColor(0xFF53F7DE);
        canvas . drawText( " Your Score " , w2 /  2 , (h2 - fontSize2) /  2  + fontSize2, textPaint);
        // Draw the horizontal line under "Aircraft Wars Score"
        canvas . translate( 0 , h2);
        canvas . drawLine( 0 , 0 , w2, 0 , paint);
        // draw the actual score
        String allScore =  String . valueOf(getScore());
        canvas . drawText(allScore, w2 /  2 , (h3 - fontSize2) /  2  + fontSize2, textPaint);
        // Draw the horizontal lines below the score
        canvas . translate( 0 , h3);
        canvas . drawLine( 0 , 0 , w2, 0 , paint);
        // draw button border
        Rect rect2 =  new  Rect ();
        rect2 . left = (w2 - buttonWidth) /  2 ;
        rect2 . right = w2 - rect2 . left;
        rect2 . top = (h4 - buttonHeight) /  2 ;
        rect2 . bottom = h4 - rect2 . top;
        canvas . drawRect(rect2, paint);
        // Draw text "continue" or "restart"
        canvas . translate( 0 , rect2 . top);
        canvas . drawText(operation, w2 /  2 , (buttonHeight - fontSize2) /  2  + fontSize2, textPaint);
        continueRect =  new  Rect (rect2);
        continueRect . left = w1 + rect2 . left;
        continueRect . right = continueRect . left + buttonWidth;
        continueRect . top = h1 + h2 + h3 + rect2 . top;
        continueRect . bottom = continueRect . top + buttonHeight;

        // Reset
        textPaint . setTextSize(originalFontSize);
        textPaint . setTextAlign(originalFontAlign);
        paint . setColor(originalColor);
        paint . setStyle(originalStyle);
    }

    // Draw the score in the upper left corner and the number of bombs in the lower left corner
    private  void  drawScoreAndBombs ( Canvas  canvas ){
        // Draw the pause button in the upper left corner
        Bitmap pauseBitmap = status ==  STATUS_GAME_STARTED  ? bitmaps . get( 9 ) : bitmaps . get( 10 );
        RectF pauseBitmapDstRecF = getPauseBitmapDstRecF();
        float pauseLeft = pauseBitmapDstRecF . left;
        float pauseTop = pauseBitmapDstRecF . top;
        canvas . drawBitmap(pauseBitmap, pauseLeft, pauseTop, paint);
        // Draw the total number of points in the upper left corner
        float scoreLeft = pauseLeft + pauseBitmap . getWidth() +  20  * density;
        float scoreTop = fontSize + pauseTop + pauseBitmap . getHeight() /  2  - fontSize /  2 ;
        canvas . drawText(score +  " " , scoreLeft, scoreTop, textPaint);

        // draw the lower left corner
        if (combatAircraft !=  null  &&  ! combatAircraft . isDestroyed()){
            int bombCount = combatAircraft . getBombCount();
            if (bombCount >  0 ){
                // Draw the bomb in the lower left corner
                Bitmap bombBitmap = bitmaps . get( 11 );
                float bombTop = canvas . getHeight() - bombBitmap . getHeight();
                canvas . drawBitmap(bombBitmap, 0 , bombTop, paint);
                // Draw the number of bombs in the lower left corner
                float bombCountLeft = bombBitmap . getWidth() +  10  * density;
                float bombCountTop = fontSize + bombTop + bombBitmap . getHeight() /  2  - fontSize /  2 ;
                canvas . drawText( " X "  + bombCount, bombCountLeft, bombCountTop, textPaint);
            }
        }
    }

    // Check the situation where the fighter went in front of the bullet
    private  void  destroyBulletsFrontOfCombatAircraft (){
        if (combatAircraft !=  null ){
            float aircraftY = combatAircraft . getY();
            List< Bullet > aliveBullets = getAliveBullets();
            for ( Bullet bullet : aliveBullets){
                // if the fighter is in front of the bullet, then destroy the bullet
                if (aircraftY <= bullet . getY()){
                    bullet . destroy();
                }
            }
        }
    }


    // Remove sprites that have been destroyed
    private  void  removeDestroyedsprites (){
        Iterator< Sprite > iterator = sprites . iterator();
        while (iterator . hasNext()){
            Sprite s = iterator . next();
            if (s . isDestroyed()){
                iterator . remove();
            }
        }
    }

    // Generate a random Sprite
    private  void  createRandomsprites ( int  canvasWidth ){
        Sprite sprite =  null ;
        int speed =  2 ;
        // callTime indicates the number of times the createRandomsprites method was called
        int callTime =  Math . round(frame /  30 );
        if ((callTime +  1 ) %  25  ==  0 ){
            // Send item prizes
            if ((callTime +  1 ) %  50  ==  0 ){
                // send bombs
                sprite = new BombAward(bitmaps.get(7));
            }
            else {
                // Send double bullets
                sprite = new BulletAward(bitmaps.get(8));
            }
        }
        else {
            // Send enemy aircraft
            int [] nums = { 0 , 0 , 0 , 0 , 0 , 1 , 0 , 0 , 1 , 0 , 0 , 0 , 0 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 2 };
            int index = ( int ) Math . floor(nums . length * Math . random());
            int type = nums[index];
            if (type ==  0 ){
                // small enemy aircraft
                sprite = new SmallEnemyPlane(bitmaps.get(4));
            }
            else  if (type ==  1 ){
                // enemy aircraft
                sprite = new MiddleEnemyPlane(bitmaps.get(5));
            }
            else  if (type ==  2 ){
                // Great enemy aircraft
                sprite = new BigEnemyPlane(bitmaps.get(6));
            }
            if (type !=  2 ){
                if ( Math . random() <  0.33 ){
                    speed =  4 ;
                }
            }
        }

        if (sprite !=  null ){
            float spriteWidth = sprite . getWidth();
            float spriteHeight = sprite . getHeight();
            float x = ( float )((canvasWidth - spriteWidth) * Math . random());
            float y =  - spriteHeight;
            sprite.setX(x);
            sprite.setY (y);
            AutoSprite autoSprite = (AutoSprite)sprite;
            autoSprite . setSpeed(speed);
            addSprite(sprite);
        }
    }

    /* -------------------------------touch---------------- -------------------- */

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public  boolean  onTouchEvent ( MotionEvent  event ){
        // Get the event type we want by calling the resolveTouchType method
        // It should be noted that the resolveTouchType method does not return the TOUCH_SINGLE_CLICK type
        // We will call isSingleClick method to detect whether the click event is triggered every time the onDraw method is executed.
        int touchType = resolveTouchType(event);
        if (status ==  STATUS_GAME_STARTED ){
            if (touchType ==  TOUCH_MOVE ){
                if (combatAircraft !=  null ){
                    combatAircraft . centerTo(touchX, touchY);
                }
            } else  if (touchType ==  TOUCH_DOUBLE_CLICK ){
                if (combatAircraft !=  null ){
                    // double click will make fighter use bomb
                    combatAircraft . bomb( this );
                }
            }
        } else  if (status ==  STATUS_GAME_PAUSED ){
            if (lastSingleClickTime >  0 ){
                postInvalidate();
            }
        } else  if (status ==  STATUS_GAME_OVER ){
            if (lastSingleClickTime >  0 ){
                postInvalidate();
            }
        }
        return  true ;
    }

    // Synthesize the event types we want
    private  int  resolveTouchType ( MotionEvent  event ){
        int touchType =  - 1 ;
        int action = event . getAction();
        touchX = event . getX();
        touchY = event . getY();
        if (action ==  MotionEvent . ACTION_MOVE ){
            Long deltaTime =  System . currentTimeMillis() - touchDownTime;
            if (deltaTime > singleClickDurationTime){
                // contact movement
                touchType =  TOUCH_MOVE ;
            }
        } else  if (action ==  MotionEvent . ACTION_DOWN ){
            //The contact is pressed
            touchDownTime =  System . currentTimeMillis();
        } else  if (action ==  MotionEvent . ACTION_UP ){
            //The contact bounces
            touchUpTime =  System . currentTimeMillis();
            // Calculate the time difference between the contact press down to the contact bounce
            Long downUpDurationTime = touchUpTime - touchDownTime;
            // if the time difference between this contact press and lift is less than the time difference specified by a single click event,
            // Then we think that there has been a single click
            if (downUpDurationTime <= singleClickDurationTime){
                // Calculate the time difference from this click on the last click
                Long twoClickDurationTime = touchUpTime - lastSingleClickTime;

                if (twoClickDurationTime <=   doubleClickDurationTime){
                    // if the time difference between two clicks is less than the time difference between the execution of double click events,
                    // Then we think that a double-click event has occurred
                    touchType =  TOUCH_DOUBLE_CLICK ;
                    // Reset variables
                    lastSingleClickTime =  - 1 ;
                    touchDownTime =  - 1 ;
                    touchUpTime =  - 1 ;
                } else {
                    // if a click event is formed this time, but no double click event is formed, then we will not trigger the click event formed this time.
                    // We should look at doubleClickDurationTime milliseconds to see if we have formed a second click event again
                    // if a second click event is formed at that time, then we will synthesize a double click event with this click event.
                    // Otherwise, this click event is triggered after doubleClickDurationTime milliseconds
                    lastSingleClickTime = touchUpTime;
                }
            }
        }
        return touchType;
    }

    // Call this method in the onDraw method, check whether a click event occurs in each frame
    private  boolean  isSingleClick (){
        Boolean singleClick =  false ;
        // We check if the last click event satisfies the condition that triggered the click event after doubleClickDurationTime milliseconds have elapsed.
        if (lastSingleClickTime >  0 ){
            // Calculate the difference between the current time and the last click event
            Long deltaTime =  System . currentTimeMillis() - lastSingleClickTime;

            if (deltaTime >= doubleClickDurationTime){
                // if the time difference exceeds the time required for a double-click event,
                // The click event that should happen before the delay is triggered at this moment
                singleClick =  true ;
                // Reset variables
                lastSingleClickTime =  - 1 ;
                touchDownTime =  - 1 ;
                touchUpTime =  - 1 ;
            }
        }
        return singleClick;
    }

    private  void  onSingleClick ( float  x , float  y ) {
        if (status ==  STATUS_GAME_STARTED ){
            if (isClickPause(x, y)){
                // Clicked the pause button
                pause();
            }
        } else  if (status ==  STATUS_GAME_PAUSED ){
            if (isClickContinueButton(x, y)){
                // Click on the "Continue" button
                resume();
            }
        } else  if (status ==  STATUS_GAME_OVER ){
            if (isClickRestartButton(x, y)){
                //Clicked on the "Restart" button
                restart();
            }
        }
    }

    //Is clicked the pause button in the upper left corner
    private  boolean  isClickPause ( float  x , float  y ){
        RectF pauseRecF = getPauseBitmapDstRecF();
        return pauseRecF . contains(x, y);
    }

    //Is it clicked on the "Continue" in the pause state
    private  boolean  isClickContinueButton ( float  x , float  y ){
        return continueRect . contains(( int )x, ( int )y);
    }

    //Is the "Restart" button in the GAME OVER state clicked?
    private  boolean  isClickRestartButton ( float  x , float  y ){
        return continueRect . contains(( int )x, ( int )y);
    }

    private  RectF  getPauseBitmapDstRecF (){
        Bitmap pauseBitmap = status ==  STATUS_GAME_STARTED  ? bitmaps . get( 9 ) : bitmaps . get( 10 );
        RectF recF =  new  RectF ();
        recF . left =  15  * density;
        recF . top =  15  * density;
        recF . right = recF . left + pauseBitmap . getWidth();
        recF . bottom = recF . top + pauseBitmap . getHeight();
        return recF;
    }

    /* -------------------------------destroy---------------- -------------------- */

    private  void  destroyNotRecyleBitmaps (){
        // Set the game to destroy
        status =  STATUS_GAME_DESTROYED ;

        // Reset the frame
        frame =  0 ;

        // Reset score
        score =  0 ;

        // destroy the fighter
        if (combatAircraft !=  null ){
            combatAircraft . destroy();
        }
        combatAircraft =  null ;

        // destroy enemy planes, bullets, rewards, explosions
        for ( Sprite s : sprites){
            s . destroy();
        }
        sprites . clear();
    }

    public  void  destroy (){
        destroyNotRecyleBitmaps();

        // Release Bitmap resources
        for ( Bitmap bitmap : bitmaps){
            bitmap . recycle();
        }
        bitmaps . clear();
    }

    /*-------------------------------public methods-----------------------------------*/

    //Add Sprite to sprites
    public  void  addSprite ( Sprite  sprite ){
        spritesNeedAdded . add(sprite);
    }

    // Add a score
    public  void  addScore ( int  value ){
        score += value;
    }

    public  int  getStatus (){
        return status;
    }

    public  float  getDensity (){
        return density;
    }

    public  Bitmap  getYellowBulletBitmap (){
        return bitmaps . get( 2 );
    }

    public  Bitmap  getBlueBulletBitmap (){
        return bitmaps . get( 3 );
    }

    public  Bitmap  getExplosionBitmap (){
        return bitmaps . get( 1 );
    }

    // Get active enemy aircraft
    public  List< EnemyPlane >  getAliveEnemyPlanes (){
        List< EnemyPlane > enemyPlanes =  new  ArrayList< EnemyPlane > ();
        for ( Sprite s : sprites){
            if ( ! s . isDestroyed() && s instanceof  EnemyPlane ){
                EnemyPlane sprite = ( EnemyPlane )s;
                enemyPlanes . add(sprite);
            }
        }
        return enemyPlanes;
    }

    // Get active bomb rewards
    public  List< BombAward >  getAliveBombAwards (){
        List< BombAward > bombAwards =  new  ArrayList< BombAward > ();
        for ( Sprite s : sprites){
            if ( ! s . isDestroyed() && s instanceof  BombAward ){
                BombAward bombAward = ( BombAward )s;
                bombAwards . add(bombAward);
            }
        }
        return bombAwards;
    }

    // Get active bullet rewards
    public  List< BulletAward >  getAliveBulletAwards (){
        List< BulletAward > bulletAwards =  new  ArrayList< BulletAward > ();
        for ( Sprite s : sprites){
            if ( ! s . isDestroyed() && s instanceof  BulletAward ){
                BulletAward bulletAward = ( BulletAward )s;
                bulletAwards . add(bulletAward);
            }
        }
        return bulletAwards;
    }

    // Get an active bullet
    public  List< Bullet >  getAliveBullets (){
        List< Bullet > bullets =  new  ArrayList< Bullet > ();
        for ( Sprite s : sprites){
            if ( ! s . isDestroyed() && s instanceof  Bullet ){
                Bullet bullet = ( Bullet )s;
                bullets . add(bullet);
            }
        }
        return bullets;
    }
}