package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    public void loginPost(View view){
        EditText editText_username=findViewById(R.id.username);
        EditText editText_password=findViewById(R.id.password);
        String username=editText_username.getText().toString();
        String password=editText_password.getText().toString();

        //异步请求
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                    coon.setDoOutput(true);
                    coon.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                    out.writeBytes("username=" + username + "&password=" + password);
                    InputStream is = coon.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    isr.close();
                    out.close();
                    return response.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String s) {
                if(s !=null){
                    if(s.equals("0") ){
                        Toast.makeText(login.this, "账号或密码错误请检查", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        saveLoginInfo(login.this,username,password);
                        Toast.makeText(login.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(login.this,MainActivity.class);
                        startActivity(intent);
                    }
                    super.onPostExecute(s);
                }
            }
        }.execute("http://39.96.169.188/login_app/");
    }
    public static void saveLoginInfo(Context context, String username, String password){
        //获取SharedPreferences对象
        SharedPreferences sharedPre=context.getSharedPreferences("config", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor=sharedPre.edit();
        //设置参数
        editor.putString("username", username);
        editor.putString("password", password);
        //提交
        editor.commit();
    }
    }