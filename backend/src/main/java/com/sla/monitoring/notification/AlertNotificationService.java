package com.sla.monitoring.notification;

/**
 * Dispatches alert notifications through configured channels.
 */
public interface AlertNotificationService {

    /**
     * Sends email and/or WebSocket notifications for the given alert.
     *
     * @param alertId persisted alert identifier
     */
    void dispatch(Long alertId);
}
