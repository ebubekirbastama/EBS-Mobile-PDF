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
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.*;
import androidx.work.*;
import java.io.*;
import java.util.*;

public class MainActivity extends ComponentActivity {
    private static final int CAMERA=11, PICK=12, SAVE=13, SERIES=14;
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
        if(token!=null) {try{currentWork=UUID.fromString(token);watch(currentWork);}catch(Exception ignored){}}
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
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xffF1F5FB);
        root=panel();root.setPadding(dp(16),dp(18),dp(16),dp(36));scroll.addView(root);setContentView(scroll);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(18),dp(12),dp(12),dp(12));header.setBackground(background(navy,22));root.addView(header,lp(-1,76,0));
        TextView brand=text("▣  EBS PDF",22,Color.WHITE,true);header.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        TextView menu=button("☰  MENÜ",0xff344563,this::showMenu);header.addView(menu,new LinearLayout.LayoutParams(dp(105),dp(48)));
        TextView subtitle=text("Belgelerini tara, düzenle ve PDF oluştur",14,0xff53627C,false);subtitle.setPadding(dp(4),dp(12),0,0);root.addView(subtitle);
        TextView series=button("◎  SERİ TARAMAYI BAŞLAT",blue,this::startSeries);root.addView(series,lp(-1,70,24));
        TextView create=button("▤  PDF OLUŞTUR",teal,this::createPdf);root.addView(create,lp(-1,62,12));
        TextView save=button("⇩  PDF'İ KAYDET",navy,this::savePdf);root.addView(save,lp(-1,58,12));
        TextView clear=button("⌫  TÜM SAYFALARI TEMİZLE",0xffB83F51,this::confirmClear);root.addView(clear,lp(-1,50,12));
        countLabel=text("SAYFALAR ("+pages.size()+")",18,navy,true);root.addView(countLabel,lp(-1,48,22));
        TextView desc=text("Tam ekran incelemek için dokunun; sağa/sola kaydırarak gezinin.",13,0xff69768E,false);root.addView(desc,lp(-1,36,0));
        list=panel();root.addView(list);drawPages();
        status=text("Çektiğiniz sayfalar cihazda saklanır.",13,0xff46546E,false);status.setPadding(dp(4),dp(18),dp(4),dp(8));root.addView(status);
    }
    private void showMenu(){
        final String[] items={"📷  Tek fotoğraf çek", "＋  Galeriden fotoğraf ekle", "▣  Seri tarama", "ℹ  Uygulama hakkında"};
        new android.app.AlertDialog.Builder(this).setTitle("EBS • Diğer işlemler").setItems(items,(dialog,which)->{
            if(which==0)takePhoto();else if(which==1)pickImages();else if(which==2)startSeries();else showAbout();
        }).show();
    }
    private void confirmClear(){if(currentWork!=null){note("Önce PDF oluşturma işleminin bitmesini bekleyin.");return;}if(pages.isEmpty()){note("Temizlenecek sayfa bulunamadı.");return;}
        new android.app.AlertDialog.Builder(this).setTitle("Tüm sayfalar silinsin mi?")
            .setMessage(pages.size()+" sayfa kalıcı olarak silinecek. Kaydedilmiş PDF dosyaları etkilenmez.")
            .setNegativeButton("Vazgeç",null).setPositiveButton("Hepsini sil",(d,w)->clearPages()).show();
    }
    private void clearPages(){if(currentWork!=null){note("PDF oluşturma sürerken sayfalar silinemez.");return;}for(String path:pages){try{new File(path).delete();}catch(Exception ignored){}}
        pages.clear();persist();drawPages();note("Tüm taranmış sayfalar temizlendi.");}
    private Bitmap loadPreview(int position) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pages.get(position), bounds);
        int sample = 1;
        while (bounds.outWidth / sample > 2400 || bounds.outHeight / sample > 2400) sample *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(pages.get(position), options);
    }
    private void previewPage(int index) {
        if (index < 0 || index >= pages.size()) return;
        final android.app.Dialog modal = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(0xff090E17);
        ImageView photo = new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        frame.addView(photo, new FrameLayout.LayoutParams(-1, -1));
        final int[] position = {index};
        TextView caption = text("", 16, Color.WHITE, true);
        caption.setGravity(Gravity.CENTER);
        caption.setBackground(background(0xcc15253c, 16));
        TextView back = text("❮", 26, Color.WHITE, true);
        TextView forward = text("❯", 26, Color.WHITE, true);
        back.setGravity(Gravity.CENTER);
        forward.setGravity(Gravity.CENTER);
        back.setBackground(background(0x9915253c, 18));
        forward.setBackground(background(0x9915253c, 18));
        Runnable update = () -> {
            // Release previous bitmap when switching pages.
            android.graphics.drawable.Drawable drawable = photo.getDrawable();
            photo.setImageDrawable(null);
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                Bitmap previous = ((android.graphics.drawable.BitmapDrawable)drawable).getBitmap();
                if (previous != null && !previous.isRecycled()) previous.recycle();
            }
            Bitmap bitmap = loadPreview(position[0]);
            if (bitmap != null) photo.setImageBitmap(bitmap);
            caption.setText("Sayfa " + (position[0] + 1) + " / " + pages.size() + "    •    Sağa / sola kaydır");
            back.setVisibility(position[0] == 0 ? View.INVISIBLE : View.VISIBLE);
            forward.setVisibility(position[0] == pages.size() - 1 ? View.INVISIBLE : View.VISIBLE);
        };
        back.setOnClickListener(v -> { if (position[0] > 0) { position[0]--; update.run(); } });
        forward.setOnClickListener(v -> { if (position[0] < pages.size() - 1) { position[0]++; update.run(); } });
        final float[] down = new float[2];
        photo.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                down[0] = event.getX(); down[1] = event.getY(); return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                float dx = event.getX() - down[0];
                float dy = event.getY() - down[1];
                if (Math.abs(dx) > dp(65) && Math.abs(dx) > Math.abs(dy) * 1.25f) {
                    int next = position[0] + (dx < 0 ? 1 : -1);
                    if (next >= 0 && next < pages.size()) {
                        position[0] = next;
                        photo.animate().alpha(0.45f).setDuration(85).withEndAction(() -> {
                            update.run(); photo.animate().alpha(1f).setDuration(130).start();
                        }).start();
                    }
                }
                return true;
            }
            return true;
        });
        FrameLayout.LayoutParams prevParams = new FrameLayout.LayoutParams(dp(47), dp(70), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        prevParams.leftMargin = dp(9); frame.addView(back, prevParams);
        FrameLayout.LayoutParams nextParams = new FrameLayout.LayoutParams(dp(47), dp(70), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        nextParams.rightMargin = dp(9); frame.addView(forward, nextParams);
        FrameLayout.LayoutParams captionParams = new FrameLayout.LayoutParams(-1, dp(55), Gravity.BOTTOM);
        captionParams.setMargins(dp(12), 0, dp(12), dp(22)); frame.addView(caption, captionParams);
        TextView close = button("✕  KAPAT", navy, modal::dismiss);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(116), dp(49), Gravity.RIGHT | Gravity.TOP);
        closeParams.setMargins(0, dp(24), dp(14), 0); frame.addView(close, closeParams);
        modal.setContentView(frame);
        modal.setOnDismissListener(d -> {
            android.graphics.drawable.Drawable drawable = photo.getDrawable();
            photo.setImageDrawable(null);
            if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                Bitmap bmp = ((android.graphics.drawable.BitmapDrawable)drawable).getBitmap();
                if (bmp != null && !bmp.isRecycled()) bmp.recycle();
            }
        });
        update.run();
        modal.show();
    }
    private void openSocial(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE));
        } catch (Exception e) { note("Bağlantı açılamadı: " + e.getMessage()); }
    }
    private void aboutSocial(LinearLayout holder, String symbol, String name, String description, String url, int color) {
        LinearLayout item = new LinearLayout(this);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12), dp(12), dp(12), dp(12));
        item.setBackground(background(Color.WHITE, 17));
        item.setElevation(dp(2));
        holder.addView(item, lp(-1, 69, 9));
        TextView icon = text(symbol, 21, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(background(color, 14));
        item.addView(icon, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout labels = panel(); labels.setPadding(dp(12), 0, 0, 0);
        item.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        labels.addView(text(name, 15, navy, true));
        labels.addView(text(description, 12, 0xff64748b, false));
        TextView arrow = text("↗", 24, blue, true); item.addView(arrow);
        item.setOnClickListener(v -> openSocial(url));
    }
    private void showAbout() {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(0xffF1F5FB);
        LinearLayout content = panel();
        content.setPadding(dp(17), dp(22), dp(17), dp(32)); scroll.addView(content);
        LinearLayout header = panel(); header.setPadding(dp(22), dp(26), dp(22), dp(24));
        header.setBackground(background(navy, 24)); content.addView(header, lp(-1, -2, 0));
        TextView close = text("✕   Kapat", 14, Color.WHITE, true);
        close.setGravity(Gravity.RIGHT); close.setOnClickListener(v -> dialog.dismiss());
        header.addView(close, lp(-1, 38, 0));
        TextView logo = text("▣   EBS PDF TARAYICI", 24, Color.WHITE, true);
        header.addView(logo, lp(-1, -2, 14));
        TextView version = text("Sürüm 2.0  •  Android belge tarama ve PDF", 13, 0xffB9D4FF, false);
        header.addView(version, lp(-1, -2, 7));
        TextView summary = text("Belgelerinizi kolayca tarayın, sayfaları düzenleyin ve tek bir PDF dosyasında birleştirin.", 15, Color.WHITE, false);
        summary.setLineSpacing(dp(4), 1f); header.addView(summary, lp(-1, -2, 19));
        TextView creatorTitle = text("GELİŞTİRİCİ", 12, 0xff64748B, true);
        content.addView(creatorTitle, lp(-1, 30, 23));
        LinearLayout developer = panel();developer.setPadding(dp(19), dp(18), dp(19), dp(18));
        developer.setBackground(background(Color.WHITE, 19));content.addView(developer);
        developer.addView(text("Ebubekir Bastama", 21, navy, true));
        developer.addView(text("Yazılım geliştirici • EBS PDF Tarayıcı", 14, 0xff64748b, false), lp(-1, -2, 8));
        TextView featuresTitle = text("UYGULAMA ÖZELLİKLERİ", 12, 0xff64748B, true);
        content.addView(featuresTitle, lp(-1, 30, 20));
        LinearLayout features = panel(); features.setPadding(dp(18), dp(16), dp(18), dp(16));
        features.setBackground(background(Color.WHITE, 18)); content.addView(features);
        String[] lines = {"✓  Kesintisiz seri belge çekimi", "✓  Galeriden çoklu fotoğraf ekleme", "✓  Tam ekran ve kaydırmalı sayfa inceleme", "✓  Sayfa sırasını değiştirme ve silme", "✓  Birden çok sayfayı PDF olarak oluşturma", "✓  Oluşturulan PDF dosyasını cihaza kaydetme"};
        for (String line : lines) { TextView t = text(line, 14, 0xff334155, false);t.setPadding(0, dp(7), 0, dp(7));features.addView(t); }
        TextView follow = text("SOSYAL MEDYA VE GELİŞTİRİCİ BAĞLANTILARI", 12, 0xff64748B, true);
        content.addView(follow, lp(-1, 36, 20));
        aboutSocial(content, "𝕏", "X (Twitter)", "@ebubekirstt", "https://x.com/ebubekirstt", 0xff171717);
        aboutSocial(content, "f", "Facebook", "Ebubekir Bastama", "https://www.facebook.com/ebubekirbastama", 0xff1877F2);
        aboutSocial(content, "in", "LinkedIn", "Profesyonel profil", "https://www.linkedin.com/in/ebubekirbastama/", 0xff0A66C2);
        aboutSocial(content, "▶", "YouTube", "Ebubekir Yazılım", "https://www.youtube.com/@ebubekiryazilim", 0xffE52335);
        aboutSocial(content, "⌘", "GitHub", "Açık kaynak projeler", "https://github.com/ebubekirbastama", 0xff24292F);
        TextView foot = text("EBS PDF Tarayıcı  •  Ebubekir Bastama", 12, 0xff64748b, false);
        foot.setGravity(Gravity.CENTER); content.addView(foot, lp(-1, 55, 13));
        dialog.setContentView(scroll); dialog.show();
    }
    private void drawPages(){
        list.removeAllViews();countLabel.setText("SAYFALAR ("+pages.size()+")");
        if(pages.isEmpty()){TextView empty=text("Henüz sayfa eklenmedi. Kamerayla çekin veya galeriden seçin.",14,0xff66748B,false);empty.setPadding(dp(14),dp(20),dp(14),dp(20));empty.setBackground(background(Color.WHITE,14));list.addView(empty,lp(-1,-2,0));return;}
        for(int i=0;i<pages.size();i++) {
            final int pos=i;String path=pages.get(i);
            LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(dp(9),dp(8),dp(8),dp(8));item.setBackground(background(Color.WHITE,16));item.setElevation(dp(1));list.addView(item,lp(-1,88,8));
            ImageView thumb=new ImageView(this);thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            BitmapFactory.Options opt=new BitmapFactory.Options();opt.inSampleSize=8;Bitmap bm=BitmapFactory.decodeFile(path,opt);if(bm!=null)thumb.setImageBitmap(bm);
            item.addView(thumb,new LinearLayout.LayoutParams(dp(62),dp(70)));thumb.setOnClickListener(v->previewPage(pos));
            TextView label=text("  Sayfa "+(i+1),15,navy,true);item.addView(label,new LinearLayout.LayoutParams(0,-1,1));label.setOnClickListener(v->previewPage(pos));
            LinearLayout buttons=panel();item.addView(buttons);
            TextView up=text("▲",20,blue,true);up.setGravity(Gravity.CENTER);up.setOnClickListener(v->move(pos,-1));buttons.addView(up,new LinearLayout.LayoutParams(dp(36),dp(34)));
            TextView down=text("▼",20,blue,true);down.setGravity(Gravity.CENTER);down.setOnClickListener(v->move(pos,1));buttons.addView(down,new LinearLayout.LayoutParams(dp(36),dp(34)));
            TextView del=text("✕",20,0xffD84552,true);del.setGravity(Gravity.CENTER);del.setOnClickListener(v->{String removed=pages.remove(pos);new File(removed).delete();persist();drawPages();});item.addView(del,new LinearLayout.LayoutParams(dp(36),-1));
        }
    }
    private void move(int at,int delta){int to=at+delta;if(to<0||to>=pages.size())return;Collections.swap(pages,at,to);persist();drawPages();}
    private File pageDir(){File f=new File(getFilesDir(),"pages");f.mkdirs();return f;}
    private String nextName(){return "page_"+System.currentTimeMillis()+"_"+UUID.randomUUID().toString().substring(0,7)+".jpg";}
    private void startSeries(){startActivityForResult(new Intent(this,CameraActivity.class),SERIES);}
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
        if(req==SERIES && data!=null){ArrayList<String> captured=data.getStringArrayListExtra("captured");
            if(captured!=null){for(String path:captured)if(new File(path).length()>0&&!pages.contains(path))pages.add(path);
                persist();drawPages();note(captured.size()+" sayfa eklendi. PDF oluşturmaya hazır.");}}
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
        if(info.getState()==WorkInfo.State.SUCCEEDED){currentWork=null;readyPdf=info.getOutputData().getString("pdf");getPreferences(0).edit().putString("pdf",readyPdf).remove("work").apply();note("PDF hazır! Kaydedebilir veya sayfaları topluca silebilirsiniz.");}
        else if(info.getState()==WorkInfo.State.FAILED){currentWork=null;getPreferences(0).edit().remove("work").apply();note("PDF hatası: "+info.getOutputData().getString("error"));}
        else if(info.getState()==WorkInfo.State.RUNNING){int done=info.getProgress().getInt("done",0);int total=info.getProgress().getInt("total",pages.size());note("PDF hazırlanıyor: "+done+"/"+total+" sayfa");}
    });}
    private void savePdf(){if(readyPdf==null||!new File(readyPdf).isFile()){note("Önce PDF oluşturun.");return;}
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");i.putExtra(Intent.EXTRA_TITLE,new File(readyPdf).getName());startActivityForResult(i,SAVE);
    }
    private void note(String s){if(status!=null)status.setText(s);Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
