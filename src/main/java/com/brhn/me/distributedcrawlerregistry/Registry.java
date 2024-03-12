package com.brhn.me.distributedcrawlerregistry;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Registry {

    private final Map<String, CrawlerNode> nodes = new ConcurrentHashMap<>();
    private CrawlerNode masterNode = null;

    @PostMapping("/register")
    public ResponseEntity<?> registerNode(@RequestBody CrawlerNode node) {
        LocalDateTime now = LocalDateTime.now();
        if (!nodes.containsKey(node.getNodeId())) {
            node.setEntryTime(now);
        }
        node.setLastHeartbeat(now);
        nodes.put(node.getNodeId(), node);
        electLeader();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deregister")
    public ResponseEntity<?> deregisterNode(@RequestBody String nodeId) {
        nodes.remove(nodeId);
        electLeader();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/discover")
    public ResponseEntity<List<CrawlerNode>> discoverNodes() {
        return ResponseEntity.ok(new ArrayList<>(nodes.values()));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> updateHeartbeat(@RequestBody String nodeId) {
        CrawlerNode node = nodes.get(nodeId);
        if (node != null) {
            node.setLastHeartbeat(LocalDateTime.now());
            String masterNodeId = "";
            if(this.masterNode != null){

            }
            return ResponseEntity.ok().build();
        }
        electLeader();
        return ResponseEntity.notFound().build();
    }

    @Scheduled(fixedRate = 5000)
    public void removeStaleNodes() {
        LocalDateTime cutoff = LocalDateTime.now().minus(60, ChronoUnit.SECONDS);
        nodes.entrySet().removeIf(entry -> entry.getValue().getLastHeartbeat().isBefore(cutoff));
        electLeader();
    }

    private void electLeader() {
        // find the current leader, if any
        CrawlerNode currentLeader = nodes.values().stream().filter(CrawlerNode::isLeader).findFirst().orElse(null);

        // Check if the current leader's last heartbeat is more than 30 seconds ago
        if (currentLeader != null && ChronoUnit.SECONDS.between(currentLeader.getLastHeartbeat(), LocalDateTime.now()) <= 30) {
            // If there's a leader and their last heartbeat was within the last 30 seconds, no need to elect a new leader
            return;
        }

        // If no leader is found, or the leader's last heartbeat is too old, clear the current leader and elect a new one
        if (currentLeader != null) {
            currentLeader.setLeader(false); // Reset the leader flag for the current leader
        }

        // Elect the oldest node (by entry time) as the new leader
        CrawlerNode oldestNode = nodes.values().stream().min(Comparator.comparing(CrawlerNode::getEntryTime)).orElse(null);

        if (oldestNode != null) {
            oldestNode.setLeader(true);
            this.masterNode = oldestNode;
        }
    }

}
