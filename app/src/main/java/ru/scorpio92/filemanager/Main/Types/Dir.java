package ru.scorpio92.filemanager.Main.Types;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by scorpio92 on 11.08.16.
 */
public class Dir {

    private String path;
    private ArrayList<Object> objects;
    private ArrayList<Integer> selectedObjects;
    private boolean selectAll;

    private String path_buf;
    public ArrayList<Object> objects_buf;

    public Dir(String path) {
        setNewContent(path);
    }

    public Dir(String path, ArrayList<Object> objects) {
        setNewContent(path, objects);
    }

    public void setNewContent(String path) {
        clear();
        this.path = path;
    }

    public void setNewContent(String path, ArrayList<Object> objects) {
        clear();
        this.path = path;
        this.objects = objects;
    }

    public void clear() {
        objects = new ArrayList<Object>();
        selectedObjects = new ArrayList<Integer>();
        selectAll = false;
    }

    //сортировка
    public Boolean sortByName(boolean folderFirst, boolean reverse) {
        ArrayList<Object> dirs, files;

        dirs = sortDirsByName(reverse);
        files = sortFilesByName(reverse);

        if(!dirs.isEmpty() && !files.isEmpty()) {
            objects.clear();
            if (folderFirst) {
                objects.addAll(dirs);
                objects.addAll(files);
                return true;
            } else {
                objects.addAll(files);
                objects.addAll(dirs);
                return true;
            }
        }

        return false;
    }

    public Boolean sortByDate(boolean reverse) { //reverse == false, then: old -> new
        ArrayList<Object> objects_tmp = new ArrayList<Object>();

        ArrayList<String> dates = new ArrayList<String>();
        int id = 0; //инкремент, ID даты в оригинальном массиве
        for (Object o:objects) {
            dates.add(o.date + ";" + Integer.toString(id)); // прибавляем ID к дате, в конец (на сортировку это не влияет, зато позволит после сортировки вытащить нужный объект используя путь найденный по ID)
            id++;
        }

        Collections.sort(dates);
        if (reverse)
            Collections.reverse(dates);

        for (String date : dates) {
            String[] mas = date.split(";");
            int originalObjID = Integer.parseInt(mas[1]); //берем дату из отсортированного массива и вырезаем последний символ с конца - это ID
            objects_tmp.add(objects.get(originalObjID)); //получаем объект по пути
        }

        if(!objects_tmp.isEmpty()) {
            objects.clear();
            objects.addAll(objects_tmp);
            return true;
        }

        return false;
    }

    public Boolean sortBySize(boolean reverse) {
        ArrayList<Object> objects_tmp = new ArrayList<Object>();
        ArrayList<Object> objects_buf = new ArrayList<Object>(objects);

        ArrayList<Long> sizes = new ArrayList<Long>();
        for (Object o:objects) {
            sizes.add(o.size);
        }

        Collections.sort(sizes);
        if (reverse)
            Collections.reverse(sizes);


        for (Long s : sizes) {
            objects_tmp.add(getObjectBySize(s));
        }

        if(!objects_tmp.isEmpty()) {
            objects.clear();
            objects.addAll(objects_tmp);
            return true;
        } else {
            objects.clear();
            objects.addAll(objects_buf);
        }


        return false;
    }


    private ArrayList<Object> sortDirsByName(boolean reverse) {
        ArrayList<Object> objects_tmp = new ArrayList<Object>();

        ArrayList<String> dirPaths = new ArrayList<String>();
        for (Object o:objects) {
            if(o.type.equals(Object.TYPE_DIR) || o.type.equals(Object.TYPE_SYMLINK_DIR))
                dirPaths.add(o.path);
        }

        Collections.sort(dirPaths);
        if (reverse)
            Collections.reverse(dirPaths);

        for (String dirPath : dirPaths) {
            objects_tmp.add(getObjectByPath(dirPath));
        }

        return objects_tmp;
    }

    private ArrayList<Object> sortFilesByName(boolean reverse) {
        ArrayList<Object> objects_tmp = new ArrayList<Object>();

        ArrayList<String> filePaths = new ArrayList<String>();
        for (Object o:objects) {
            if(o.type.equals(Object.TYPE_FILE) || o.type.equals(Object.TYPE_SYMLINK_FILE))
                filePaths.add(o.path);
        }

        Collections.sort(filePaths);
        if (reverse)
            Collections.reverse(filePaths);

        for (String filePath : filePaths) {
            objects_tmp.add(getObjectByPath(filePath));
        }

        return objects_tmp;
    }

    private Object getObjectByPath(String path) {
        for (Object o: objects) {
            if (o.path.equals(path))
                return o;
        }
        return null;
    }

    private Object getObjectBySize(long size) {
        for (Object o: objects) {
            if (o.size == size) {
                objects.remove(o);
                return o;
            }
        }
        return null;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setObjects(ArrayList<Object> objects) {
        this.objects = objects;
    }

    public ArrayList<Object> getObjects() {
        return objects;
    }

    public void setSelectedObjects(ArrayList<Integer> selectedObjects) {
        this.selectedObjects = selectedObjects;
    }

    public ArrayList<Integer> getSelectedObjects() {
        return selectedObjects;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setObjectsToBuffer() {
        try {
            path_buf = path;
            objects_buf = new ArrayList<Object>(objects);
        } catch (Exception e) {
            Log.e("setObjectsToBuffer", null, e);
        }
    }

    public void returnObjectsFromBuffer() {
        try {
            path = path_buf;
            objects = new ArrayList<Object>(objects_buf);
        } catch (Exception e) {
            Log.e("returnObjectsFromBuffer", null, e);
        }
    }

    public ArrayList<String> getSelectedObjectsPaths() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            for (int i:selectedObjects) {
                list.add(objects.get(i).path);
            }
        } catch (Exception e) {
            Log.e("getSelectedObjectsPaths", null, e);
        }
        return list;
    }
}
