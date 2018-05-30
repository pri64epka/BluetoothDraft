package com.example.android.bluetoothdraft;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;




public class Pop extends AppCompatActivity {

    public static int device_bt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);

        ListView listView = findViewById(R.id.list);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.8));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, MainActivity.strings);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                device_bt = i;
                
               // MainActivity.ClientClass clientClass = new MainActivity.ClientClass(btArray[i]);
              //  clientClass.start();

                //   finish();
                // MainActivity mn = new MainActivity();
                // mn.good();
                MainActivity.status.setText("Cong");
                finish();


                //  MainActivity.ClientClass clientClass = new MainActivity.ClientClass(btArray[i]);
                //  clientClass.start();
                // status.setText("Connecting");
            }
        });
    }
}
