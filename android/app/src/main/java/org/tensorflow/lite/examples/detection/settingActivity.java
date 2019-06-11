package org.tensorflow.lite.examples.detection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

public class settingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("设置");
        QMUIGroupListView qmuiGroupListView=findViewById(R.id.groupListView1);
        QMUICommonListItemView qmuiCommonListItemView=qmuiGroupListView.createItemView("切换账号");
        qmuiCommonListItemView.setDetailText("点击更改账号");
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    new QMUIDialog.MessageDialogBuilder(settingActivity.this)
                            .setTitle("警告")
                            .setMessage("确定要切换账号吗")
                            .addAction(0,"确定",QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    login.saveLoginInfo(settingActivity.this,"","");
                                    dialog.dismiss();
                                    Intent intent=new Intent(settingActivity.this,MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }).setCanceledOnTouchOutside(false).addAction(0,"取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            }
        };
        QMUIGroupListView.newSection(getBaseContext()).setTitle("  ").addItemView(qmuiCommonListItemView,onClickListener)
                .addTo(qmuiGroupListView);
    }

}

