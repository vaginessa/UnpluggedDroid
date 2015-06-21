package co.gounplugged.unpluggeddroid.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

import co.gounplugged.unpluggeddroid.api.APICaller;
import co.gounplugged.unpluggeddroid.db.DatabaseAccess;
import co.gounplugged.unpluggeddroid.managers.ThrowManager;
import co.gounplugged.unpluggeddroid.models.Conversation;
import co.gounplugged.unpluggeddroid.models.Mask;
import co.gounplugged.unpluggeddroid.models.Profile;
import co.gounplugged.unpluggeddroid.services.EdgenetClientService;
import co.gounplugged.unpluggeddroid.services.OpenPGPBridgeService;
import co.gounplugged.unpluggeddroid.utils.ContactUtil;
import co.gounplugged.unpluggeddroid.utils.MaskUtil;

/**
 * Serves as global application instance
 */
public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";
    public static final String SMS_DEFAULT_APPLICATION = "sms_default_application";

    private APICaller mApiCaller;
    private List<Mask> mKnownMasks;
    private List<Conversation> mRecentConversations;


    public static final BaseApplication getInstance(Context c) {
        return (BaseApplication) c.getApplicationContext();
    }

    public List<Conversation> getRecentConversations() {
        return mRecentConversations;
    }

    public static class App {
        public static ThrowManager ThrowManager;
    }

    /**
     * Get new masks from api or cache on app start
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "APPLICATION STARTED");
        initManagers();

        Profile.loadProfile(getApplicationContext());
        mApiCaller = new APICaller(getApplicationContext());

        switch(Profile.getApplicationState()) {
            case(Profile.APPLICATION_STATE_UNINITALIZED):
                break;
            case(Profile.APPLICATION_STATE_INITALIZED):
                seedKnownMasks();
                break;
        }

        fetchConversations();

        Log.d(TAG, "APPLICATION PROGRESSED");
        startService(new Intent(this, EdgenetClientService.class));
        startService(new Intent(this, OpenPGPBridgeService.class));
    }

    private void fetchConversations() {
        final DatabaseAccess<Conversation> conversationAccess = new DatabaseAccess<>(getApplicationContext(), Conversation.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRecentConversations = conversationAccess.getAll();
            }
        }).start();
    }

    private void initManagers() {
        App.ThrowManager = new ThrowManager(getApplicationContext());
    }

    public void refreshKnownMasks() {
        mKnownMasks = null;
        seedKnownMasks();
    }

    public void seedKnownMasks() {
        if(mKnownMasks == null) mKnownMasks = MaskUtil.getCachedMasks(getApplicationContext());
        if(mKnownMasks.isEmpty()) mApiCaller.getMasks(Profile.getCountryCodeFilter());
    }

    public List<Mask> getKnownMasks() {
        seedKnownMasks();
        return mKnownMasks;
    }

    public void setKnownMasks(List<Mask> knownMasks) {
        this.mKnownMasks = knownMasks;
    }

    public void loadContacts() {
        if(Profile.areContactsSynced()) return;
        ContactUtil.loadContactsInThread(getApplicationContext());
        Profile.setContactsSynced(true);
    }

    public boolean isDefaultSMSApp() {
        String defaultApplication = Settings.Secure.getString(getContentResolver(),  SMS_DEFAULT_APPLICATION);
        String thisApplication = getApplicationContext().getPackageName();

        return defaultApplication.equals(thisApplication);
    }
}
