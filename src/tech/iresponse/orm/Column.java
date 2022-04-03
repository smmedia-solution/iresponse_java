package tech.iresponse.orm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

        String name() default ""; //do

        boolean autoincrement() default false; //if

        boolean primary() default false; //for

        String type() default "integer"; //int

        boolean nullable() default false; //new

        boolean indexed() default false; //try

        boolean unique() default false; //byte

        int length() default 0; //case
}
