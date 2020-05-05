package com.cws.qoutestolistentoo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class TextListAdapter extends ArrayAdapter<Text> {

    private ArrayList<Text> list;


    public TextListAdapter(@NonNull Context context, @NonNull ArrayList<Text> texts) {
        super(context, 0, texts);
        list = texts;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Text text = list.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.text_info, parent, false);
        }


        TextView textTitle = (TextView) convertView.findViewById(R.id.TextTitle);
        TextView textAuthor = (TextView) convertView.findViewById(R.id.TextAuthor);
        ImageView textIcon = (ImageView) convertView.findViewById(R.id.textIcon);
        LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.listItem);
        textTitle.setText(text.getTitle());
        textAuthor.setText(text.getAuthor());
        textIcon.setImageBitmap(BitmapFactory.decodeResource(convertView.getResources(), convertView.getResources().getIdentifier("i"+String.valueOf(position), "raw", "com.cws.qoutestolistentoo")));
        linearLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent playerIntent = new Intent(getContext(), PlayerActivity.class);
                playerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                int[] resid = new int[list.size()];
                String[] CC = new String[list.size()];
                String[] title = new String[list.size()];
                String[] author = new String[list.size()];


                for (int x = 0; x < list.size(); x++){
                    resid[x] = list.get(x).getResid();
                    CC[x] = list.get(x).getCC();
                    title[x] = list.get(x).getTitle();
                    author[x] = list.get(x).getAuthor();
                }

                b.putIntArray("resid", resid);
                b.putStringArray("CC", CC);
                b.putStringArray("title", title);
                b.putStringArray("author", author);
                b.putInt("Index", position);

                playerIntent.putExtras(b);
                getContext().startActivity(playerIntent);
            }
        });
        return convertView;
    }

}
