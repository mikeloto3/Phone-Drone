package com.example.akoni.phonedrone.Threads.P2PConnectionThreads;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientThread implements Runnable{

    private InetAddress hostAddress;
    private int port = 0;
    private DatagramSocket socket;
    private byte[] sendData = new byte[64];
    private byte[] receiveData = new byte[64];
    private String dronePhone = "CLIENT";
    private String controllerPhone;
    private int sendCount = 1;
    private int receiveCount = 0;

    //Activity passes the address and port for the use of thread
    public ClientThread(InetAddress address, int port){
        this.hostAddress = address;
        this.port = port;
    }

    //called when thread is kick off by the activity
    @Override
    public void run() {

        //confirms host address and port are established
        if(hostAddress != null && port != 0){

            //good to go, infinite loop to blast packets non-stop
            while (true){
                //creates new socket using the given port number
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

                //Ready to send
                //attempt sending of the packet containg the message "CLIENT"
                try {
                    //data convert to string of bytes
                    sendData = (dronePhone + sendCount).getBytes();
                    sendCount++;

                    DatagramPacket sendpacket = new DatagramPacket(sendData, sendData.length, hostAddress, port);
                    socket.send(sendpacket);
                    Log.e("MyTag", "Client: Packet Sent");

                }
                catch (IOException e){
                    if(e.getMessage() == null){
                        Log.d("Set Socket", "Unknown Message");
                    }
                    else {
                        Log.d("Set Socket", e.getMessage());
                    }
                }//end send

                //Receive
                try {
                    //create a packet to store the incoming packet
                    DatagramPacket receivepacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivepacket);

                    receivepacket.getData(); //extract data from packet

                    controllerPhone = new String(receivepacket.getData(), 0, receivepacket.getLength());
                    receiveCount++;
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
            }
        }
    }

    //used to display message
    public String getDronePhone(){
        return dronePhone + receiveCount;
    }

    public String getControllerPhone(){
        return controllerPhone;
    }
}
