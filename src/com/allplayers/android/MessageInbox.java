package com.allplayers.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.MessageData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.ISideNavigationCallback;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

public class MessageInbox extends AllplayersSherlockActivity implements ISideNavigationCallback {
    private ArrayList<MessageData> messageList;
    private boolean hasMessages = false;
    private ProgressBar loading;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inboxlist);
        loading = (ProgressBar) findViewById(R.id.progress_indicator);

        GetUserInboxTask helper = new GetUserInboxTask();
        helper.execute();

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle("Messages");
        actionbar.setSubtitle("Inbox");

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

    }

    public void populateInbox(String json) {
        MessagesMap messages = new MessagesMap(json);
        messageList = messages.getMessageData();

        Collections.reverse(messageList);

        ListView list = (ListView) findViewById(R.id.customListView);
        list.setClickable(true);

        if (!messageList.isEmpty()) {
            hasMessages = true;
        } else {
            hasMessages = false;
        }

        final List<MessageData> messageList2 = messageList;
        MessageAdapter adapter = new MessageAdapter(MessageInbox.this, messageList2);

        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
                if (hasMessages) {
                    // Go to the message thread.
                    Intent intent = (new Router(MessageInbox.this)).getMessageThreadIntent(messageList.get(position));
                    startActivity(intent);
                }
            }
        });

        list.setAdapter(adapter);
    }

    public class GetUserInboxTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... Args) {
            return RestApiV1.getUserInbox();
        }

        protected void onPostExecute(String jsonResult) {
            populateInbox(jsonResult);
            loading.setVisibility(View.GONE);
        }
    }
}