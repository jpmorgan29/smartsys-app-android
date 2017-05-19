package com.example.jp.robot195;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
//    MQTT
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client;
//    Connection info
    String server = "ssl://m10.cloudmqtt.com:23915";
    String username = "bxaxrkah";
    String password = "1zQixURXUYuB";
    String topic = "Robot/Debug";

    String Manualsend = "test";
    String newmessage;
    int qos = 1;
    protected static final int RESULT_SPEECH = 1;

//    XML
    private Button btnLeft, btnRight, btnForward, btnBackward;
    private ImageButton btnMicrophone;
    private TextView tvSaid;
    private TextView tvLatest;
    private TextView tvStatus;
//    Text To Speech
    private TextToSpeech tts;
    Boolean send = true;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    //What we say
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    tvSaid.setText(text.get(0));
                    String saidPublished = tvSaid.getText().toString();
                    if (saidPublished.contains("robot")) {
                        topic = "Robot/Command";
                        send = true;
                    } else if (saidPublished.contains("auto")) {
                        topic = "Robot/Command";
                        send = true;
                    } else if (saidPublished.contains("robert")) {
                        topic = "Robot/Command";
                        send = true;
                    } else {
                        topic = "Not Understood";
                        newmessage = "Not Understood";
                        send = false;
                    }
                    if (saidPublished.contains("rechtdoor")) {
                        newmessage = "forward";
                        send = true;
                        if (saidPublished.contains("vooruit")) {
                            newmessage = "forward";
                            send = true;
                        } else if (saidPublished.contains("voorruit")) {
                            newmessage = "forward";
                            send = true;
                        } else if (saidPublished.contains("achteruit")) {
                            newmessage = "backward";
                            send = true;
                        } else if (saidPublished.contains("achterruit")) {
                            newmessage = "backward";
                            send = true;
                        } else if (saidPublished.contains("links")) {
                            newmessage = "left";
                            send = true;
                        } else if (saidPublished.contains("lynx")) {
                            newmessage = "left";
                            send = true;
                        } else if (saidPublished.contains("rechts")) {
                            newmessage = "right";
                            send = true;
                        } else if (saidPublished.contains("stop")) {
                            newmessage = "stop";
                            send = true;
                        } else if (saidPublished.contains("los")) {
                            newmessage = "los";
                            send = true;
                        } else {
                            topic = "Not understood";
                            newmessage = "Not understood";
                            send = false;
                        }

                        writeMessage();
                    }
                    break;
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        XML

        btnMicrophone = (ImageButton) findViewById(R.id.btnMicrophone);
        tvLatest = (TextView) findViewById(R.id.tvlatest);
        tvSaid = (TextView) findViewById(R.id.tvSaid);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
                            /*Manual controller*/
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
        btnForward = (Button) findViewById(R.id.btnUp);
        btnBackward = (Button) findViewById(R.id.btnBackwards);
                            /*#################*/
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("####", "IS GOING FORWARD ####");
                newmessage = "forward";
                send = true;
                writeMessage();
            }
        });
        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("####", "IS GOING BACKWARDS ####");
                newmessage = "backwards";
                send = true;
                writeMessage();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("####", "IS GOING RIGHT ####");
                newmessage = "right";
                send = true;
                writeMessage();
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("####", "IS GOING LEFT ####");
                newmessage = "left";
                send = true;
                writeMessage();
            }
        });
//        Text to speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
                if(status != TextToSpeech.ERROR){
                    Log.d("Testen", "Dit is een test");
                }
            }
        });

//        When you click on the Microphone button
        btnMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Nieuwe intent bij het klikken
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                Taal van de smartphone moet in het nederlands zijn omdat er in het Nederlands wordt ingesproken
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());

                try{
                    startActivityForResult(i, RESULT_SPEECH);
                    tvSaid.setText("");
                } catch (ActivityNotFoundException a){
                    Toast.makeText(getApplicationContext(),
                            "Uw telefoon beschikt niet over de Speech to Text mogelijkheid",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
//        Connecting to the MQTT
        client = new MqttAndroidClient(this.getApplicationContext(), server, clientId);
        client.setCallback(new MqttCallbackExtended(){
            @Override
            public void connectComplete(boolean reconnect, String server){
                String Verbonden = "Status: Connected";

                if (reconnect){
                    addToHistory("Reconnected to : " + server);
                    tvStatus.setText(Verbonden);
                    try {
                        subscribeToTopic();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    addToHistory("Connected to: " + server);
                    tvStatus.setText(Verbonden);
                }
            }
            @Override
            public void connectionLost(Throwable cause){
                String Onverbonden = "Status: Disconnected";
                addToHistory("The connection was lost.");
                tvStatus.setText(Onverbonden);
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception{
                addToHistory("Incoming message: " + new String(message.getPayload()));
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token){
//                Leeg
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setConnectionTimeout(240000);
//        TODO: mqttConnectOptions.setAutomaticReconnect(true);
        try{
            client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions dcBufferOptions = new DisconnectedBufferOptions();
                    dcBufferOptions.setBufferEnabled(true);
                    dcBufferOptions.setBufferSize(100);
                    dcBufferOptions.setPersistBuffer(false);
                    dcBufferOptions.setDeleteOldestMessages(false);
//                      TODO: client.setBufferOpts(dcBufferOptions);
                    try {
                        subscribeToTopic();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + server);
                }
            });
        }
        catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void writeMessage() {
        if (send){
            MqttMessage msg = new MqttMessage(newmessage.getBytes());
            msg.setQos(qos);
            msg.setRetained(false);
            try{
                client.publish(topic, msg);

            } catch (MqttException e){
                e.printStackTrace();
                Log.d("ERROR", e.toString());
            }
        }
    }


    private void subscribeToTopic() throws MqttException {
        try {
            client.subscribe("Robot/Debug", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d("####LOG####", "Successfully subscribed to topic");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                       TODO: Show latest msg
                            tvLatest.setText("Latest Message: ");
                        }
                    });
                }
                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d("####LOG####", "Failed to subscribe to topic.");

                }
            });
        }
        catch(MqttException ex){
            System.err.println("Exception tijdens subscribing");
            ex.printStackTrace();
        }
    }
    public void publishMessage(){
        try{
            MqttMessage msg = new MqttMessage();
            msg.setPayload(Manualsend.getBytes());
            client.publish(topic,msg);
            addToHistory("Message published! ");
            if(!client.isConnected()){
//                TODO: addToHistory(client.getBufferedMessageCount() + "Messages in buffer");
            }
        }
        catch(MqttException e){
            System.err.println("Error publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void SendMessage(View view) throws MqttException{
        publishMessage();
    }
    public void calibrateScale(View view){
        try{
            MqttMessage msg = new MqttMessage("Calibrate".getBytes());
            client.publish("Scale", msg);
        }catch(MqttException e){
            e.printStackTrace();
        }
    }

    private void addToHistory(String mainText) {
        System.out.println("LOG : " + mainText);
    }

}
