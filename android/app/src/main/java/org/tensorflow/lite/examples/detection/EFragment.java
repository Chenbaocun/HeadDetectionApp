package org.tensorflow.lite.examples.detection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class EFragment extends android.support.v4.app.Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_e, container,false);
        SharedPreferences sharedPre=getActivity().getSharedPreferences("config", getActivity().MODE_PRIVATE);
        final String username=sharedPre.getString("username", "");
//        第一个信息
        QMUIGroupListView qmuiGroupListView=view.findViewById(R.id.groupListView1);
        QMUICommonListItemView qmuiCommonListItemView=qmuiGroupListView.createItemView(username);
        qmuiCommonListItemView.setOrientation(QMUICommonListItemView.VERTICAL);
        qmuiCommonListItemView.setDetailText("\uFEFFε≡٩(๑>₃<)۶ 一心向学");
        qmuiCommonListItemView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        //        qmuiCommonListItemView.showNewTip(true);
        qmuiCommonListItemView.setImageDrawable(getResources().getDrawable(R.mipmap.touxiang));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
//                    Intent intent=new Intent(activity_me.this,activity_profile.class);
//                    startActivity(intent);
//                    Toast.makeText(activity_me.this,  "表情", Toast.LENGTH_SHORT).show();
                }
            }
        };
        QMUICommonListItemView qmuiCommonListItemView1=qmuiGroupListView.createItemView("阈值设定");
        qmuiCommonListItemView1.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        qmuiCommonListItemView1.setImageDrawable(getResources().getDrawable(R.mipmap.threshold));
        View.OnClickListener onClickListener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    final QMUITipDialog tipDialog=new QMUITipDialog.Builder(getActivity()).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                            .setTipWord("Loading")
                            .create();
                    tipDialog.show();
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
                                if (s.equals("0")) {
                                    Toast.makeText(getActivity(), "你还未设置过阈值", Toast.LENGTH_SHORT).show();
                                } else {
//                                    System.out.println("s:"+s);
//                                    System.out.println("threshold:"+threshold);
                                    final String threshold=sharedPre.getString("threshold", "");
                                    if(s.equals(threshold)){

                                    }
                                    else{
                                        Toast.makeText(getActivity(), "阈值从云端更新为："+s, Toast.LENGTH_SHORT).show();
                                        MainActivity.saveThreshold(getActivity(),s);
                                    }
                                }
                                Intent intent=new Intent(getActivity(),thresholdActivity.class);
                                startActivity(intent);
                                tipDialog.dismiss();
                                super.onPostExecute(s);
                            }
                        }
                    }.execute("http://39.96.169.188/get_threshold_app/");



                }
            }
        };
        //        第二个
        QMUICommonListItemView qmuiCommonListItemView2=qmuiGroupListView.createItemView("收藏");
        qmuiCommonListItemView2.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        qmuiCommonListItemView2.setImageDrawable(getResources().getDrawable(R.mipmap.collection));
        View.OnClickListener onClickListener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
//                    Intent intent=new Intent(activity_me.this,activity_collection.class);
//                    startActivity(intent);
//                    Toast.makeText(activity_me.this,  "表情", Toast.LENGTH_SHORT).show();
                }
            }
        };
//        第四个
        QMUICommonListItemView qmuiCommonListItemView3=qmuiGroupListView.createItemView("关注");
        qmuiCommonListItemView3.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        qmuiCommonListItemView3.setImageDrawable(getResources().getDrawable(R.mipmap.follow));
        View.OnClickListener onClickListener3 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
//                    Intent intent=new Intent(activity_me.this,activity_follow.class);
//                    startActivity(intent);
//                    Toast.makeText(activity_me.this,  "表情", Toast.LENGTH_SHORT).show();
                }
            }
        };
        QMUICommonListItemView qmuiCommonListItemView4=qmuiGroupListView.createItemView("关于");
        qmuiCommonListItemView4.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        qmuiCommonListItemView4.setImageDrawable(getResources().getDrawable(R.mipmap.about));
        View.OnClickListener onClickListener4 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    Intent intent=new Intent(getActivity(),aboutActivity.class);
                    startActivity(intent);
                }
            }
        };

        QMUICommonListItemView qmuiCommonListItemView8=qmuiGroupListView.createItemView("设置");
        qmuiCommonListItemView8.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        qmuiCommonListItemView8.setImageDrawable(getResources().getDrawable(R.mipmap.setting));
        View.OnClickListener onClickListener8 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    Intent intent=new Intent(getActivity(),settingActivity.class);
                    startActivity(intent);
                }
            }
        };
        QMUIGroupListView.newSection(getActivity()).setTitle("个人信息").addItemView(qmuiCommonListItemView,onClickListener)
                .addTo(qmuiGroupListView);
        QMUIGroupListView.newSection(getActivity()).addItemView(qmuiCommonListItemView1,onClickListener1)
                .addTo(qmuiGroupListView);
        QMUIGroupListView.newSection(getActivity()).addItemView(qmuiCommonListItemView2,onClickListener2)
                .addTo(qmuiGroupListView);
        QMUIGroupListView.newSection(getActivity()).addItemView(qmuiCommonListItemView3,onClickListener3)
                .addTo(qmuiGroupListView);
        QMUIGroupListView.newSection(getActivity()).addItemView(qmuiCommonListItemView4,onClickListener4)
                .addTo(qmuiGroupListView);
        QMUIGroupListView.newSection(getActivity()).addItemView(qmuiCommonListItemView8,onClickListener8)
                .addTo(qmuiGroupListView);
        return view;

    }

    //提示消息
    public void showToast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }
}