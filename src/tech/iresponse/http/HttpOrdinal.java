package tech.iresponse.http;

class HttpOrdinal {

    static final int[] value = new int[HttpEnumeration.values().length];

        static {
        try {
            value[HttpEnumeration.POST.ordinal()] = 1;
        } catch (NoSuchFieldError v1) {}

        try {
            value[HttpEnumeration.PUT.ordinal()] = 2;
        } catch (NoSuchFieldError v2) {}

        try {
            value[HttpEnumeration.DELETE.ordinal()] = 3;
        } catch (NoSuchFieldError v3) {}

    }
}

