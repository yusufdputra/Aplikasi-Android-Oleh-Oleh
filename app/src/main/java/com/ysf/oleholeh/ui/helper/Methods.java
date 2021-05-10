package com.ysf.oleholeh.ui.helper;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.ahmadrosid.svgloader.SvgLoader;
import com.android.volley.toolbox.HttpResponse;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.jpvs0101.currencyfy.Currencyfy;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ysf.oleholeh.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_LONG;

public class Methods  {

    public Methods() {
    }

    public void setImg(StorageReference stRef, ImageView iv_foto, String foto_path){
        //set image to layout
        stRef.child(String.valueOf(foto_path)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fetch(new Callback() {
                    @Override
                    public void onSuccess() {

                        iv_foto.setAlpha(0f);
                        Picasso.get()
                                .load(uri)
                                .placeholder(R.drawable.loading)
                                .fit().centerInside()
                                .into(iv_foto);
                        iv_foto.animate().setDuration(10).alpha(1f).start();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        });
    }

    public void setSVG(StorageReference stRef, ImageView iv_foto, String foto_path, FragmentActivity activity){
        stRef.child(String.valueOf(foto_path)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                SvgLoader.pluck()
                        .with(activity)
                        .setPlaceHolder(R.drawable.loading, R.drawable.loading)
                        .load(uri.toString(), iv_foto);
            }
        });
    }


}
