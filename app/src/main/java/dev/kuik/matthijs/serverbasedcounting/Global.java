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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matthijs Kuik on 25-12-2015.
 */
public class Global {

    public static Integer counter_value = 0;
    public static Integer submit_value = 0;
    public static Integer submit_buffer_value = 0;
    public static Integer counter_max_value = 0;
    public static List<Adapter> listeners = new ArrayList<>();
    public static List<Runnable> queue = new ArrayList<>();
    public static boolean hostConnectionActive = false;
    private static ServerAddress host;
    private static User user;

    public static void getPrefrences(Activity activity) {
        if (activity != null) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            final String jsonstr = preferences.getString("user", "");
            try {
                JSONObject jsonobj = new JSONObject(jsonstr);
                setUser(new User(jsonobj));
            } catch (JSONException e) {
                setUser(new User(activity));
            }
            try {
                setHost(new ServerAddress(new JSONObject(preferences.getString("host", ""))));
                setHostBitmap(preferences.getString("icon", null));
            } catch (JSONException e) {
                setHost(new ServerAddress("", 0, ""));
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
            editor.putInt("color1", getColor1());
            editor.putInt("color2", getColor2());
            try {
                editor.putString("user", user.toJSON().toString());
            } catch (JSONException | NullPointerException e) {
                Log.e("store user in pref", e.toString());
            }
            try {
                editor.putString("host", getHost().toJSON().toString());
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            editor.putString("icon", toBase64(getHost().getIcon()));
            editor.putInt("count", getCounterValue());
            editor.putInt("subtotal", getSubmitValue());
            editor.putInt("max", getCounterMaxValue());
            editor.putInt("buffer", Global.getSubmitBufferValue());
            editor.commit();
        }
    }

    public static Bitmap toBitmap(final String base64) {
        try {
            byte[] bytes;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                bytes = Base64.decode(base64, Base64.DEFAULT);
            } else {
                bytes = Base64api7.decode(base64, Base64api7.DEFAULT);
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException e) {
            Log.e("Global.toBitmap", e.toString());
        }
        return null;
    }

    public static void setHostBitmap(final String base64) {
        if (base64 != null && getHost() != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    getHost().setIcon(toBitmap(base64));
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    notifyTheme();
                }
            }.execute();
        }
    }

    public static String toBase64(final Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64api7.encodeToString(byteArray, Base64.DEFAULT);
        }
        return null;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Global.user = user;
        notifyUser();
    }

    public static void setTheme(Integer color1, Integer color2) {
        getHost().setColor1(color1);
        getHost().setColor2(color2);
    }

    public static Integer getColor1() {
        return getHost().getColor1();
    }

    public static Integer getColor2() {
        return getHost().getColor2();
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
                        notifyResponse(jsonString);
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
                        notifyResponse(jsonString);
                        try {
                            JSONObject jsonResponse = new JSONObject(jsonString);
                            final String color1String = jsonResponse.getString("color1");
                            final String color2String = jsonResponse.getString("color2");
                            if (color1String.compareTo("") != 0 && color2String.compareTo("") != 0) {
                                setTheme(Color.parseColor(color1String), Color.parseColor(color2String));
                            }
                            setHostBitmap(jsonResponse.getString("icon"));
                        } catch (JSONException e) {
                            Log.e("sync theme response", e.toString());
                        }
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
                        notifyResponse(jsonString);
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
                adapter.OnThemeChanged(getHost().getIcon(), getHost().getColor1(), getHost().getColor2());
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

    public static void notifyUser() {
        Log.i("notify", "user " + getUser().toString());
        for (Adapter adapter : listeners) {
            if (adapter != null)
                adapter.OnUserChanged(getUser());
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
        void OnUserChanged(final User user);
    }
}

