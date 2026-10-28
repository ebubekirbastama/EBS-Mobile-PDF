package com.ebs.tarayici;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.exifinterface.media.ExifInterface;
import android.graphics.Matrix;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.io.FileOutputStream;

public class PdfWorker extends Worker {
    public PdfWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context,params); }
    @NonNull @Override public Result doWork() {
        String[] paths = getInputData().getStringArray("paths");
        String fileName = getInputData().getString("output");
        if (paths == null || paths.length == 0 || fileName == null) return Result.failure();
        File directory = new File(getApplicationContext().getFilesDir(), "exports");
        if (!directory.exists() && !directory.mkdirs()) return Result.failure();
        File target = new File(directory, fileName);
        File partial = new File(directory, fileName + ".partial");
        PdfDocument pdf = new PdfDocument();
        try {
            for (int i=0; i<paths.length; i++) {
                if (isStopped()) throw new Exception("cancelled");
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds=true;
                BitmapFactory.decodeFile(paths[i],bounds);
                if (bounds.outWidth<=0 || bounds.outHeight<=0) throw new Exception("Unreadable page " + (i+1));
                int scale=1;
                while (bounds.outWidth/scale>2000 || bounds.outHeight/scale>2600) scale*=2;
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inSampleSize=scale;
                Bitmap bmp=BitmapFactory.decodeFile(paths[i],options);
                if(bmp==null) throw new Exception("Image decode failed");
                ExifInterface exif = new ExifInterface(paths[i]);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotation = orientation==ExifInterface.ORIENTATION_ROTATE_90?90:orientation==ExifInterface.ORIENTATION_ROTATE_180?180:orientation==ExifInterface.ORIENTATION_ROTATE_270?270:0;
                if(rotation!=0) {Matrix m=new Matrix();m.postRotate(rotation);Bitmap adjusted=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),m,true);bmp.recycle();bmp=adjusted;}
                try {
                    int pw=595, ph=842, margin=20;
                    PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(pw,ph,i+1).create());
                    Canvas canvas=page.getCanvas();canvas.drawColor(Color.WHITE);
                    float factor=Math.min((pw-2f*margin)/bmp.getWidth(),(ph-2f*margin)/bmp.getHeight());
                    float w=bmp.getWidth()*factor,h=bmp.getHeight()*factor;
                    android.graphics.RectF dest=new android.graphics.RectF((pw-w)/2,(ph-h)/2,(pw+w)/2,(ph+h)/2);
                    Paint p=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG);
                    canvas.drawBitmap(bmp,null,dest,p);
                    pdf.finishPage(page);
                } finally { bmp.recycle(); }
                setProgressAsync(new Data.Builder().putInt("done",i+1).putInt("total",paths.length).build());
            }
            try(FileOutputStream out=new FileOutputStream(partial)) { pdf.writeTo(out);out.getFD().sync(); }
            pdf.close();
            if(target.exists() && !target.delete()) throw new Exception("Cannot replace previous PDF");
            if(!partial.renameTo(target)) throw new Exception("Cannot commit PDF");
            return Result.success(new Data.Builder().putString("pdf",target.getAbsolutePath()).build());
        } catch(Exception e) {
            partial.delete();
            return Result.failure(new Data.Builder().putString("error", e.getMessage()==null?"PDF failure":e.getMessage()).build());
        } finally {try{pdf.close();}catch(Exception ignored) {}}
    }
}
