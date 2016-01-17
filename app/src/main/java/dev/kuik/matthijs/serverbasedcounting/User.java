package dev.kuik.matthijs.serverbasedcounting;

import org.json.JSONException;
import org.json.JSONObject;

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
}
