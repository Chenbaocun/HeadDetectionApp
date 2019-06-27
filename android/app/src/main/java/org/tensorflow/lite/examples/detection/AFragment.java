package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

//校园大门、学校食堂、大型商超、政府大门、交通枢纽
public class AFragment extends android.support.v4.app.Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_a, null);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView textView=getActivity().findViewById(R.id.target);
        SharedPreferences sharedPre=getActivity().getSharedPreferences("config", MODE_PRIVATE);
        final String target=sharedPre.getString("target", "");
        if(target==""){
            textView.setText("您还未设置检测场景，请点击右上角切换场景按钮进行设置。");
        }
        switch (target){
            case "1":
                textView.setText("学校大门");
                break;
            case "2":
                textView.setText("学校食堂");
                break;
            case "3":
                textView.setText("大型商超");
                break;
            case "4":
                textView.setText("政府大门");
                break;
            case "5":
                textView.setText("交通枢纽");
                break;
        }
        getActivity().findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPre=getActivity().getSharedPreferences("config", MODE_PRIVATE);
                final String username=sharedPre.getString("username", "");
                if(username==""){
                    Toast.makeText(getActivity(), "请您登陆", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getActivity(), login.class);
                    startActivity(intent);
                    return;
                }
                openBottomSheet_grid();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void openBottomSheet_grid(){
        final int School = 0;
        final int Dining = 1;
        final int Mall = 2;
        final int Government = 3;
        final int Traffic = 4;
        TextView textView =getActivity().findViewById(R.id.target);
        QMUIBottomSheet.BottomGridSheetBuilder builder=new QMUIBottomSheet.BottomGridSheetBuilder(getContext());
        //第一个参数：图片的id，第二个参数：文字内容，第三个参数：标记在监听里面根据当前标记做其他操作，第四个参数：表示当前item放到一行FIRST_LINE：第一行，SECOND_LINE：第二行
        builder.addItem(R.mipmap.school,"校园大门",School, QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.mipmap.dining,"学校食堂",Dining,QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.mipmap.mall,"大型商超",Mall,QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.mipmap.government,"政府大门",Government,QMUIBottomSheet.BottomGridSheetBuilder.SECOND_LINE)
                .addItem(R.mipmap.traffic,"交通枢纽",Traffic,QMUIBottomSheet.BottomGridSheetBuilder.SECOND_LINE)
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomGridSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView) {
                        int tag= (int) itemView.getTag();
                        switch (tag){
                            case School:
                                textView.setText("学校大门");
                                dialog.dismiss();
                                showToast(getActivity(),"识别场景切换到-学校大门");
                                setTarget("1");
                                break;
                            case Dining:
                                textView.setText("学校食堂");
                                dialog.dismiss();
                                showToast(getActivity(),"识别场景切换到-学校食堂");
                                setTarget("2");

                                break;
                            case Mall:
                                textView.setText("大型商超");
                                dialog.dismiss();
                                showToast(getActivity(),"识别场景切换到-大型商超");
                                setTarget("3");

                                break;
                            case Government:
                                textView.setText("政府大门");
                                dialog.dismiss();
                                showToast(getActivity(),"识别场景切换到-政府大门");
                                setTarget("4");

                                break;
                            case Traffic:
                                textView.setText("交通枢纽");
                                dialog.dismiss();
                                showToast(getActivity(),"识别场景切换到-交通枢纽");
                                setTarget("5");
                                break;
                        }
                    }
                })
                .build().show();
    }
    public void setTarget(String target){
        SharedPreferences sharedPre=getActivity().getSharedPreferences("config", MODE_PRIVATE);
        final String username=sharedPre.getString("username", "");
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection coon = (HttpURLConnection) url.openConnection();
                    coon.setDoOutput(true);
                    coon.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(coon.getOutputStream());
                    out.writeBytes("username=" + username + "&target=" +target);
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
                        Toast.makeText(getActivity(), "账号或密码错误请检查", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(),  "监测场景切换成功", Toast.LENGTH_SHORT).show();
                        saveTarget(getActivity(),target);
                    }
                    super.onPostExecute(s);
                }
            }
        }.execute("http://39.96.169.188/setTarget_app/");


    }
    public static void saveTarget(Context context, String target){
        //获取SharedPreferences对象
        SharedPreferences sharedPre=context.getSharedPreferences("config", MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor=sharedPre.edit();
        //设置参数
        editor.putString("target", target);
        //提交
        editor.commit();
    }
    public static void showToast(Context context,String text){
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
    }

}