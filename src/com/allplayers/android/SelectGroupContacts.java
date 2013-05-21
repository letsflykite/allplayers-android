package com.allplayers.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.GroupData;
import com.allplayers.objects.GroupMemberData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;
import com.google.gson.Gson;

public class SelectGroupContacts extends AllplayersSherlockListActivity {

    private ArrayList<GroupData> groupsList;
    private ArrayList<GroupData> selectedGroups;
    private ArrayList<GroupMemberData> selectedMembers;
    private Intent parentIntent;
    private ProgressBar spinner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.selectgroupcontacts);

        spinner = (ProgressBar) findViewById(R.id.progress_indicator);

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle("Compose Message");
        actionbar.setSubtitle("Select Group Recipients");

        sideNavigationView = (SideNavigationView) findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        selectedGroups = new ArrayList<GroupData>();
        selectedMembers = new ArrayList<GroupMemberData>();

        new GetUserGroupsTask().execute();

        final GetGroupMembersByGroupIdTask helper = new GetGroupMembersByGroupIdTask();

        final Button doneButton = (Button)findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (selectedGroups.size() == 0) {
                    finish();
                } else {
                    parentIntent = new Intent();
                    spinner.setVisibility(View.VISIBLE);
                    helper.execute(selectedGroups);
                }
            }
        });
    }

    /**
     * Listener for the Action Bar Options Menu.
     *
     * @param item: The selected menu item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case android.R.id.home: {
            sideNavigationView.toggleMenu();
            return true;
        }

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Listener for the Side Navigation Menu.
     *
     * @param itemId: The ID of the list item that was selected.
     */
    @Override
    public void onSideNavigationItemClick(int itemId) {

        switch (itemId) {

        case R.id.side_navigation_menu_item1:
            invokeActivity(GroupsActivity.class);
            break;

        case R.id.side_navigation_menu_item2:
            invokeActivity(MessageActivity.class);
            break;

        case R.id.side_navigation_menu_item3:
            invokeActivity(PhotosActivity.class);
            break;

        case R.id.side_navigation_menu_item4:
            invokeActivity(EventsActivity.class);
            break;

        case R.id.side_navigation_menu_item5: {
            search();
            break;
        }

        case R.id.side_navigation_menu_item6: {
            logOut();
            break;
        }

        case R.id.side_navigation_menu_item7: {
            refresh();
            break;
        }

        default:
            return;
        }

        finish();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (!selectedGroups.contains(groupsList.get(position))) {
            v.setBackgroundResource(R.color.android_blue);
            selectedGroups.add(groupsList.get(position));
        } else {
            v.setBackgroundResource(R.drawable.backgroundstate);
            selectedGroups.remove(groupsList.get(position));
        }
    }

    /*
     * Gets a user's groups.
     */
    public class GetUserGroupsTask extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... args) {
            return RestApiV1.getUserGroups(0, 0);
        }

        protected void onPostExecute(String jsonResult) {
            GroupsMap groups = new GroupsMap(jsonResult);
            groupsList = groups.getGroupData();
            String[] values;

            if (!groupsList.isEmpty()) {
                values = new String[groupsList.size()];

                for (int i = 0; i < groupsList.size(); i++) {
                    values[i] = groupsList.get(i).getTitle();
                }
            } else {
                values = new String[] {
                    "No groups to display"
                };
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectGroupContacts.this,
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);
            spinner.setVisibility(View.GONE);
        }
    }

    /*
     * Gets a group's members using a rest call.
     */
    public class GetGroupMembersByGroupIdTask extends AsyncTask<ArrayList<GroupData>, Void, String> {

        protected String doInBackground(ArrayList<GroupData>... groups) {
            String jsonResult = new String();
            for (int i = 0; i < groups[0].size(); i++) {
                // @TODO: Move to asynchronous loading.
                jsonResult += (RestApiV1.getGroupMembersByGroupId(groups[0].get(i).getUUID(), 0));
            }
            return jsonResult;
        }

        protected void onPostExecute(String jsonResult) {
            GroupMembersMap groupMembers = new GroupMembersMap(jsonResult);
            selectedMembers = groupMembers.getGroupMemberData();
            Gson gson = new Gson();
            String userData = gson.toJson(selectedMembers);
            parentIntent.putExtra("userData", userData);
            setResult(Activity.RESULT_OK, parentIntent);
            finish();
        }
    }
}