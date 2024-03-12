package com.brhn.me.distributedcrawlerregistry;

import java.time.LocalDateTime;

public class CrawlerNode {
    private String nodeId;
    private String address;
    private int port;
    private boolean leader;
    private LocalDateTime entryTime;

    private LocalDateTime lastHeartbeat;

    public CrawlerNode(String nodeId, String address, int port, boolean leader, LocalDateTime entryTime) {
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.leader = leader;
        this.entryTime = entryTime;
        this.lastHeartbeat = entryTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public String toString() {
        return "CrawlerNode{" +
                "nodeId='" + nodeId + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", leader=" + leader +
                ", entryTime=" + entryTime +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}