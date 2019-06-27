package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;


public class BFragment extends android.support.v4.app.Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_b, container,false);

//        // 第一个信息
//        QMUIGroupListView qmuiGroupListView=view.findViewById(R.id.groupListView3);
//        QMUICommonListItemView qmuiCommonListItemView=qmuiGroupListView.createItemView("计算结果");
//        qmuiCommonListItemView.setDetailText("您可以在此查看您所上传的异常图片由云端模型计算完成后的结果。");
//        qmuiCommonListItemView.setOrientation(QMUICommonListItemView.VERTICAL);
//
//        qmuiCommonListItemView.setImageDrawable(getResources().getDrawable(R.mipmap.picture));
//        View.OnClickListener onClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v instanceof QMUICommonListItemView) {
////                    Toast.makeText(activity_me.this,  "表情", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//        QMUIGroupListView.newSection(getActivity()).setTitle("结果面板").addItemView(qmuiCommonListItemView,onClickListener)
//                .addTo(qmuiGroupListView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//      不加这一段是无法加载图片的，事实证明不加也是可以的。= =
//        StrictMode.setThreadPolicy(new
//                StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//        StrictMode.setVmPolicy(
//                new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        Button last=getActivity().findViewById(R.id.last);
        Button next=getActivity().findViewById(R.id.next);
        SharedPreferences sharedPre=getActivity().getSharedPreferences("config", MODE_PRIVATE);
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final QMUITipDialog tipDialog=new QMUITipDialog.Builder(getActivity()).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("Loading")
                        .create();
                tipDialog.show();
                String index="";
                final String ImageIndex=sharedPre.getString("ImageIndex", "");
                final String username=sharedPre.getString("username", "");
                if (ImageIndex=="" || ImageIndex.equals("0") ){
                    Toast.makeText(getActivity(),"已经到头儿了喂！(－＂－怒)",Toast.LENGTH_SHORT).show();
                    index="0";
                    tipDialog.dismiss();
                    return;
                }
                else {
                    index=Integer.parseInt(ImageIndex)-1+"";
                }
                new AsyncTask<String, Void, Bitmap>(){
                    @Override
                    protected Bitmap doInBackground(String... strings) {
                        String index=strings[0];
                        Bitmap bitmap=getURLimage("http://39.96.169.188/image_play_app/?username="+username+"&num="+index);
                        int i = Integer.parseInt(index);
                        saveImageIndex(getActivity(), i + "");
                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap s) {
                        ImageView imageView = getActivity().findViewById(R.id.image1);
                        imageView.setImageBitmap(s);
                        tipDialog.dismiss();
                        super.onPostExecute(s);
                    }
                }.execute(index);

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final QMUITipDialog tipDialog=new QMUITipDialog.Builder(getActivity()).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("Loading")
                        .create();
                tipDialog.show();
                String index="";
                final String ImageIndex=sharedPre.getString("ImageIndex", "");
                final String username=sharedPre.getString("username", "");
                if (ImageIndex==""){
                    saveImageIndex(getActivity(),"0");
                    index="0";
                }
                else{
                    index=Integer.parseInt(ImageIndex)+1+"";
                }
                new AsyncTask<String, Void, Bitmap>(){
                    @Override
                    protected Bitmap doInBackground(String... strings) {
                        String index=strings[0];
                        Bitmap bitmap=getURLimage("http://39.96.169.188/image_play_app/?username="+username+"&num="+index);
//                        System.out.println("收到"+(bitmap)+"");
                        if (bitmap==null){
                        }
                        else {
                            int i = Integer.parseInt(index);
                            saveImageIndex(getActivity(), i + "");
                            return bitmap;
                        }
                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap s) {
                        if(s==(null)){
                            Toast.makeText(getActivity(),"到底儿了大哥(〃'▽'〃)",Toast.LENGTH_SHORT).show();
                            tipDialog.dismiss();
                        }
                        else {
                        ImageView imageView = getActivity().findViewById(R.id.image1);
                        imageView.setImageBitmap(s);
                        tipDialog.dismiss();}
                        super.onPostExecute(s);
                    }
                }.execute(index);

            }
        });

    }



    public static Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }
    public static void saveImageIndex(Context context, String index){
        //获取SharedPreferences对象
        SharedPreferences sharedPre=context.getSharedPreferences("config", MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor=sharedPre.edit();
        //设置参数
        editor.putString("ImageIndex", index);
        //提交
        editor.commit();
    }
}
