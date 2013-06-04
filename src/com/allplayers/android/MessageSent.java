package com.allplayers.android;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.MessageData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

/**
 * MessageInbox.
 * User's messages sent mail box.
 *
 */
public class MessageSent extends AllplayersSherlockActivity {

    private final int LIMIT = 15;
    private ArrayList<MessageData> mMessageList;
    private Button mLoadMoreButton;
    private ListView mListView;
    private SentMessageAdapter mMessageListAdapter;
    private ProgressBar mLoadingIndicator;
    private ViewGroup mFooter;
    private boolean mEndOfData;
    private int mOffset;

    /**
     * onCreate().
     * Called when the activity is first created.
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_sent);
        //setContentView(R.layout.inboxlist);

        mActionBar = getSupportActionBar();
        mActionBar.setIcon(R.drawable.menu_icon);
        mActionBar.setTitle("Messages");
        mActionBar.setSubtitle("Sent");

        mSideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        mSideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        mSideNavigationView.setMenuClickCallback(this);
        mSideNavigationView.setMode(Mode.LEFT);

        // Load the user's sentbox.
        new GetUserSentboxTask().execute();

        mMessageList = new ArrayList<MessageData>();
        mListView = (ListView) findViewById(R.id.customListView);
        mMessageListAdapter = new SentMessageAdapter(MessageSent.this, mMessageList);
        mFooter = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.load_more, null);
        mLoadMoreButton = (Button) mFooter.findViewById(R.id.load_more_button);
        mLoadingIndicator = (ProgressBar) mFooter.findViewById(R.id.loading_indicator);

        // Set up the "load more" button.
        mLoadMoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreButton.setVisibility(View.GONE);
                mLoadingIndicator.setVisibility(View.VISIBLE);
                new GetUserSentboxTask().execute();
            }
        });

        // Set up the list view that will show all of the data.
        mListView.addFooterView(mFooter);
        mListView.setAdapter(mMessageListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {

                Intent intent = (new Router(MessageSent.this)).getMessageThreadIntent(mMessageList.get(position));
                startActivity(intent);
            }
        });

        // Check for a user long clicking on a list item
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long arg3) {
                PopupMenu menu = new PopupMenu(getBaseContext(), view);
                menu.inflate(R.menu.message_thread_menu);
                menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        switch (item.getItemId()) {
                        case R.id.delete:
                            new DeleteMessageTask(position).execute();
                            break;
                        }
                        return true;
                    }
                });
                menu.show();
                return true;
            }
        });
    }

    /**
     * GetUserInboxTask.
     * Fetches the user's message inbox asynchronously.
     *
     */
    public class GetUserSentboxTask extends AsyncTask<Void, Void, String> {

        /**
         * doInBackground().
         * Fetch the user's message inbox.
         *
         */
        @Override
        protected String doInBackground(Void... Args) {
            return RestApiV1.getUserSentBox(mOffset, LIMIT);
        }

        /**
         * onPoseExecute().
         * Take the JSON result from fetching the user's inbox and make it into useful data.
         *
         */
        @Override
        protected void onPostExecute(String jsonResult) {
            MessagesMap messages = new MessagesMap(jsonResult);

            // Check if there was any data returned. If there wasn't, either all of the data has
            // been fetched already or there wasn't any to fetch in the beginning.
            if (messages.size() == 0) {
                mEndOfData = true;
                mListView.removeFooterView(mFooter);


                // Check if the list of messages is empty. If so, there was never any data to be
                // fetched.
                if (mMessageList.size() == 0) {

                    // We need to display to the user that there aren't any messanges. We do this
                    // by making a blank MessageData object and setting its last_message_sender
                    // field to a notification. We use this field because it is the most prominent.
                    String[] blankMessage = {"No messages to display"};
                    ArrayAdapter<String> blank = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, blankMessage);
                    mListView.setAdapter(blank);
                    mListView.setEnabled(false);
                }
            }

            // If we made it here we know that there was at least part of a set of data fetched.
            else {

                // If the size of the returned data is less than the maximum size we specified, we
                // know that we have reached the end of the data to be fetched.
                if (messages.size() < LIMIT) {
                    mEndOfData = true;
                }

                mMessageList.addAll(messages.getMessageData());
                mMessageListAdapter.notifyDataSetChanged();
                if (!mEndOfData) {
                    mLoadMoreButton.setVisibility(View.VISIBLE);
                    mLoadingIndicator.setVisibility(View.GONE);
                    mOffset += LIMIT;
                } else {
                    mListView.removeFooterView(mFooter);
                }
            }

        }
    }
    public class DeleteMessageTask extends AsyncTask<Void, Void, String> {
        private int position;

        public DeleteMessageTask(int i) {
            position = i;
        }

        @Override
        protected String doInBackground(Void... params) {
            return RestApiV1.deleteMessage(mMessageList.get(position).getId(), "msg");
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("done")) {
                mMessageList.remove(position);
                mMessageListAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getBaseContext(), "There was an error deleting the message.\n" + result, Toast.LENGTH_LONG).show();
            }
        }
    }
}