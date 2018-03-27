package com.example.administrator.skygame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

import com.example.administrator.skygame.game.GameView;

import static android.os.Build.ID;


public class GameActivity extends Activity {
    public GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameView = (GameView)findViewById(R.id.gameView);


        // 0: combatAircraft
        // 1: explosion
        // 2: yellowBullet
        // 3: blueBullet
        // 4: smallEnemyPlane
        // 5: middleEnemyPlane
        // 6: bigEnemyPlane
        // 7: bombAward
        // 8: bulletAward
        // 9: pause1
        // 10: pause2
        // 11: bomb
        int[] bitmapIds = {

                R.drawable.shipsmall,
                R.drawable.explosion,
                R.drawable.bullet_long,
                R.drawable.bulletbc,
                R.drawable.smallship,
                R.drawable.middleshipx,
                R.drawable.bigcraft4,
                R.drawable.awardbomb,
                R.drawable.awardbullet,
                R.drawable.pause_style,
                R.drawable.pause_style,
                R.drawable.bomb_count
        };
        gameView.start(bitmapIds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null ){
            gameView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null){
            gameView.destroy();
        }
        gameView = null;
    }
}
