package com.example.android.bluetoothdraft;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.example.android.bluetoothdraft.Pop.device_bt;

public class MainActivity extends AppCompatActivity {

    Button listen, send, listDevices, window;
    ListView listView;
    @SuppressLint("StaticFieldLeak")
    public static TextView msg_box, status;
    EditText writeMsg;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendRecieve sendRecieve;

    public static String[] strings;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;


    private final static String APP_NAME = "BluetoothDraft";
    private final static UUID MY_UUID = UUID.fromString("8848ea50-ec54-4219-8e19-ed9e6625fa25");
    // Intent btEnablingIntent;
    //  int requestCodeForEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getActionBar() != null) {
            getActionBar().setIcon(R.drawable.baseline_import_export_black_18dp);
        }


        listen = findViewById(R.id.btListen);
        window = findViewById(R.id.sten);
        listDevices = findViewById(R.id.btListDevices);
        send = findViewById(R.id.btSend);
        listView = findViewById(R.id.listView2);
        msg_box = findViewById(R.id.textView2);
        status = findViewById(R.id.textView);
        writeMsg = findViewById(R.id.editText);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
        }


        implementListeners();

        // btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //  requestCodeForEnable = 1;

        //  bluetoothONMethod();
        //  bluetoothOFFMethod();
    }

    public void good() {
        ClientClass clientClass = new ClientClass(btArray[device_bt]);
        clientClass.start();
        status.setText("Connecting");
    }

    private void implementListeners() {

        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientClass clientClass = new ClientClass(btArray[device_bt]);
                clientClass.start();
                status.setText("Connecting");

            }
        });

        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {

                        btArray[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }
                    startActivity(new Intent(MainActivity.this, Pop.class));

                    //  ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    //  listView.setAdapter(arrayAdapter);
                }
                //   startActivity(new Intent(MainActivity.this,Pop.class));
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();
                status.setText("Connecting");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = String.valueOf(writeMsg.getText());

                sendRecieve.write(string.getBytes());
            }
        });

    }

    android.os.Handler handler = new android.os.Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listenning");
                    break;
                case STATE_CONNECTING:
                    status.setText("CONNECTING");
                    break;
                case STATE_CONNECTED:
                    status.setText("CONNECTED");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("CONNECTION_FAILED");
                    break;

                case STATE_MESSAGE_RECIEVED:

                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMSG = new String(readBuff, 0, msg.arg1);

                    if (!tempMSG.equals("")) {
                        Toast.makeText(getBaseContext(), "Full", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Empty", Toast.LENGTH_SHORT).show();
                    }

                    msg_box.setText(tempMSG);

                    status.setText("MESSAGE_RECIEVED");
                    break;

            }

            return true;
        }
    });


    public class ServerClass extends Thread {

        private BluetoothServerSocket serverSocket;

        public ServerClass() {

            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if (socket != null) {

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendRecieve = new SendRecieve(socket);
                    sendRecieve.start();

                    break;
                }
            }
        }

    }


    public class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }

    }

    private class SendRecieve extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieve(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;

        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

/*
    private void bluetoothOFFMethod() {
        buttonOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myBluetoothAdapter.isEnabled()){
                    myBluetoothAdapter.disable();
                }
            }
        });
    }
*/
/*
    @Override

    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==requestCodeForEnable){
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(),"BLUETOOTH SUPPORT",Toast.LENGTH_LONG).show();
            } else if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(),"BLUETOOTH DOES NOT ALLOW",Toast.LENGTH_LONG).show();
            }
        }
    }*/
/*
    private void bluetoothONMethod(){
        buttonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myBluetoothAdapter==null){
                    Toast.makeText(getApplicationContext(),"BLUETOOTH CANCELED",Toast.LENGTH_LONG).show();
                } else{
                    if(!myBluetoothAdapter.isEnabled()){

                        startActivityForResult(btEnablingIntent,requestCodeForEnable);

                    }
                }
            }
        });
    }
    */
}
