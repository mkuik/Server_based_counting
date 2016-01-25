package dev.kuik.matthijs.serverbasedcounting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class InstructionsFragment extends Fragment implements Global.Adapter, View.OnClickListener {

    ThemeButton sendfeedbackButton;
    LinearLayout instructionsLayout;

    public InstructionsFragment() {
        // Required empty public constructor
    }

    public void setTheme() {
        if (sendfeedbackButton != null) sendfeedbackButton.setColor(Global.getColor1());
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.addListener(this);
        setTheme();
    }

    @Override
    public void onStop() {
        super.onStop();
        Global.removeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructions, container, false);
        if (view != null) {
            view.findViewById(R.id.sendfeedback_button).setOnClickListener(this);
            view.findViewById(R.id.developers_button).setOnClickListener(this);

            instructionsLayout = (LinearLayout) view.findViewById(R.id.instructions_layout);

        }
        return view;
    }

    public void showDevelopers() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.developer_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create();
        builder.show();
    }

    public String getAndroidId() {
        return Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getLocalIpAddress() {
        return ServerAddress.getIPString(getActivity());
    }

    public String getPublicIpAddress() {
        String ip = "";
        try {
            URL url = new URL("https://wtfismyip.com/text");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            ip = in.readLine();
            in.close();
        } catch (IOException e) {
            Log.i("public ip", e.toString());
        }
        return ip;
    }

    public String getResolution() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        String resolution = String.valueOf(displayMetrics.widthPixels) + "x";
        resolution += String.valueOf(displayMetrics.heightPixels);
        return resolution;
    }

    public void sendFeedback() {
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Please wait while we're collecting device info");

        AsyncTask<Void, String, Intent> email = new AsyncTask<Void, String, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                Log.i("public ip", "start");
                String template = "-----------------------------------------------------------\n";
                template += "Datum: " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + "\n";
                template += "Androidversie: " + Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n";
                template += "Specs: \n";
                template += "\t" + getLocalIpAddress() + "\n";
                template += "\t" + getPublicIpAddress() + "\n";
                template += "\t" + getAndroidId() + "\n";
                template += "\t" + getResolution();
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"matthijs.kuik@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback brandveiligheid teller app");
                emailIntent.putExtra(Intent.EXTRA_TEXT, template);
                Log.i("public ip", "end");
                return emailIntent;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                super.onPostExecute(intent);
                progress.dismiss();
                startActivity(intent);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.show();
            }
        };

        email.execute();
    }

    @Override
    public void OnHostAddressChanged(ServerAddress address) {

    }

    @Override
    public void OnHostResponseRecieved(ServerAddress address, String response) {

    }

    @Override
    public void OnHostResponseLost(ServerAddress address, String response) {

    }

    @Override
    public void OnThemeChanged(Bitmap icon, int color1, int color2) {
        setTheme();
    }

    @Override
    public void OnCounterValueChanged(int counter, int max) {

    }

    @Override
    public void OnUserListRecieved(List<User> users) {

    }

    @Override
    public void OnUserChanged(User user) {

    }

    @Override
    public void OnStatusChanged(String status) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendfeedback_button:
                sendFeedback();
                break;
            case R.id.developers_button:
                showDevelopers();
                break;
        }
    }
}
