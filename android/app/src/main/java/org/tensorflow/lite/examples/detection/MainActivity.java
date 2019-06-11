package org.tensorflow.lite.examples.detection;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.next.easynavigation.view.EasyNavigationBar;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//底部状态栏预设参数
    private EasyNavigationBar navigationBar;
    private String[] tabText = {"首页", "发现", "检测", "消息", "我的"};
    //未选中icon
    private int[] normalIcon = {R.mipmap.index, R.mipmap.find, R.mipmap.camera, R.mipmap.message, R.mipmap.me};
    //选中时icon
    private int[] selectIcon = {R.mipmap.index1, R.mipmap.find1, R.mipmap.camera, R.mipmap.message1, R.mipmap.me1};
    private List<Fragment> fragments = new ArrayList<>();
    private Handler mHandler = new Handler();
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//异步请求
        SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
        final String username=sharedPre.getString("username", "");
        final String password=sharedPre.getString("password", "");
        if(username!="" && password !="" ) {
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
                    if (s != null) {
                        if (s.equals("0")) {
                            Toast.makeText(MainActivity.this, "账号或密码错误请检查", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, s + "欢迎回来!", Toast.LENGTH_SHORT).show();
                        }
                        super.onPostExecute(s);
                    }
                }
            }.execute("http://39.96.169.188/login_app/");
        }
//        底部导航
        navigationBar = findViewById(R.id.navigationBar);
        fragments.add(new AFragment());
        fragments.add(new BFragment());
        fragments.add(new CFragment());
        fragments.add(new DFragment());
        fragments.add(new EFragment());
        navigationBar.titleItems(tabText)
                .normalIconItems(normalIcon)
                .selectIconItems(selectIcon)
                .fragmentList(fragments)
                .anim(null)
                .addLayoutRule(EasyNavigationBar.RULE_BOTTOM)
                .addLayoutBottom(0)
                .addAlignBottom(true)
                .addAsFragment(true)
                .fragmentManager(getSupportFragmentManager())
                .onTabClickListener(new EasyNavigationBar.OnTabClickListener() {
                    @Override
                    public boolean onTabClickEvent(View view, int position) {
                        Log.e("onTabClickEvent", position + "");
                        if (position == 2) {
                            SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
                            final String username=sharedPre.getString("username", "");
                            final String password=sharedPre.getString("password", "");
                            if(username=="" && password =="" ){
                                Toast.makeText(MainActivity.this, "请您登陆", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(MainActivity.this, login.class);
                                startActivity(intent);
                                return true;
                            }
//                            请求最新的threshold
                            new AsyncTask<String, Void, String>() {
                                @Override
                                protected String doInBackground(String... strings) {
                                    try {
                                        URL url = new URL(strings[0]);
                                        HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                                        coon.setDoOutput(true);
                                        coon.setRequestMethod("POST");
                                        DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                                        out.writeBytes("username=" + username+"&startCount=1");
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
                                        if (s.equals("0")) {
                                            Toast.makeText(MainActivity.this, "您还未设置过阈值，阈值被初始化为："+5, Toast.LENGTH_SHORT).show();
                                            saveThreshold(MainActivity.this,s);
                                            setThreshold(username,"5");
                                        } else {
                                            final String threshold=sharedPre.getString("threshold", "");
                                            if(!(s.equals(threshold))){
                                                Toast.makeText(MainActivity.this, "阈值从云端更新为："+s, Toast.LENGTH_SHORT).show();
                                                saveThreshold(MainActivity.this,s);
                                            }
                                        }
                                        super.onPostExecute(s);
                                    }
                                }
                            }.execute("http://39.96.169.188/get_threshold_app/");

                            Intent intent=new Intent(MainActivity.this,DetectorActivity.class);
                            startActivity(intent);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //＋ 旋转动画
//                                    if (flag) {
//                                        navigationBar.getAddImage().animate().rotation(45).setDuration(400);
//                                    } else {
//                                        navigationBar.getAddImage().animate().rotation(0).setDuration(400);
//                                    }
//                                    flag = !flag;
                                }
                            });
                            return true;//阻止fragment跳转
                        }
                        if(position==4){
                            SharedPreferences sharedPre=getSharedPreferences("config", MODE_PRIVATE);
                            final String username=sharedPre.getString("username", "");
                            final String password=sharedPre.getString("password", "");
                            if(username=="" && password =="" ){
                                Toast.makeText(MainActivity.this, "请您登陆", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(MainActivity.this, login.class);
                                startActivity(intent);
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .canScroll(true)
                .mode(EasyNavigationBar.MODE_ADD)
                .build();
    }
    public static void saveThreshold(Context context, String threshold){
        //获取SharedPreferences对象
        SharedPreferences sharedPre=context.getSharedPreferences("config", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor=sharedPre.edit();
        //设置参数
        editor.putString("threshold", threshold);
        //提交
        editor.commit();
    }
    public void setThreshold(String username,String threshold){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                    coon.setDoOutput(true);
                    coon.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                    out.writeBytes("username=" + username+"&threshold="+threshold);
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
                        Toast.makeText(MainActivity.this, "云端阈值同步成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "云端阈值更新失败2：请检查网络！", Toast.LENGTH_SHORT).show();
                    }
                    super.onPostExecute(s);
                }
            }
        }.execute("http://39.96.169.188/set_threshold_app/");

    }
}

