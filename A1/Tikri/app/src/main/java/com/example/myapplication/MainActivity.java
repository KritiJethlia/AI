package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    private MessageAdapt mAdapter;
    boolean firstmsg = true;
    String userid = "test";
    public MainActivity()
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        mAdapter = new MessageAdapt(this, new ArrayList<MessageFn>());
        mListView.setAdapter(mAdapter);
        //MessageFn msgquery = new MessageFn("Enter your User Id to chat with me! \n\n If you have previously not talked to me,I suggest you pick up a username and we are good to go.", false, false);
        //mAdapter.add(msgquery);
        //SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        //userid = prefs.getString("name", "");


//code for sending the message
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String msg = mEditTextMessage.getText().toString();
                if(firstmsg == true){
                   userid = msg;

                    MessageFn userMsg = new MessageFn(userid, true, false);
                    mAdapter.add(userMsg);

                    String toDisplay = userid + " I am fetching your messages.Let me use my speed booster for a busy person like you!";
                    MessageFn displayGetMsg = new MessageFn(toDisplay, false, false);
                    mAdapter.add(displayGetMsg);
                    getMessages(mAdapter,userid);
                    firstmsg = false;

                    mEditTextMessage.setText("");
                    mListView.setSelection(mAdapter.getCount() - 1);

                }
                else{
                    sendMessage(msg,userid);
                    mEditTextMessage.setText("");
                    mListView.setSelection(mAdapter.getCount() - 1);
                }
            }
        });
    }
    public void getMessages(final MessageAdapt mAdapter,String userid){
        System.out.println("AAYA");
        String URL = "http://10.0.2.2:8000/users/" + userid;
        JsonArrayRequest objectRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("Response received");
                        Log.e("rest Response",response.toString());
                        try {
                            displayPastMessages(response,mAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("error");
                        Log.e("resttt",error.toString());
                    }
                }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(objectRequest);
    }
    private void displayPastMessages(JSONArray response,MessageAdapt mAdapter) throws JSONException {
        if(response.length() == 0){
            MessageFn newUserMessage = new MessageFn("Looks like you are a new user!Welcome "+userid,false,false);
            mAdapter.add(newUserMessage);
            return;
        }

        for (int i = 0 ;i < response.length();i++){
            JSONObject tmp = (JSONObject) response.get(i);
            System.out.println(tmp.get("response"));
            MessageFn chatQuery = new MessageFn((String) tmp.get("query"), true, false);
            mAdapter.add(chatQuery);
            MessageFn chatResponse = new MessageFn((String) tmp.get("response"), false, false);
            mAdapter.add(chatResponse);
        }
        MessageFn chatQuery = new MessageFn("It was wonderful chatting with you last time.\n\nI hope we have a better conversation today!", false, false);
        mAdapter.add(chatQuery);

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMessage(String message, String userid) {
        MessageFn chatMessage = new MessageFn(message, true, false);
        mAdapter.add(chatMessage);
        //respond message
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String URL = "http://10.0.2.2:8000/users/" + userid + "/query";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("query", message);
        params.put("userid",userid);
        params.put("time",dtf.format(now));
        JsonObjectRequest objectRequest = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response to post query:%n %s", response.toString(4));
                            Log.e("rest Response",response.toString());
//                            System.out.println(response.get("response"));
                            parsedata(response);

                        } catch (JSONException e) {
                            Log.e("resttt",e.toString());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        requestQueue.add(objectRequest);

    }
    public void parsedata(JSONObject response) throws JSONException {
        String botResponse = (String) response.get("response");
//        System.out.println("Testing post request response" + botResponse);
        mimicOtherMessage(botResponse);
    }

    private void mimicOtherMessage(String message) {
        MessageFn chatMessage = new MessageFn(message, false, false);
        mAdapter.add(chatMessage);
    }

    private void sendMessage() {
        MessageFn chatMessage = new MessageFn(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        MessageFn chatMessage = new MessageFn(null, false, true);
        mAdapter.add(chatMessage);
    }
}