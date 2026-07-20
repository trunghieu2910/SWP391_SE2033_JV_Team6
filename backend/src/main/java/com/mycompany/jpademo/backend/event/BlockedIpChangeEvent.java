package com.mycompany.jpademo.backend.event;

import lombok.Getter;

@Getter
public class BlockedIpChangeEvent {
    private final String ipAddress;
    private final boolean blocked;

    public BlockedIpChangeEvent(String ipAddress, boolean blocked) {
        this.ipAddress = ipAddress;
        this.blocked = blocked;
    }
}
