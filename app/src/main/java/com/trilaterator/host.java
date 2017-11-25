package com.trilaterator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class host extends AppCompatActivity implements Serializable {

    private WifiConfiguration netConfig;
    private WifiManager wifiManager;
    private ProgressDialog p1;
    private ToggleButton t1;
    private TextView SSID;
    private WifiManager wMan;
    private LinearLayout LL;
    HashMap<String,String> dev ;
    private ArrayList<Object> listNote;
    private TextView textResult;
    private ListView list;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<String> nodes;
    HashMap<String, String> macip;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    HashMap<String,String> nameip=new HashMap<>();
    private UdpClientThread send1;
    private UdpClientThread send2;
    List<String> namelist=new ArrayList<>();

    private HashMap<String, String> ipname=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        macip=new HashMap<String, String>();
        expListView=(ExpandableListView)findViewById(R.id.el1);
        listNote = new ArrayList<>();
        dev=new HashMap<String, String>();
        textResult=(TextView)findViewById(R.id.res);
        //list=(ListView)findViewById(R.id.list1);
        LL=(LinearLayout)findViewById(R.id.LL1);
        netConfig = new WifiConfiguration();
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        t1=(ToggleButton)findViewById(R.id.toggleButton);
        SSID=(TextView)findViewById(R.id.SSID);
        nodes=new ArrayList<>();

    }
    void turnon(View view) {
        nameip.put(SSID.getText().toString(),"192.168.43.1");
        ipname.put("192.168.43.1",SSID.getText().toString());
        namelist.add(SSID.getText().toString());
        p1=new ProgressDialog(this);
        p1.setMessage("Please Wait ...\nTurning on Personal Hotspot");
        p1.show();
        if(t1.isChecked()) {
            final ProgressDialog p = new ProgressDialog(this);
            p.setMessage("Turning on hotspot ...");
            p.show();
            Thread t = new Thread() {
                @Override
                public void run() {
                    wMan.setWifiEnabled(false);
                    netConfig.SSID = SSID.getText().toString();
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                    try {
                        Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                        boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

                        Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
                        while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
                        }
                        ;
                        Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
                        int apstate = (Integer) getWifiApStateMethod.invoke(wifiManager);
                        Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
                        netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
                        Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
                        p.dismiss();
                    } catch (Exception e) {
                        Log.e(this.getClass().toString(), "", e);
                    }
                }
            };
            t.start();
        }
        else
        {
            wMan.setWifiEnabled(true);
        }
        p1.dismiss();
        myhandler hand=new myhandler(this);
        UdpServerThread receive=new UdpServerThread(4445,hand);
        receive.start();
    }

    public void dis(View view) {
        readAddresses();
        if(listNote.size()==0)
            textResult.setText("No Devices Found");
        textResult.setText("");
        for(int i=0; i<listNote.size(); i++){
            textResult.append(i+1. + "   ");
            textResult.append(listNote.get(i).toString());
            textResult.append("\n");
        }
    }
    private void readAddresses() {
        listNote.clear();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        Node thisNode = new Node(ip, mac);
                        listNote.add(thisNode);
                        macip.put(ip,mac);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Node {
        String ip;
        String mac;

        Node(String ip, String mac){
            this.ip = ip;
            this.mac = mac;
        }

        @Override
        public String toString() {
            return "IP:"+ip + "  MAC:" + mac;
        }
    }
private void deliver(final String Message,  String addr)
{
    final String address=addr.substring(1);
    nodes.add(Message);
    final String finalAddress = address;
    new AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Invite from "+Message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    textResult.setText(textResult.getText().toString()+"\n"+Message);
                    List<String> info=new ArrayList<String>();
                    listDataHeader.add(Message);
                  info.add(finalAddress);
                    info.add(macip.get(finalAddress));
                    nameip.put(Message,address);
                    ipname.put(address,Message);
                    namelist.add(Message);
                    listDataChild.put(listDataHeader.get(listDataHeader.size()-1), info);
                    listAdapter = new ExpandableListAdapter(host.this, listDataHeader, listDataChild);
                    expListView.setAdapter(listAdapter);
                    UdpClientThread send=new UdpClientThread("_ACCEPT_".getBytes(),address,4445);
                    send.start();
                }}
            )
            .setNegativeButton("Reject",new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(host.this, "Rejected", Toast.LENGTH_SHORT).show();
                    Snackbar.make(findViewById(R.id.LL1),"Invite from "+Message+"Rejected !",Snackbar.LENGTH_SHORT).show();
                    UdpClientThread send=new UdpClientThread("_REJECT_".getBytes(),address,4445);
                    send.start();
                }}).show();
    //textResult.setText(Message);

}
    public static class myhandler extends Handler implements Serializable {
        private host parent;

        public myhandler(host parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            parent.readAddresses();
           DatagramPacket packet= (DatagramPacket) msg.obj;
            String invite=new String(packet.getData());
            String dname;
            String address=packet.getAddress().toString();

                  String[] x=invite.split("_");
                if(x[0].equals("JOIN-GROUP")) {
                    dname = x[1];
                    //Toast.makeText(parent, "Invite from "+dname+"\nIP:"+address, Toast.LENGTH_SHORT).show();
                    parent.deliver(dname, address);

                }


    }}
void start(View view) throws InterruptedException {
    UdpClientThread send;
    String[] ips=macip.keySet().toArray(new String[macip.size()]);
    for(int i=0;i<macip.size();i++)
    {
        try {
            send1=new UdpClientThread(serialize(namelist),ips[i],4445);
            send1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            send1=new UdpClientThread(serialize(nameip),ips[i],4445);
            send1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            send2=new UdpClientThread(serialize(ipname),ips[i],4445);
            send2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send=new UdpClientThread("_START_".getBytes(),ips[i],4445);
        send.start();
    }
    Intent in = new Intent(this,GroupSelect.class);
    in.putExtra("SEN",true);
    Bundle b = new Bundle();
    b.putSerializable("macip",macip);
    b.putSerializable("nameip",nameip);
    b.putSerializable("ipname",ipname);
    b.putSerializable("namelist", (Serializable) namelist);
    in.putExtra("Name",SSID.getText().toString());
    in.putExtras(b);
    startActivity(in);
   // Toast.makeText(this, "Localise", Toast.LENGTH_SHORT).show();


}

    public  byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public  Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
}

