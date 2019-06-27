package org.tensorflow.lite.examples.detection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.next.easynavigation.view.EasyNavigationBar;
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

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Jue on 2018/6/2.
 */

public class DFragment extends android.support.v4.app.Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_d, container,false);

       // 第一个信息
        QMUIGroupListView qmuiGroupListView=view.findViewById(R.id.groupListView2);
        QMUICommonListItemView qmuiCommonListItemView=qmuiGroupListView.createItemView("云平台通知");
        qmuiCommonListItemView.setOrientation(QMUICommonListItemView.VERTICAL);
        qmuiCommonListItemView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        qmuiCommonListItemView.setDetailText("点击查看详情....");
//                qmuiCommonListItemView.showNewTip(true);
        qmuiCommonListItemView.setImageDrawable(getResources().getDrawable(R.mipmap.message));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    Intent intent=new Intent(getActivity(),messageActivity.class);
                    startActivity(intent);
//                    Toast.makeText(activity_me.this,  "表情", Toast.LENGTH_SHORT).show();
                }
            }
        };


        QMUIGroupListView.newSection(getActivity()).setTitle("消息面板").addItemView(qmuiCommonListItemView,onClickListener)
                .addTo(qmuiGroupListView);
        return view;
    }

    //提示消息
    public void showToast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }
}
