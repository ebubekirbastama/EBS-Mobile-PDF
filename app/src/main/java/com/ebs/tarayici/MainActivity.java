package com.ebs.tarayici;

import androidx.activity.ComponentActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.*;
import androidx.work.*;
import java.io.*;
import java.util.*;

public class MainActivity extends ComponentActivity {
    private static final int CAMERA=11, PICK=12, SAVE=13;
    private final ArrayList<String> pages=new ArrayList<>();
    private LinearLayout list, root;
    private TextView countLabel, status;
    private Uri pendingCamera;
    private String pendingFile;
    private String readyPdf;
    private UUID currentWork;
    private boolean continuousCapture = false;
    private int navy=Color.rgb(16,27,50), blue=Color.rgb(45,116,245), teal=Color.rgb(0,174,159);
    private int dp(float val) {return (int)(val*getResources().getDisplayMetrics().density+0.5f);}
    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);
        restore(); render();
        String token=getPreferences(0).getString("work",null);
        if(token!=null) {try{watch(UUID.fromString(token));}catch(Exception ignored){}}
    }
    private GradientDrawable background(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String s,int size,int color, boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private LinearLayout panel(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}
    private LinearLayout.LayoutParams lp(int w,int h,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));p.topMargin=dp(top);return p;}
    private TextView button(String title,int color,Runnable action){
        TextView b=text(title,15,Color.WHITE,true);b.setGravity(Gravity.CENTER);b.setBackground(background(color,16));
        b.setElevation(dp(4));b.setPadding(dp(12),0,dp(12),0);b.setOnClickListener(v->{b.animate().scaleX(.96f).scaleY(.96f).setDuration(90).withEndAction(()->{b.animate().scaleX(1).scaleY(1).setDuration(140);action.run();});});return b;
    }
    private void render(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xffF2F5FA);
        root=panel();root.setPadding(dp(18),dp(20),dp(18),dp(30));scroll.addView(root);setContentView(scroll);
        TextView banner=text("▣  EBS PDF TARAYICI",24,Color.WHITE,true);banner.setPadding(dp(20),dp(18),dp(20),dp(18));banner.setBackground(background(navy,20));root.addView(banner,lp(-1,80,0));
        TextView subtitle=text("Fotoğraf çek • Sayfaları sırala • Tek PDF oluştur",13,0xff53627C,false);subtitle.setPadding(dp(3),dp(8),0,0);root.addView(subtitle);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);root.addView(actions,lp(-1,66,20));
        TextView photo=button("📷  FOTOĞRAF ÇEK",blue,this::takePhoto);LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,dp(58),1);half.rightMargin=dp(6);actions.addView(photo,half);
        TextView gallery=button("＋  GALERİDEN EKLE",teal,this::pickImages);LinearLayout.LayoutParams half2=new LinearLayout.LayoutParams(0,dp(58),1);half2.leftMargin=dp(6);actions.addView(gallery,half2);
        TextView series=button("📷  SERİ ÇEKİM — ARKA ARKAYA SAYFA EKLE",0xff6C42B8,this::startSeries);root.addView(series,lp(-1,56,9));
        countLabel=text("SAYFALAR ("+pages.size()+")",17,navy,true);root.addView(countLabel,lp(-1,45,16));
        list=panel();root.addView(list);drawPages();
        TextView create=button("▤  SAYFALARI PDF YAP",blue,this::createPdf);root.addView(create,lp(-1,58,20));
        TextView save=button("⇩  OLUŞTURULAN PDF'İ KAYDET",navy,this::savePdf);root.addView(save,lp(-1,55,12));
        status=text("Taranan sayfalar telefonda saklanır. Arama gelirse kaldığınız yerden devam edebilirsiniz.",13,0xff46546E,false);status.setPadding(dp(4),dp(14),dp(4),dp(8));root.addView(status);
    }
    private void drawPages(){
        list.removeAllViews();countLabel.setText("SAYFALAR ("+pages.size()+")");
        if(pages.isEmpty()){TextView empty=text("Henüz sayfa eklenmedi. Kamerayla çekin veya galeriden seçin.",14,0xff66748B,false);empty.setPadding(dp(14),dp(20),dp(14),dp(20));empty.setBackground(background(Color.WHITE,14));list.addView(empty,lp(-1,-2,0));return;}
        for(int i=0;i<pages.size();i++) {
            final int pos=i;String path=pages.get(i);
            LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(dp(9),dp(8),dp(8),dp(8));item.setBackground(background(Color.WHITE,16));item.setElevation(dp(1));list.addView(item,lp(-1,88,8));
            ImageView thumb=new ImageView(this);thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            BitmapFactory.Options opt=new BitmapFactory.Options();opt.inSampleSize=8;Bitmap bm=BitmapFactory.decodeFile(path,opt);if(bm!=null)thumb.setImageBitmap(bm);
            item.addView(thumb,new LinearLayout.LayoutParams(dp(62),dp(70)));
            TextView label=text("  Sayfa "+(i+1),15,navy,true);item.addView(label,new LinearLayout.LayoutParams(0,-1,1));
            LinearLayout buttons=panel();item.addView(buttons);
            TextView up=text("▲",20,blue,true);up.setGravity(Gravity.CENTER);up.setOnClickListener(v->move(pos,-1));buttons.addView(up,new LinearLayout.LayoutParams(dp(36),dp(34)));
            TextView down=text("▼",20,blue,true);down.setGravity(Gravity.CENTER);down.setOnClickListener(v->move(pos,1));buttons.addView(down,new LinearLayout.LayoutParams(dp(36),dp(34)));
            TextView del=text("✕",20,0xffD84552,true);del.setGravity(Gravity.CENTER);del.setOnClickListener(v->{pages.remove(pos);persist();drawPages();});item.addView(del,new LinearLayout.LayoutParams(dp(36),-1));
        }
    }
    private void move(int at,int delta){int to=at+delta;if(to<0||to>=pages.size())return;Collections.swap(pages,at,to);persist();drawPages();}
    private File pageDir(){File f=new File(getFilesDir(),"pages");f.mkdirs();return f;}
    private String nextName(){return "page_"+System.currentTimeMillis()+"_"+UUID.randomUUID().toString().substring(0,7)+".jpg";}
    private void startSeries(){
        continuousCapture=true;
        takePhoto();
    }
    private void takePhoto(){
        try{File f=new File(pageDir(),nextName());pendingFile=f.getAbsolutePath();getPreferences(0).edit().putString("pending",pendingFile).apply();
            pendingCamera=FileProvider.getUriForFile(this,getPackageName()+".provider",f);
            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);intent.putExtra(MediaStore.EXTRA_OUTPUT,pendingCamera);intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if(intent.resolveActivity(getPackageManager())==null){continuousCapture=false;note("Kamera uygulaması bulunamadı.");return;}
            startActivityForResult(intent,CAMERA);
        }catch(Exception e){continuousCapture=false;note("Kamera açılamadı: "+e.getMessage());}
    }
    private void pickImages(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(i,PICK);}
    @Override protected void onActivityResult(int req,int result,@Nullable Intent data){super.onActivityResult(req,result,data);
        if(result!=RESULT_OK){
            if(req==CAMERA && continuousCapture){continuousCapture=false;note("Seri çekim durdu. Kaydedilmiş sayfalar korundu.");}
            return;
        }
        if(req==CAMERA){
            String path=getPreferences(0).getString("pending",pendingFile);
            if(path!=null && new File(path).length()>0 && !pages.contains(path)){
                pages.add(path);persist();drawPages();note("Sayfa eklendi (toplam "+pages.size()+").");
                if(continuousCapture){
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Seri çekim — "+pages.size()+" sayfa")
                        .setMessage("Sonraki sayfayı çekmek ister misiniz? Çekimi bitirirseniz tüm fotoğraflar listede kalır.")
                        .setPositiveButton("Sonraki sayfayı çek",(dialog,which)->takePhoto())
                        .setNegativeButton("Çekimi bitir",(dialog,which)->{continuousCapture=false;note(pages.size()+" sayfa PDF için hazır.");})
                        .setOnCancelListener(dialog->{continuousCapture=false;})
                        .show();
                }
            } else {continuousCapture=false;note("Fotoğraf kaydedilemedi, önceki sayfalar korundu.");}
        }
        if(req==PICK && data!=null){int added=0;
            try{if(data.getClipData()!=null){for(int j=0;j<data.getClipData().getItemCount();j++){importUri(data.getClipData().getItemAt(j).getUri());added++;}}
                else if(data.getData()!=null){importUri(data.getData());added++;}
                persist();drawPages();note(added+" sayfa eklendi.");
            }catch(Exception e){persist();drawPages();note("Aktarım hatası: "+e.getMessage());}}
        if(req==SAVE && data!=null && data.getData()!=null && readyPdf!=null){
            try(InputStream src=new FileInputStream(readyPdf);OutputStream dest=getContentResolver().openOutputStream(data.getData(),"w")){
                if(dest==null)throw new IOException("Destination not writable");byte[] buf=new byte[65536];int n;while((n=src.read(buf))!=-1)dest.write(buf,0,n);dest.flush();note("PDF kaydedildi.");
            }catch(Exception e){note("Kaydetme hatası: "+e.getMessage());}}
    }
    private void importUri(Uri uri)throws IOException{
        File f=new File(pageDir(),nextName());
        try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(f)){
            if(in==null)throw new IOException("Fotoğraf okunamadı");byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
        }if(f.length()==0)throw new IOException("Boş resim dosyası");pages.add(f.getAbsolutePath());
    }
    private void persist(){getPreferences(0).edit().putString("pages",android.text.TextUtils.join("\n",pages)).apply();}
    private void restore(){String saved=getPreferences(0).getString("pages","");for(String p:saved.split("\n"))if(!p.isEmpty() && new File(p).isFile())pages.add(p);
        readyPdf=getPreferences(0).getString("pdf",null);}
    private void createPdf(){continuousCapture=false;if(pages.isEmpty()){note("Önce fotoğraf ekleyin.");return;}
        String name="EBS_Tarama_"+System.currentTimeMillis()+".pdf";
        Data input=new Data.Builder().putStringArray("paths",pages.toArray(new String[0])).putString("output",name).build();
        OneTimeWorkRequest request=new OneTimeWorkRequest.Builder(PdfWorker.class).setInputData(input).addTag("pdf-export").build();
        currentWork=request.getId();getPreferences(0).edit().putString("work",currentWork.toString()).apply();
        WorkManager.getInstance(this).enqueue(request);watch(currentWork);note("PDF oluşturma arka planda başladı. Arama gelirse işlem korunur.");
    }
    private void watch(UUID id){WorkManager.getInstance(this).getWorkInfoByIdLiveData(id).observe(this,info->{if(info==null)return;
        if(info.getState()==WorkInfo.State.SUCCEEDED){readyPdf=info.getOutputData().getString("pdf");getPreferences(0).edit().putString("pdf",readyPdf).remove("work").apply();note("PDF hazır! Kaydet düğmesine dokunun.");}
        else if(info.getState()==WorkInfo.State.FAILED){getPreferences(0).edit().remove("work").apply();note("PDF hatası: "+info.getOutputData().getString("error"));}
        else if(info.getState()==WorkInfo.State.RUNNING){int done=info.getProgress().getInt("done",0);int total=info.getProgress().getInt("total",pages.size());note("PDF hazırlanıyor: "+done+"/"+total+" sayfa");}
    });}
    private void savePdf(){if(readyPdf==null||!new File(readyPdf).isFile()){note("Önce PDF oluşturun.");return;}
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");i.putExtra(Intent.EXTRA_TITLE,new File(readyPdf).getName());startActivityForResult(i,SAVE);
    }
    private void note(String s){if(status!=null)status.setText(s);Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
