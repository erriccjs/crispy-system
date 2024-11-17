package com.myproject.crispysystem.constants;

public class Constants {
    private static final String ENCRYPTION_SECRET = "ZuW0XhTAJKIq/YyhphrMCJJvag6fF3ykrZ/0X0ZrsCM=";
    private static final String JWT_SECRET = "dEhkc3lEaWNoUmRQSFBtUTBsRFRYNFQ0RDRWMVdMWng=";
    private static final long JWT_EXPIRY = 86400000;

    public static String getEncryptionSecret() {
        return ENCRYPTION_SECRET;
    }

    public static String getJwtSecret(){
        return JWT_SECRET;
    }

    public static long getJwtExpiry(){
        return JWT_EXPIRY;
    }
}
