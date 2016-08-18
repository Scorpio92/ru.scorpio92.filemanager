package ru.scorpio92.filemanager.Main.Types;

/**
 * Created by scorpio92 on 12.03.16.
 */
public class ObjectProperties {

    private String type;
    private String name;
    private String changeTime;
    private String owner;
    private String group;
    private String permissions;
    private String size;
    private String hash;

    public ObjectProperties() {
        this.type="?";
        this.name="?";
        this.changeTime="?";
        this.owner="?";
        this.group="?";
        this.permissions="?";
        this.size="?";
        this.hash="?";
    }

    public ObjectProperties(String type, String name, String changeTime, String owner, String group, String permissions, String size, String hash) {
        this.type=type;
        this.name=name;
        this.changeTime=changeTime;
        this.owner=owner;
        this.group=group;
        this.permissions=permissions;
        this.size=size;
        this.hash=hash;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getChangeTime() {
        return changeTime;
    }

    public String getOwner() {
        return owner;
    }

    public String getGroup() {
        return group;
    }

    public String getPermissions() {
        return permissions;
    }

    public String getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }
}
