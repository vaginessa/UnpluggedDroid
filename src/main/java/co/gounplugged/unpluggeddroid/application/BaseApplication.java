package co.gounplugged.unpluggeddroid.application;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.gounplugged.unpluggeddroid.api.APICaller;
import co.gounplugged.unpluggeddroid.db.DatabaseAccess;
import co.gounplugged.unpluggeddroid.exceptions.InvalidPhoneNumberException;
import co.gounplugged.unpluggeddroid.models.Contact;
import co.gounplugged.unpluggeddroid.models.Krewe;
import co.gounplugged.unpluggeddroid.models.Mask;
import co.gounplugged.unpluggeddroid.models.Profile;

/**
 * Serves as global application instance
 */
public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";
    private APICaller apiCaller;
    private Krewe mKnownMasks;
    private Profile profile;

    public List<Contact> getContacts() {
        return contacts;
    }

    private List<Contact> contacts;

    /**
     * Get new masks from api or cache on app start
     */
    @Override
    public void onCreate() {
        super.onCreate();
        profile = new Profile(getApplicationContext());
        apiCaller = new APICaller(getApplicationContext());
        seedKnownMasks();
        loadContacts();
    }

    private void seedKnownMasks() {
        if(mKnownMasks == null){
            mKnownMasks = new Krewe();
        }
        DatabaseAccess<Mask> maskAccess = new DatabaseAccess<>(getApplicationContext(), Mask.class);
        // TODO: Prefill from db

        if(mKnownMasks.isEmpty()) {
            apiCaller.getMasks(profile.getCountryCode());
        }
    }


    public Krewe getKnownMasks() {
        return mKnownMasks;
    }

    public void setKnownMasks(Krewe knownMasks) {
        this.mKnownMasks = knownMasks;
    }

    private void loadContacts() {
        //TODO: prefill from db
        this.contacts = new ArrayList();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact c = null;
                        try {
                            c = new Contact(name, phoneNo);
                            Log.d(TAG, "Adding Name: " + name + ", Phone No: " + phoneNo);
                            contacts.add(c);
                        } catch (InvalidPhoneNumberException e) {
                            Log.d(TAG, "Skipping Name: " + name + ", Phone No: " + phoneNo);
                        }
                    }
                    pCur.close();
                }
            }
        }
    }
}
