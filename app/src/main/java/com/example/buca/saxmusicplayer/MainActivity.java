package com.example.buca.saxmusicplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.buca.saxmusicplayer.activities.ChoosePlaylistActivity;
import com.example.buca.saxmusicplayer.activities.LoadAllSongsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loadAllButton = (Button) findViewById(R.id.loadAllButton);
        loadAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoadAllSongsActivity.class);
                startActivity(intent);
            }
        });

        Button btnChoosePlaylist = (Button)findViewById(R.id.btnChoosePlaylist);
        btnChoosePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChoosePlaylistActivity.class);
                startActivity(intent);
            }
        });


    }
}
