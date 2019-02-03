package com.example.user.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.myapplication.SessionManager;

public class MainActivity extends AppCompatActivity {

    SessionManager session;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pd_button=(Button)findViewById(R.id.picture_detect);
        pd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,PictureDetector.class);
                startActivity(intent);
            }
        });

        Button cr_button=(Button)findViewById(R.id.camera_record);
        cr_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,CameraRecorder.class);
                startActivity(intent);
            }
        });


        session = new SessionManager(getApplicationContext());  // 이걸 빼먹으묜 안댕
    }

    // 로그인 하고나서 뒤로버튼 누르면 끌거냐고 물어보는 부분
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert_ex = new AlertDialog.Builder(this);
        alert_ex.setMessage("정말로 종료하시겠습니까?");

        alert_ex.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert_ex.setNegativeButton("종료", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        alert_ex.setTitle("종료하지마");
        AlertDialog alert = alert_ex.create();
        alert.show();
    }   // 다이얼로그 END 부분


    //로그아웃 누르면 요로코롱
    public void doLogout(View v) {
        //로그아웃 리퀘스트 부분
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest logoutRequest = new StringRequest(Request.Method.GET, "http://localhost:3000/logout", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                session.logoutUser();   // 로그아웃하면 세션 빠이

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", "onErrorResponse: " + error.toString() );
            }
        });

        requestQueue.add(logoutRequest);
    }   // 로그아웃 END
}
