package com.example.akoni.phonedrone.Threads.P2PConnectionThreads;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerThread implements Runnable{
    private InetAddress clientAddress;
    private int port = 0;
    private DatagramSocket socket;
    private byte[] sendData = new byte[64];
    private byte[] receiveData = new byte[64];
    private String controllerPhone = "HOST";
    private String dronePhone;
    private int sendCount = 1;
    private int receiveCount = 0;
    private boolean getPacket = false;

    //called by the host of connection the port will run on
    public ServerThread(int port){
        this.port = port;
    }

    //called when thread is kick off by the activity
    @Override
    public void run() {

        //good to go, infinite loop to blast packets non-stop
        while (true){
            //open the socket if it has not yet been done
            //only runs on the first iteration of the loop when socket is null
            try {
                if (socket == null){
                    socket = new DatagramSocket(port);
                    socket.setSoTimeout(1); //timeout at 1 ms
                }
            }
            catch (IOException e){
                if(e.getMessage() == null){
                    Log.d("Set Socket", "Unknown Message");
                }
                else {
                    Log.d("Set Socket", e.getMessage());
                }
            }

            //Receive here
            //Server can't send until it receives packet from someone to obtain an address

            //create a packet to store the incoming packet
            DatagramPacket receivepacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                socket.receive(receivepacket);
                receivepacket.getData(); //extract data from packet

                dronePhone = new String(receivepacket.getData(), 0, receivepacket.getLength());
                receiveCount++;

                if(clientAddress == null){
                    clientAddress = receivepacket.getAddress();
                }
            }
            catch (IOException e){
                if(e.getMessage() == null){
                    Log.d("Set Socket", "Unknown Message");
                }
                else {
                    Log.d("Set Socket", e.getMessage());
                }

                continue;
            }

            //Ready to send
            try {

                if(clientAddress != null){
                    //data convert to string of bytes
                    sendData = (controllerPhone + sendCount).getBytes();
                    sendCount++;

                    DatagramPacket sendpacket = new DatagramPacket(sendData, sendData.length, clientAddress, port);
                    socket.send(sendpacket);
                    Log.e("MyTag", "Client: Packet Sent");
                }

            }
            catch (IOException e){
                if(e.getMessage() == null){
                    Log.d("Set Socket", "Unknown Message");
                }
                else {
                    Log.d("Set Socket", e.getMessage());
                }
            }//end send
        }
    }

    //used to display message
    public String getClient(){
        return controllerPhone + receiveCount;
    }

    public String getDronePhone(){
        return dronePhone;
    }
}
