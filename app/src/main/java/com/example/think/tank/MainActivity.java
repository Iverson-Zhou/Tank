package com.example.think.tank;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.example.think.tank.view.FightingView;
import com.example.think.tank.view.FireButton;
import com.example.think.tank.view.SteeringWheelView;

public class MainActivity extends AppCompatActivity {
    private SteeringWheelView steeringWheelView;
    private FightingView fightingView;
    private FireButton btnFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        steeringWheelView = findViewById(R.id.v_steeringwheel);
        fightingView = findViewById(R.id.v_fighting);
        btnFire = findViewById(R.id.btn_fire);

        fightingView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                steeringWheelView.setListener(fightingView.getFightingDelegate());
                btnFire.setFireListener(fightingView.getFightingDelegate());
            }
        });
    }
}
