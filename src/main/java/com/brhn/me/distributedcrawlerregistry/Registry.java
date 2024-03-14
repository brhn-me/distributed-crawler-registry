package com.brhn.me.distributedcrawlerregistry;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Registry {
    private static final Logger logger = LoggerFactory.getLogger(Registry.class);
    private final Map<String, CrawlerNode> nodes = new ConcurrentHashMap<>();
    private CrawlerNode masterNode = null;

    @PostMapping("/register")
    public ResponseEntity<?> registerNode(@RequestBody CrawlerNode node) {
        logger.info("Registering node with ID: {}", node.getNodeId());
        LocalDateTime now = LocalDateTime.now();
        if (!nodes.containsKey(node.getNodeId())) {
            node.setEntryTime(now);
        }
        node.setLastHeartbeat(now);
        nodes.put(node.getNodeId(), node);
        electLeader();
        logger.info("Registered node with ID: {}", node.getNodeId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deregister")
    public ResponseEntity<?> deregisterNode(@RequestBody String nodeId) {
        logger.info("De-registering node with ID: {}", nodeId);
        nodes.remove(nodeId);
        electLeader();
        logger.info("De-registered node with ID: {}", nodeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/discover")
    public ResponseEntity<List<CrawlerNode>> discoverNodes() {
        logger.info("Discovering nodes: {}", nodes.size());
        return ResponseEntity.ok(new ArrayList<>(nodes.values()));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> updateHeartbeat(@RequestBody String nodeId) {
        CrawlerNode node = nodes.get(nodeId);
        if (node != null) {
            logger.info("Heartbeat from node ID: {}, IP: {}", node.getNodeId(), node.getAddress());
            node.setLastHeartbeat(LocalDateTime.now());
            return ResponseEntity.ok().build();
        }
        electLeader();
        if (this.masterNode != null) {
            return ResponseEntity.ok(this.masterNode.getNodeId());
        }
        return ResponseEntity.notFound().build();
    }

    @Scheduled(fixedRate = 5000)
    public void removeStaleNodes() {
        LocalDateTime cutoff = LocalDateTime.now().minus(60, ChronoUnit.SECONDS);
        int beforeSize = nodes.size();
        nodes.entrySet().removeIf(entry -> entry.getValue().getLastHeartbeat().isBefore(cutoff));
        electLeader();
        int removedNodesCount = beforeSize - nodes.size();
        if (removedNodesCount > 0) {
            logger.info("Removed {} stale node(s)", removedNodesCount);
        }
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
            logger.info("Master node updated to: {}", masterNode.getNodeId());
        }
    }

}
