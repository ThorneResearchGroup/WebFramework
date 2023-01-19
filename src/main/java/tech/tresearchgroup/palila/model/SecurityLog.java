package tech.tresearchgroup.palila.model;

import lombok.Data;
import tech.tresearchgroup.dao.model.BasicObjectInterface;
import tech.tresearchgroup.dao.model.LockType;

import java.util.Date;

@Data
public class SecurityLog implements BasicObjectInterface {
    private transient Date created;
    private transient Date updated;
    private Long id;
    private LockType lockType;
    private String action;
    private String targetClassName;
    private Long userId;
    private String apiKey;

    public SecurityLog(String action, String targetClassName, Long userId, String apiKey) {
        this.action = action;
        this.targetClassName = targetClassName;
        this.userId = userId;
        this.apiKey = apiKey;
    }
}
