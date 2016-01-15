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
    public static String iconB64;
    public static Bitmap iconBitmap;
    public static User user;

    public static void getPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            Global.color1 = preferences.getInt("color1", Color.BLACK);
            Global.color2 = preferences.getInt("color2", Color.WHITE);
            iconB64 = preferences.getString("icon", null);
            final String jsonstr = preferences.getString("user", "");
            try {
                JSONObject jsonobj = new JSONObject(jsonstr);
                user = new User(jsonobj);
            } catch (JSONException e) {
                setUser(activity);
            }
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

    public static void setUser(Activity activity) {
        user = new User(getUsername(activity));
    }

    public static void setPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("color1", Global.color1);
            editor.putInt("color2", Global.color2);
            editor.putString("icon", iconB64);
            try {
                editor.putString("user", user.toJSON().toString());
            } catch (JSONException | NullPointerException e) {
                Log.e("store user in pref", e.toString());
            }
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

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Global.user = user;
    }

    public static void setTheme(Integer color1, Integer color2) {
        Global.color1 = color1;
        Global.color2 = color2;
    }

    public static String getUsername(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1) return parts[0];
        }
        return "";
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
            jsonCommand.put("delta", getSubmitBufferValue());
            jsonCommand.put("user", user.toJSON());
            jsonCommand.put("max", 0);
            jsonCommand.put("count", 0);
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                        hostConnectionActive = false;
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                        hostConnectionActive = false;
                    } else {
                        try {
                            JSONObject jsonResponse = new JSONObject(jsonString);
                            setCounterValue(jsonResponse.getInt("count"));
                            setCounterMaxValue(jsonResponse.getInt("max"));
                            notifyCounter();
                        } catch (JSONException e) {
                            Log.e("sync counter response", e.toString());
                        }
                        notifyResponse(jsonString);
                        runFirstInQueue();
                    }

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
            jsonCommand.put("color1", "");
            jsonCommand.put("color2", "");
            jsonCommand.put("icon", "");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                    } else {
                        try {
                            JSONObject jsonResponse = new JSONObject(jsonString);
                            final String color1String = jsonResponse.getString("color1");
                            final String color2String = jsonResponse.getString("color2");
                            if (color1String.compareTo("") != 0 && color2String.compareTo("") != 0) {
                                setTheme(Color.parseColor(color1String), Color.parseColor(color2String));
                                notifyTheme();
                            }
                            iconB64 = jsonResponse.getString("icon");
                            setBitmapIcon();
                        } catch (JSONException e) {
                            Log.e("sync theme response", e.toString());
                        }
                        notifyResponse(jsonString);
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
            jsonCommand.put("user", user.toJSON());
            jsonCommand.put("users", 0);
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                    } else {
                        try {
                            JSONObject jsonResponse = new JSONObject(jsonString);
                            final int nUsers = jsonResponse.getInt("users");
                            final List<User> userArray = new ArrayList<>();
                            for (int i = 0; i != nUsers; ++i) {
                                JSONObject json = jsonResponse.getJSONObject("u#" + i);
                                User user = new User(json.getString("name"), json.getInt("id"));
                                user.setEditorRights(json.getBoolean("edit"));
                                user.setAdminRights(json.getBoolean("admin"));
                                userArray.add(user);
                            }
                            notifyUsers(userArray);
                        } catch (JSONException e) {
                            Log.e("sync users response", e.toString());
                        }
                        notifyResponse(jsonString);

                    }
                    runFirstInQueue();
                }
            };
            add(new ServerTask(jsonCommand.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync users", e.toString());
        }
    }

    public static void syncUserRights(final User chmod) {
        Log.i("task in queue", "sync user rights");
        if (getHost() == null) return;
        final JSONObject jsonCommand = new JSONObject();
        try {
            jsonCommand.put("user", user.toJSON());
            jsonCommand.put("chmod", chmod.toJSON());
            final ServerCommunicator serverCommunicator = new ServerCommunicator(getHost()) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                    } else {
                        notifyResponse(jsonString);
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

    public static void syncServerName(final ServerAddress server, final Runnable post) {
        Log.i("task in queue", "get name " + server.toString());
        if (getHost() == null) return;
        JSONObject json = new JSONObject();
        try {
            json.put("user", user.toJSON());
            json.put("info", "");
            final ServerCommunicator serverCommunicator = new ServerCommunicator(server) {
                @Override
                protected void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);
                    if (jsonString == null) {
                        notifyLost("response is null");
                    } else if (jsonString.compareTo("") == 0) {
                        notifyLost("response is empty");
                    } else {
                        notifyResponse(jsonString);
                        try {
                            JSONObject resp = new JSONObject(jsonString);
                            server.setName(resp.getString("info"));
                            if (post != null) post.run();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    runFirstInQueue();
                }
            };
            add(new ServerTask(json.toString(), serverCommunicator));
        } catch (JSONException e) {
            Log.e("sync users", e.toString());
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
                task.run();
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
        Log.i("notify", "response " + response);
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject jsonUser = jsonResponse.getJSONObject("user");
            user.setID(jsonUser.getInt("id"));
        } catch (JSONException e) {
            Log.i("get user from response", e.toString());
        }
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
        Log.i("notify", "host " + getHost().toString());
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

