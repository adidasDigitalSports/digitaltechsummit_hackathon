package com.adidas.hackathon.smartjacket.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.ListView;

import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = ContactsFragment.class.getName();

    @BindView(R.id.contacts_list_view)
    ListView contactsListView;

    private SimpleCursorAdapter cursorAdapter;
    private String nameFavContact = "";
    private String numberFavContact = "";

    private final static String[] FROM_COLUMNS = {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private final static int[] TO_IDS = {
            android.R.id.text1,
            android.R.id.text2
    };

    public static ContactsFragment newInstance() {
        Bundle args = new Bundle();

        ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, rootView);

        getLoaderManager().initLoader(0, null, this);

        cursorAdapter = new SimpleCursorAdapter(
                Objects.requireNonNull(getActivity()),
                R.layout.contacts_item,
                null,
                FROM_COLUMNS,
                TO_IDS,
                0);

        contactsListView.setAdapter(cursorAdapter);
        contactsListView.setOnItemClickListener((adapterView, view, i, l) -> {
            view.setSelected(true);

            CursorAdapter cursorAdapter = (CursorAdapter) adapterView.getAdapter();
            Cursor cursor = cursorAdapter.getCursor();
            cursor.moveToPosition(i);
            nameFavContact = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            numberFavContact = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            AppSharedPreferences.savePrefContactName(getContext(), nameFavContact);
            AppSharedPreferences.savePrefContactNumber(getContext(), numberFavContact);
        });
        return rootView;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(
                Objects.requireNonNull(getActivity()),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
