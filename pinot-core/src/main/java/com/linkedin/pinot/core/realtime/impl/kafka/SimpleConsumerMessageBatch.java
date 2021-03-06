/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.pinot.core.realtime.impl.kafka;

import java.util.ArrayList;
import kafka.message.MessageAndOffset;


public class SimpleConsumerMessageBatch implements MessageBatch<byte[]>{

  private ArrayList<MessageAndOffset> messageList = new ArrayList<>();

  public SimpleConsumerMessageBatch(Iterable<MessageAndOffset> messageAndOffsetIterable) {
    for (MessageAndOffset messageAndOffset : messageAndOffsetIterable) {
      messageList.add(messageAndOffset);
    }
  }

  public int getMessageCount() {
    return messageList.size();
  }

  public byte[] getMessageAtIndex(int index) {
    return messageList.get(index).message().payload().array();
  }

  public int getMessageOffsetAtIndex(int index) {
    return messageList.get(index).message().payload().arrayOffset();
  }

  public int getMessageLengthAtIndex(int index) {
    return messageList.get(index).message().payloadSize();
  }

  public long getNextKafkaMessageOffsetAtIndex(int index) {
    return messageList.get(index).nextOffset();
  }
}
