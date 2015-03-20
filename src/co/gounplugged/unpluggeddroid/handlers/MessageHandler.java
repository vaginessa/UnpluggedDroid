package co.gounplugged.unpluggeddroid.handlers;

import android.content.Context;
import android.os.Handler;
import android.telephony.SmsManager;

import co.gounplugged.unpluggeddroid.adapters.MessageAdapter;
import co.gounplugged.unpluggeddroid.db.DatabaseAccess;
import co.gounplugged.unpluggeddroid.models.Contact;
import co.gounplugged.unpluggeddroid.models.Message;

public class MessageHandler extends Handler {
    private static final String TAG = "UnpluggedMessageHandler";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;

    private MessageAdapter messageAdapter;
    private DatabaseAccess<Message> messageDatabaseAccess;

    public MessageHandler(MessageAdapter messageAdapter, Context context) {
        this.messageAdapter = messageAdapter;
        this.messageDatabaseAccess = new DatabaseAccess<Message>(context, Message.class);
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        Message message = (Message) msg.obj;
        switch (msg.what) {
            case MESSAGE_WRITE:
                String sms = (String) message.getMessage();
                // construct a string from the buffer
                SmsManager smsManager= SmsManager.getDefault();
                smsManager.sendTextMessage(Contact.DEFAULT_CONTACT, null, sms, null, null);
//                Message unpluggedMessage = new Message(writeMessage,
//                        Message.TYPE_OUTGOING, System.currentTimeMillis());
                messageAdapter.addMessage(message);
                messageDatabaseAccess.create(message);

                break;
            case MESSAGE_READ:
                messageAdapter.addMessage(message);
                messageDatabaseAccess.create(message);
                break;

        }
    }
}