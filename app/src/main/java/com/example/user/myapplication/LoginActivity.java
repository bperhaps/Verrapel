package com.example.user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText username,passwd;
    CheckBox etIdSave;
    CheckBox etPwdSave;

    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        // 객체 참조 - 아이디, 비번, 세이브 박스 2개
        username = (EditText) findViewById(R.id.username);
        passwd = (EditText) findViewById(R.id.password);
        etIdSave = (CheckBox) findViewById(R.id.id_save);
        etPwdSave = (CheckBox) findViewById(R.id.pwd_save);
        username.requestFocus();

        //로그인 여부 확인하는 부분
        if (session.isLoggedIn()) {
            try {
                HashMap<String, String> result = new HashMap<>();
                HashMap<String, String> params = new HashMap<>();
                if (session.getValue("id") != null || session.getValue("pw") != null) {
                    params.put("username", session.getValue("id"));
                    params.put("password", session.getValue("pw"));

                    Sender sender = new Sender();
                    sender.setParams(params);
                    sender.start();
                    sender.join();
                    result = sender.getResult();
                    if (result.get("islogin").equals("true")) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                } else {

                }
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }


        // 액티비티 불러올 시 SharedPreFerences에 저장된 pref 에서 조건에 맞춰 값 가져오기 슉슉
        SharedPreferences pref_id_pw_save = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String id = pref_id_pw_save.getString("id_save", "");
        String pwd = pref_id_pw_save.getString("pwd_save", "");
        Boolean chk1 = pref_id_pw_save.getBoolean("chk1", false);
        Boolean chk2 = pref_id_pw_save.getBoolean("chk2", false);

        // 아이디 / 비밀번호 체크박스
        if(chk1 == true) {
            username.setText(id);
            etIdSave.setChecked(chk1);
        }
        if(chk2 == true) {
            passwd.setText(pwd);
            etPwdSave.setChecked(chk2);
        }
    }

    // 현재 액티비티를 나갈 시 아이디와 비밀번호 저장
    public void onStop() {
        super.onStop();

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        EditText username = (EditText) findViewById(R.id.username);
        EditText passwd = (EditText) findViewById(R.id.password);
        CheckBox etIdSave = (CheckBox) findViewById(R.id.id_save);
        CheckBox etPwdSave = (CheckBox) findViewById(R.id.pwd_save);

        // shared프리퍼런스에 각 아이디 비번 저장
        editor.putString("id_save", username.getText().toString());
        editor.putString("pwd_save", passwd.getText().toString());
        editor.putBoolean("chk1", etIdSave.isChecked());
        editor.putBoolean("chk2", etPwdSave.isChecked());

        editor.commit();
    }


    //로그인 버튼 실행 함수
    //로그인 버튼 실행 함수
    public void doLogin(View v) throws InterruptedException {
        final String uname_string  = username.getText().toString();
        final String pw_string = passwd.getText().toString();

        HashMap<String, String> result;

        HashMap<String, String> params = new HashMap<>();
        params.put("username", uname_string);
        params.put("password", pw_string);

        Sender jot = new Sender();
        sender.setParams(params);
        sender.start();
        sender.join();
        result = sender.getResult();
        if(result.get("islogin").equals("true")){
            Toast.makeText(LoginActivity.this, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show();

            session.createLoginSession(uname_string);   // 로그인 성공시 username받아온거 uname_string에 박아서 세션 생성함.
            session.setValue("id", uname_string);
            session.setValue("pw", pw_string);
            session.setValue("cookie", result.get("cookie"));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 로그인 후 메인 액티비티로 이동
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "로그인에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
        }

               // 로그인을 위해서 보낼 아이디와 비번입네다

/*
        //로그인 리퀘스트 부분
        RequestQueue postReqeustQueue = Volley.newRequestQueue(this);
        StringRequest postStringRequest = new StringRequest(Request.Method.POST, "http://203.249.22.59:3000/login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String result = response; //첫 500글자만 표현
                Log.e("result",result);
                Log.e("length",Integer.toString(result.length()));

                if(result.length() != 2835)     // 로그인 실패시 리스폰즈 length가 2835임
                {    // 서버에서 전달되는 길이가 달라지면 안됨. 주의사항. 왜달라지는지는 모르겠음.

                }
                else {  }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // 102행 밑에서 생성한 2개의 아이디와 비밀번호 파라미터 저장
                Map<String, String> params = new HashMap<>();
                params.put("username", uname_string);
                params.put("password", pw_string);

                return params;
            }
        };

        postReqeustQueue.add(postStringRequest);
        */
    }   // 로그인 버튼 END



    //회원가입 누르면 JoinActivity로 전환하는 곳
    public void doJoin(View v){
        Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
        startActivity(intent);
        finish();
    }

    private void clearVideoCache(java.io.File dir){
        if(dir==null) {
            File sdCard = Environment.getExternalStorageDirectory();
            String dirPath = sdCard.getAbsolutePath() + "/Verrapel/Video";
            dir = new File(dirPath);
        }
        else;
        if(dir==null)
            return;
        else;

        java.io.File[] children = dir.listFiles();
        if (dir.exists()) {
            for (File childFile : children) {
                if (childFile.isDirectory()) {
                    clearVideoCache(new File(childFile.getAbsolutePath()));    //하위 디렉토리
                } else {
                    childFile.delete();    //하위 파일
                }
            }
            dir.delete();
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        clearVideoCache(null);
    }
}
