import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



/**
 * TODO: Add the Train Stations, have these a different colour/shape
 *          - Make these optional, (button to turn these off / on)
 *       Change shape to route since it is a route
 *       RealTime information of where all busses are (show all, show a certain number, show a list of numbers, search by bus ID)
 *       Fix when some stops have 0 upcoming departures, and it shows the wrong things.
 *       Add detection for "status : cancelled"
 *       Search by stop number, stop name
 *       Map the whole outline of NZ and respective boundaries
 *       Map all roads in Welly. Any need for
 */

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 *
 * @author tony
 */
public class Mapper extends GUI{
    public static final Color NODE_COLOUR = new Color(77, 113, 255);
    public static final Color SEGMENT_COLOUR = new Color(130, 130, 130).brighter();
    public static final Color HIGHLIGHT_COLOUR = new Color(224, 91, 78);
    public static boolean time;


    Dimension dimension = this.getDrawingAreaDimension();

    public int total = 0;
    // these two constants define the size of the node squares at different zoom
    // levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
    // log(scale)
    public static final int NODE_INTERCEPT = 1;
    public static final double NODE_GRADIENT = 0.8;

    // defines how much you move per button press, and is dependent on scale.
    public static final double MOVE_AMOUNT = 100;
    // defines how much you zoom in/out per button press, and the maximum and
    // minimum zoom levels.
    public static final double ZOOM_FACTOR = 1.3;
    public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

    // how far away from a node you can click before it isn't counted.
    public static final double MAX_CLICKED_DISTANCE = 0.15;

    // our data structures.
    private Graph graph;
//    private Trie trie;

    private Node startNode = null;
    private Node targetNode = null;

    private Point point2 = new Point(-dimension.width, -dimension.height); // gets center screen
    private Location originalOrigin = new Location(0, 0);            // creates Location of top left of screen

    // these two define the 'view' of the program, ie. where you're looking and
    // how zoomed in you are.
    private double scale = 8;
    private Location origin = Location.newFromPoint(point2, originalOrigin, scale); // creates Location of center screen

    @Override
    protected void redraw(Graphics g) {
        if (graph != null) {
            // recalculate center screen and origin
            dimension = getDrawingAreaDimension();
            point2 = new Point(-dimension.width / 2, -dimension.height / 2);
            origin = Location.newFromPoint(point2, originalOrigin, scale);

            graph.draw(g, getDrawingAreaDimension(), origin, scale);
        }
    }

    @Override
    protected void onClick(MouseEvent e) {
        Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
        // find the closest node.
        double bestDist = Double.MAX_VALUE;
        Node closest = null;

        for (Node node : graph.nodes.values()) {
            double distance = clicked.distance(node.location);
            if (distance < bestDist) {
                bestDist = distance;
                closest = node;
            }
        }

        graph.setHighlight(closest);

        if (startNode == null) {
            startNode = closest;
        } else {
            targetNode = closest;
        }

        try {
            parseJson(closest);
        } catch (IOException | ParseException | java.text.ParseException ioException) {
            ioException.printStackTrace();
        }

    }

    public void parseJson(Node closest) throws IOException, org.json.simple.parser.ParseException, java.text.ParseException {
        getTextOutputArea().append("\nUpcoming buses:");
        URL url = new URL("https://api.opendata.metlink.org.nz/v1/stop-predictions?stop_id=" + closest.nodeID);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestProperty("stop_id", Integer.toString(closest.nodeID));
        http.setRequestProperty("accept", "application/json");
        http.setRequestProperty("x-api-key", "YOUR_API_KEY_HERE"); // TODO: Your personal API key here

        InputStreamReader inputStreamReader = new InputStreamReader(http.getInputStream());

        Object obj = new JSONParser().parse(inputStreamReader);
        JSONObject jo = (JSONObject) obj;

        ArrayList<JSONObject> departures = ((ArrayList<JSONObject>)jo.get("departures"));

        String timeDue;
        getTextOutputArea().setText(closest.toString());
        for (int i = 0; i < 10 && i <departures.size(); i++) {
            JSONObject jsonObject = departures.get(i);
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");

            // need to get service number, origin name
            String serviceId = (String) jsonObject.get("service_id");
            String status = (String) jsonObject.get("status");

            HashMap<String, String> destination = ((HashMap)jsonObject.get("destination"));
            String name = destination.get("name");

            HashMap<String, String> departure = ((HashMap)jsonObject.get("departure"));
            if(departure.get("expected") == null){
                timeDue = "— Scheduled at: " + departure.get("aimed").substring(11, 19);
            } else {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String h = dtf.format(now);

                Date date1 = dateFormat.parse(departure.get("expected").substring(11, 19)); // we only want the HH:mm:ss part
                Date date2 = dateFormat.parse(h);
                long difference = (Math.max(date1.getTime(), date2.getTime()) - Math.min(date1.getTime(), date2.getTime()))/60000;

                timeDue = "— Due in: " + difference + " minutes";
            }
            if(status != null && status.equals("cancelled")) timeDue += " (CANCELLED)";
            System.out.println( serviceId + " " + name + " " + timeDue);
            getTextOutputArea().append("\n" + serviceId + " " + name + " " + timeDue);
        }

        if(departures.isEmpty()){
            getTextOutputArea().append("\nNo departures to show.");
        }
        http.disconnect();
    }

