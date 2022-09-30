package tech.iresponse.affiliate.workers;

import org.apache.commons.lang3.StringUtils;

public class PlaceHoldeNames {

    public static String remplace(String value) {
        value = value.contains("[FNAME]") ? StringUtils.replace(value, "[FNAME]", "[first_name]") : value;
        value = value.contains("{Firstname}") ? StringUtils.replace(value, "[FNAME]", "[first_name]") : value;
        value = value.contains("[FIRST NAME]") ? StringUtils.replace(value, "[FIRST NAME]", "[first_name]") : value;
        value = value.contains("%%FIRST_NAME%%") ? StringUtils.replace(value, "%%FIRST_NAME%%", "[first_name]") : value;
        value = value.contains("[$FIRST_NAME$]") ? StringUtils.replace(value, "[$FIRST_NAME$]", "[first_name]") : value;
        value = value.contains("%%TODAY_DATE%%") ? StringUtils.replace(value, "%%TODAY_DATE%%", "[mail_date]") : value;
        return value.contains("%%EMAIL_ADDRESS%%") ? StringUtils.replace(value, "%%EMAIL_ADDRESS%%", "[email]") : value;
    }
}

