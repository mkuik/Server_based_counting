package dev.kuik.matthijs.serverbasedcounting;

import android.os.AsyncTask;
import android.util.Log;

public class ServerTask implements Runnable {
    public String json;
    public Integer id;
    public ServerCommunicator task;
    private static int id_counter = 0;

    ServerTask(final String json, final ServerCommunicator task) {
        this.json = json;
        this.task = task;
        this.id = id_counter++;
    }

    @Override
    public void run() {
        Log.i("task", json);
        task.execute(json);
    }
}
