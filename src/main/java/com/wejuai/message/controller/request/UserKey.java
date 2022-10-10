package com.wejuai.message.controller.request;

import com.endofmaster.rest.exception.BadRequestException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ZM.Wang
 */
public class UserKey {

    private final String id;
    private final String sessionId;

    public UserKey(String id, String sessionId) {
        if (StringUtils.isBlank(id) && StringUtils.isBlank(sessionId)) {
            throw new BadRequestException("连接session错误");
        }
        if (id == null) {
            this.id = "";
        } else {
            this.id = id;
        }
        if (sessionId == null) {
            this.sessionId = "";
        } else {
            this.sessionId = sessionId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserKey userKey = (UserKey) o;
        return id.equals(userKey.id) || sessionId.equals(userKey.sessionId);
    }

    @Override
    public int hashCode() {
        return "1".hashCode();
    }

    @Override
    public String toString() {
        return "UserKey{" +
                "id='" + id + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }
}
