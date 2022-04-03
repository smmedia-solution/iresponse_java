package tech.iresponse.registry;

import java.util.LinkedHashMap;
import java.util.List;

public class Packager {

    private static Packager instance;
    private LinkedHashMap registry;

    public static Packager getInstance() {
        if (instance == null){
            instance = new Packager();
        }
        return instance;
    }

    public void setRegistry(String paramString, Object paramObject) {
        instance.registry.put(paramString, paramObject);
    }

    public Object getRegistry(String paramString) {
        return checkInstance(paramString) ? instance.registry.get(paramString) : null;
    }

    public boolean checkInstance(String paramString) {
        return instance.registry.containsKey(paramString);
    }

    private Packager() {
        this.registry = new LinkedHashMap<>();
    }

    private Packager(List paramList) {
        this.registry = new LinkedHashMap<>();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("This object cound not be cloned !");
    }
}
