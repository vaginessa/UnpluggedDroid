package co.gounplugged.unpluggeddroid.test.activities;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.util.Date;

import co.gounplugged.unpluggeddroid.R;
import co.gounplugged.unpluggeddroid.activities.ChatActivity;
import co.gounplugged.unpluggeddroid.exceptions.InvalidConversationException;
import co.gounplugged.unpluggeddroid.exceptions.InvalidPhoneNumberException;
import co.gounplugged.unpluggeddroid.models.Conversation;
import co.gounplugged.unpluggeddroid.models.Message;
import co.gounplugged.unpluggeddroid.models.Profile;
import co.gounplugged.unpluggeddroid.utils.ContactUtil;
import co.gounplugged.unpluggeddroid.utils.ConversationUtil;
import co.gounplugged.unpluggeddroid.utils.MessageUtil;

/**
 * Created by Marvin Arnold on 8/06/15.
 */
public class ChatActivityTest extends ActivityUnitTestCase<ChatActivity> {
    private final static String TAG = "ChatActivityTest";
    ChatActivity mChatActivity;
    Context mContext;
    public ChatActivityTest() {
        super(ChatActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = this.getInstrumentation().getTargetContext();
        ContextThemeWrapper context = new ContextThemeWrapper(mContext, R.style.AppTheme);
        setActivityContext(context);
        // Don't change this, see: https://stackoverflow.com/questions/21611539/actionbaractivity-activityunittest-namenotfoundexception
        // and https://stackoverflow.com/questions/24760354/namenotfoundexception-at-activityunittestcase-with-actionbaractivity
        setActivity(launchActivity("co.gounplugged.unpluggeddroid", ChatActivity.class, null));
        mChatActivity = getActivity();
    }

//    public void testGetCurrentConversation() {
//        Profile.setLastConversationId(Profile.LAST_SELECTED_CONVERSATION_UNSET_ID);
//        ConversationUtil.deleteAll(mContext);
//
//        assertNull(mChatActivity.getLastSelectedConversation());
//
//        Conversation conversation;
//        try {
//            conversation = ConversationUtil.createConversation(ContactUtil.create(mContext, "", "+130168645876"), mContext);
//        } catch (InvalidConversationException e) {
//            assertTrue(false);
//            return;
//        } catch (InvalidPhoneNumberException e) {
//            assertTrue(false);
//            return;
//        }
//
//        assertEquals(conversation, mChatActivity.getLastSelectedConversation());
//    }

//    public void testMessageVisibility() {
//        mChatActivity.runOnUiThread(new Runnable() {
//            public void run() {
//                Conversation current;
//                Conversation noncurrent;
//                try {
//                    current = ConversationUtil.createConversation(ContactUtil.firstOrCreate(mContext, "", "+130168645876"), mContext);
//                    noncurrent = ConversationUtil.createConversation(ContactUtil.firstOrCreate(mContext, "", "+130168645876"), mContext);
//                } catch (InvalidConversationException e) {
//                    assertTrue(false);
//                    return;
//                } catch (InvalidPhoneNumberException e) {
//                    assertTrue(false);
//                    return;
//                }
//
//                mChatActivity.setLastConversation(current);
//                MessageUtil.create(mContext, noncurrent, "hi", Message.TYPE_INCOMING, System.currentTimeMillis());
//
//                assertEquals(0, mChatActivity.getChatArrayAdapter().getCount());
//
//                mChatActivity.setLastConversation(noncurrent);
//                assertEquals(1, mChatActivity.getChatArrayAdapter().getCount());
//            }
//        });
//    }
}
