package org.tensorflow.lite.examples.detection;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class thresholdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold);
        TextView textView=findViewById(R.id.threshold_now);
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        final String username=sharedPre.getString("username", "");
        final String threshold=sharedPre.getString("threshold", "");
        textView.setText("当前阈值为："+threshold);
    }
    public void onSubmit(View view){
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        final String username=sharedPre.getString("username", "");
        TextView textView=findViewById(R.id.threshold_now);
        EditText editText=findViewById(R.id.threshold_update);
        String threshold_update=editText.getText().toString();
        if(threshold_update.equals("")){
            Toast.makeText(thresholdActivity.this, "您未输入任何数值", Toast.LENGTH_SHORT).show();
        }
        else {
//        异步更新
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... strings) {
                    try {
                        URL url = new URL(strings[0]);
                        HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                        coon.setDoOutput(true);
                        coon.setRequestMethod("POST");
                        DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                        out.writeBytes("username=" + username + "&threshold=" + threshold_update);
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
                        if (s.equals("1")) {
                            Toast.makeText(thresholdActivity.this, "云端阈值更新成功", Toast.LENGTH_SHORT).show();
                            textView.setText("当前阈值为：" + threshold_update);
                            editText.getText().clear();
                        } else {
                            Toast.makeText(thresholdActivity.this, "云端阈值更新失败2：请检查网络！", Toast.LENGTH_SHORT).show();
                        }
                        super.onPostExecute(s);
                    }
                }
            }.execute("http://39.96.169.188/set_threshold_app/");
        }
    }
}
