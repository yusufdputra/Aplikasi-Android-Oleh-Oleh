package com.ysf.oleholeh.ui.helper;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class HitungJarakWaktuMaps extends AppCompatActivity implements RoutingListener {
    private LatLng start, end;
    private TextView tv_ongkir;

    public void HitungJarakWaktuMaps(LatLng start, LatLng end, TextView tv_ongkir) {
       this.start = start;
       this.end = end;
       this.tv_ongkir = tv_ongkir;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .waypoints(start, end)
                .alternativeRoutes(false)
                .key("AIzaSyDq6StqP1C9Lgqk9g-EmhBd8XP14_YcDlY")
                .build();
        routing.execute();
    }



    @Override
    public void onRoutingFailure(RouteException e) {
        Log.i("TAG", "berhasil "+e);
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int i) {
        double jarak = 0, waktu = 0;
        for (int j = 0; j < routes.size(); j++) {
            tv_ongkir.setText("JARAK "+routes.get(j).getDistanceValue()  + " waktu "+waktu);
        }

        Log.i("TAG", "berhasil "+routes);
    }

    @Override
    public void onRoutingCancelled() {

    }

}
