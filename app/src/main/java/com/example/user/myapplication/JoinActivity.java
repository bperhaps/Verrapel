package com.example.user.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class JoinActivity extends AppCompatActivity {

    EditText username, name, email, passwd, passwd_cnf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // 객체 참조
        username = (EditText) findViewById(R.id.username);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        passwd = (EditText) findViewById(R.id.password);
        passwd_cnf = (EditText) findViewById(R.id.password_confirm);
    }

    //쪼인 버튼 실행 함수
    public void doJoin(View v){
        /*Log.e(username.getText().toString(),name.getText().toString());
        Log.e(email.getText().toString(),passwd.getText().toString());
        Log.e(passwd_cnf.getText().toString(),"fuck");*/

        // 회원가입할때 날릴 파라미터들을 위한 제물
        final String uname_string  = username.getText().toString();
        final String name_string = name.getText().toString();
        final String email_string = email.getText().toString();
        final String pw_string = passwd.getText().toString();
        final String pw_cnf_string = passwd_cnf.getText().toString();

        //세션 끊기 지우든 말든 알아서 하셈
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://localhost:3000/logout", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String result = response.substring(0,500);
                Log.e("logout", result);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(stringRequest);
        //요기까지


        //회원가입 요청 부분
        RequestQueue postReqeustQueue = Volley.newRequestQueue(this);
        StringRequest postStringRequest = new StringRequest(Request.Method.POST, "http://localhost:3000/users/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("result",response);
                Log.e("length",Integer.toString(response.length()));

                if(response.matches("id=\"email\"")) {
                    Toast.makeText(JoinActivity.this, "회원 가입에 실패했습니다.", Toast.LENGTH_LONG).show();
                } else {

                    Intent intent = new Intent(JoinActivity.this, LoginActivity.class); // 회원가입화면에서 로그인 화면으로 감
                    startActivity(intent);
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // 회원가입에 필요한 제물들
                params.put("username", uname_string);
                params.put("name", name_string);
                params.put("email", email_string);
                params.put("password", pw_string);
                params.put("passwordConfirmation", pw_cnf_string);

                return params;  // 파라미터 다 쳐담아서 리턴
            }
        };

        postReqeustQueue.add(postStringRequest);
    }
}