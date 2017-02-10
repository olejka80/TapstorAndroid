package com.iproject.tapstor.rest.maps;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;


public class Places {
    public static final String ADMINISTRATIVE_AREA_LEVEL_5 = "administrative_area_level_5";
    public static final String ADMINISTRATIVE_AREA_LEVEL_4 = "administrative_area_level_4";
    public static final String ADMINISTRATIVE_AREA_LEVEL_3 = "administrative_area_level_3";
    public List<AddressComponents> address_components;

    @Nullable
    private AddressComponents containsValue(String value) {

        for (AddressComponents addressComponent : address_components) {
            if (addressComponent.types.contains(value))
                return addressComponent;
        }

        return null;
    }

    @NonNull
    public String getLongName() {
        AddressComponents addressComponent = containsValue(ADMINISTRATIVE_AREA_LEVEL_5);
        if (addressComponent != null) {
            return addressComponent.long_name;
        }
        addressComponent = containsValue(ADMINISTRATIVE_AREA_LEVEL_4);
        if (addressComponent != null) {
            return addressComponent.long_name;
        }

        addressComponent = containsValue(ADMINISTRATIVE_AREA_LEVEL_3);
        if (addressComponent != null) {
            return addressComponent.long_name;
        }
        return "";
    }

}
