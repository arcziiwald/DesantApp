package com.example.android.desantapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btmDomination, btmPointProtect, btmBomb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btmDomination = (Button) findViewById(R.id.btnDomination);
        btmPointProtect = (Button) findViewById(R.id.btnPointProtect);
        btmBomb = (Button) findViewById(R.id.btnBomb);

        btmDomination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DominationActivity.class);
                startActivity(intent);
            }
        });



    }

}
