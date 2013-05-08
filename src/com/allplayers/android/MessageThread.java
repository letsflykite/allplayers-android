package com.allplayers.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.MessageData;
import com.allplayers.objects.MessageThreadData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

public class MessageThread extends AllplayersSherlockListActivity {
    private ArrayList<MessageThreadData> mMessageThreadList;
    private boolean hasMessages = false;
    private String mJsonResult = "";
    private int mThreadId;
    private ArrayList<HashMap<String, String>> mInfoList = new ArrayList<HashMap<String, String>>(2);
    private MessageData mMessage;
    private ProgressBar mLoadingIndicator;

    /**
     * Called when the activity is first created, this sets up some variables,
     * creates the Action Bar, and sets up the Side Navigation Menu.
     * @param savedInstanceState: Saved data from the last instance of the
     * activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mMessage = (new Router(this)).getIntentMessage();
        String threadID = mMessage.getThreadID();

        setContentView(R.layout.message_thread);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.progress_indicator);

        actionbar.setTitle("Messages");

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        PutAndGetMessagesTask helper = new PutAndGetMessagesTask();
        helper.execute(threadID);
    }

    /**
     * Listener for the list on the page.
     * @param l
     * @param v
     * @param position: The position in the list of the clicked item.
     * @param id: The id of the list item that was clicked.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        if (hasMessages) {
            Intent intent = (new Router(this)).getMessageViewSingleIntent(mMessage, mMessageThreadList.get(position));
            startActivity(intent);
        }
    }

    /**
     * An async task containing the REST calls needed to populate the messages
     * list.
     */
    public class PutAndGetMessagesTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... threadID) {

            mThreadId = Integer.parseInt(threadID[0]);
            RestApiV1.putMessage(mThreadId, 0, "thread");

            mJsonResult = RestApiV1.getUserMessagesByThreadId(threadID[0]);

            return mJsonResult;
        }

        /**
         * Gets a user's sent and received messages and puts this data into the
         * user's message thread.
         * @param jsonResult: The json result containing the data for the user's
         * sent and received messages in this thread.
         */
        protected void onPostExecute(String jsonResult) {

            HashMap<String, String> map;
            MessageThreadMap messages = new MessageThreadMap(jsonResult);
            mMessageThreadList = messages.getMessageThreadData();

            actionbar.setSubtitle("Thread started by " + mMessageThreadList.get(0).getSenderName());

            Collections.sort(mMessageThreadList, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    MessageThreadData m1 = (MessageThreadData) o1;
                    MessageThreadData m2 = (MessageThreadData) o2;
                    return m2.getTimestampString().compareToIgnoreCase(m1.getTimestampString());
                }
            });

            if (!mMessageThreadList.isEmpty()) {
                hasMessages = true;
                for (int i = 0; i < mMessageThreadList.size(); i++) {
                    map = new HashMap<String, String>();
                    map.put("line1", mMessageThreadList.get(i).getMessageBody());
                    map.put("line2", "From: " + mMessageThreadList.get(i).getSenderName() + " - " + mMessageThreadList.get(i).getDateString());
                    mInfoList.add(map);
                }
            } else {
                hasMessages = false;

                map = new HashMap<String, String>();
                map.put("line1", "You have no new messages.");
                map.put("line2", "");
                mInfoList.add(map);
            }

            String[] from = { "line1", "line2" };

            int[] to = { android.R.id.text1, android.R.id.text2 };

            SimpleAdapter adapter = new SimpleAdapter(MessageThread.this, mInfoList, android.R.layout.simple_list_item_2, from, to);
            setListAdapter(adapter);
            mLoadingIndicator.setVisibility(View.GONE);
        }
    }
}