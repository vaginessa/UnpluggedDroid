package co.gounplugged.unpluggeddroid.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;

import co.gounplugged.unpluggeddroid.R;
import co.gounplugged.unpluggeddroid.adapters.ContactRecyclerViewAdapter;
import co.gounplugged.unpluggeddroid.adapters.MessageRecyclerViewAdapter;
import co.gounplugged.unpluggeddroid.application.BaseApplication;
import co.gounplugged.unpluggeddroid.exceptions.InvalidConversationException;
import co.gounplugged.unpluggeddroid.exceptions.NotFoundInDatabaseException;
import co.gounplugged.unpluggeddroid.fragments.MessageInputFragment;
import co.gounplugged.unpluggeddroid.fragments.SearchContactFragment;
import co.gounplugged.unpluggeddroid.listeners.RecyclerItemClickListener;
import co.gounplugged.unpluggeddroid.models.Contact;
import co.gounplugged.unpluggeddroid.models.Conversation;
import co.gounplugged.unpluggeddroid.models.Message;
import co.gounplugged.unpluggeddroid.models.Profile;
import co.gounplugged.unpluggeddroid.services.OpenPGPBridgeService;
import co.gounplugged.unpluggeddroid.utils.ContactUtil;
import co.gounplugged.unpluggeddroid.utils.ConversationUtil;
import de.greenrobot.event.EventBus;


public class ChatActivity extends BaseActivity {

    // Debug
    private final String TAG = "ChatActivity";

    // Constants
    public static final int VIEWPAGE_MESSAGE_INPUT = 0;
    public static final int VIEWPAGE_SEARCH_CONTACT = 1;

    // GUI
    private ViewPager mViewPager;
    private UnderlinePageIndicator mUnderlinePageIndicator;

    private OpenPGPBridgeService mOpenPGPBridgeService;
    private ServiceConnection mOpenPGPBridgeConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OpenPGPBridgeService.LocalBinder binder = (OpenPGPBridgeService.LocalBinder) service;
            mOpenPGPBridgeService = binder.getService();
            mIsBoundToOpenPGP = true;
            Log.d(TAG, "bound to pgp bridge");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBoundToOpenPGP = false;
            Log.d(TAG, "unbound from pgp bridge");
        }
    };
    ;
    private boolean mIsBoundToOpenPGP = false;

    private Conversation mSelectedConversation;

    private MessageRecyclerViewAdapter mMessageRecyclerViewAdapter;
    private ContactRecyclerViewAdapter mContactRecyclerViewAdapter;

    private MessageInputFragment mMessageInputFragment;
    private SearchContactFragment mSearchContactFragment;

    /*
        Return the last selected conversation. Null if no last conversation.
     */
    public synchronized Conversation getLastSelectedConversation() {
        if (mSelectedConversation != null)
            ConversationUtil.refresh(getApplicationContext(), mSelectedConversation);
        if (mSelectedConversation == null) {
            long cid = Profile.getLastConversationId();
            if (cid != Profile.LAST_SELECTED_CONVERSATION_UNSET_ID) {
                try {
                    mSelectedConversation = ConversationUtil.findById(getApplicationContext(), cid);
                } catch (NotFoundInDatabaseException e) {
                    e.printStackTrace();
                }
            } else {
                List<Conversation> conversations = ConversationUtil.getAll(getApplicationContext());
                if (conversations != null && conversations.size() > 0)
                    mSelectedConversation = conversations.get(0);
            }
        }
        return mSelectedConversation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_new);
        Log.d(TAG, "onCreate");

        getLastSelectedConversation();
        loadGui();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        bindService(
                new Intent(this, OpenPGPBridgeService.class),
                mOpenPGPBridgeConnection,
                Context.BIND_AUTO_CREATE);

        ((BaseApplication) getApplicationContext()).seedKnownMasks();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        if (mIsBoundToOpenPGP) {
            unbindService(mOpenPGPBridgeConnection);
            mIsBoundToOpenPGP = false;
        }
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        EventBus.getDefault().removeStickyEvent(Message.class);
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onEventMainThread(Message message) {
        mMessageRecyclerViewAdapter.addMessage(message);
    }

    public void filterContacts(String query) {
        mContactRecyclerViewAdapter.filter(query);
    }

    private void loadGui() {
        setupToolbar(NAVIGATION_MAIN_HOME);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        List<Fragment> fragments = new ArrayList<>();
        mMessageInputFragment = (MessageInputFragment) Fragment.instantiate(getApplicationContext(),
                MessageInputFragment.class.getName(), getIntent().getExtras());
        mSearchContactFragment = (SearchContactFragment) Fragment.instantiate(getApplicationContext(),
                SearchContactFragment.class.getName(), getIntent().getExtras());
        fragments.add(mMessageInputFragment);
        fragments.add(mSearchContactFragment);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);

        mUnderlinePageIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
