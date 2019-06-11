package org.tensorflow.lite.examples.detection;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView;

public class aboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        QMUILinkTextView qmuiLinkTextView=findViewById(R.id.link_text_view);
        QMUILinkTextView.OnLinkClickListener mOnLinkListener=new QMUILinkTextView.OnLinkClickListener(){
            //识别号码的方法
            @Override
            public void onTelLinkClick(final String phoneNumber) {
//                Toast.makeText(getBaseContext(), "识别到电话号码是：" + phoneNumber, Toast.LENGTH_SHORT).show();
                new QMUIBottomSheet.BottomListSheetBuilder(aboutActivity.this).addItem("拨打电话").addItem("添加到联系人")
                        .addItem("复制")
                        .addItem("取消") .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener(){
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        if(position==0){
                            Intent dialIntent =  new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));//跳转到拨号界面，同时传递电话号码
                            startActivity(dialIntent);
                        }
                        else if(position==1){
                            Intent dialIntent1 =  new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + phoneNumber));//跳转到拨号界面，同时传递电话号码
                            startActivity(dialIntent1);

                        }
                        else if(position==2){
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(null, phoneNumber);
                            cm.setPrimaryClip(clipData);
                            Toast.makeText(aboutActivity.this,"复制成功",Toast.LENGTH_SHORT).show();
                        }

                    }

                }).build()
                        .show();
            }
            //识别邮件的方法
            @Override
            public void onMailLinkClick(final String mailAddress) {
                new QMUIBottomSheet.BottomListSheetBuilder(aboutActivity.this).addItem("复制").addItem("取消")
                        .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
//                                dialog.dismiss();
                                if(position==0){
                                    dialog.dismiss();
                                    ClipboardManager cm=(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clipData=ClipData.newPlainText(null,mailAddress);
                                    cm.setPrimaryClip(clipData);
                                    Toast.makeText(aboutActivity.this,"复制成功",Toast.LENGTH_SHORT).show();
                                }
                                else if(position==1){
                                    dialog.dismiss();
                                }
                            }
                        }).build().show();
//                Toast.makeText(getBaseContext(), "识别到邮件地址是：" + mailAddress, Toast.LENGTH_SHORT).show();
            }
            //识别网页链接的号码
            @Override
            public void onWebUrlLinkClick(final String url) {
                new QMUIBottomSheet.BottomListSheetBuilder(aboutActivity.this).addItem("打开网页").addItem("取消")
                        .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(final QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                if(position==0){
                                    new QMUIDialog.MessageDialogBuilder(aboutActivity.this).setTitle("打开网页").setMessage("确定要在浏览器中打开"+url+"吗？").addAction("打开",
                                            new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog1, int index) {
                                                    dialog.dismiss();
                                                    dialog1.dismiss();
                                                    Uri uri=Uri.parse(url);
                                                    Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                                                    startActivity(intent);
//                                                    Toast.makeText(getBaseContext(), "识别到网页链接是：" + url, Toast.LENGTH_SHORT).show();
                                                }
                                            }


                                    ).addAction("取消", new QMUIDialogAction.ActionListener() {
                                        @Override
                                        public void onClick(QMUIDialog dialog1, int index) {
                                            dialog1.dismiss();
                                            dialog.dismiss();
                                        }
                                    }).show();
                                }
                                else{
                                    dialog.dismiss();
                                }
                            }
                        }).build().show();



            }

        };
        qmuiLinkTextView.setOnLinkClickListener(mOnLinkListener);
    }
}
