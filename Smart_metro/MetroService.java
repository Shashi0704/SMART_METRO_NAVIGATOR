package com.smartmetro.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.smartmetro.model.RouteResponse;
import com.smartmetro.model.Station;
import com.smartmetro.model.StatusUpdate;

@Service
public class MetroService {
    private static final int V = 248;
    private static Station[] stations = new Station[V];
    private static float[][] graph = new float[V][V];
    private static List<StatusUpdate> statusUpdates = new ArrayList<>();

    public MetroService() {
        loadData();
    }

    public List<Station> getAllStations() {
        List<Station> validStations = new ArrayList<>();
        for (Station station : stations) {
            if (station != null && station.getName() != null && !station.getName().isEmpty()) {
                validStations.add(station);
            }
        }
        return validStations;
    }

    public RouteResponse findRoute(String startStation, String endStation, String viaStation) {
        // Convert station names to codes
        int startCode = findStationCode(startStation);
        int endCode = findStationCode(endStation);
        int viaCode = viaStation != null && !viaStation.isEmpty() ? findStationCode(viaStation) : -1;

        if (startCode == -1 || endCode == -1 || (viaStation != null && !viaStation.isEmpty() && viaCode == -1)) {
            throw new IllegalArgumentException("Invalid station name");
        }

        RouteResponse response = new RouteResponse();
        List<Station> routeStations = new ArrayList<>();
        float totalDistance = 0;

        // Calculate route
        List<Integer> path = new ArrayList<>();
        if (viaCode != -1) {
            totalDistance += dijkstra(graph, startCode, viaCode, path);
            totalDistance += dijkstra(graph, viaCode, endCode, path);
        } else {
            totalDistance += dijkstra(graph, startCode, endCode, path);
        }

        // Convert path to stations
        for (int stationId : path) {
            routeStations.add(stations[stationId]);
        }

        // Calculate metro information
        List<RouteResponse.MetroInfo> metros = calculateMetroInfo(path);
        List<StatusUpdate> relevantUpdates = getRelevantStatusUpdates(path);

        response.setStations(routeStations);
        response.setDistance(totalDistance);
        response.setEstimatedTime(calculateEstimatedTime(path));
        response.setMetros(metros);
        response.setStatusUpdates(relevantUpdates);

        return response;
    }

    public List<StatusUpdate> getStatusUpdates() {
        return statusUpdates;
    }

    private int findStationCode(String stationName) {
        String upperName = stationName.toUpperCase();
        for (Station station : stations) {
            if (station != null && station.getName() != null && 
                station.getName().toUpperCase().equals(upperName)) {
                return station.getCode();
            }
        }
        return -1;
    }

