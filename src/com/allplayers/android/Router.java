package com.allplayers.android;

import android.app.Activity;
import android.content.Intent;

import com.allplayers.objects.AlbumData;
import com.allplayers.objects.EventData;
import com.allplayers.objects.GroupData;
import com.allplayers.objects.MessageData;
import com.allplayers.objects.MessageThreadData;
import com.allplayers.objects.PhotoData;

/**
 * Handle routing the user and data between activities by type matching
 * and casting Intent data in one place.
 */
public class Router {

    private static final String INTENT_PREFIX = "com.allplayers.android.";

    private static final String EXTRA_ALBUM = INTENT_PREFIX + "ALBUM";

    private static final String EXTRA_EVENT = INTENT_PREFIX + "EVENT";

    private static final String EXTRA_GROUP = INTENT_PREFIX + "GROUP";

    private static final String EXTRA_MESSAGE = INTENT_PREFIX + "MESSAGE";

    private static final String EXTRA_MESSAGE_THREAD = INTENT_PREFIX + "MESSAGE_THREAD";

    private static final String EXTRA_PHOTO = INTENT_PREFIX + "PHOTO";

    private static final String EXTRA_GROUP_SEARCH_QUERY = INTENT_PREFIX + "GROUP_SEARCH_QUERY";

    private static final String EXTRA_GROUP_SEARCH_ZIPCODE = INTENT_PREFIX + "GROUP_SEARCH_ZIPCODE";

    private static final String EXTRA_GROUP_SEARCH_DISTANCE = INTENT_PREFIX + "GROUP_SEARCH_DISTANCE";

    private Activity mActivity;

    public Router(Activity activity) {
        mActivity = activity;
    }

    private Intent getAlbumIntent(Class<?> cls, AlbumData album) {
        Intent intent = new Intent(mActivity, cls);
        intent.putExtra(EXTRA_ALBUM, album);
        return intent;
    }

    public Intent getEventDisplayActivityIntent(EventData event) {
        Intent intent = new Intent(mActivity, EventDisplayActivity.class);
        intent.putExtra(EXTRA_EVENT, event);
        return intent;
    }

    public Intent getEventDetailActivityIntent(EventData event) {
        Intent intent = new Intent(mActivity, EventDetailActivity.class);
        intent.putExtra(EXTRA_EVENT, event);
        return intent;
    }

    private Intent getGroupIntent(Class<?> cls, GroupData group) {
        Intent intent = new Intent(mActivity, cls);
        intent.putExtra(EXTRA_GROUP, group);
        return intent;
    }

    private Intent getMessageIntent(Class<?> cls, MessageData message) {
        Intent intent = new Intent(mActivity, cls);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    public Intent getAlbumPhotosActivityIntent(AlbumData album) {
        return getAlbumIntent(AlbumPhotosActivity.class, album);
    }

    public Intent getGroupAlbumsActivityIntent(GroupData group) {
        return getGroupIntent(GroupAlbumsActivity.class, group);
    }

    public Intent getGroupLocationActivityIntent(GroupData group) {
        return getGroupIntent(GroupLocationActivity.class, group);
    }

    public Intent getGroupEventsActivityIntent(GroupData group) {
        return getGroupIntent(GroupEventsActivity.class, group);
    }

    public Intent getGroupMembersActivityIntent(GroupData group) {
        return getGroupIntent(GroupMembersActivity.class, group);
    }

    public Intent getGroupPageActivityIntent(GroupData group) {
        return getGroupIntent(GroupPageActivity.class, group);
    }

    public Intent getMessagReplyIntent(MessageData message) {
        return getMessageIntent(MessageReply.class, message);
    }

    public Intent getMessageThreadIntent(MessageData message) {
        return getMessageIntent(MessageThread.class, message);
    }

    public Intent getMessageViewSingleIntent(MessageData message, MessageThreadData thread) {
        Intent intent = getMessageIntent(MessageViewSingle.class, message);
        intent.putExtra(EXTRA_MESSAGE_THREAD, thread);
        return intent;
    }

    public Intent getPhotoPagerActivityIntent(PhotoData photo) {
        Intent intent = new Intent(mActivity, PhotoPager.class);
        intent.putExtra(EXTRA_PHOTO, photo);
        return intent;
    }

    public Intent getSearchGroupsListActivityIntent(String query, int zipcode, int distance) {
        Intent intent = new Intent(mActivity, SearchGroupsListActivity.class);
        intent.putExtra(EXTRA_GROUP_SEARCH_QUERY, query);
        intent.putExtra(EXTRA_GROUP_SEARCH_ZIPCODE, zipcode);
        intent.putExtra(EXTRA_GROUP_SEARCH_DISTANCE, distance);
        return intent;
    }

    public AlbumData getIntentAlbum() {
        return (AlbumData) mActivity.getIntent().getSerializableExtra(EXTRA_ALBUM);
    }

    public EventData getIntentEvent() {
        return (EventData) mActivity.getIntent().getSerializableExtra(EXTRA_EVENT);
    }

    public GroupData getIntentGroup() {
        return (GroupData) mActivity.getIntent().getSerializableExtra(EXTRA_GROUP);
    }

    public MessageData getIntentMessage() {
        return (MessageData) mActivity.getIntent().getSerializableExtra(EXTRA_MESSAGE);
    }

    public MessageThreadData getIntentMessageThread() {
        return (MessageThreadData) mActivity.getIntent().getSerializableExtra(EXTRA_MESSAGE_THREAD);
    }

    public PhotoData getIntentPhoto() {
        return (PhotoData) mActivity.getIntent().getSerializableExtra(EXTRA_PHOTO);
    }

    public String getIntentSearchQuery() {
        return mActivity.getIntent().getExtras().getString(EXTRA_GROUP_SEARCH_QUERY);
    }

    public int getIntentSearchZipcode() {
        return mActivity.getIntent().getExtras().getInt(EXTRA_GROUP_SEARCH_ZIPCODE);
    }

    public int getIntentSearchDistance() {
        return mActivity.getIntent().getExtras().getInt(EXTRA_GROUP_SEARCH_DISTANCE);
    }
}
