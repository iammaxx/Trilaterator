package com.trilaterator;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by SIDDHU on 16/10/2017.
 */

public class ConSenderThread extends Thread{
    String dstAddress;
    int dstPort;
    DatagramSocket socket;
    InetAddress address;
    byte[] message;
    public ConSenderThread(byte[] message, String addr, int port) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.message=message;
    }
    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(dstAddress);
            DatagramPacket packet =
                    new DatagramPacket(message,message.length, address, dstPort);
            socket.send(packet);
            Log.d("Invite","Sent");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();

            }
        }

    }
}
