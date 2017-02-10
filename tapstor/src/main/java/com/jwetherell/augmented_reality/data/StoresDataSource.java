package com.jwetherell.augmented_reality.data;

import com.jwetherell.augmented_reality.ui.Marker;

import org.json.JSONObject;

import java.util.List;

public class StoresDataSource extends NetworkDataSource {

    @Override
    public String createRequestURL(double lat, double lon, double alt,
                                   float radius, String locale) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Marker> parse(JSONObject root) {
        // TODO Auto-generated method stub
        return null;
    }
}
