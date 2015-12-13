package dev.kuik.matthijs.serverbasedcounting;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class CounterActitity extends ActionBarActivity implements CounterFragment.Adapter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_actitity);
    }

}
