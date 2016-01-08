package dev.kuik.matthijs.serverbasedcounting;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

public class ConvertToBitmapTask implements Runnable {
    public String iconBase64;
    public Integer id;
    public AsyncTask<Void, Void, Bitmap> task;
    private static int id_counter = 0;

    ConvertToBitmapTask(final String iconBase64, final AsyncTask<Void, Void, Bitmap> task) {
        this.iconBase64 = iconBase64;
        this.task = task;
        this.id = id_counter++;
    }

    @Override
    public void run() {
        Log.i("task", iconBase64);
        task.execute();
    }
}
