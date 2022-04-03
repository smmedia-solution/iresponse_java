package tech.iresponse.utils;

import java.lang.reflect.Field;
import org.apache.commons.lang.ArrayUtils;
import tech.iresponse.orm.Column;

public class Inspector {

    public static String[] classFields(Object objct) {
        String[] fields = new String[]{};
        if (objct != null) {
            Field[] declaredFields = objct.getClass().getDeclaredFields();
            for (Field field : declaredFields){
                fields = (String[])ArrayUtils.add((Object[])fields, field.getName());
            }
        }
        return fields;
    }

    public static Column columnMeta(Object objct, String columnName) throws Exception {
        if (objct != null) {
            Field field = objct.getClass().getDeclaredField(columnName);
            Column[] annotations = field.<Column>getAnnotationsByType(Column.class);
            if (annotations.length > 0){
                return annotations[0];
            }
        }
        return null;
    }
}
