package ru.scorpio92.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by scorpio92 on 14.08.16.
 */

//https://developer.android.com/training/displaying-bitmaps/load-bitmap.html#decodeSampledBitmapFromResource
public class ImageViewScrLoader {

    private Context context;
    private ImageView imageView;
    private int drawable;

    public ImageViewScrLoader(Context context, ImageView imageView, int drawable) {
        this.context = context;
        this.imageView = imageView;
        this.drawable = drawable;
    }

    public void loadScr() {
        try {
            new LoadTask().execute(drawable);
        } catch (Exception e) {
            Log.e("ImageViewScrLoader", "failed start task", e);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        //Log.w("calculateInSampleSize", Integer.toString(height) + " " + Integer.toString(width));
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //Log.w("calculateInSampleSize half", Integer.toString(halfHeight) + " " + Integer.toString(halfWidth));

            //Log.w("calculateInSampleSize req", Integer.toString(reqHeight) + " " + Integer.toString(reqWidth));

            //если максимально возможный размер (ширина или высота) больше половины реального размера, но меньше реального - устанавливаем параметр inSampleSize=2
            if((halfHeight / inSampleSize) < reqHeight || (halfWidth / inSampleSize) < reqWidth) {
                return 2;
            } else { //иначе - уменьшаем в 2 раза пока не выполнится равенство
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight || (halfWidth / inSampleSize) >= reqWidth) {
                    //Log.w("calculateInSampleSize", "inSampleSize*2");
                    inSampleSize *= 2;
                }
            }
        }

        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapFromResource(int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.w("calculateInSampleSize options.inSampleSize", Integer.toString(options.inSampleSize));

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }


    private BitmapFactory.Options getRealImageSize (int resID) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resID, options);
        return options;
    }

    private Bitmap createRectangleBitmap(int width, int height) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    }

    private void drawEmptyImage(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);
    }


    class LoadTask extends AsyncTask<Integer, Void, Bitmap> {

        private Bitmap tmp_bmp; //врмененный битмап в котором хранится прямоугольник, необходимый для определения реальных размеров ImageView при параметре adjustViewBounds=true
        //реальные размеры при параметре adjustViewBounds=true полученные в ViewTreeObserver
        private int adjustViewBoundsHeight=0;
        private int adjustViewBoundsWidth=0;


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            drawEmptyImage(tmp_bmp);
            imageView.setImageBitmap(tmp_bmp);
            //ждем и получаем реальные размеры картинки
            ViewTreeObserver vto = imageView.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    adjustViewBoundsHeight = imageView.getMeasuredHeight();
                    adjustViewBoundsWidth = imageView.getMeasuredWidth();
                    //Log.w("onCreateView imageView. ViewTreeObserver", Integer.toString(adjustViewBoundsHeight) + " " + Integer.toString(adjustViewBoundsWidth));
                    return true;
                }
            });
        }

        @Override
        protected Bitmap doInBackground(Integer... drawable) {
            try {
                //получаем реальные размеры картинки
                BitmapFactory.Options options = getRealImageSize(drawable[0]);
                //Log.w("doInBackground orig h/w", Integer.toString(height) + " " + Integer.toString(width));
                //создаем по полученным размерам прямоугольник с черным фоном
                tmp_bmp = createRectangleBitmap(options.outWidth, options.outHeight);
                //рисуем врменную картинку - прямоугольник
                publishProgress();
                //ждем пока ViewTreeObserver получит реальные размеры картинки
                while (true) {
                    //Log.w("doInBackground", "wait ViewTreeObserver");
                    if(adjustViewBoundsWidth!=0 && adjustViewBoundsHeight!=0) {
                        break;
                    }
                }
                //рисуем переданную картинку, в кач-ве максимальных размеров передаем вычисленные
                //также на основе adjustViewBoundsWidth и adjustViewBoundsHeight определяем параметр inSampleSize - кач-во картинки
                return decodeSampledBitmapFromResource(drawable[0], adjustViewBoundsWidth, adjustViewBoundsHeight);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            try {
                imageView.setImageBitmap(result);
                //Log.w("imageView.getLayoutParams", Integer.toString(imageView.getMeasuredHeight()) + " " + Integer.toString(imageView.getMeasuredWidth()));
            } catch (Exception e) {
                Log.e("ImageViewScrLoader", "failed onPostExecute", e);
            }
        }
    }
}
