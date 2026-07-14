package com.example.DiagnosIQ.entity;

import java.util.Date;

public class AuditLog {
    private  Integer logId ;
    private Integer userId ;
    private  String action;
    private String entityAffected;
    private Date timestamp;
}
