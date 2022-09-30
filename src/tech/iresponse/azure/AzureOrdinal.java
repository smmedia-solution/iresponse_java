package tech.iresponse.azure;

import tech.iresponse.http.HttpEnumeration;

class AzureOrdinal {

     static final int[] value = new int[HttpEnumeration.values().length];
     static {
            try {
                value[HttpEnumeration.GET.ordinal()] = 1;
            }
            catch (NoSuchFieldError var1) {}
            try {
                value[HttpEnumeration.POST.ordinal()] = 2;
            }
            catch (NoSuchFieldError var2) {}
            try {
                value[HttpEnumeration.PUT.ordinal()] = 3;
            }
            catch (NoSuchFieldError nvar3) {}
            try {
                value[HttpEnumeration.DELETE.ordinal()] = 4;
            }
            catch (NoSuchFieldError var4) {}
     }
}
