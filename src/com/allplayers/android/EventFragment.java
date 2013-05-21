package com.allplayers.android;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.allplayers.objects.EventData;
import com.allplayers.rest.RestApiV1;

public class EventFragment extends ListFragment {
    private ArrayList<EventData> eventsList;
    private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);
    private boolean hasEvents = false;
    private String jsonResult;
    private Activity parentActivity;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = this.getActivity();
        new GetUserEventsTask().execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent;
        if (hasEvents) {
            if ((!(eventsList.get(position).getLatitude().equals("")
                    && eventsList.get(position).getLatitude().equals(""))) && (!(Build.VERSION.SDK_INT < 11))) {
                intent = (new Router(parentActivity)).getEventDisplayActivityIntent(eventsList.get(position));
            } else {
                intent = (new Router(parentActivity)).getEventDetailActivityIntent(eventsList.get(position));
            }
            startActivity(intent);
        }
    }

    /*
     * Populates a hash map with event information.
     */
    protected void setEventsMap() {
        EventsMap events = new EventsMap(jsonResult);
        eventsList = events.getEventData();
        HashMap<String, String> map;

        if (!eventsList.isEmpty()) {
            for (int i = 0; i < eventsList.size(); i++) {
                map = new HashMap<String, String>();
                map.put("line1", eventsList.get(i).getTitle());

                String start = eventsList.get(i).getStartDateString();
                map.put("line2", start);
                list.add(map);
            }

            hasEvents = true;
        } else {
            map = new HashMap<String, String>();
            map.put("line1", "No events to display.");
            map.put("line2", "");
            list.add(map);
            hasEvents = false;
        }

        String[] from = {"line1", "line2"};

        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleAdapter adapter = new SimpleAdapter(parentActivity, list, android.R.layout.simple_list_item_2, from, to);
        setListAdapter(adapter);
    }

    /*
     * Gets event information for a user using a rest call.
     */
    public class GetUserEventsTask extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... args) {
            // @TODO: Move to asynchronous loading.
            return RestApiV1.getUserEvents(0);
        }

        protected void onPostExecute(String jsonResult) {
            EventFragment.this.jsonResult = jsonResult;
            setEventsMap();
        }
    }
}
