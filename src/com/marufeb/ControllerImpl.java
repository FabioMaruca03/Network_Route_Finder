package com.marufeb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerImpl implements Controller{

    private final ArrayList<Line> lines = new ArrayList<>();
    private final ArrayList<String> stepFree = new ArrayList<>();

    /**
     * The controller constructor. O(n)
     * @param lines The File which holds lines data
     * @param accesses The File which holds accesses data
     * @throws FileNotFoundException FNFe
     */
    public ControllerImpl(File lines, File accesses) throws FileNotFoundException {
        final Stream<List<String>> tokens = new Scanner(lines) // Extracts tokens
                .useDelimiter("\n")
                .tokens()
                .distinct()
                .skip(1)
                .map(it-> Arrays.asList(it.split(",")));
        stepFree.addAll(new Scanner(accesses).useDelimiter("\n").tokens().skip(1).collect(Collectors.toList()));
        init(tokens);
    }

    /**
     * Initializes the controller. Called by the constructor. O(n)
     * @param tokens The data tokens used in this context.
     */
    private void init(Stream<List<String>> tokens) {
        lines.clear();
        lines.addAll(tokens // Parsing tokens
                .map(token -> new Line(token.get(0), token.get(1), token.get(2), Integer.parseInt(token.get(3))))
                .collect(Collectors.toList()));
        createGraph();
    }

    /**
     * Initializes the path graph. O(n)
     */
    private void createGraph() {
        if (lines.size()>0) {
            final Map<String, List<Line>> lines = this.lines.stream()
                    .collect(Collectors.groupingBy(it -> it.name));
            lines.forEach((key, value) -> value.forEach(station -> lines.get(key).stream()
                    .filter(it->it.to.equals(station.from))
                    .findAny()
                    .ifPresent(line -> { // Link stations
                        if (!station.left.contains(line))
                            station.left.add(line);
                        if (!line.right.contains(line))
                            line.right.add(station);

                    })));
            lines.forEach((name, stations) -> stations.forEach(it -> { // Define step free lines
                it.stepFree = stepFree.contains(it.from);
            }));
        }
    }

    /**
     * Makes sure to have no cycles
     * @param opt The operation sequence
     * @param condition The condition to pick a line
     * @return The list of all lines the method worked onto
     */
    private ArrayList<Line> doOnce(Consumer<Line> opt, Predicate<Line> condition) {
        final ArrayList<Line> result = new ArrayList<>();
        lines.stream().filter(condition).forEach(line->{
            if (!line.visited) {
                result.add(line);
                opt.accept(line);
                line.visited = true;
            }
        });
        lines.stream().filter(condition).forEach(it->it.visited=false);
        return result;
    }

    /**
     * Navigates through the graph and takes all ending stations
     * @return All end stations with their time
     */
    public ArrayList<Line> findEndPoints(Line line) {
        final ArrayList<Line> start = findStartPoint(line);
        final ArrayList<Line> lines = start.stream().flatMap(it -> traverseRight(it, 0).stream()).distinct().collect(Collectors.toCollection(ArrayList::new));
        this.lines.forEach(it->it.visited = false);
        return lines;
    }

    /**
     * Traverses the graph all the way up to it's very right
     * @param start The current start line
     * @param depth The current recursive depth stack height
     * @return The end points of a particular line
     */
    private ArrayList<Line> traverseRight(Line start, int depth) {
        if (start.visited) return new ArrayList<>();
        start.visited = true;
        depth+=start.minutes;
        start.minutesFromX = depth;
        if (start.right.isEmpty()) {
            final ArrayList<Line> res = new ArrayList<>();
            start.visited = false;
            res.add(start);
            return res;
        }
        int finalDepth = depth;
        return start.right.stream().flatMap(it->traverseRight(it, finalDepth).stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Finds the shortest path between two station, allows you to calculate how many changes you need.
     * @param start The station want to start from
     * @param stop The station you want to arrive in
     * @param traversable The steep-free only flag
     * @param depth The current line timing
     * @return An ArrayList which holds all lines traversed
     */
    private ArrayList<Line> findPath(Line start, Line stop, boolean traversable, int depth) {
        if (traversable) {
            if (!start.stepFree) {
                Line l = start;
                do {
                    assert l != null;
                    l = l.left.stream().filter(it->it.stepFree).findFirst().orElse(null);
                } while (l == null);
                return findPath(l, stop, traversable, depth);
            }
            if (!stop.stepFree) {
                Line l = stop;
                do {
                    assert l != null;
                    l = l.right.stream().filter(it->it.stepFree).findFirst().orElse(null);
                } while (l == null);
                return findPath(l, stop, traversable, depth);
            }
        }
        return findPath(start, stop, depth);
    }

    /**
     * Goes recursively through the entire line in order to find
     * @param start The line you want to start to
     * @param stop The line you want to achieve
     * @param depth The stack depth
     * @return The effective line
     */
    private ArrayList<Line> findPath(Line start, Line stop, int depth) {
        final ArrayList<Line> result = new ArrayList<>();
        if (start.right.isEmpty()) {
            result.add(Line.ERROR);
            return result;
        }

        if (start.left.contains(stop)) {
            result.add(stop);
            return result;
        }

        start.minutesFromX = depth += start.minutes;
        int finalDepth = depth;
        final ArrayList<Line> collect = new ArrayList<>();
        collect.add(start);
        collect.addAll(start.right.stream().map(it -> findPath(it, stop, finalDepth)).filter(it -> !it.isEmpty() && !it.contains(Line.ERROR)).flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new)));
        return collect;
    }

    /**
     * Traverses all graph all the way down to it's very left
     * @param line The line you want to find the start of
     * @return The actually starts
     */
    public ArrayList<Line> findStartPoint(Line line){
        if (line.visited) return new ArrayList<>();
        line.visited = true;
        if (line.left.isEmpty()) {
            final ArrayList<Line> objects = new ArrayList<>();
            line.visited = false;
            objects.add(line);
            return objects;
        }
        return line.left.stream().flatMap(it->findStartPoint(line).stream()).distinct().collect(Collectors.toCollection(ArrayList::new));
    }



    /**
     * The WMR Line
     */
    private static class Line {
        private final String name;
        private final String from;
        private final String to;
        private final int minutes;
        private int minutesFromX = Integer.MAX_VALUE;
        private boolean stepFree = false;
        private final static Line ERROR = new Line("NOT FOUND", "", "", -1);

        /* Linked list style */
        private ArrayList<Line> left = new ArrayList<>();
        private ArrayList<Line> right = new ArrayList<>();

        /*   Avoid  cycles   */
        private boolean visited = false;

        public Line(String name, String from, String to, int minutes) {
            this.name = name;
            this.from = from;
            this.to = to;
            this.minutes = minutes; // The graph weight
        }

        public String stations() {
            return (from!=null?from + " & ":"") + (to!=null?to:"");
        }

//        @Override
//        public String toString() {
//            final StringBuilder builder = new StringBuilder(name + "\n");
//            for (String name : name.split(" - ")) {
//
//            }
//        }
    }

    @Override
    public String listTermini(String line) {
        final String finalLine = line.replace("––", "–");
        final StringBuilder builder = new StringBuilder();
        AtomicInteger total = new AtomicInteger();
        doOnce(it-> builder.append(it.from), it->it.name.equals(finalLine) && it.left.isEmpty() && !it.right.isEmpty())
                .stream()
                .findFirst().ifPresent(it-> total.addAndGet(it.minutes));
        builder.append(" -- ");
        doOnce(it->builder.append(it.to), it->it.name.equals(finalLine) && it.right.isEmpty()&& !it.left.isEmpty())
                .stream()
                .findFirst().ifPresent(it-> total.addAndGet(it.minutes));
        return !builder.toString().equals(" -- ")?builder.append(" (").append(total.get()).append(")").toString():"Line "+line+" has no termini.";
    }

    @Override
    public String listStationsInLine(String line) {
        final String finalLine = line.replace("––", "–");
        final StringBuilder builder = new StringBuilder();
        final AtomicInteger timing = new AtomicInteger(0);
        final Deque<String> stations = new ArrayDeque<>();

        /*
         Composes the pattern:
         Nuneaton -- Coventry (22 mins):
         Nuneaton <4> Bermuda Park <8> Bedworth <14> Coventry Arena <22> Coventry
        */
        doOnce(it -> {}, it -> it.name.equals(finalLine)).forEach(it -> stations.add(it.from==null?it.to:it.from + " <" + timing.addAndGet(it.minutes) + "> "));

        builder.append(line).append(" (").append(timing.get()).append(") :\n"); // Construct header
        while (!stations.isEmpty()) { builder.append(stations.pollFirst()); } // Construct body

        return !builder.toString().isEmpty()?builder.toString():"Line "+line+" has no stations.";
    }

    @Override
    public String listAllLines() {
        return lines.stream().flatMap(line -> findEndPoints(line).stream().map(it -> findStartPoint(line).get(0).from+" <...> "+it.to+" ("+it.minutesFromX+"mins)")).distinct().collect(Collectors.joining("\n"));
    }

    @Override
    public String showAccessiblePath(String fromStation, String toStation) {
        final Map<String, List<Line>> map = lines.stream()
                .distinct()
                .collect(Collectors.groupingBy(it -> it.name));
        final StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> {
            Line start = value.stream().filter(it->it.from.equals(fromStation)).findAny().orElse(null);
            Line end = value.stream().filter(it->it.to.equals(toStation)).findAny().orElse(null);
            if (start != null && end != null) {
                findPath(start, end, true, 0).forEach(it -> builder.append(it.from).append(" -> "));
                this.lines.forEach(it -> it.visited = false);
            }
        });
        return builder.toString();
    }

    @Override
    public String showAllPaths(String fromStation, String toStation) {
        final Map<String, List<Line>> map = lines.stream()
                .distinct()
                .collect(Collectors.groupingBy(it -> it.name));
        final StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> {
            Line start = value.stream().filter(it->it.from.equals(fromStation)).findAny().orElse(null);
            Line end = value.stream().filter(it->it.to.equals(toStation)).findAny().orElse(null);
            if (start != null && end != null) {
                final Map<String, List<Line>> collect = findPath(start, end, false, 0).stream().collect(Collectors.groupingBy(it -> it.name));
                collect.forEach((k, v) -> {
                            builder.append("\n").append(collect.keySet().size()-1).append(" changes: ");
                            v.forEach(i->builder.append(i.from).append(" -> "));
                        });
                this.lines.forEach(it -> it.visited = false);
                builder.append(toStation).append("\n");
            }
        });
        return builder.toString();
    }

    @Override
    public String showShortestPath(String fromStation, String toStation) {
        final Map<String, List<Line>> map = lines.stream()
                .distinct()
                .collect(Collectors.groupingBy(it -> it.name));
        final StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> {
            Line start = value.stream().filter(it->it.from.equals(fromStation)).findAny().orElse(null);
            Line end = value.stream().filter(it->it.to.equals(toStation)).findAny().orElse(null);
            if (start != null && end != null) {
                final Map<String, List<Line>> collect = findPath(start, end, false, 0).stream().collect(Collectors.groupingBy(it -> it.name));
                final Optional<List<Line>> min = collect.values().stream().min(Comparator.comparing(i -> i.get(i.size() - 1).minutesFromX));
                min.ifPresent(line->{
                    builder.append("\n").append("shortest: ");
                    line.forEach(i->builder.append(i.from).append(" -> "));
                    builder.append(toStation).append(" (").append(line.get(min.get().size()-1).minutesFromX).append(" mins)");
                });
            }
        });
        return builder.toString();
    }
}
