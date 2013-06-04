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

import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.GroupData;
import com.allplayers.objects.GroupMemberData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;
import com.google.gson.Gson;

public class SelectGroupContacts extends AllplayersSherlockListActivity {

    private ArrayList<GroupData> mGroupsList;
    private ArrayList<GroupData> mSelectedGroups;
    private ArrayList<GroupMemberData> mSelectedMembers;
    private ProgressBar mLoadingIndicator;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.selectgroupcontacts);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.progress_indicator);

        mActionBar.setTitle("Compose Message");
        mActionBar.setSubtitle("Select Group Recipients");

        mSideNavigationView = (SideNavigationView) findViewById(R.id.side_navigation_view);
        mSideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        mSideNavigationView.setMenuClickCallback(this);
        mSideNavigationView.setMode(Mode.LEFT);

        mSelectedGroups = new ArrayList<GroupData>();
        mSelectedMembers = new ArrayList<GroupMemberData>();

        new GetUserGroupsTask().execute();

        final Button doneButton = (Button)findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mSelectedGroups.size() == 0) {
                    finish();
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    new GetGroupMembersByGroupIdTask().execute(mSelectedGroups);
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (!mSelectedGroups.contains(mGroupsList.get(position))) {
            v.setBackgroundResource(R.color.android_blue);
            mSelectedGroups.add(mGroupsList.get(position));
        } else {
            v.setBackgroundResource(R.drawable.backgroundstate);
            mSelectedGroups.remove(mGroupsList.get(position));
        }
    }

    /*
     * Gets a user's groups.
     */
    public class GetUserGroupsTask extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... args) {
            return RestApiV1.getUserGroups(0, 0, null);
        }

        protected void onPostExecute(String jsonResult) {
            GroupsMap groups = new GroupsMap(jsonResult);
            mGroupsList = groups.getGroupData();
            String[] values;

            if (!mGroupsList.isEmpty()) {
                values = new String[mGroupsList.size()];

                for (int i = 0; i < mGroupsList.size(); i++) {
                    values[i] = mGroupsList.get(i).getTitle();
                }
            } else {
                values = new String[] {"No groups to display"};
                getListView().setEnabled(false);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectGroupContacts.this,
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);
            mLoadingIndicator.setVisibility(View.GONE);
        }
    }

    /*
     * Gets a group's members using a rest call.
     */
    public class GetGroupMembersByGroupIdTask extends AsyncTask<ArrayList<GroupData>, Void, String> {

        protected String doInBackground(ArrayList<GroupData>... groups) {
            String jsonResult = new String();
            for (int i = 0; i < groups[0].size(); i++) {
                jsonResult += (RestApiV1.getGroupMembersByGroupId(groups[0].get(i).getUUID(), 0, 0));
            }
            return jsonResult;
        }

        protected void onPostExecute(String jsonResult) {
            GroupMembersMap groupMembers = new GroupMembersMap(jsonResult);
            mSelectedMembers = groupMembers.getGroupMemberData();
            Gson gson = new Gson();
            String userData = gson.toJson(mSelectedMembers);
            Intent intent = new Intent();
            intent.putExtra("userData", userData);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}