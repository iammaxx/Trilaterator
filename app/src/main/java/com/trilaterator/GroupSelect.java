package com.trilaterator;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.List;

public class GroupSelect extends AppCompatActivity {
    TextView tx,txt1,st;
    private TextView mypath;
    private TextView path;
    static HashMap<String, String> macip;
    HashMap<String, String> hostip;
    HashMap<String, String> iphost;
    String distances[][];
    Bitmap mutableBitmap;
    Bitmap workingBitmap;
    Paint[] paint = new Paint[4];
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
    private Double temp=0.0;
    private double chng;
    private double deg;
    WifiManager wMan;
    List<ScanResult> wifiList;
    private long RSSI;


    static HashMap<String,constraint> ipcons = new HashMap<>();
    private int u=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select);
        macip = (HashMap<String, String>) getIntent().getExtras().getSerializable("macip");
        hostip=(HashMap<String, String>) getIntent().getExtras().getSerializable("hostip");
        iphost =(HashMap<String, String>) getIntent().getExtras().getSerializable("iphost");
        ipdist=new HashMap<String, String[]>();
        x=0;
        y=0;
        for (int i=0;i<4;i++){
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
                    Toast.makeText(GroupSelect.this, "x value"+fx+"y value"+fy, Toast.LENGTH_SHORT).show();
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
    getrssi receive;
    Thread t;
    void start(View view)
    {
        st.setText("**Recording**");
               if(u==1)//TODO                           HOST
        {
                        myhandler hand = new myhandler(this);
                        receive=new getrssi(4555,hand,0);
                        receive.start();
        }
        else//TODO                         NODE
        {
            wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            GroupSelect.wifiReceiver wifiReciever = new GroupSelect.wifiReceiver();
            registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            Thread x;
            x = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            wMan.startScan();
                            Thread.sleep(5000);
                        }
                    } catch (Exception e) {
                    }
                }
            };
            x.start();

        }

    }
    private static constraint c1;
    public class myhandler extends Handler {
        private GroupSelect parent;
        double avg_x,avg_y;

        public myhandler(GroupSelect parent) {
            super();
            this.parent=parent;
        }

        @Override
        public void handleMessage(Message msg) {
            if(u==1){
            DatagramPacket packet = (DatagramPacket) msg.obj;
        //    switch(msg.what){
         //       case 0 :
            Toast.makeText(parent,"Data Received from "+packet.getAddress().toString(), Toast.LENGTH_SHORT).show();
            byte[] rcvmsg=packet.getData();
            //Constructor
            c1=new constraint(rcvmsg);
            ipcons.put(packet.getAddress().toString(),c1);
            Log.d("TABLE:",ipcons.toString());
            if(ipcons.size()>=3)
            //String address=packet.getAddress().toString();
            {
                constraint[] c=ipcons.values().toArray(new constraint[ipcons.values().size()]);
                int len=ipcons.size();
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
                t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            UdpClientThread send = new UdpClientThread((String.valueOf(avg_x)+"_"+String.valueOf(avg_y)).getBytes(), "192.168.43.1", 4555);
                            send.start();
                            //Toast.makeText(context, "Data Sent", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                        }
                    }
                };
                t.start();
                parent.a =(float) avg_x;
                parent.b =(float) avg_y;
                parent.canvas.drawCircle(parent.a * 10, parent.b * 10, 15, parent.paint[0]);
                parent.imageView.setAdjustViewBounds(true);
                parent.imageView.setImageBitmap(parent.mutableBitmap);
                parent.mutableBitmap = parent.workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            }
        else
            {
                DatagramPacket packet = (DatagramPacket) msg.obj;
                //    switch(msg.what){
                //       case 0 :
                String invite=new String(packet.getData());
                //String address=packet.getAddress().toString();
                String x[]=invite.split("_");


                parent.a =(float) Float.parseFloat(x[0]);
                parent.b =(float) Float.parseFloat(x[1]);
                parent.canvas.drawCircle(parent.a * 10, parent.b * 10, 15, parent.paint[0]);
                parent.canvas.drawCircle(parent.ix * 10, parent.iy * 10, 15, parent.paint[1]);
                parent.imageView.setAdjustViewBounds(true);
                parent.imageView.setImageBitmap(parent.mutableBitmap);
                parent.mutableBitmap = parent.workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        }
        }
    class wifiReceiver extends BroadcastReceiver {

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
              ssi=  wifiList.get(i).SSID;
                if (ssi.equals("HelloMoto"))
                    RSSI = -wifiList.get(i).level;
                //TODO x,y
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
                            //Toast.makeText(context, "Data Sent", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                        }
                    }
                };
                t.start();
            }
        }
        else
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
        }
    }
    class constraint
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

