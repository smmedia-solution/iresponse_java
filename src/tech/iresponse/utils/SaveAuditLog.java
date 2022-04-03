package tech.iresponse.utils;

import java.sql.Timestamp;

import tech.iresponse.models.admin.AuditLog;
import tech.iresponse.core.Application;

public class SaveAuditLog {

    public static void insertAuditLog(int recordId, String recordName, String recordType, String actionType) throws Exception {
        AuditLog log = new AuditLog();
        log.recordId = recordId;
        log.recordName = recordName;
        log.recordType = recordType;
        log.actionType = actionType;
        log.actionTime = new Timestamp(System.currentTimeMillis());
        log.actionBy = Application.checkUser();
        log.insert();
    }
}

