package sg.edu.rp.c346.smsretriever;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentSecond extends Fragment {
    Button btnRetrieve;
    EditText etWord;
    TextView tvResults;
    String [] array;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_second, container, false);

        btnRetrieve = view.findViewById(R.id.btnRetrieve);
        etWord = view.findViewById(R.id.etWord);
        tvResults = view.findViewById(R.id.tvResults);

        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = etWord.getText().toString();
                if (word.length() == 0) {
                    Toast.makeText(getContext(), "Please enter a keyword", Toast.LENGTH_LONG).show();
                } else {
                    array = word.split("\\s+");
                    int permissionCheck = PermissionChecker.checkSelfPermission
                            (getContext(), Manifest.permission.READ_SMS);
                    if (permissionCheck != PermissionChecker.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_SMS}, 0);
                        return;
                    }

                    Uri uri = Uri.parse("content://sms");
                    String[] reqCols = new String[]{"date", "address", "body", "type"};
                    ContentResolver cr = getActivity().getContentResolver();
                    String[] filterArgs = new String[array.length];
                    String filter="body LIKE ?";
                    for (int i = 0; i < array.length; i++) {
                        filterArgs[i] = "%" + array[i] + "%";
                        filter += " OR body LIKE ?";
                    }
                    Cursor cursor = cr.query(uri, reqCols, filter, filterArgs, null);
                    String smsBody = "";
                    if (cursor.moveToFirst()) {
                        do {
                            long dateInMillis = cursor.getLong(0);
                            String date = (String) DateFormat
                                    .format("dd MMM yyyy h:mm:ss aa", dateInMillis);
                            String address = cursor.getString(1);
                            String body = cursor.getString(2);
                            String type = cursor.getString(3);
                            if (type.equalsIgnoreCase("1")) {
                                type = "Inbox:";
                            } else {
                                type = "Sent:";
                            }
                            smsBody += type + " " + address + "\n at " + date
                                    + "\n\"" + body + "\"\n\n";
                        } while (cursor.moveToNext());
                    }
                    tvResults.setText(smsBody);
                }
            }
        });

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnRetrieve.performClick();

                } else {
                    Toast.makeText(getContext(), "Permission not granted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

