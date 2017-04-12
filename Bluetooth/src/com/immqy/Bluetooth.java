package com.immqy;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import com.immqy.utils.BluetoothUtils;

/** 
 * 蓝牙设备连接 
 * @see BluetoothSocket
 * @author KARL-Dujinyang
 * @author https://github.com/sheep0704
 */
public class Bluetooth {
    public static final int CONNECT_FAILED = 0;
    public static final int CONNECT_SUCCESS = 1;
    public static final int WRITE_FAILED = 2;
    public static final int READ_FAILED = 3;
    public static final int DATA = 4;


    private BluetoothDevice device;
    private Handler handler;
    private static BluetoothSocket socket;

    public Bluetooth(BluetoothDevice device, Handler handler) {
        this.device = device;
        this.handler = handler;
    }
    
    /**
     * BluetoothSocket消息
     * @param msg
     */
    public void connect(String msg) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                BluetoothSocket tmp = null;
                Method method;
                try {
                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } catch (Exception e) {
                    Log.e("CreateSocket", e.toString());
                }
                socket = tmp;
                try {
                    socket.connect();
                } catch (Exception e) {
                    setState(CONNECT_FAILED);
                    Log.e("Connect", e.toString());
                }
                setState(CONNECT_SUCCESS);
            }
        });
        thread.start();
    }
    
    
    /**
     * 连接
     */
    public void connect() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                BluetoothSocket tmp = null;
                Method method;
                try {
                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } catch (Exception e) {
                    Log.e("CreateSocket", e.toString());
                }
                socket = tmp;
                try {
                    socket.connect();
                } catch (Exception e) {
                    setState(CONNECT_FAILED);
                    Log.e("Connect", e.toString());
                }
                setState(CONNECT_SUCCESS);
            }
        });
        thread.start();
    }

    /**
     * 发送数据
     * @param bytes
     */
    public void sendKey(final byte[] bytes) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream out = socket.getOutputStream();
                    for (byte b : bytes) {
                        out.write(b);
                        Thread.sleep(10);
                    }
                    out.flush();
                } catch (Exception e) {
                    setState(WRITE_FAILED);
                    Log.e("Write", e.toString());
                }
                try {
                    while (true) {
                        if (!BluetoothUtils.isReceiver) {
                            read(socket.getInputStream(), 0);
                            break;
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    setState(READ_FAILED);
                    Log.e("Read", e.toString());
                }
            }
        });
        thread.start();
    }

    
    public void comminute(final byte[] bytes, final int type) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream out = socket.getOutputStream();
                    out.write(bytes);
                    Thread.sleep(40);
                    out.flush();
                } catch (Exception e) {
                    setState(WRITE_FAILED);
                    Log.e("Write", e.toString());
                }
                try {
                    read(socket.getInputStream(), type);
                } catch (Exception e) {
                    setState(READ_FAILED);
                    Log.e("Read", e.toString());
                }
            }

        });
        thread.start();
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("Close", e.toString());
        }
    }
    
    /**
     * 读
     * @param in
     * @param type
     * @throws IOException
     */
    private void read(InputStream in, int type) throws IOException {
        int count = 4;
        byte[] bytes = new byte[4];
        int realCount = 0;
        while (realCount < count) {
            realCount += in.read(bytes, realCount, count - realCount);
        }
        String message = BluetoothUtils.bytesToHexString(bytes);
        Message msg = handler.obtainMessage();
        msg.what = DATA;
        msg.obj = message;
        msg.arg1 = type;
        handler.sendMessage(msg);
    }
    
    /**
     * 设置状态
     * @param state
     */
    private void setState(int state) {
        Message msg = handler.obtainMessage();
        msg.what = state;
        handler.sendMessage(msg);
    }
}
