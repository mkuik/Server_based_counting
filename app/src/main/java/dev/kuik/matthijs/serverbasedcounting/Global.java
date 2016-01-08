package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    public static List<Runnable> queue = new ArrayList<>();
    public static boolean hostConnectionActive = false;
    public static String clientID = "";
    public static String iconB64;
    public static Bitmap iconBitmap;

    public static void getPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            Global.color1 = preferences.getInt("color1", Color.BLACK);
            Global.color2 = preferences.getInt("color2", Color.WHITE);
            iconB64 = preferences.getString("icon", null);
            final String ip = preferences.getString("ip", "");
            if (ip.compareTo("") != 0) {
                final Integer port = preferences.getInt("port", 0);
                final String hostname = preferences.getString("hostname", "");
                Global.setHost(new ServerAddress(ip, port, hostname));
            }
            Global.counter_value = preferences.getInt("counter", 0);
            Global.submit_value = preferences.getInt("subtotal", 0);
            Global.counter_max_value = preferences.getInt("max", 0);
            Global.setSubmitBufferValue(preferences.getInt("buffer", 0));
        }
    }

    public static void setPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("color1", Global.color1);
            editor.putInt("color2", Global.color2);
            editor.putString("icon", iconB64);
            if (Global.getHost() != null) {
                editor.putString("ip", Global.getHost().ip);
                editor.putInt("port", Global.getHost().port);
                editor.putString("hostname", Global.getHost().name);
            }
            editor.putInt("count", Global.counter_value);
            editor.putInt("subtotal", Global.submit_value);
            editor.putInt("max", Global.counter_max_value);
            editor.putInt("buffer", Global.getSubmitBufferValue());
            editor.commit();
        }
    }


    public static void setTheme(Integer color1, Integer color2) {
        Global.color1 = color1;
        Global.color2 = color2;
    }

    public static boolean setUsername(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1)
                clientID = parts[0];
            return true;
        }
        return false;
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

    public static void syncCounter() {
        Log.i("task in queue", "sync counter");
        if (getHost() == null) return;
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("subtotal", getSubmitBufferValue());
            jsonCommand.put("user", clientID);
            jsonCommand.put("function", "status");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                        return;
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                        return;
                    }
                    notifyResponse("counter " + jsonString);
                    try {
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        setCounterValue(jsonResponse.getInt("count"));
                        setCounterMaxValue(jsonResponse.getInt("max"));
                        notifyCounter();
                    } catch (JSONException e) {
                        Log.e("sync counter response", e.toString());
                    }
                    runFirstInQueue();
                }
            };
            add(new ServerTask(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync counter", e.toString());
        }
    }

    public static void syncTheme() {
        Log.i("task in queue", "sync theme");
        if (getHost() == null) return;
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("primary_color", "");
            jsonCommand.put("secondary_color", "");
            jsonCommand.put("icon", "");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                        return;
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                        return;
                    }
                    notifyResponse("theme " + jsonString);
                    try {
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        final String color1String = jsonResponse.getString("primary_color");
                        final String color2String = jsonResponse.getString("secondary_color");
                        if (color1String.compareTo("") != 0 && color2String.compareTo("") != 0) {
                            setTheme(Color.parseColor(color1String), Color.parseColor(color2String));
                            notifyTheme();
                        }
                        iconB64 = jsonResponse.getString("icon");
                        setBitmapIcon();
                    } catch (JSONException e) {
                        Log.e("sync theme response", e.toString());
                    }
                    runFirstInQueue();
                }
            };
            add(new ServerTask(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync theme", e.toString());
        }
    }

    public static void syncUsers() {
        Log.i("task in queue", "sync users");
        if (getHost() == null) return;
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("user", clientID);
            jsonCommand.put("function", "users");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                        return;
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                        return;
                    }
                    notifyResponse("users " + jsonString);
                    try {
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        final JSONArray users = jsonResponse.getJSONArray("users");
                        final List<User> userArray = new ArrayList<>();
                        for (int i = 0; i != users.length(); ++i) {
                            userArray.add(new User(users.getString(i)));
                        }
                        notifyUsers(userArray);
                    } catch (JSONException e) {
                        Log.e("sync users response", e.toString());
                    }
                    runFirstInQueue();
                }
            };
            add(new ServerTask(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync users", e.toString());
        }
    }

    public static void setBitmapIcon() {
        if (iconB64 != null) {
            AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        byte[] bytes;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                            bytes = Base64.decode(iconB64, Base64.DEFAULT);
                        } else {
                            bytes = Base64api7.decode(iconB64, Base64api7.DEFAULT);
                        }
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e("set header icon", e.toString());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(final Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    iconBitmap = bitmap;
                    Log.i("set bitmap icon", "end");
                    notifyTheme();
                    runFirstInQueue();
                }
            };
            add(new ConvertToBitmapTask(iconB64, task));
        }
    }

    private static void add(final Runnable task) {
        queue.add(task);
        if (!hostConnectionActive) runFirstInQueue();
    }

    private static void runFirstInQueue() {
        try {
            final Runnable task = queue.get(0);
            queue.remove(0);
            if (task != null) {
                hostConnectionActive = true;
                new Thread(task).run();
            }
        } catch (IndexOutOfBoundsException e) {
            hostConnectionActive = false;
            Log.i("runFirstInQueue", "empty");
        }
    }

    public static void notifyTheme() {
        Log.i("notify", "theme");
        for (Adapter adapter : listeners) {
            if (adapter != null) {
                adapter.OnThemeChanged(iconBitmap, getColor1(), getColor2());
            }
        }
    }

    public static void notifyCounter() {
        Log.i("notify", "counter");
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnCounterValueChanged(getCounterValue(), getCounterMaxValue());
        }
    }

    public static void notifyResponse(final String response) {
        Log.i("notify", "response");
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostResponseRecieved(getHost(), response);
        }
    }

    public static void notifyLost(final String error) {
        Log.i("notify", "lost");
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostResponseLost(getHost(), error);
        }
    }

    public static void notifyHost() {
        Log.i("notify", "host");
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnHostAddressChanged(getHost());
        }
    }

    public static void notifyUsers(final List<User> users) {
        Log.i("notify", "users");
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnUserListRecieved(users);
        }
    }

    public interface Adapter {
        void OnHostAddressChanged(ServerAddress address);
        void OnHostResponseRecieved(ServerAddress address, String response);
        void OnHostResponseLost(ServerAddress address, String response);
        void OnThemeChanged(Bitmap icon, int color1, int color2);
        void OnCounterValueChanged(int counter, int max);
        void OnUserListRecieved(List<User> users);
    }
}

