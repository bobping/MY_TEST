package isaphttpclienttest;

import java.util.ResourceBundle;

public class IsapModuleCode {
    private static final ResourceBundle res = ResourceBundle.getBundle("isap.module");

    public static String getValue(String key) {
        return res.getString(key);
    }

    private static final String ISAP_DOMAIN_PRODUCT = getValue("DOMAIN") + getValue("PRODUCT_CODE");

    public static final String ISAP_NAI_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_NAI") + "000";
    public static final String ISAP_EVT_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_EVT") + "000";
    public static final String ISAP_NPI_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_NPI") + "000";
    public static final String ISAP_ODC_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_ODC") + "000";
    public static final String ISAP_WEB_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_WEB") + "000";
    public static final String ISAP_WEB_PROVIDER_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_WEB_PROVIDER") + "000";
    public static final String ISAP_PERSISTENCE_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_PERSISTENCE") + "000";
    public static final String ISAP_ASYNC_SERV_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_ASYNC_SERV") + "000";
    public static final String ISAP_ARCHIVER_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_ARCHIVER") + "000";
    public static final String ISAP_SPE_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_SPE") + "000";
    public static final String ISAP_FILE_READER_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_FILE_READER_CODE") + "000";
    public static final String ISAP_NPI_QUERY_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_NPI_QUERY") + "000";
    public static final String ISAP_ODC_JOB_CODE = ISAP_DOMAIN_PRODUCT + getValue("ISAP_ODC_JOB") + "000";
    public static final String ISAP_COMMON_CODE = ISAP_DOMAIN_PRODUCT + "15000";
}
