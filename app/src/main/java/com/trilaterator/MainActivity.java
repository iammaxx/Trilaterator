package com.trilaterator;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    WifiManager wMan;
    List<ScanResult> wifiList;
    TextView tv;
    StringBuilder sb;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    HashMap<String,Integer> macrssi ;
    private static int CODE_WRITE_SETTINGS_PERMISSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Available Hosts");
        boolean permission;
        Context context=getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(getApplicationContext());
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            //do your code
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                this.startActivityForResult(intent, MainActivity.CODE_WRITE_SETTINGS_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, MainActivity.CODE_WRITE_SETTINGS_PERMISSION);
            }
        }
        Toast.makeText(this, "Main", Toast.LENGTH_SHORT).show();
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiReceiver wifiReciever = new wifiReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (wMan.isWifiEnabled() == false) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            wMan.setWifiEnabled(true);
        }
        wMan.startScan();
        macrssi=new HashMap<String, Integer>();
        expListView=(ExpandableListView)findViewById(R.id.el1);
    }

    void ref(View view) {
        Snackbar.make(findViewById(R.id.R2), "Wifi Search Initiated", Snackbar.LENGTH_SHORT).show();
        wMan.startScan();
    }

    void addgroup(View view)
    {
        ref(view);

        Bundle b = new Bundle();
        b.putSerializable("macrssi",macrssi);
        b.putSerializable("wifiList", (Serializable) wifiList);

        Intent in = new Intent(this,GroupCreation.class);
        in.putExtras(b);
        startActivity(in);
    }


    class wifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            sb = new StringBuilder();
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            wifiList = wMan.getScanResults();
            sb.append("\n        Number Of Wifi connections :"+wifiList.size()+"\n\n");
            //List<List<String>> tot = new ArrayList<List<String>>();
            //Toast.makeText(context, "Scanned"+wifiList.size(), Toast.LENGTH_SHORT).show();
            Log.d("TAG","Scanned");

            for(int i = 0; i < wifiList.size(); i++){
               String ssi=wifiList.get(i).SSID;
                Boolean b =ssi.equals("HelloMoto");
                Log.d("WiFiList",ssi+b.toString());
                List<String> info=new ArrayList<String>();
                listDataHeader.add((i+1)+"."+wifiList.get(i).SSID);
                info.add("MAC:"+wifiList.get(i).BSSID);
                info.add("RSSI:"+wifiList.get(i).level);
                //Log.d("TXP:",wifiList.get(i).toString());
               // Toast.makeText(context, wifiList.get(i).toString(), Toast.LENGTH_SHORT).show();

                macrssi.put(wifiList.get(i).BSSID,wifiList.get(i).level);
                listDataChild.put(listDataHeader.get(i), info);

            }
            listAdapter = new ExpandableListAdapter(context, listDataHeader, listDataChild);
            expListView.setAdapter(listAdapter);
        }
    }
void host(View view){
    Intent in = new Intent(this,host.class);
    startActivity(in);
}

}
