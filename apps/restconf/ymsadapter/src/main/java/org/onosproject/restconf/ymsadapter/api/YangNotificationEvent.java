/*
 * Copyright 2016 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.restconf.ymsadapter.api;

import org.onosproject.event.AbstractEvent;

/**
 * YANG Notification Event.
 */
public class YangNotificationEvent extends AbstractEvent<YangNotificationEvent.Type, YangNotificationData> {
    /*
     * YANG Notification Event types. Each event type maps
     * to a specific event stream.
     */
    public enum Type {
        NETCONF_NOTIFICATION,
        YANG_NOTIFICATION,
        SYSLOG_EVENT,
        SNMP_EVENT
    }

    /**
     * Creates YangNotificationEvent object with specified type and subject.
     *
     * @param type
     * @param subject
     */
    public YangNotificationEvent(Type type, YangNotificationData subject) {
        super(type, subject);
    }

}
