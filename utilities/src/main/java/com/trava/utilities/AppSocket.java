package com.trava.utilities;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.trava.utilities.chatModel.ChatMessageListing;
import com.trava.utilities.constants.ConstantsKt;
import com.trava.utilities.webservices.BaseRestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;



/**
 * Created by Rohit Sharma on 9/8/17.
 */
public class AppSocket {

    private static Socket mSocket = null;
    private List<OnMessageReceiver> onMessageReceiverList = new ArrayList<>();
    private List<ConnectionListener> onConnectionListeners = new ArrayList<>();

    private static final AppSocket ourInstance = new AppSocket();

    private final String TAG = "AppSocket";

    public static AppSocket get() {
        return ourInstance;
    }

    private AppSocket() {
    }

    /**
     * Initialize socket
     *
     * @return true if initialized successfully, otherwise false
     */
    public boolean init() {
        try {
            if (!ConstantsKt.getACCESS_TOKEN().isEmpty()) {
                if (mSocket != null) {
                    mSocket.off();
                    mSocket.disconnect();
                    mSocket = null;
                }
                IO.Options options = new IO.Options();
                options.reconnection = true;
                options.query = "access_token=" + ConstantsKt.getACCESS_TOKEN()+"&secretdbkey="+Constants.INSTANCE.getSECRET_DB_KEY();
                options.multiplex = false;
                mSocket = IO.socket(Constants.INSTANCE.getSOCKET_URL(), options);
                connect();
                mSocket.on(Socket.EVENT_CONNECT, onConnect);
                mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, onError);
                mSocket.on(Socket.EVENT_ERROR, onError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeOut);
                mSocket.on(Socket.EVENT_RECONNECTING, onReconnecting);
                mSocket.on(Socket.EVENT_RECONNECT_ERROR, onReconnectError);
                mSocket.on(Socket.EVENT_RECONNECT_FAILED, onReconnectError);
                return true;
            } else {
                return false;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Logger.INSTANCE.e(TAG, "onConnect called:" + mSocket.id());
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Logger.INSTANCE.e(TAG, "onDisconnect called");
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Logger.INSTANCE.e(TAG, "onError called");
        }
    };

    private Emitter.Listener onTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Logger.INSTANCE.e(TAG, "onTimeOut called");
        }
    };
    private Emitter.Listener onReconnecting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Logger.INSTANCE.e(TAG, "onReconnecting called");
        }
    };
    private Emitter.Listener onReconnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Logger.INSTANCE.e(TAG, "onReconnectError called");
        }
    };

    /**
     * Check whether socket is connected or not
     */
    public boolean isConnected() {


        return mSocket.connected();
    }

    /**
     * Gets the current instance of the socket
     */
    public Socket getSocket() {
        return mSocket;
    }

    public Socket setSocket(Socket socket) {
        mSocket = socket;
        return mSocket;
    }

    /**
     * Connects the socket
     */
    public void connect() {
        if (mSocket != null && !mSocket.connected()) {
            mSocket.connect();
        }
    }

    /**
     * Disconnects the socket.
     */
    public void disconnect() {
        if (mSocket != null)
            mSocket.disconnect();
    }

    /**
     * Emits an event. When you pass {@link Ack} at the last argument, then the acknowledge is done.
     *
     * @param event an event name.
     * @param args  data to send.
     */
    public void emit(final String event, final Object... args) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit(event, args);
        }
    }

    /**
     * Emits an event with an acknowledge.
     *
     * @param event an event name
     * @param args  data to send.
     * @param ack   the acknowledgement to be called
     */
    public void emit(final String event, final Object[] args, Ack ack) {
        if (mSocket != null)
            mSocket.emit(event, args, ack);
    }

    /**
     * Listens on the event.
     *
     * @param event event name.
     * @param fn    listener for the event
     */
    public void on(String event, Emitter.Listener fn) {
        if (mSocket != null)
            mSocket.on(event, fn);
    }

    /**
     * Removes all registered listeners.
     */
    public void off() {
        if (mSocket != null)
            mSocket.off();
    }

    /**
     * Removes all listeners of the specified event.
     *
     * @param event an event name.
     */
    public void off(String event) {
        if (mSocket != null)
            mSocket.off(event);
    }

    /**
     * Removes the listener.
     *
     * @param event an event name.
     * @param fn    listener for {@link String event}
     */
    public void off(String event, Emitter.Listener fn) {
        if (mSocket != null)
            mSocket.off(event, fn);
    }

    public interface OnMessageReceiver {
        void onMessageReceive(ChatMessageListing message);
    }

    public interface ConnectionListener {
        void onConnectionStatusChanged(String status);
    }

    public void addConnectionListener(ConnectionListener listener) {
        onConnectionListeners.add(listener);
    }

    public void addOnMessageReceiver(OnMessageReceiver receiver) {
        if (onMessageReceiverList.isEmpty()) {
            onReceiveMessageEvent();
        }
        onMessageReceiverList.add(receiver);
    }


    private void onReceiveMessageEvent() {
        if (mSocket != null) {
            mSocket.on(Events.INSTANCE.getRECEIVE_MESSAGE(), new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    ChatMessageListing chat = new Gson().fromJson(args[0].toString(), ChatMessageListing.class);
                    notifyMessageReceivers(chat);
                    sendMessageDelivery(chat);
                }
            });
        }

    }

    private void notifyMessageReceivers(final ChatMessageListing message) {
        for (final OnMessageReceiver receiver : onMessageReceiverList) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    receiver.onMessageReceive(message);
                }
            });

        }
    }

    public void sendMessage(ChatMessageListing message, final OnMessageReceiver msgAck) {
        JSONObject jsonObject = null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("to", message.getSend_to());
            obj.put("text", message.getText());
            obj.put("original", message.getOriginal_image());
            obj.put("thumbnail", message.getThumbnail_image());
            obj.put("sent_at", message.getSent_at());
            obj.put("chat_type", message.getChat_type());
            obj.put("user_detail_id", message.getUser_detail_id());
            obj.put("order_id",message.getOrder_id());

            jsonObject = new JSONObject();
            jsonObject.put("detail", obj);
            System.out.println("sentMessage -> " + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit(Events.INSTANCE.getSEND_MESSAGE(), jsonObject, new Ack() {
            @Override
            public void call(final Object... args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        msgAck.onMessageReceive(new Gson().fromJson(args[0].toString(), ChatMessageListing.class));
                    }
                });
            }
        });
    }



    public void sendMessageDelivery(ChatMessageListing message) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new Gson().toJson(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Events.INSTANCE.getSEND_MESSAGE(), jsonObject);
    }

    public void removeOnMessageReceiver(OnMessageReceiver receiver) {
        onMessageReceiverList.remove(receiver);
        if (onMessageReceiverList.isEmpty()) {
            if (mSocket != null)
                mSocket.off(Events.INSTANCE.getRECEIVE_MESSAGE());
        }
    }
}
