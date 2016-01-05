package dev.kuik.matthijs.serverbasedcounting;

public class TaskQueueItem implements Runnable {
    public String json;
    public Integer id;
    public ServerCommunicator task;
    private static int id_counter = 0;

    TaskQueueItem(final String json, final ServerCommunicator task) {
        this.json = json;
        this.task = task;
        this.id = id_counter++;
    }

    @Override
    public void run() {
        task.execute(json);
    }
}
