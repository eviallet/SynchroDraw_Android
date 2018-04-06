package com.gueg.synchrodraw;

import android.graphics.Point;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Socket;

@SuppressWarnings("WeakerAccess")
public class ConnexionHandler extends Thread {
    public static final String SEP_PACKETS = "<PCK>";
    public static final String SEP_MOVED = "&";
    public static final String SEP_CLICKED = "#";
    public static final String SEP_RCLICKED = "-";
    public static final String SEP_RELEASED = "=";
    public static final String SEP_XY = "+";
    public static final String SEP_PIC = "<PIC>";
    public static final String SEP_ENDPIC = "</PIC>";
    public static final String SEP_REFRESH = "~";
    public static final String SEP_UNDO= "<CTZ>";
    public static final String SEP_REDO = "<CTY>";
    public static final String SEP_ZM = "<Z->";
    public static final String SEP_ZP = "<Z+>";
    public static final String SEP_TOOLS = "<TLS>";
    public static f
    inal String SEP_LAYERS = "<LAY>";

    MainActivity activity;
    boolean isConnected;
    Socket socket;
    InputStream in;
    OutputStream out;
    StringBuilder buffer = new StringBuilder();
    boolean ipSet = false;
    String ip;
    int port;

    public ConnexionHandler(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        while(!interrupted()) {
            if (!isConnected && ipSet) {
                try {
                    socket = new Socket(Inet4Address.getByName(ip), port);
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    isConnected = true;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.connected();
                        }
                    });
                } catch (IOException e) {
                    activity.error();
                    e.printStackTrace();
                }
                ipSet = false;
            } else if (isConnected) {
                try {
                    int byteCount = in.available();
                    if (byteCount > 0) {
                        boolean valid = false;
                        byte[] raw = new byte[byteCount];
                        in.read(raw);
                        final String string = new String(raw, "ASCII");
                        String cmd = "";
                        if (!isComplete(string)) {
                            buffer.append(string);
                            if (isComplete(buffer.toString())) {
                                cmd = buffer.toString();
                                valid = true;
                                buffer.setLength(0);
                            }
                        } else {
                            cmd = string;
                            valid = true;
                        }
                        if (valid) {
                            cmd = cmd.replace(SEP_PACKETS,"");
                            cmd = cmd.replace(SEP_PIC,"");
                            cmd = cmd.replace(SEP_ENDPIC,"");
                            final String img = cmd;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.setScreenImage(img);
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void connect(String ip, int port) {
        this.ip = ip;
        this.port = port;
        ipSet = true;
    }


    private boolean isComplete(String str) {
        return str.contains(SEP_PACKETS)&&str.contains(SEP_PIC)&&str.contains(SEP_ENDPIC);
    }


    public void sendMove(int x, int y) {
        if(!isConnected)
            return;
        try {
            Log.d(":-:","Sending move");
            out.write((SEP_PACKETS + SEP_MOVED + SEP_XY + x + SEP_XY + y).getBytes());
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                isConnected = false;
            }
        }
    }

    public void sendClic(int x, int y) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_PACKETS + SEP_CLICKED + SEP_XY + x + SEP_XY + y).getBytes());
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                isConnected = false;
            }
        }
    }

    public void sendRClic(int x, int y) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_PACKETS + SEP_RCLICKED + SEP_XY + x + SEP_XY + y).getBytes());
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                isConnected = false;
            }
        }
    }

    public void sendRelease(int x, int y) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_PACKETS + SEP_RELEASED + SEP_XY + x + SEP_XY + y).getBytes());
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                isConnected = false;
            }
        }
    }
    public void sendZoomIn(Point points[]) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_PACKETS + SEP_ZP + points[0].x + SEP_ZP + points[0].y).getBytes());
            //out.write((SEP_PACKETS + SEP_ZP + points[0].x + SEP_ZP + points[0].y + SEP_ZP + points[1].x + SEP_ZP + points[1].y).getBytes());
            //Log.d(":-:","Wrote : " +SEP_PACKETS + SEP_ZP + points[0].x + SEP_ZP + points[0].y + SEP_ZP + points[1].x + SEP_ZP + points[1].y);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                isConnected = false;
            }
        }
    }

    public void sendAction(String sep) {
        if(!isConnected)
            return;
        try {
            out.write((SEP_PACKETS + sep).getBytes());
            Log.d(":-:","Wrote : "+SEP_PACKETS+sep);
        } catch(IOException e) {
            e.printStackTrace();
            if(e.toString().contains("Broken pipe")) {
                isConnected = false;
            }
        }
    }
}