//        mUnderlinePageIndicator.setViewPager(mViewPager);
//        mUnderlinePageIndicator.setCurrentItem(0);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {  }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, (position % 2 == 0 ? "input" : "search") + "-fragment in viewpager selected");
                toggleRecyclerView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mMessageRecyclerViewAdapter = new MessageRecyclerViewAdapter(this, mSelectedConversation);
        mContactRecyclerViewAdapter = new ContactRecyclerViewAdapter(this, ContactUtil.getCachedContacts(getApplicationContext()));
        recyclerView.setAdapter(mMessageRecyclerViewAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Contact c = mContactRecyclerViewAdapter.getContact(position);
                        addConversation(c);
                        //Re-order menu

                    }
                })
        );
    }

    private void toggleRecyclerView(int position) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
//        mUnderlinePageIndicator.setCurrentItem(position);
        switch (position) {
            case VIEWPAGE_MESSAGE_INPUT:
                recyclerView.setAdapter(mMessageRecyclerViewAdapter);
                break;
            case VIEWPAGE_SEARCH_CONTACT:
                recyclerView.setAdapter(mContactRecyclerViewAdapter);
                break;
        }

    }


    public void addConversation(Contact contact) {
        Conversation newConversation;

        try {
            newConversation = ConversationUtil.findByParticipant(contact, getApplicationContext());
        } catch(NotFoundInDatabaseException e) {
            try {
                newConversation = ConversationUtil.createConversation(contact, getApplicationContext());
            } catch (InvalidConversationException e1) {
                //TODO let user know something went wrong
                return;
            }
        }

        mSelectedConversation = newConversation;
//        mConversations.add(mSelectedConversation);
        Profile.setLastConversationId(mSelectedConversation.id);

        //update ui with new convo
        updateActivityViews();

    }

    private void updateActivityViews() {
        mMessageRecyclerViewAdapter.setConversation(mSelectedConversation);
        toggleRecyclerView(VIEWPAGE_MESSAGE_INPUT);
        mViewPager.setCurrentItem(VIEWPAGE_MESSAGE_INPUT, true);
        getSupportActionBar().setTitle(mSelectedConversation.getParticipant().getName());
        mMessageInputFragment.updateViews();
//        rebuildSubMenu();
//        addConversationToSubMenu(mSelectedConversation);
//        notifyNavigationMenuChanged();
    }

    public OpenPGPBridgeService getOpenPGPBridgeService() {
        return mOpenPGPBridgeService;
    }

    static class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter{

        private final List<Fragment> mViewPagerFragments;

        public FragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mViewPagerFragments = fragments;
        }

        @Override
        public Fragment getItem(int index) {
            return mViewPagerFragments.get(index);
        }

        @Override
        public int getCount() {
            return mViewPagerFragments.size();
        }

        //Always return POSITION_NONE from getItemPosition() method. Which means: "Fragment must be always recreated"
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "BLABLABLABALBALA";
        }
    }

}