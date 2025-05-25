package com.smartmetro.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmetro.model.RouteResponse;
import com.smartmetro.model.Station;
import com.smartmetro.model.StatusUpdate;
import com.smartmetro.service.MetroService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MetroController {

    @Autowired
    private MetroService metroService;

    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(metroService.getAllStations());
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponse> findRoute(@RequestBody Map<String, String> request) {
        String start = request.get("start");
        String end = request.get("end");
        String via = request.get("via");

        if (start == null || end == null) {
            return ResponseEntity.badRequest().build();
        }

        RouteResponse response = metroService.findRoute(start, end, via);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<List<StatusUpdate>> getStatusUpdates() {
        return ResponseEntity.ok(metroService.getStatusUpdates());
    }
} 