package com.allplayers.android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.MessageData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

public class MessageReply extends AllplayersSherlockActivity {
    private String threadID;
    private String sendBody;

    /** called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.replymessage);

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle("Messages");
        actionbar.setSubtitle("Reply");

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        MessageData message = (new Router(this)).getIntentMessage();

        String subject = message.getSubject();
        String sender = message.getLastSender();
        String date = message.getDateString();
        threadID = message.getThreadID();

        final TextView subjectText = (TextView)findViewById(R.id.subjectText);
        subjectText.setText("This is the Subject.");
        subjectText.setText(subject);

        final TextView senderText = (TextView)findViewById(R.id.senderText);
        senderText.setText("This is the Sender's Name.");
        senderText.setText("From: " + sender);

        final TextView dateText = (TextView)findViewById(R.id.dateText);
        dateText.setText("This is the Date last sent.");
        dateText.setText("Last Message: " + date);


        final EditText bodyField = (EditText)findViewById(R.id.bodyField);
        bodyField.setText("This is the Body text.");
        bodyField.setText("");

        final Button sendButton = (Button)findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendBody = bodyField.getText().toString();

                PostMessageTask helper = new PostMessageTask();
                helper.execute(Integer.parseInt(threadID), sendBody);

                Toast toast = Toast.makeText(getBaseContext(), "Message Sent!", Toast.LENGTH_LONG);
                toast.show();

                finish();
            }
        });
    }

    /*
     * Posts a user's message using a rest call.
     * It was necessary to use an "Object" due to the fact that you cannot pass
     *      variables of different type into doIbBackground.
     */
    public class PostMessageTask extends AsyncTask<Object, Void, Void> {
        protected Void doInBackground(Object... args) {
            RestApiV1.postMessage((Integer)args[0], (String)args[1]);
            return null;
        }
    }
}