    /**
     * Prints the result of the curl request.
     * @param isr
     */
    private void printResult(InputStreamReader isr) {
        BufferedReader r = new BufferedReader(isr);
        String line = "";
        String curlResult = "";
        while (true) {
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            curlResult = curlResult + line;
        }
        System.out.println(curlResult);
    }

//    @Override
//    protected void onSearch() {
//        if (trie == null)
//            return;
//
//        // get the search query and run it through the trie.
//        String query = getSearchBox().getText();
//        Collection<Road> selected = trie.get(query);
//
//        // figure out if any of our selected roads exactly matches the search
//        // query. if so, as per the specification, we should only highlight
//        // exact matches. there may be (and are) many exact matches, however, so
//        // we have to do this carefully.
//        boolean exactMatch = false;
//        for (Road road : selected)
//            if (road.name.equals(query))
//                exactMatch = true;
//
//        // make a set of all the roads that match exactly, and make this our new
//        // selected set.
//        if (exactMatch) {
//            Collection<Road> exactMatches = new HashSet<>();
//            for (Road road : selected)
//                if (road.name.equals(query))
//                    exactMatches.add(road);
//            selected = exactMatches;
//        }
//
//        // set the highlighted roads.
//        graph.setHighlight(selected);
//
//        // now build the string for display. we filter out duplicates by putting
//        // it through a set first, and then combine it.
//        Collection<String> names = new HashSet<>();
//        for (Road road : selected)
//            names.add(road.name);
//        String str = "";
//        for (String name : names)
//            str += name + "; ";
//
//        if (str.length() != 0)
//            str = str.substring(0, str.length() - 2);
//        getTextOutputArea().setText(str);
//    }

    @Override
    protected void onMove(Move m) {
        if (m == Move.NORTH) {
            originalOrigin = originalOrigin.moveBy(0, MOVE_AMOUNT / scale);
        } else if (m == Move.SOUTH) {
            originalOrigin = originalOrigin.moveBy(0, -MOVE_AMOUNT / scale);
        } else if (m == Move.EAST) {
            originalOrigin = originalOrigin.moveBy(MOVE_AMOUNT / scale, 0);
        } else if (m == Move.WEST) {
            originalOrigin = originalOrigin.moveBy(-MOVE_AMOUNT / scale, 0);
        } else if (m == Move.ZOOM_IN) {
            if (scale < MAX_ZOOM) {
                scale *= ZOOM_FACTOR;
            }
        } else if (m == Move.ZOOM_OUT) {
            if (scale > MIN_ZOOM) {
                scale /= ZOOM_FACTOR;
            }
        }
    }

    /**
     * Method that controls the zooming when scrolling is done.
     * This is done by evaluating whether the wheel rotation is greater or less than 0.
     */
    @Override
    protected void scroll(MouseWheelEvent e) {
        int scrollValue = e.getWheelRotation();

        if (scrollValue > 0) {
            onMove(Move.ZOOM_IN);
        } else if (scrollValue < 0) {
            onMove(Move.ZOOM_OUT);
        }
    }

    /**
     * Loads the relevant files
     *
     * @param nodes        a File for nodeID-lat-lon.tab
     * @param roads        a File for roadID-roadInfo.tab
     */
    @Override
    protected void onLoad(File nodes, File roads, File shapes, File boundaries) {
        graph = new Graph(nodes, roads, shapes, boundaries);
        redraw();
        Graph.highlightedNodes.clear();
//        trie = new Trie(graph.roads.values());
//        origin = new Location(-250, 250); // close enough
    }

    public static void main(String[] args) {
        new Mapper();
    }
}