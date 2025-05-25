package com.smartmetro;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Metro {
    private static final int V = 248;
    private static Station[] stations = new Station[V];
    private static float[][] graph = new float[V][V];
    private static List<StatusUpdate> statusUpdates = new ArrayList<>();

    static class Station {
        String name;
        int code;
        String color;
        List<String> arrivalTime;

        Station() {
            arrivalTime = new ArrayList<>();
        }
    }

    static class StatusUpdate {
        int metroId;
        int stationId;
        int delayMinutes;
        String crowdLevel;
    }

    // Console cursor position for Windows
    private static void gotoxy(int x, int y) {
        // Move cursor to position
        StringBuilder str = new StringBuilder();
        str.append(String.format("\033[%d;%dH", y, x));
        System.out.print(str);
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String makeCapital(String str) {
        return str.toUpperCase();
    }

    private static int minDistance(float[] dist, boolean[] sptSet) {
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

    private static int lcs(String X, String Y) {
        int m = X.length(), n = Y.length();
        int[][] L = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (X.charAt(i - 1) == Y.charAt(j - 1))
                    L[i][j] = L[i - 1][j - 1] + 1;
                else
                    L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
            }
        }
        return L[m][n];
    }

    private static int sameMatch(String s) {
        int max = 0, maxi = -1;
        for (int i = 0; i < V; i++) {
            if (stations[i].name == null || stations[i].name.isEmpty())
                continue;
            int val = lcs(s, stations[i].name);
            if (val > max) {
                maxi = stations[i].code;
                max = val;
            }
        }
        return (maxi == -1 || max < stations[maxi].name.length() / 2) ? -1 : maxi;
    }

    private static void displayColorTransitions(List<Integer> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            String color = stations[path.get(i)].color;
            String colorCode = "\033[0m"; // Reset color by default

            switch (color.toUpperCase()) {
                case "BLUE": colorCode = "\033[34m"; break;
                case "YELLOW": colorCode = "\033[33m"; break;
                case "PINK": colorCode = "\033[35m"; break;
                case "RED": colorCode = "\033[31m"; break;
                case "MAGENTA": colorCode = "\033[35m"; break;
                case "VIOLET": colorCode = "\033[34m"; break;
                case "GREEN": colorCode = "\033[32m"; break;
                case "AQUA": colorCode = "\033[36m"; break;
                case "ORANGE": colorCode = "\033[33m"; break;
            }

            System.out.print(colorCode + "■" + "\033[0m");
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
    }

    private static int timeToMinutes(String time) {
        try {
            // Parse hours and minutes from time string (format: "HH:MM")
            int h = Integer.parseInt(time.substring(0, 2));
            int m = Integer.parseInt(time.substring(3, 5));
            return h * 60 + m;
        } catch (Exception e) {
            System.err.println("Error parsing time: " + time);
            return 0;
        }
    }

    private static void loadStatusData(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#')
                    continue;

                Scanner sc = new Scanner(line);
                int stationId = sc.nextInt();
                int delay = sc.nextInt();
                int crowd = sc.nextInt();

                StatusUpdate su = new StatusUpdate();
                su.stationId = stationId - 1;
                su.metroId = stationId % 3;
                su.delayMinutes = delay;
                su.crowdLevel = crowd == 1 ? "Crowded" : "Not Crowded";
                statusUpdates.add(su);
                sc.close();
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
    }

    private static void notifyDelaysAndCrowding(List<Integer> path) {
        System.out.println("\n\t\t\t\t\t\tSTATUS NOTIFICATIONS:");
        for (StatusUpdate su : statusUpdates) {
            if (path.contains(su.stationId)) {
                System.out.printf("\tMetro %d at %s", 
                    su.metroId + 1, 
                    stations[su.stationId].name);
                if (su.delayMinutes > 0)
                    System.out.printf(" has a delay of %d minutes.", su.delayMinutes);
                if (su.crowdLevel != null && !su.crowdLevel.isEmpty())
                    System.out.printf(" Crowding level: %s.", su.crowdLevel);
                System.out.println();
            }
        }
    }

    private static void calculateTimeTaken(List<Integer> path) {
        final int metroCount = 3;
        int bestMetro = -1;
        int minEffectiveTime = Integer.MAX_VALUE;

        System.out.println("\n\t\t\t\t\t\tTOTAL TIME TAKEN BY EACH METRO (with penalties):");
        for (int metro = 0; metro < metroCount; metro++) {
            List<String> times = new ArrayList<>();
            int totalDelay = 0;
            boolean isCrowded = false;

            for (int id : path) {
                if (stations[id].arrivalTime.size() > metro)
                    times.add(stations[id].arrivalTime.get(metro));

                for (StatusUpdate su : statusUpdates) {
                    if (su.metroId == metro && su.stationId == id) {
                        totalDelay += su.delayMinutes;
                        if ("Crowded".equals(su.crowdLevel))
                            isCrowded = true;
                    }
                }
            }

            if (times.size() < 2) {
                System.out.printf("\t\t\t\t\t\tMetro %d: Not enough data\n", metro + 1);
                continue;
            }

            int startMin = timeToMinutes(times.get(0));
            int endMin = timeToMinutes(times.get(times.size() - 1));
            int diff = endMin - startMin;
            if (diff < 0)
                diff += 1440; // Add 24 hours in minutes if end time is on next day

            int effectiveTime = diff + totalDelay + (isCrowded ? 10 : 0);

            int hr = effectiveTime / 60;
            int min = effectiveTime % 60;

            System.out.printf("\t\t\t\t\t\tMetro %d: %d hour(s) %d minute(s)", 
                metro + 1, hr, min);
            if (totalDelay > 0)
                System.out.printf(" (+delay: %d mins)", totalDelay);
            if (isCrowded)
                System.out.print(" [Crowded]");
            System.out.println();

            if (effectiveTime < minEffectiveTime) {
                minEffectiveTime = effectiveTime;
                bestMetro = metro;
            }
        }

        if (bestMetro != -1)
            System.out.printf("\n\t\t\t\t\t\tRecommended Metro: Metro %d (best overall)\n", 
                bestMetro + 1);
    }

    private static float dijkstra(float[][] graph, int src, int targ, List<Integer> fullPath) {
        float[] dist = new float[V];
        boolean[] sptSet = new boolean[V];
        int[] parent = new int[V];

        Arrays.fill(dist, Float.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[src] = 0;

        for (int count = 0; count < V - 1; count++) {
            int u = minDistance(dist, sptSet);
            if (u == -1)
                break;
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

        if (!fullPath.isEmpty() && fullPath.get(fullPath.size() - 1).equals(path.get(0)))
            path.remove(0);

        fullPath.addAll(path);
        return dist[targ];
    }

    private static void takeInput() {
        Scanner scanner = new Scanner(System.in);
        
        gotoxy(50, 5);
        System.out.print("ENTER THE STARTING STATION:: ");
        String startS = scanner.nextLine();
        
        gotoxy(50, 7);
        System.out.print("ENTER THE DESTINATION STATION:: ");
        String endS = scanner.nextLine();
        
        gotoxy(50, 9);
        System.out.print("ENTER THE INTERMEDIATE STATION (or type 'NO'):: ");
        String interS = scanner.nextLine();

        startS = makeCapital(startS);
        endS = makeCapital(endS);
        interS = makeCapital(interS);

        int startcode = sameMatch(startS);
        int endcode = sameMatch(endS);
        int intercode = sameMatch(interS);
        boolean hasIntermediate = !interS.equals("NO") && !interS.equals("N");

        if (startcode == -1 || endcode == -1 || (hasIntermediate && intercode == -1)) {
            gotoxy(50, 12);
            System.out.println("INVALID STATION NAME ENTERED. TRY AGAIN.");
            return;
        }

        gotoxy(50, 16);
        System.out.println("\n\t\t\t\t\t\t       ...CALCULATING SHORTEST PATH...");
        gotoxy(50, 18);
        System.out.println("\n\t\t\t\t\t\t       ...ARRIVAL TIME OF 3 METRO...");

        List<Integer> totalPath = new ArrayList<>();
        float totalDistance = 0;

        if (hasIntermediate) {
            totalDistance += dijkstra(graph, startcode, intercode, totalPath);
            totalDistance += dijkstra(graph, intercode, endcode, totalPath);
        } else {
            totalDistance += dijkstra(graph, startcode, endcode, totalPath);
        }

        gotoxy(50, 22);
        System.out.println("\n\t\t\t\t\t\t......SHORTEST AND FASTEST METRO ROUTE.......");
        for (int i = 0; i < totalPath.size(); i++) {
            boolean hasDelay = false, isCrowded = false;
            for (StatusUpdate su : statusUpdates) {
                if (su.stationId == totalPath.get(i)) {
                    if (su.delayMinutes > 0)
                        hasDelay = true;
                    if ("Crowded".equals(su.crowdLevel))
                        isCrowded = true;
                }
            }

            System.out.print(stations[totalPath.get(i)].name);
            if (hasDelay)
                System.out.print(" [⚠Delay]");
            if (isCrowded)
                System.out.print(" [⚠Crowded]");
            System.out.print(" (Arrivals: ");

            List<String> arrivalTimes = stations[totalPath.get(i)].arrivalTime;
            for (int j = 0; j < arrivalTimes.size(); j++) {
                System.out.print(arrivalTimes.get(j));
                if (j < arrivalTimes.size() - 1)
                    System.out.print(", ");
            }
            System.out.print(")");

            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if (i < totalPath.size() - 1)
                System.out.print(" -----> ");
        }

        gotoxy(50, 26);
        System.out.printf("\n\t\t\t\t\t\t\tNUMBER OF STATIONS TRAVELLED:: %d\n", 
            totalPath.size() - 1);
        System.out.printf("\n\t\t\t\t\t\t\tTOTAL DISTANCE:: %.2f km\n", totalDistance);

        displayColorTransitions(totalPath);
        calculateTimeTaken(totalPath);
        notifyDelaysAndCrowding(totalPath);

        gotoxy(50, 30);
        System.out.println("\n\t\t\t\t\t\t\tTHANK YOU FOR VISITING");
    }

    private static void loadData() {
        try {
            // Initialize graph
            for (int i = 0; i < V; i++) {
                stations[i] = new Station();
                for (int j = 0; j < V; j++) {
                    graph[i][j] = 0;
                }
            }

            // Load node values
            Scanner nodeValuesFile = new Scanner(new File("node_values_new.txt"));
            while (nodeValuesFile.hasNext()) {
                int temp = nodeValuesFile.nextInt();
                int n1 = nodeValuesFile.nextInt();
                for (int j = 0; j < temp; j++) {
                    int n2 = nodeValuesFile.nextInt();
                    float dis = nodeValuesFile.nextFloat();
                    if (n1 > 0 && n2 > 0 && n1 <= V && n2 <= V) {
                        graph[n1 - 1][n2 - 1] = dis;
                        graph[n2 - 1][n1 - 1] = dis;
                    }
                }
            }
            nodeValuesFile.close();

            // Load station names and colors
            Scanner stationFile = new Scanner(new File("station.txt"));
            Scanner colorCodesFile = new Scanner(new File("colorcodes.txt"));
            int index = 0;
            while (stationFile.hasNextLine() && colorCodesFile.hasNextLine() && index < V) {
                String stationLine = stationFile.nextLine();
                String colorLine = colorCodesFile.nextLine();
                stations[index].name = stationLine;
                stations[index].code = index;
                stations[index].color = colorLine;
                index++;
            }
            stationFile.close();
            colorCodesFile.close();

            // Load arrival times
            Scanner arrivalTimeFile = new Scanner(new File("arrival_times.txt"));
            index = 0;
            while (arrivalTimeFile.hasNextLine() && index < V) {
                String line = arrivalTimeFile.nextLine();
                String[] times = line.split(",");
                stations[index].arrivalTime = new ArrayList<>(Arrays.asList(times));
                index++;
            }
            arrivalTimeFile.close();

            loadStatusData("status_updates.txt");

        } catch (FileNotFoundException e) {
            System.err.println("Error loading data files: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        loadData();
        Scanner scanner = new Scanner(System.in);
        char choice;
        
        do {
            clearScreen();
            
            System.out.println("=======================================================");
            System.out.println("         WELCOME TO METRO DESKTOP APP");
            System.out.println("=======================================================");
            System.out.println("\n         PRESS ENTER TO CONTINUE...");
            
            scanner.nextLine();
            
            clearScreen();
            takeInput();
            
            System.out.print("\n\t\t\t\t\t\tDO YOU WANT TO TRY AGAIN? (Y/N): ");
            choice = scanner.next().charAt(0);
            scanner.nextLine(); // consume newline
            
        } while (choice == 'Y' || choice == 'y');

        System.out.println("\n\t\t\t\t\t\tExiting Metro Route Planner. Goodbye!");
        scanner.close();
    }
}