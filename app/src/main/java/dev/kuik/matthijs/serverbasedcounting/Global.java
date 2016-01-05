package dev.kuik.matthijs.serverbasedcounting;

import android.graphics.Color;
import android.location.Address;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Matthijs Kuik on 25-12-2015.
 */
public class Global {

    private static ServerAddress host = null;
    public static Integer color1 = Color.BLACK;
    public static Integer color2 = Color.WHITE;
    public static Integer counter_value = 0;
    public static Integer submit_value = 0;
    public static Integer submit_buffer_value = 0;
    public static Integer counter_max_value = 0;
    public static List<Adapter> listeners = new ArrayList<>();
    public static Queue<TaskQueueItem> queue;
    public static boolean hostConnectionActive = false;

    public static void setTheme(Integer color1, Integer color2) {
        Global.color1 = color1;
        Global.color2 = color2;
    }

    public static Integer getColor1() {
        return color1;
    }

    public static Integer getColor2() {
        return color2;
    }

    public static Integer getCounterMaxValue() {
        return counter_max_value;
    }

    public static void setCounterMaxValue(Integer counter_max_value) {
        Global.counter_max_value = counter_max_value;
    }

    public static Integer getCounterValue() {
        return counter_value;
    }

    public static void setCounterValue(Integer counter_value) {
        Global.counter_value = counter_value;
    }

    public static ServerAddress getHost() {
        return host;
    }

    public static void setHost(ServerAddress host) {
        Global.host = host;
    }

    public static Integer getSubmitBufferValue() {
        return submit_buffer_value;
    }

    public static void setSubmitBufferValue(Integer submit_buffer_value) {
        Global.submit_buffer_value = submit_buffer_value;
    }

    public static Integer getSubmitValue() {
        return submit_value;
    }

    public static void setSubmitValue(Integer submit_value) {
        Global.submit_value = submit_value;
    }

    public static void addListener(Adapter adapter) {
        listeners.add(adapter);
    }

    public static void removeListener(Adapter adapter) {
        listeners.remove(adapter);
    }

    public static void syncCounterVariablesWithHost(final String clientID) {
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("subtotal", getSubmitBufferValue());
            jsonCommand.put("client", clientID);
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    try {
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        setCounterValue(jsonResponse.getInt("counter"));
                        setCounterMaxValue(jsonResponse.getInt("max"));
                        notifyCounter();
                    } catch (JSONException e) {
                        Log.e("sync counter response", e.toString());
                    }
                }
            };
            add(new TaskQueueItem(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync counter", e.toString());
        }
    }

    public static void syncTheme() {
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("color1", "");
            jsonCommand.put("color2", "");
            jsonCommand.put("icon", "");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    try {
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        final String color1String = jsonResponse.getString("color1");
                        final String color2String = jsonResponse.getString("color2");
                        final String iconB64 = jsonResponse.getString("icon");
                        setTheme(Color.parseColor(color1String), Color.parseColor(color2String));
                        notifyTheme();
                    } catch (JSONException e) {
                        Log.e("sync theme response", e.toString());
                    }
                }
            };
            add(new TaskQueueItem(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync theme", e.toString());
        }
    }

    public static void syncUsers(final String clientID) {
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("cient", "clientID");
            jsonCommand.put("users", "");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    try {
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        final JSONArray users = jsonResponse.getJSONArray("users");
                        final List<User> userArray = new ArrayList<User>();
                        for (int i = 0; i != users.length(); ++i) {
                            userArray.add(new User(users.getString(i)));
                        }
                        notifyUsers(userArray);
                    } catch (JSONException e) {
                        Log.e("sync users response", e.toString());
                    }
                }
            };
            add(new TaskQueueItem(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync users", e.toString());
        }
    }

    private static void add(final TaskQueueItem task) {
        queue.add(task);
        if (!hostConnectionActive) runNextTaskInQueue();
    }

    private static void runNextTaskInQueue() {
        final TaskQueueItem task = queue.poll();
        if (task != null) {
            task.run();
        }
    }

    public static void setHostConnectionActive(boolean hostConnectionActive) {
        Global.hostConnectionActive = hostConnectionActive;
        if (!hostConnectionActive && !queue.isEmpty()) {
            runNextTaskInQueue();
        }
    }

    public static void notifyTheme() {
        for (Adapter adapter : listeners) {
            if (adapter != null) adapter.OnThemeChanged(getColor1(), getColor2());
        }
    }

    public static void notifyCounter() {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnCounterValueChanged(getCounterValue(), getCounterMaxValue());
        }
    }

    public static void notifyResponse(final String response) {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostResponseRecieved(getHost(), response);
        }
    }

    public static void notifyStatus() {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostStatusChanged(getHost(), hostConnectionActive);
        }
    }

    public static void notifyHost() {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostAddressChanged(getHost());
        }
    }

    public static void notifyUsers(final List<User> users) {
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnRecievedUserList(users);
        }
    }

    public interface Adapter {
        void OnHostAddressChanged(ServerAddress address);
        void OnHostStatusChanged(ServerAddress address, Boolean connected);
        void OnHostResponseRecieved(ServerAddress address, String response);
        void OnThemeChanged(int color1, int color2);
        void OnCounterValueChanged(int counter, int max);
        void OnRecievedUserList(List<User> users);
    }
}

