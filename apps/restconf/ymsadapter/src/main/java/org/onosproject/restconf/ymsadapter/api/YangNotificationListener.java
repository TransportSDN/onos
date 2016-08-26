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

import org.onosproject.event.EventListener;

/**
 * The YANG Notification listener interface. The actually implementation is
 * in the YMS Adapter. When YMS sends notifications to YMS Adapter, the listener
 * will be invoked.
 */

public interface YangNotificationListener extends EventListener<YangNotificationEvent> {

}
