package org.tensorflow.lite.examples.detection;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class messageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setTitle("新消息");

        TextView textView=findViewById(R.id.target);

        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        final String username=sharedPre.getString("username", "");
        //获取数据组合消息。
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                    coon.setDoOutput(true);
                    coon.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                    out.writeBytes("username=" + username);
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
                if (s != null) {
                    textView.setText("根据平台内所有用户上报的监测数据，近期" +s.split("#")[0]+ "出现异常数最多为" + s.split("#")[1]+"次,现请您前往并部署边缘设备，做好维持秩序准备。(*^▽^*)！");
                    super.onPostExecute(s);
                }
            }
        }.execute("http://39.96.169.188/getMessageApp/");
    }
}
