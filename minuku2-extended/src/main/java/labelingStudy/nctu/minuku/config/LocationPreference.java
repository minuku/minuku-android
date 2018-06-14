/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku.config;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neerajkumar on 8/23.
 */
public class LocationPreference {

    private List<SelectedLocation> selectedLocationList =
            new ArrayList<>();
    private static LocationPreference instance;

    private LocationPreference() {
        this.selectedLocationList = getSelectedLocationsFromPersistedStorage();
    }

    public static LocationPreference getInstance() {
        if(instance == null) {
            instance = new LocationPreference();
        }
        return instance;
    }

    public List<SelectedLocation> getLocations() {
        return new ArrayList<>(this.selectedLocationList);
    }

    public void deleteLocation(int itemPosition) {
        selectedLocationList.remove(itemPosition);
        persistSelectedLocations();
    }

    public void addLocation(SelectedLocation location) {
        selectedLocationList.add(location);
        persistSelectedLocations();
    }

    public boolean isEmpty() {
        if(selectedLocationList.size()==0)
            return true;
        return false;
    }

    public void updateMapMarkers(GoogleMap googleMap) {

        LatLng sydney = new LatLng(-33.867, 151.206);

        //TODO: iterate through the location list and add markers
        if(!this.isEmpty()) {
            for (SelectedLocation location : selectedLocationList) {
                LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(coordinates)
                        .title(location.getPlace()));
            }
        }
        else {
            googleMap.addMarker(new MarkerOptions()
                    .position(sydney)
                    .title("Sydney"));
        }
    }

    private List<SelectedLocation> getSelectedLocationsFromPersistedStorage() {
        Type typeOfListObject = new TypeToken<List<SelectedLocation>>(){}.getType();
        Gson gson = new Gson();
        String serializedLocationList = UserPreferences.getInstance()
                .getPreference(Constants.SELECTED_LOCATIONS);
        if(serializedLocationList == null) {
            return new ArrayList<>();
        }
        return gson.fromJson(serializedLocationList, typeOfListObject);
    }

    private void persistSelectedLocations() {
        Type typeOfListObject = new TypeToken<List<SelectedLocation>>(){}.getType();
        Gson gson = new Gson();
        UserPreferences.getInstance().writePreference(Constants.SELECTED_LOCATIONS,
                gson.toJson(selectedLocationList, typeOfListObject));
    }
}
