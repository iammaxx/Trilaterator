package com.trilaterator;

import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static android.content.ContentValues.TAG;


public class startresponse extends Thread {

    int serverPort;
    DatagramSocket socket;
    GroupCreation.myhandler handler;
    boolean running;
    public startresponse(int serverPort, GroupCreation.myhandler handler) {
        super();
        this.serverPort = serverPort;
        this.handler=handler;
    }
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        running = true;

        try {
            Log.e(TAG, "UDP Server is running");

            while(running){
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);     //this code block the program flow
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String[] ob=new String[10];
                ob[0]=   new String(packet.getData());
                Log.e(TAG,"Packet Received:"+ob[0]);
                handler.sendMessage(Message.obtain(handler,1,packet));
            }
            Log.e(TAG, "UDP Server ended");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                Log.e(TAG, "socket.close()");
            }
        }
    }
}