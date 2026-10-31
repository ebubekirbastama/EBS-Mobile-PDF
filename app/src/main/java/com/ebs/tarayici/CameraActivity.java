package com.ebs.tarayici;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

/** Full-screen, uninterrupted document capture. Camera stays in this activity until Done. */
public class CameraActivity extends Activity {
    private TextureView preview;
    private CameraDevice device;
    private CameraCaptureSession session;
    private ImageReader reader;
    private HandlerThread thread;
    private Handler handler;
    private CaptureRequest.Builder previewRequest;
    private TextView badge, shutter;
    private String cameraId;
    private int count=0;
    private boolean busy=false, closing=false;
    private final ArrayList<String> created=new ArrayList<>();
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView action(String label,int background){TextView v=new TextView(this);v.setText(label);v.setTextSize(16);v.setTypeface(null,Typeface.BOLD);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);v.setBackground(bg(background,18));return v;}
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FrameLayout base=new FrameLayout(this);base.setBackgroundColor(Color.BLACK);setContentView(base);
        preview=new TextureView(this);base.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(16),dp(16),dp(16),dp(12));top.setBackgroundColor(0x990B1425);
        FrameLayout.LayoutParams topParams=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);base.addView(top,topParams);
        badge=action("0 sayfa",0xff25364F);badge.setPadding(dp(16),dp(12),dp(16),dp(12));top.addView(badge,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView done=action("✓  BİTİR / ÇIK",0xff0E9F91);LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(dp(150),dp(48));dl.leftMargin=dp(12);top.addView(done,dl);done.setOnClickListener(v->finishCapture());
        LinearLayout bottom=new LinearLayout(this);bottom.setOrientation(LinearLayout.VERTICAL);bottom.setGravity(Gravity.CENTER);bottom.setPadding(dp(16),dp(16),dp(16),dp(24));bottom.setBackgroundColor(0xB20B1425);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);base.addView(bottom,bp);
        TextView hint=new TextView(this);hint.setText("Belgeyi kadraja yerleştir · Çekime devam et");hint.setTextColor(Color.WHITE);hint.setTextSize(14);hint.setGravity(Gravity.CENTER);bottom.addView(hint);
        shutter=action("◎   FOTOĞRAF ÇEK",0xff336CF5);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(66));sp.topMargin=dp(14);bottom.addView(shutter,sp);shutter.setOnClickListener(v->shoot());
        preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h){openCamera();}public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h){}public boolean onSurfaceTextureDestroyed(SurfaceTexture t){closeCamera();return true;}public void onSurfaceTextureUpdated(SurfaceTexture t){}});
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.CAMERA},41);
    }
    @Override public void onRequestPermissionsResult(int request,String[] perms,int[] results){super.onRequestPermissionsResult(request,perms,results);if(request==41){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)openCamera();else {Toast.makeText(this,"Kamera izni gerekli",Toast.LENGTH_LONG).show();finishCapture();}}}
    @SuppressWarnings("MissingPermission") private void openCamera(){if(closing||device!=null||!preview.isAvailable()||checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        try{thread=new HandlerThread("EBS-Camera");thread.start();handler=new Handler(thread.getLooper());CameraManager manager=(CameraManager)getSystemService(CAMERA_SERVICE);
            for(String id:manager.getCameraIdList()){CameraCharacteristics c=manager.getCameraCharacteristics(id);Integer facing=c.get(CameraCharacteristics.LENS_FACING);if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_BACK){cameraId=id;break;}}
            if(cameraId==null)cameraId=manager.getCameraIdList()[0];
            android.hardware.camera2.params.StreamConfigurationMap map=manager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size[] sizes=map.getOutputSizes(android.graphics.ImageFormat.JPEG);android.util.Size selected=sizes[0];for(android.util.Size s:sizes){if(s.getWidth()*s.getHeight()>selected.getWidth()*selected.getHeight()&&s.getWidth()*s.getHeight()<=16000000)selected=s;}
            reader=ImageReader.newInstance(selected.getWidth(),selected.getHeight(),android.graphics.ImageFormat.JPEG,2);
            reader.setOnImageAvailableListener(r->{Image image=null;try{image=r.acquireNextImage();if(image==null)return;java.nio.ByteBuffer buffer=image.getPlanes()[0].getBuffer();byte[] bytes=new byte[buffer.remaining()];buffer.get(bytes);File folder=new File(getFilesDir(),"pages");folder.mkdirs();File target=new File(folder,"page_"+System.currentTimeMillis()+"_"+UUID.randomUUID().toString().substring(0,6)+".jpg");try(FileOutputStream out=new FileOutputStream(target)){out.write(bytes);}created.add(target.getAbsolutePath());runOnUiThread(()->{count=created.size();badge.setText("✓ "+count+" sayfa kaydedildi");shutter.setText("◎   SONRAKİ SAYFAYI ÇEK");busy=false;shutter.setEnabled(true);});}catch(Exception e){runOnUiThread(()->{busy=false;shutter.setEnabled(true);Toast.makeText(this,"Kayıt hatası: "+e.getMessage(),Toast.LENGTH_LONG).show();});}finally{if(image!=null)image.close();}},handler);
            manager.openCamera(cameraId,new CameraDevice.StateCallback(){public void onOpened(CameraDevice c){device=c;startPreview();}public void onDisconnected(CameraDevice c){c.close();device=null;}public void onError(CameraDevice c,int error){c.close();device=null;runOnUiThread(()->Toast.makeText(CameraActivity.this,"Kamera hatası: "+error,Toast.LENGTH_LONG).show());}},handler);
        }catch(Exception e){Toast.makeText(this,"Kamera açılamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    private void startPreview(){if(device==null||reader==null||!preview.isAvailable())return;try{SurfaceTexture texture=preview.getSurfaceTexture();texture.setDefaultBufferSize(preview.getWidth(),preview.getHeight());Surface surface=new Surface(texture);previewRequest=device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);previewRequest.addTarget(surface);previewRequest.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);device.createCaptureSession(Arrays.asList(surface,reader.getSurface()),new CameraCaptureSession.StateCallback(){public void onConfigured(CameraCaptureSession s){session=s;try{s.setRepeatingRequest(previewRequest.build(),null,handler);}catch(Exception ignored){}}public void onConfigureFailed(CameraCaptureSession s){runOnUiThread(()->Toast.makeText(CameraActivity.this,"Önizleme başlatılamadı",Toast.LENGTH_LONG).show());}},handler);}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();}}
    private void shoot(){if(busy||session==null||device==null)return;busy=true;shutter.setEnabled(false);shutter.setText("Kaydediliyor…");try{CaptureRequest.Builder request=device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);request.addTarget(reader.getSurface());request.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);request.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);CameraCharacteristics chars=((CameraManager)getSystemService(CAMERA_SERVICE)).getCameraCharacteristics(cameraId);Integer sensor=chars.get(CameraCharacteristics.SENSOR_ORIENTATION);int rotation=getWindowManager().getDefaultDisplay().getRotation();int degrees=new int[]{0,90,180,270}[rotation];int jpegOrientation=((sensor==null?90:sensor)+degrees*(chars.get(CameraCharacteristics.LENS_FACING)==CameraCharacteristics.LENS_FACING_FRONT?1:-1)+360)%360;request.set(CaptureRequest.JPEG_ORIENTATION,jpegOrientation);session.capture(request.build(),null,handler);}catch(Exception e){busy=false;shutter.setEnabled(true);shutter.setText("◎   TEKRAR DENE");Toast.makeText(this,"Çekim hatası: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    private void finishCapture(){if(busy){Toast.makeText(this,"Fotoğraf kaydediliyor, tekrar dokunun.",Toast.LENGTH_SHORT).show();return;}closing=true;android.content.Intent result=new android.content.Intent();result.putStringArrayListExtra("captured",new ArrayList<>(created));setResult(RESULT_OK,result);finish();}
    private void closeCamera(){try{if(session!=null)session.close();}catch(Exception ignored){}session=null;try{if(device!=null)device.close();}catch(Exception ignored){}device=null;try{if(reader!=null)reader.close();}catch(Exception ignored){}reader=null;if(thread!=null){thread.quitSafely();thread=null;handler=null;}}
    @Override protected void onPause(){super.onPause();closeCamera();}
    @Override protected void onResume(){super.onResume();if(preview!=null&&preview.isAvailable())openCamera();}
    @Override public void onBackPressed(){finishCapture();}
}
