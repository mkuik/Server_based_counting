package dev.kuik.matthijs.serverbasedcounting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matthijs Kuik on 4-12-2015.
 */
public class User {

    String name;
    Integer id;
    boolean edit_rights = false;
    boolean admin_rights = false;

    User( final String name ) {
        this.name = name;
    }

    User( final String name, final Integer id ) {
        this.name = name;
        this.id = id;
    }

    User( final JSONObject json ) throws JSONException {
        id = json.getInt("id");
        name = json.getString("name");
        edit_rights = json.getBoolean("edit");
        admin_rights = json.getBoolean("admin");
    }

    User( Context context ) {
        this.name = getUsername(context);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        if (id != null) json.put("id", id);
        json.put("name", name);
        json.put("edit", isEditor());
        json.put("admin", isAdmīn());
        return json;
    }

    public int getID() {
        return id;
    }

    public void setID(final Integer id ) { this.id = id; }

    public String getName() { return name; }

    public boolean isEditor() {
        return edit_rights;
    }

    public boolean isAdmīn() {
        return admin_rights;
    }

    public void setEditorRights(boolean edit_rights) {
        this.edit_rights = edit_rights;
    }

    public void setAdminRights(boolean admin_rights) {
        this.admin_rights = admin_rights;
    }


    public static String getUsername(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");

            if (parts.length > 1) return parts[0];
        }
        return "";
    }
}
