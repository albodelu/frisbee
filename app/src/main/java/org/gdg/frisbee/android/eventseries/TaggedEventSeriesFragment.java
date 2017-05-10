/*
 * Copyright 2014 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.eventseries;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.PagedList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;

import butterknife.ButterKnife;

public class TaggedEventSeriesFragment extends EventListFragment {

    private static final String ARGS_ADD_DESCRIPTION_AS_HEADER = "add_description";

    private ModelCache modelCache;
    private GdgXHub gdgXHub;

    private String mCacheKey = "";
    private TaggedEventSeries mTaggedEventSeries;
    private Comparator<EventAdapter.Item> comparator;

    public static TaggedEventSeriesFragment newInstance(String cacheKey,
                                                        TaggedEventSeries taggedEventSeries,
                                                        boolean addDescriptionAsHeader) {
        TaggedEventSeriesFragment frag = new TaggedEventSeriesFragment();
        Bundle args = new Bundle();
        args.putString(Const.EXTRA_TAGGED_EVENT_CACHEKEY, cacheKey);
        args.putParcelable(Const.EXTRA_TAGGED_EVENT, taggedEventSeries);
        args.putBoolean(ARGS_ADD_DESCRIPTION_AS_HEADER, addDescriptionAsHeader);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        modelCache = App.from(context).getModelCache();
        gdgXHub = App.from(context).getGdgXHub();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        comparator = new TaggedEventDistanceComparator(App.from(getContext()).getLastLocation());

        if (getArguments() != null) {
            Bundle args = getArguments();
            mCacheKey = args.getString(Const.EXTRA_TAGGED_EVENT_CACHEKEY);
            mTaggedEventSeries = args.getParcelable(Const.EXTRA_TAGGED_EVENT);
        }
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ListView list = ButterKnife.findById(v, android.R.id.list);

        if (getArguments() != null && getArguments().getBoolean(ARGS_ADD_DESCRIPTION_AS_HEADER, false)) {
            //noinspection RestrictedApi
            View header = getLayoutInflater(null)
                .inflate(R.layout.header_list_special_event_series, (ViewGroup) getView(), false);

            TextView mDescription = ButterKnife.findById(header, R.id.special_description);
            mDescription.setText(mTaggedEventSeries.getDescriptionResId());
            mDescription.setCompoundDrawablesWithIntrinsicBounds(0,
                mTaggedEventSeries.getLogoResId(), 0, 0);

            list.addHeaderView(header, null, false);
        }

        final ViewGroup.LayoutParams listLayoutParams = list.getLayoutParams();
        if (getView() != null && listLayoutParams.width > getView().getWidth()) {
            listLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        return v;
    }

    @Override
    EventAdapter createEventAdapter() {
        Picasso picasso = App.from(getContext()).getPicasso();
        return new EventAdapter(getContext(), picasso, mTaggedEventSeries.getDefaultIconResId());
    }

    @Override
    void fetchEvents() {
        setIsLoading(true);
        Callback<PagedList<Event>> listener = new Callback<PagedList<Event>>() {
            @Override
            public void onSuccess(final PagedList<Event> taggedEventPagedList) {
                ArrayList<Event> events = taggedEventPagedList.getItems();
                if (isContextValid()) {
                    mAdapter.addAll(events);
                    sortEvents();
                    setIsLoading(false);
                }
                modelCache.putAsync(mCacheKey, events, DateTime.now().plusHours(2));
            }

            @Override
            public void onError() {
                setIsLoading(false);
                showError(R.string.fetch_events_failed);
            }

            @Override
            public void onNetworkFailure(Throwable error) {
                setIsLoading(false);
                showError(R.string.offline_alert);
            }
        };

        if (Utils.isOnline(getActivity())) {
            gdgXHub.getTaggedEventUpcomingList(mTaggedEventSeries.getTag(), DateTime.now()).enqueue(listener);
        } else {
            modelCache.getAsync(mCacheKey, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    if (checkValidCache(item)) {
                        ArrayList<Event> events = (ArrayList<Event>) item;
                        mAdapter.addAll(events);
                        sortEvents();
                        setIsLoading(false);
                        if (isAdded()) {
                            Snackbar snackbar = Snackbar.make(getView(), R.string.cached_content,
                                Snackbar.LENGTH_SHORT);
                            ColoredSnackBar.info(snackbar).show();
                        }
                    } else {
                        modelCache.removeAsync(mCacheKey);
                        onNotFound();
                    }
                }

                @Override
                public void onNotFound(String key) {
                    onNotFound();
                }

                private void onNotFound() {
                    setIsLoading(false);
                    showError(R.string.offline_alert);
                }
            });
        }
    }

    private void sortEvents() {
        mAdapter.sort(comparator);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.special_event_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (comparator instanceof TaggedEventDistanceComparator) {
            menu.findItem(R.id.order_by_date).setVisible(true);
            menu.findItem(R.id.order_by_distance).setVisible(false);
        } else {
            menu.findItem(R.id.order_by_distance).setVisible(true);
            menu.findItem(R.id.order_by_date).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.order_by_date) {
            comparator = new EventDateComparator();
            setIsLoading(true);
            sortEvents();
            setIsLoading(false);
            getActivity().supportInvalidateOptionsMenu();
            scrollToSoonestEvent();
            return true;
        } else if (item.getItemId() == R.id.order_by_distance) {
            comparator = new TaggedEventDistanceComparator(App.from(getContext()).getLastLocation());
            setIsLoading(true);
            sortEvents();
            setIsLoading(false);
            getActivity().supportInvalidateOptionsMenu();
            getListView().setSelection(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scrollToSoonestEvent() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).getStart().getMillis() > now) {
                getListView().setSelection(i);
                return;
            }
        }
    }
}
