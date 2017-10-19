package ru.trinitydigital.textselecting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        container.addView(new WordAnswerView(this, "hello", 40));
    }
}