    private float dijkstra(float[][] graph, int src, int targ, List<Integer> fullPath) {
        float[] dist = new float[V];
        boolean[] sptSet = new boolean[V];
        int[] parent = new int[V];

        Arrays.fill(dist, Float.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[src] = 0;

        for (int count = 0; count < V - 1; count++) {
            int u = minDistance(dist, sptSet);
            if (u == -1) break;
            sptSet[u] = true;

            for (int v = 0; v < V; v++) {
                if (!sptSet[v] && graph[u][v] > 0 && 
                    dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    parent[v] = u;
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        int move = targ;
        while (move != -1) {
            path.add(move);
            move = parent[move];
        }
        Collections.reverse(path);

        if (!fullPath.isEmpty() && fullPath.get(fullPath.size() - 1).equals(path.get(0))) {
            path.remove(0);
        }

        fullPath.addAll(path);
        return dist[targ];
    }

    private int minDistance(float[] dist, boolean[] sptSet) {
        float min = Float.MAX_VALUE;
        int minIndex = -1;
        for (int v = 0; v < V; v++) {
            if (!sptSet[v] && dist[v] <= min) {
                min = dist[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    private List<RouteResponse.MetroInfo> calculateMetroInfo(List<Integer> path) {
        List<RouteResponse.MetroInfo> metros = new ArrayList<>();
        final int metroCount = 3;
        int bestMetro = -1;
        int minEffectiveTime = Integer.MAX_VALUE;

        for (int metro = 0; metro < metroCount; metro++) {
            RouteResponse.MetroInfo metroInfo = new RouteResponse.MetroInfo();
            metroInfo.setId(metro + 1);

            int totalDelay = 0;
            boolean isCrowded = false;

            for (int stationId : path) {
                for (StatusUpdate su : statusUpdates) {
                    if (su.getMetroId() == metro && su.getStationId() == stationId) {
                        totalDelay += su.getDelayMinutes();
                        if ("Crowded".equals(su.getCrowdLevel())) {
                            isCrowded = true;
                        }
                    }
                }
            }

            int baseTime = calculateBaseTime(path, metro);
            int effectiveTime = baseTime + totalDelay + (isCrowded ? 10 : 0);

            metroInfo.setTime(effectiveTime);
            metroInfo.setDelay(totalDelay);
            metroInfo.setCrowded(isCrowded);

            if (effectiveTime < minEffectiveTime) {
                minEffectiveTime = effectiveTime;
                bestMetro = metro;
            }

            metros.add(metroInfo);
        }

        if (bestMetro != -1) {
            metros.get(bestMetro).setRecommended(true);
        }

        return metros;
    }

    private int calculateBaseTime(List<Integer> path, int metroIndex) {
        return path.size() * 3; // Assuming 3 minutes between stations
    }

    private List<StatusUpdate> getRelevantStatusUpdates(List<Integer> path) {
        List<StatusUpdate> relevant = new ArrayList<>();
        for (StatusUpdate update : statusUpdates) {
            if (path.contains(update.getStationId())) {
                update.setStation(stations[update.getStationId()].getName());
                update.setMessage(String.format("Metro %d: %s. %s",
                    update.getMetroId() + 1,
                    update.getDelayMinutes() > 0 ? update.getDelayMinutes() + " min delay" : "On time",
                    update.getCrowdLevel()));
                relevant.add(update);
            }
        }
        return relevant;
    }

    private int calculateEstimatedTime(List<Integer> path) {
        return path.size() * 3; // Assuming 3 minutes between stations
    }

    private void loadData() {
        try {
            // Initialize graph
            for (int i = 0; i < V; i++) {
                stations[i] = new Station();
                Arrays.fill(graph[i], 0);
            }

            // Load node values
            Scanner nodeScanner = new Scanner(new ClassPathResource("data/node_values_new.txt").getInputStream());
            while (nodeScanner.hasNext()) {
                int temp = nodeScanner.nextInt();
                int n1 = nodeScanner.nextInt();
                for (int j = 0; j < temp; j++) {
                    int n2 = nodeScanner.nextInt();
                    float dis = nodeScanner.nextFloat();
                    if (n1 > 0 && n2 > 0 && n1 <= V && n2 <= V) {
                        graph[n1 - 1][n2 - 1] = dis;
                        graph[n2 - 1][n1 - 1] = dis;
                    }
                }
            }
            nodeScanner.close();

            // Load station names and colors
            Scanner stationScanner = new Scanner(new ClassPathResource("data/station.txt").getInputStream());
            Scanner colorScanner = new Scanner(new ClassPathResource("data/colorcodes.txt").getInputStream());
            int index = 0;
            while (stationScanner.hasNextLine() && colorScanner.hasNextLine() && index < V) {
                String stationLine = stationScanner.nextLine();
                String colorLine = colorScanner.nextLine();
                stations[index].setName(stationLine);
                stations[index].setCode(index);
                stations[index].setColor(colorLine);
                index++;
            }
            stationScanner.close();
            colorScanner.close();

            // Load arrival times
            Scanner arrivalScanner = new Scanner(new ClassPathResource("data/arrival_times.txt").getInputStream());
            index = 0;
            while (arrivalScanner.hasNextLine() && index < V) {
                String line = arrivalScanner.nextLine();
                String[] times = line.split(",");
                stations[index].setArrivalTime(new ArrayList<>(Arrays.asList(times)));
                index++;
            }
            arrivalScanner.close();

            // Load status updates
            Scanner statusScanner = new Scanner(new ClassPathResource("data/status_updates.txt").getInputStream());
            while (statusScanner.hasNextLine()) {
                String line = statusScanner.nextLine();
                if (line.isEmpty() || line.charAt(0) == '#') continue;

                Scanner sc = new Scanner(line);
                int stationId = sc.nextInt();
                int delay = sc.nextInt();
                int crowd = sc.nextInt();

                StatusUpdate su = new StatusUpdate();
                su.setStationId(stationId - 1);
                su.setMetroId(stationId % 3);
                su.setDelayMinutes(delay);
                su.setCrowdLevel(crowd == 1 ? "Crowded" : "Not Crowded");
                statusUpdates.add(su);
                sc.close();
            }
            statusScanner.close();

        } catch (IOException e) {
            throw new RuntimeException("Error loading data files: " + e.getMessage(), e);
        }
    }
} 