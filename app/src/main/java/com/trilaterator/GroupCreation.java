package com.trilaterator;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;


public class GroupCreation extends AppCompatActivity implements Serializable {
    FloatingActionButton fab;
    WifiManager wMan;
    HashMap<String, Integer> macrssi;
    List<ScanResult> wifiList;
    LinearLayout sv;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private RadioGroup g1;
    private TextView dname;
    List<String> namelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);
        setTitle("Join Group");
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        GroupCreation.wifiReceiver wifiReciever = new GroupCreation.wifiReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        sv=(LinearLayout) findViewById(R.id.sv);
        if(getIntent().getExtras().getSerializable("macrssi") instanceof HashMap)
            Log.d("hello","Hashmap");
        if(getIntent().getExtras().getSerializable("macrssi") instanceof HashMap)
            Log.d("hello","HashMap");

        macrssi=(HashMap<String, Integer>) getIntent().getExtras().getSerializable("macrssi");
        wifiList=(List<ScanResult>)getIntent().getExtras().getSerializable("wifiList");
        if(macrssi==null||wifiList==null)
            Snackbar.make(findViewById(R.id.R1),"Wifi Scan Import Failed",Snackbar.LENGTH_SHORT).show();
        init();
    }

    private void init() {
        sv.removeAllViews();
        Snackbar.make(findViewById(R.id.R1),"View Refreshed",Snackbar.LENGTH_SHORT).show();
            int size=wifiList.size();
         g1=new RadioGroup(this);
        sv.addView(g1);
        if(g1!=null) {
            for (int i = 0; i < size; i++) {
                RadioButton c1 = new RadioButton(this);
                c1.setText("\n" + wifiList.get(i).SSID + "\n(" + wifiList.get(i).BSSID + ")");
                g1.addView(c1);
            }
        }
        else
            Toast.makeText(this, "G1:NULL", Toast.LENGTH_SHORT).show();
    }
    void invite(View view) throws InterruptedException {
        int radioButtonID = g1.getCheckedRadioButtonId();
        RadioButton radioButton =(RadioButton) g1.findViewById(radioButtonID);
       String info= radioButton.getText().toString();
        String[] x=info.split("\n");
        String SSID=x[1];
       // Toast.makeText(this,SSID, Toast.LENGTH_SHORT).show();
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wMan.addNetwork(conf);
        List<WifiConfiguration> list = wMan.getConfiguredNetworks();
        if(!wMan.getConnectionInfo().getSSID().equals("\""+SSID+"\""))
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                wMan.disconnect();
                wMan.enableNetwork(i.networkId, true);
                wMan.reconnect();
                break;
            }
        }
        final ProgressDialog p = new ProgressDialog(this);
        p.setMessage("Sending Invite ...");
        p.show();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    //check if connected!
                    while (!isConnected(GroupCreation.this)) {
                        //Wait to connect
                        Thread.sleep(1000);
                    }
                    Thread.sleep(1000);
                    p.dismiss();
                    dname = (TextView) findViewById(R.id.dname);
                    String invite = "JOIN-GROUP_" + dname.getText().toString() + "_";
                    UdpClientThread send = new UdpClientThread(invite.getBytes(), "192.168.43.1", 4445);
                    send.start();

                } catch (Exception e) {
                }
            }
        };
        t.start();
        myhandler handler = new myhandler(this);
        Inviteresponse i = new Inviteresponse(4445,handler,1);
        i.start();
    }
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
    void ref(View view){
        wMan.startScan();

    }
    class wifiReceiver extends BroadcastReceiver implements Serializable {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving

            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            wifiList = wMan.getScanResults();
            //List<List<String>> tot = new ArrayList<List<String>>();

            for(int i = 0; i < wifiList.size(); i++){
                List<String> info=new ArrayList<String>();
                listDataHeader.add((i+1)+"."+wifiList.get(i).SSID);
                info.add("MAC:"+wifiList.get(i).BSSID);
                info.add("RSSI:"+wifiList.get(i).level);

                macrssi.put(wifiList.get(i).BSSID,wifiList.get(i).level);
                listDataChild.put(listDataHeader.get(i), info);
            }
            init();
        }
    }
    private static HashMap<String, String> nameip;
    private static HashMap<String, String> ipname;

    public static class myhandler extends Handler implements Serializable {
        private GroupCreation parent;
        private int ct=0;
        private int dsa=0;


        public myhandler(GroupCreation parent) {
            super();
            this.parent=parent;
        }

        @Override
        public void handleMessage(Message msg) {
            DatagramPacket packet= (DatagramPacket) msg.obj;
        int io=0;
                Object m = null;
                try {
                    m = parent.deserialize(packet.getData());
                    if(m instanceof ArrayList)
                        parent.namelist=(ArrayList)m;
                    else if (parent.nameip == null)
                        parent.nameip = (HashMap<String, String>) m;
                    else
                        parent.ipname = (HashMap<String, String>) m;
                    io = 1;
                    if (ipname != null)
                        Log.d("RECEIVED HashMap :", ipname.toString());
                    if(nameip!=null &&ipname!=null)
                    {   Intent in = new Intent(parent, GroupSelect.class);
                    Bundle b = new Bundle();
                    in.putExtra("SEN", false);
                    in.putExtra("Name", parent.dname.getText().toString());
                    b.putSerializable("nameip", parent.nameip);
                    b.putSerializable("ipname", parent.ipname);
                        b.putSerializable("namelist", (Serializable) parent.namelist);
                    in.putExtras(b);
                    parent.startActivity(in);
                }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
        if(io==0){
                String invite=new String(packet.getData());
            String address=packet.getAddress().toString();
            String x[]=invite.split("_");
            switch (msg.what) {
                case 1:
                    if (x[1].equals("ACCEPT")) {
                        Toast.makeText(parent, "Invitation Accepted !", Toast.LENGTH_SHORT).show();
                    dsa++;
                    }
                        else if (x[1].equals("REJECT"))
                        Toast.makeText(parent, "Invitation Rejected !", Toast.LENGTH_SHORT).show();

            }}
    }}
    public static byte[] serialize(Object obj) throws IOException {

        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);

            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }


}