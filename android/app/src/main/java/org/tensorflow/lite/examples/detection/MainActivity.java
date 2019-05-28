package org.tensorflow.lite.examples.detection;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.next.easynavigation.view.EasyNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//底部状态栏预设参数
    private EasyNavigationBar navigationBar;
    private String[] tabText = {"首页", "发现", "发布", "消息", "我的"};
    //未选中icon
    private int[] normalIcon = {R.mipmap.index, R.mipmap.find, R.mipmap.add_image, R.mipmap.message, R.mipmap.me};
    //选中时icon
    private int[] selectIcon = {R.mipmap.index1, R.mipmap.find1, R.mipmap.add_image, R.mipmap.message1, R.mipmap.me1};
    private List<Fragment> fragments = new ArrayList<>();
    private Handler mHandler = new Handler();
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                            Intent intent=new Intent(MainActivity.this,DetectorActivity.class);
                            startActivity(intent);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //＋ 旋转动画
                                    if (flag) {
                                        navigationBar.getAddImage().animate().rotation(45).setDuration(400);
                                    } else {
                                        navigationBar.getAddImage().animate().rotation(0).setDuration(400);
                                    }
                                    flag = !flag;
                                }
                            });
                            return true;//阻止fragment跳转
                        }
                        return false;
                    }
                })
                .canScroll(true)
                .mode(EasyNavigationBar.MODE_ADD)
                .build();
    }
}
