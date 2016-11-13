package com.enroutetechnologies.enroute;

import com.yelp.clientlib.entities.Coordinate;
import com.yelp.clientlib.entities.Location;

import java.util.ArrayList;

/**
 * Created by SadruddinHashmani on 2016-11-12.
 */

public class PointOfInterest {

    String id;
    String name;
    String phoneNumber;
    String imageURL;
    Boolean isClosed;
    Double rating;
    Location location;
    Coordinate coordinate;

    PointOfInterest(String idArg, String nameArg, String phoneNumberArg, String imageURLArg, Boolean isClosedArg,
                    Location locationArg, Double ratingArg) {
        id = idArg;
        name = nameArg;
        phoneNumber = phoneNumberArg;
        imageURL = imageURLArg;
        isClosed = isClosedArg;
        location = locationArg;
        coordinate = location.coordinate();
        rating = ratingArg;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImageURL() {
        return imageURL;
    }

    public boolean getClosed() {
        return isClosed;
    }

    public ArrayList getAddress() {
        return location.displayAddress();
    }

    public double getLatitude() {
        return coordinate.latitude();
    }

    public double getLongitude() {
        return coordinate.longitude();
    }

    public double getRating() {
        return rating;
    }

}
