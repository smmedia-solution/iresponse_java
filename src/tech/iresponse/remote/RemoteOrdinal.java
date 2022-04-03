package tech.iresponse.remote;

class RemoteOrdinal {

    static final int[] value = new int[RemoteEnumeration.values().length];

    static {
        try {
            value[RemoteEnumeration.userpass.ordinal()] = 1;
        } catch (NoSuchFieldError var1) {}

        try {
            value[RemoteEnumeration.pem.ordinal()] = 2;
        } catch (NoSuchFieldError var2) {}

        try {
            value[RemoteEnumeration.rsa.ordinal()] = 3;
        } catch (NoSuchFieldError var3) {}

    }
}

