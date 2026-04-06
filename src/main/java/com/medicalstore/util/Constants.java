package com.medicalstore.util;

public class Constants {
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    
    // Dashboard Stats (mock values)
    public static final int TOTAL_MEDICINES = 120;
    public static final int AVAILABLE_STOCKS = 350;
    public static final int TODAY_SALES = 45;
    public static final int PENDING_PURCHASE = 8;
    
    // Excel Export
    public static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String EXCEL_FILENAME = "DhruvitMedical.xlsx";
    
    // Session Attributes
    public static final String USER_SESSION_KEY = "user";
    public static final String USER_ROLE_KEY = "userRole";

    // Admin-only paths
    public static final String[] ADMIN_PATHS = {
        "/api/backup", "/api/medicine/delete"
    };
}