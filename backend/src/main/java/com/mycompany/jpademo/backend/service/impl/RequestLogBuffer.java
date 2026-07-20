package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.internal.RequestLogEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class RequestLogBuffer {
    private final BlockingDeque<RequestLogEvent> queue = new LinkedBlockingDeque<>(50_000);

    public boolean offer(RequestLogEvent event) {
        return queue.offer(event);
    }

    public List<RequestLogEvent> drain(int maxItems) {
        List<RequestLogEvent> batch = new ArrayList<>(maxItems);
        queue.drainTo(batch, maxItems);
        return batch;
    }

    public int size() {
        return queue.size();
    }
}
