package com.fekracomputers.islamiclibrary.homeScreen.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.fekracomputers.islamiclibrary.R;
import com.fekracomputers.islamiclibrary.homeScreen.controller.BookCollectionsController;
import com.fekracomputers.islamiclibrary.homeScreen.adapters.CollectionRecyclerViewAdapter;
import com.fekracomputers.islamiclibrary.model.BookCollectionInfo;
import com.fekracomputers.islamiclibrary.model.BooksCollection;
import com.fekracomputers.islamiclibrary.utility.Util;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Created by Mohammad on 1/11/2017.
 */

public class CollectionEditDialogFragmnet extends DialogFragment {
    public static final String TAG_FRAGMENT_COLLECTION = "CollectionEditDialogFragmnet";
    private static final String KEY_COLLECTION_IDS = "KEY_COLLECTION_IDS";
    private BookCollectionInfo bookCollectionInfo;
    private CollectionDialogFragmnetListener listener;
    private ArrayList<BooksCollection> bookCollections;
    private BookCollectionsController bookCollectionsController;
    private HashSet<Integer> oldBookIdCollectionSet;
    private BookCollectionsController.BookCollectionsControllerCallback collectionsControllerCallback;

    public static CollectionEditDialogFragmnet newInstance(BookCollectionInfo bookCollectionInfo) {
        CollectionEditDialogFragmnet frag = new CollectionEditDialogFragmnet();
        Bundle args = new Bundle();
        args.putIntegerArrayList(KEY_COLLECTION_IDS, new ArrayList<>(bookCollectionInfo.getBooksCollectionIds()));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        Bundle arguments = getArguments();
        ArrayList<Integer> collectionsIdsArrayList = arguments.getIntegerArrayList(KEY_COLLECTION_IDS);
        oldBookIdCollectionSet = collectionsIdsArrayList == null ?
                new HashSet<>(0)
                : new HashSet<>(collectionsIdsArrayList);
        bookCollectionsController = new BookCollectionsController(getContext(), collectionsControllerCallback);
        bookCollections = bookCollectionsController.getAllBookCollections(getContext(), true, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.collection_fragment_dialog, container, false);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CollectionDialogFragmnetListener) {
            listener = (CollectionDialogFragmnetListener) parentFragment;
        } else {
            throw new RuntimeException(parentFragment.toString()
                    + " must implement CollectionDialogFragmnetListener");
        }

        RecyclerView collectionRecyclerView = rootView.findViewById(R.id.collection_recycler_view);
        CollectionRecyclerViewAdapter adapter = new CollectionRecyclerViewAdapter();
        adapter.setHasStableIds(true);
        collectionRecyclerView.setHasFixedSize(true);
        collectionRecyclerView.setAdapter(adapter);
        collectionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,
                false));
        Button okButton = rootView.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(v -> {
            listener.collectionChanged(bookCollectionInfo);
            bookCollectionsController.updateCollectionStatus(bookCollectionInfo, oldBookIdCollectionSet);
            dismiss();
        });
        EditText newCollectionName = rootView.findViewById(R.id.new_collection_edit_text);
        ImageButton addCollectionButton = rootView.findViewById(R.id.add_collection);
        addCollectionButton.setOnClickListener(v ->
        {
            bookCollectionsController.createNewCollection(newCollectionName.getText().toString());
            bookCollections = bookCollectionsController.getAllBookCollections(getContext(), true, true);
            adapter.notifyDataSetChanged();
            collectionRecyclerView.scrollToPosition(bookCollections.size() - 1);
            newCollectionName.setText("");
        });

        boolean enabled = !newCollectionName.getText().toString().isEmpty();
        setEnabledAddButton(addCollectionButton, enabled);
        //addCollectionButton.setClickable(!newCollectionName.getText().toString().isEmpty());
        newCollectionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    setEnabledAddButton(addCollectionButton, true);
                } else {
                    setEnabledAddButton(addCollectionButton, false);
                }
            }
        });

        Button cancelButton = rootView.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        return rootView;
    }

    private void setEnabledAddButton(ImageButton addCollectionButton, boolean enabled) {
        Util.setImageButtonEnabled(getContext(),
                enabled,
                addCollectionButton, R.drawable.ic_add_black_24dp);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookCollectionsController.BookCollectionsControllerCallback) {
            collectionsControllerCallback =
                    ((BookCollectionsController.BookCollectionsControllerCallback) context);

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookCollectionsController.BookCollectionsControllerCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof BookCollectionsController.BookCollectionsControllerCallback) {
            collectionsControllerCallback = null;
        }
    }

    public interface CollectionDialogFragmnetListener {
        void collectionChanged(BookCollectionInfo bookCollectionInfo);
    }


}