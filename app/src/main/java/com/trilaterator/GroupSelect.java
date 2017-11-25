package com.trilaterator;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.trilaterator.GroupCreation.isConnected;

public class GroupSelect extends AppCompatActivity implements Serializable {

    private static final String TAG = "omg";
    TextView tx,txt1,st;
    private TextView mypath;
    private TextView path;
    static HashMap<String, String> macip;
    HashMap<String, String> hostip;
    HashMap<String, String> iphost;
    String distances[][];
    Bitmap mutableBitmap;
    HashMap<String,WifiConfiguration> wicon;
    Bitmap workingBitmap;
    Paint[] paint = new Paint[5];
    float x,y;
    float a,b;
    double theta;
    TextView degree;
    TextView textView;
    static int cnt=4;
    float ix,iy,fx,fy;
    Canvas canvas;
    TextView textView2;
    ImageView imageView;
    Button bx;
    HashMap<String,String[]>ipdist;
    HashMap<String,constraint>namecons;
    Thread      chcon=new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
            if(isConnected(GroupSelect.this))
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            else{
                //todo RECONNECT WIFI
                //tiosnfsd
                //sdafsadfsda
                while(!wMan.isWifiEnabled()){
                    try {


                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("NETPROB","trying to reconnect with "+hotrack[track_h]);
                WifiConfiguration conf;
                    conf = new WifiConfiguration();
                    conf.SSID = "\"" + namelist.get(track_h) + "\"";
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wMan.addNetwork(conf);
                List<WifiConfiguration> list = wMan.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals("\"" + namelist.get(track_h) + "\"")) {
                        wMan.disconnect();
                        wMan.enableNetwork(i.networkId, true);
                        wMan.reconnect();
                        break;
                    }
                }
               try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                   e.printStackTrace();
                }
                while(!isConnected(getApplicationContext()))
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                UdpClientThread send = new UdpClientThread(("IPUPDATE_"+dName).getBytes(),"192.168.43.1", 4555);
                send.start();
            }}
        }

    });
    private Double temp=0.0;
    private double chng;
    private double deg;
    WifiManager wMan;
    List<ScanResult> wifiList;
    private long RSSI;
    GroupSelect.wifiReceiver Wrec;

    static HashMap<String,constraint> ipcons = new HashMap<>();
    static HashMap<String,String> nameip = new HashMap<>();
    static HashMap<String,String> ipname = new HashMap<>();
    private int u=0;
    private String dName;
    private WifiConfiguration netConfig;
    private String SSID;
    private String[] hotrack;
    int track_h=0;
    Thread chkcon;
    private List<String> namelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select);
        wicon=new HashMap<>();
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        dName=getIntent().getStringExtra("Name");
        nameip=(HashMap<String, String>) getIntent().getExtras().getSerializable("nameip");
        ipname=(HashMap<String, String>) getIntent().getExtras().getSerializable("ipname");
        ipname=(HashMap<String, String>) getIntent().getExtras().getSerializable("ipname");
        Log.d(TAG,ipname.toString());
        namelist=(List<String>) getIntent().getExtras().getSerializable("namelist");

        Log.d("FinalHash:",nameip.toString());
        Log.d("FinalHash:",ipname.toString());
        hotrack= (String[]) namelist.toArray(new String[namelist.size()]);
        macip = (HashMap<String, String>) getIntent().getExtras().getSerializable("macip");
        hostip=(HashMap<String, String>) getIntent().getExtras().getSerializable("hostip");
        iphost =(HashMap<String, String>) getIntent().getExtras().getSerializable("iphost");
        ipdist=new HashMap<String, String[]>();
        namecons=new HashMap<String,constraint>();
        addnetworkconfig();
        x=0;
        y=0;
        for (int i=0;i<5;i++){

            paint[i] = new Paint();
        }

        bx=(Button)findViewById(R.id.start);
        tx=(TextView)findViewById(R.id.tx1);
        txt1=(TextView)findViewById(R.id.txt1);
        st=(TextView)findViewById(R.id.status);
        degree = (TextView) findViewById(R.id.deg);
        if(getIntent().getBooleanExtra("SEN",false))
            u=1;
        mypath=(TextView)findViewById(R.id.mypath);
        path=(TextView)findViewById(R.id.path);



        imageView = (ImageView)findViewById(R.id.imageView);
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        myOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map,myOptions);
        paint[0].setAntiAlias(true);
        paint[0].setColor(Color.BLUE);
        paint[1].setAntiAlias(true);
        paint[1].setColor(Color.RED);
        paint[2].setAntiAlias(true);
        paint[2].setColor(Color.YELLOW);
        paint[3].setAntiAlias(true);
        paint[3].setColor(Color.BLACK);
        paint[4].setAntiAlias(true);
        paint[4].setColor(Color.MAGENTA);

        workingBitmap = Bitmap.createBitmap(bitmap);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                canvas = new Canvas(mutableBitmap);

                if(cnt==4) {
                    // Toast.makeText(getApplicationContext(),"if",Toast.LENGTH_SHORT).show();
                    ix = event.getX();
                    iy = event.getY();
                   // Toast.makeText(GroupSelect.this, "x value"+ix+"y value"+iy, Toast.LENGTH_SHORT).show();
                    x=ix/10;
                    y=iy/10;
                    canvas.drawCircle(ix, iy, 25, paint[0]);
                    cnt--;
                }
                else if(cnt==3)
                {
                    cnt--;
                }
                else if (cnt==2) {
                    //  Toast.makeText(getApplicationContext(),"else",Toast.LENGTH_SHORT).show();
                    fx = event.getX();
                    fy = event.getY();
                   // Toast.makeText(GroupSelect.this, "x value"+fx+"y value"+fy, Toast.LENGTH_SHORT).show();
                    canvas.drawCircle(fx, fy, 25, paint[0]);
                    cnt--;

                    theta=Math.round(Math.toDegrees(Math.atan((fy-iy)/(fx-ix))));
                    if(fx>ix&&fy>iy)
                        theta+=360;
                    else if(fy>iy &&fx<ix)
                        theta+=180;
                    else if(fx<ix && fy<iy)
                        theta=180+theta;
                    bx.setEnabled(true);
                    //deg+=theta;
                    imageView.setOnTouchListener(null);
                }
                imageView.setAdjustViewBounds(true);
                imageView.setImageBitmap(mutableBitmap);

                // mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                //Toast.makeText(MainActivity.this, String.valueOf(cnt), Toast.LENGTH_SHORT).show();

                return true;
            }
        });

    }

    private void addnetworkconfig() {
        //TODO               ADD HOTSPOT INFO
        WifiConfiguration conf;
        Iterator it=nameip.keySet().iterator();
        while(it.hasNext()) {
            String id1 =(String) it.next();
             conf = new WifiConfiguration();
            conf.SSID = "\"" + id1 + "\"";
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wMan.addNetwork(conf);
            wicon.put(id1,conf);
        }
        Log.d("WiConList",wicon.values().toString());
    }

    //TODO         CHANGE TYPE OF NODE
    void chtype(View view) throws InterruptedException {
        track_h++;
        if(u==1){
            u=0;
            wMan.setWifiEnabled(true);
            ProgressDialog p = new ProgressDialog(this);
            p.setMessage("Connection Lost ....Enabling Wifi !");
            p.show();
            while(!wMan.isWifiEnabled()){
                Thread.sleep(300);
            }
            p.dismiss();
            start(findViewById(R.id.addg));

        }
        else{
            u=1;
            try {
                chcon.stop();
                unregisterReceiver(Wrec);
            }catch (Exception e){}
                Thread t = new Thread() {
                @Override
                public void run() {
                    wMan.setWifiEnabled(false);
                    netConfig = new WifiConfiguration();
                   // netConfig.SSID = dName;

                    netConfig.SSID=dName;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                    try {
                        Method setWifiApMethod = wMan.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                        boolean apstatus = (Boolean) setWifiApMethod.invoke(wMan, netConfig, true);

                        Method isWifiApEnabledmethod = wMan.getClass().getMethod("isWifiApEnabled");
                        while (!(Boolean) isWifiApEnabledmethod.invoke(wMan)) {
                        }
                        ;
                        Method getWifiApStateMethod = wMan.getClass().getMethod("getWifiApState");
                        int apstate = (Integer) getWifiApStateMethod.invoke(wMan);
                        Method getWifiApConfigurationMethod = wMan.getClass().getMethod("getWifiApConfiguration");
                        netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wMan);
                        Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
                    } catch (Exception e) {
                        Log.e(this.getClass().toString(), "", e);
                    }
                }
            };
            t.start();

            start(findViewById(R.id.addg));
        }


    }
    getrssi receive;
    Thread t;
    void start(View view) throws InterruptedException {
        bx.setEnabled(false);
        if(receive==null) {
            myhandler hand = new myhandler(this);
            receive = new getrssi(4555, hand, 0);
            receive.start();
        }
        if(u==0)//TODO      CONFIGURE NODE
        {

            Toast.makeText(this, "Node Initialisation", Toast.LENGTH_SHORT).show();
            wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Wrec = new GroupSelect.wifiReceiver();
            registerReceiver(Wrec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            chcon.start();
            ProgressDialog p = new ProgressDialog(this);
                        Thread x;
            x = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            wMan.startScan();
                            Thread.sleep(1500);
                        }
                    } catch (Exception e) {
                    }
                }
            };
            x.start();

        }

                }

    private static constraint c1;
    public class myhandler extends Handler implements Serializable{
        private GroupSelect parent;
        double avg_x,avg_y;

        public myhandler(GroupSelect parent) {
            super();
            this.parent=parent;
        }

        @Override
        public void handleMessage(Message msg) {
                byte[] rcvx=((DatagramPacket)msg.obj).getData();
            //Constructor
            String msg1=new String(rcvx);
            Log.d("Data Received :",msg1);
            if(u==1){
            DatagramPacket packet = (DatagramPacket) msg.obj;
        //    switch(msg.what){
         //       case 0 :
            Toast.makeText(parent,"Data Received from "+packet.getAddress().toString(), Toast.LENGTH_SHORT).show();
            byte[] rcvmsg=packet.getData();
            //Constructor
            c1=new constraint(rcvmsg);
            ipcons.put(packet.getAddress().toString(),c1);

                namecons.put(ipname.get(packet.getAddress().toString()),c1);
            Log.d("TABLE:",namecons.toString());
            if(namecons.size()>=3)
            //String address=packet.getAddress().toString();
            {
                constraint[] c=namecons.values().toArray(new constraint[namecons.values().size()]);
                int len=namecons.size();
                parent.canvas = new Canvas(parent.mutableBitmap);
                int p=0;

                double n=nCr(len,2);
                double[][] arr = new double[(int) n][4];


                for(int i=0;i<len;i++)
                {
                    for(int j=0;j<i;j++)
                    {
                        if(i!=j)
                        {
                            //System.out.println(i + " " + j);
                            getcommon(c[i],c[j],p,arr);
                            p++;
                        }
                    }
                }

                for(int i=0;i<n;i++)
                {
                    for(int j=0;j<4;j++)
                    {
                        System.out.print(arr[i][j] + " ");
                    }
                    System.out.println();
                }



                double d1=distance(arr[0][0],arr[0][1],arr[1][0],arr[1][1]);
                double d2=distance(arr[0][0],arr[0][1],arr[1][2],arr[1][3]);
                double d3=distance(arr[0][2],arr[0][3],arr[1][0],arr[1][1]);
                double d4=distance(arr[0][2],arr[0][3],arr[1][2],arr[1][3]);

                double fin_dist=(Math.min(Math.min(Math.min(d1,d2),d3), d4));

                System.out.println(" fin_dist = " +fin_dist);

                double a = 0,b = 0;

                if(fin_dist==d1 || fin_dist==d2)
                {
                    a=arr[0][0];
                    b=arr[0][1];
                }
                else if(fin_dist==d3 || fin_dist==d4)
                {
                    a=arr[0][2];
                    b=arr[0][3];
                }

                System.out.println("----- final ----");

                double sum_x=0,sum_y=0;
                double radius=0;
                for(int i=0;i<n;i++)
                {
                    double d11=distance(a,b,arr[i][0],arr[i][1]);
                    double d12=distance(a,b,arr[i][2],arr[i][3]);
                    double d13=Math.min(d11, d12);
                    radius=Math.max(d11, d12);
                    if(d13==d11)
                    {
                        System.out.println(arr[i][0] + " " + arr[i][1]);
                        sum_x+=arr[i][0];
                        sum_y+=arr[i][1];
                    }
                    else
                    {	System.out.println(arr[i][2] + " " + arr[i][3]);
                        sum_x+=arr[i][2];
                        sum_y+=arr[i][3];
                    }
                }

                 avg_x=sum_x/n;
                 avg_y=sum_y/n;

                System.out.println(" Final coordinate - ");
                System.out.println(avg_x + " " + avg_y);

                System.out.println(" radius : " +radius);
            Thread    tx = new Thread() {
                    @Override
                    public void run() {
                        Iterator o=nameip.keySet().iterator();
                        while(o.hasNext()){
                        try {

                            Log.d("Monitor:","DataSent:"+(String.valueOf(avg_x)+"_"+String.valueOf(avg_y)));
                            String qw=o.next().toString();
                            Log.d("Receiver IP:",qw);
                            UdpClientThread send = new UdpClientThread((String.valueOf(avg_x)+"_"+String.valueOf(avg_y)).getBytes(),qw, 4555);
                            send.start();
                            //Toast.makeText(context, "Data Sent", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }}
                };
                tx.start();
                Log.d("Monitor:","After Transmission");
                parent.a =(float) avg_x;
                parent.b =(float) avg_y;
                parent.x=(float) avg_x;
                parent.y=(float) avg_y;
                parent.canvas = new Canvas(parent.mutableBitmap);
                parent.canvas.drawCircle(parent.a * 10, parent.b * 10, 15, parent.paint[0]);
                int h=1;
                constraint z;
                Iterator<constraint> d=namecons.values().iterator();
                while(d.hasNext())
                {
                    z=d.next();

                    parent.canvas.drawCircle(new Float(z.xi * 10),new Float( z.yi * 10), 15, parent.paint[h]);
                    h++;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                parent.imageView.setAdjustViewBounds(true);
                parent.imageView.setImageBitmap(parent.mutableBitmap);
                parent.mutableBitmap = parent.workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                try {
                    chtype(findViewById(R.id.addg));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            }
        else
            {
                DatagramPacket packet = (DatagramPacket) msg.obj;
                //    switch(msg.what){
                //       case 0 :



                String invite=new String(packet.getData());
                String x[]=invite.split("_");
                if(x[0].equals("IPUPDATE"))
                {
                ipname.put(packet.getAddress().toString(),x[1]);
                }else{
                Log.d("NODE:","Packet Received from host "+x);
                //String address=packet.getAddress().toString();
                parent.a =(float) Float.parseFloat(x[0]);
                parent.b =(float) Float.parseFloat(x[1]);
                parent.canvas = new Canvas(parent.mutableBitmap);
                parent.canvas.drawCircle(parent.a * 10, parent.b * 10, 15, parent.paint[1]);
                parent.canvas.drawCircle(parent.ix * 10, parent.iy * 10, 15, parent.paint[0]);
                parent.imageView.setAdjustViewBounds(true);
                parent.imageView.setImageBitmap(parent.mutableBitmap);
                parent.mutableBitmap = parent.workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                track_h++;
                wMan.disconnect();
                Toast.makeText(parent, "Next node :"+namelist.get(track_h), Toast.LENGTH_SHORT).show();
                if(track_h==namelist.size())
                    track_h=0;
                if(namelist.get(track_h).equals(dName))
                    try {
                        Toast.makeText(parent, "TypeChanger", Toast.LENGTH_SHORT).show();
                        chtype(findViewById(R.id.deg));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }}
        }
    class wifiReceiver extends BroadcastReceiver implements Serializable {

        @Override
        public void onReceive(final Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
//            Toast.makeText(context, "Scanned", Toast.LENGTH_SHORT).show();
            //Toast.makeText(context, "Scan Complete", Toast.LENGTH_SHORT).show();
            if(u==0){

            String ssi;
                RSSI=0;
               // Toast.makeText(context, "OMG", Toast.LENGTH_SHORT).show();
            wifiList = wMan.getScanResults();
            for (int i = 0; i < wifiList.size(); i++) {
                ssi = wifiList.get(i).SSID;
                if (ssi.equals(namelist.get(track_h)))
                    RSSI = -wifiList.get(i).level;
                //TODO x,y
            }
            Log.d("Broadcast "+"receiver","RSSI OF "+namelist.get(track_h)+"is"+RSSI);
                constraint c=new constraint();
                c.set(x,y,RSSI);
                //Toast.makeText(context, "X:"+x+"\nY:"+y+"\nR:"+RSSI, Toast.LENGTH_SHORT).show();
                byte[] msg=c.convert_str();
                final byte[] finalMsg = msg;
                t = new Thread() {
                    @Override
                    public void run() {
                        try {
                                UdpClientThread send = new UdpClientThread(finalMsg, "192.168.43.1", 4555);
                                send.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();

        }
        }
    }
    class constraint implements Serializable
    {
        byte[] convert_str()
        {
            String str=Double.toString(xi)+"_"+Double.toString(yi)+"_"+Double.toString(rssi);
            return str.getBytes();
        }

        constraint(byte[] b)
        {
            //String s = b.toString();

            String s = new String(b);
            String[] d = s.split("_");
            Log.d("fdsa",s);
            xi=Double.parseDouble(d[0]);
            yi=Double.parseDouble(d[1]);
            rssi=Double.parseDouble(d[2]);
            System.out.println(xi+" "+yi+" "+rssi);


        }
        double xi,yi,r,rssi,p=-14,n=2.67;
        constraint()
        {
            xi=0;
            yi=0;
        }

        void s (double x,double y,double distance)
        {
            xi=x;
            yi=y;
            r=distance;
            rssi=0;
        }
        void set(double x,double y,double rssi)
        {
            xi=x;
            yi=y;
            this.rssi=rssi;
            r=Math.pow(10,((p+rssi) /26.70));
           // System.out.println("r = " +r);
        }
    }
    static double distance(double x1,double y1,double x2,double y2)
    {

        double q=Math.abs(x1-x2);
        double w=Math.abs(y1-y2);
        double dist=Math.sqrt((q*q)+(w*w));
        return dist;
    }

    static double nCr(int n, int r){
        int rfact=1, nfact=1, nrfact=1,temp1 = n-r ,temp2 = r;
        if(r>n-r)
        {
            temp1 =r;
            temp2 =n-r;
        }
        for(int i=1;i<=n;i++)
        {
            if(i<=temp2)
            {
                rfact *= i;
                nrfact *= i;
            }
            else if(i<=temp1)
            {
                nrfact *= i;
            }
            nfact *= i;
        }
        return nfact/(double)(rfact*nrfact);
    }




    static void getcommon(constraint c1,constraint c2,int p,double arr[][])
    {
        double x1,y1,x2,y2,xt,yt;
        double d= distance(c1.xi,c1.yi,c2.xi,c2.yi);


        if(d>c1.r+c2.r)
        {
            //System.out.println("inside if");
            double k1=c1.r;
            double k2=d-c1.r;

            x1=((k1*c2.xi)+(k2*c1.xi))/(k1+k2);
            y1=((k1*c2.yi)+(k2*c1.yi))/(k1+k2);

            double k11=c2.r;
            double k22=d-c2.r;

            x2=((k11*c1.xi)+(k22*c2.xi))/(k11+k22);
            y2=((k11*c1.yi)+(k22*c2.yi))/(k11+k22);

            //System.out.println(" Inner point = " + x1 + " " + y1);
            //	System.out.println(" outer point = " + x2 + " " + y2);

            xt=(x1+x2)/2;
            yt=(y1+y2)/2;

            //System.out.println(" avg point = " + xt + " " + yt);

        }
        else if (d+c2.r < c1.r)
        {
            System.out.println("inside first elseif");
            double k1=c2.r;
            double k2=c2.r+d;
            x1=((k1*c1.xi)-(k2*c2.xi))/(k1-k2);
            y1=((k1*c1.yi)-(k2*c2.yi))/(k1-k2);

            double k11=c2.r+(c1.r-k2);
            double k22=c1.r;

            x2=((k11*c1.xi)-(k22*c2.xi))/(k11-k22);
            y2=((k11*c1.yi)-(k22*c2.yi))/(k11-k22);

            //System.out.println(" Inner point = " + x1 + " " + y1);
            //System.out.println(" outer point = " + x2 + " " + y2);

            xt=(x1+x2)/2;
            yt=(y1+y2)/2;

            //System.out.println(" point = " + xt + " " + yt);
        }
        else if (d+c1.r < c2.r)
        {
            System.out.println("inside 2nd elsif");
            double k1=c1.r;
            double k2=c1.r+d;
            x1=((k1*c2.xi)-(k2*c1.xi))/(k1-k2);
            y1=((k1*c2.yi)-(k2*c1.yi))/(k1-k2);

            double k11=c1.r+(c2.r-k2);
            double k22=c2.r;

            x2=((k11*c2.xi)-(k22*c1.xi))/(k11-k22);
            y2=((k11*c2.yi)-(k22*c1.yi))/(k11-k22);

            //System.out.println(" Inner point = " + x1 + " " + y1);
            //System.out.println(" outer point = " + x2 + " " + y2);

            xt=(x1+x2)/2;
            yt=(y1+y2)/2;

            //System.out.println(" point = " + xt + " " + yt);
        }


        else
        {
            double a = ((c1.r*c1.r)-(c2.r*c2.r)+(d*d))/(2*d);

            double h=Math.sqrt((c1.r*c1.r)-(a*a));

            xt = c1.xi + (a * (c2.xi-c1.xi)) / d;

            yt = c1.yi + (a * (c2.yi-c1.yi)) / d;

            x1=xt+(h*(c2.yi-c1.yi))/d;
            y1=yt-(h*(c2.xi-c1.xi))/d;
            x2=xt-(h*(c2.yi-c1.yi))/d;
            y2=yt+(h*(c2.xi-c1.xi))/d;
            //System.out.println(" first point " + x1 + " " + y1);
            // System.out.println(" second point " + x2 + " " + y2);
        }
        arr[p][0]=x1;
        arr[p][1]=y1;
        arr[p][2]=x2;
        arr[p][3]=y2;
    }
}

