package com.homework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.homework.Stock.StockUpdate;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void kid(View view) {
        Intent i = new Intent(MainActivity.this, StockUpdate.class);

        i.putExtra("list","kid");
        startActivity(i);
    }

    public void woman(View view) {
        Intent i = new Intent(MainActivity.this, StockUpdate.class);

        i.putExtra("list","woman");
        startActivity(i);
    }

    public void man(View view)
    {
        Intent i = new Intent(MainActivity.this, StockUpdate.class);

        i.putExtra("list","man");
        startActivity(i);
    }
}
