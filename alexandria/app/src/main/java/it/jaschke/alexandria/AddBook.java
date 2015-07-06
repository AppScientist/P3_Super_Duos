package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT = "eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";


    public AddBook() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Abhilash:Removed configChanges:"orientation" from manifest file.I believe it is not recommended practice.
        //Added setRetainInstance(true) instead
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }

                //Abhilash: am fixing number 2 and number 3 of the UX issues listed.
                // Number 2 :If the there is no connectivity intent service will crash,so a check is put in place.
                //Number 3:Wrong ISBN or less than 13 digit number wud result in Edit text field being cleard.
                //Removed the function which checks if entered value is less than 13 and clears the edit text field.
                //Instead added a check point to see if entered value is equal to 13 digits then only start intent service.
                //If wrong ISBN number is entered ,already a LocalBroadcastManager is implemented which would show the Toast "No Book Found".

                if (isConnected() && ean.length() == 13) {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                    restartLoader();
                } else if (!isConnected()) {
                    Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abhilash:Solving issue number 1.
                //Added barcode functionality without the use of external app.
                initiateScan();
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Abhilash: Extra changes.Once "Next button is clicked, book gets added in Database.But the item is still listed on Screen.
                //So i am clearing the fields and sending a toast to acknowledge book added
                clearScreen("Add");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Abhilash: The button is named "Cancel", but the operation provided here is to delete the book from DataBaase
                //Changed to simple clear screen.
//                    Intent bookIntent = new Intent(getActivity(), BookService.class);
//                    bookIntent.putExtra(BookService.EAN, ean.getText().toString());
//                    bookIntent.setAction(BookService.DELETE_BOOK);
//                    getActivity().startService(bookIntent);
                clearScreen("cancel");
            }

        });

        //Abhilash:No need to save instance state here.It is automattcally taken care.
//        if (savedInstanceState != null) {
//            ean.setText(savedInstanceState.getString(EAN_CONTENT));
//            ean.setHint("");
//        }

        return rootView;
    }

    //Abhilash:Function to clear screen
    void clearScreen(String input) {
        ean.setText("");
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");

        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");

        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);

        ((TextView) rootView.findViewById(R.id.categories)).setText("");

        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);

        if (input.equals("Add")) {
            Toast.makeText(getActivity(), getString(R.string.book_added), Toast.LENGTH_SHORT).show();
        }
    }

    //Abhilash:Function which starts the Camera to scan
    void initiateScan() {
        IntentIntegrator.forSupportFragment(this).initiateScan();
    }

    //Abhilash:After Scanning is complete,the result would come back in the form of an intent in this function.
    //Also Checking for connectivity applied.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String scanResult = result.getContents();
        if (isConnected() && scanResult != null) {
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, scanResult);
            bookIntent.setAction(BookService.FETCH_BOOK);
            getActivity().startService(bookIntent);
            ean.setText(scanResult);
            restartLoader();
        } else {
            Toast.makeText(getActivity(), getString(R.string.scan_failed), Toast.LENGTH_SHORT).show();
        }
    }

    //Abhilash:Function to check for network connectivity
    boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
