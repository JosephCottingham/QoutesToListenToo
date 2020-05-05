package com.cws.qoutestolistentoo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView ContentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContentList = findViewById(R.id.ListView);
        ContentList.setAdapter(new TextListAdapter(getApplicationContext(), genArrayList()));
    }


    private ArrayList<Text> genArrayList() {
        ArrayList<Text> arrayList = new ArrayList<Text>();
        for (int i=0; i<9; i++){
            String[] mediaData = loadMediaData(i);
            arrayList.add(new Text(mediaData[0], mediaData[1], mediaData[2], getResources().getIdentifier("m"+String.valueOf(i), "raw", getPackageName())));
        }
        return arrayList;
    }

    private String[] loadMediaData(int index){
        String[] string = new String[3];
        try(BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(getResources().getIdentifier("d"+String.valueOf(index), "raw", getPackageName()))))) {
            StringBuilder sb = new StringBuilder();
            string[0] = br.readLine();
            string[1] = br.readLine();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(" ");
                line = br.readLine();
            }
            string[2] = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }
}
