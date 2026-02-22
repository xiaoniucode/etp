package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;

public interface ConnectionCallback {
    default void onSuccess(VisitorSession session, Target target) {
    }

    default void onFailure(VisitorSession session, Target target, Throwable cause) {
    }

    default void onNoTargetAvailable(VisitorSession session) {
    }

    default void onError(VisitorSession session, Throwable error) {
    }
}