package co.gounplugged.unpluggeddroid.models;

import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by pili on 20/03/15.
 */
@DatabaseTable(tableName = "masks")
public class Contact extends Mask {
    public static final String DEFAULT_CONTACT_NUMBER = "3016864576";
    public static final String DEFAULT_COUNTRY_CODE = "+1";

    public String getName() {
        return name;
    }

    private String name;

    public Contact(String name, String phoneNumber, String countryCode) {
        super(phoneNumber, countryCode);
        this.name = name;
    }
}
