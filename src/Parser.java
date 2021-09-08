import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code that parses the json, txt and csv files.
 *
 * @author Harrison Blackburn Churcher
 */
public class Parser {

	public static Map<Integer, Node> parseNodes(File nodes, Graph graph) {
		Map<Integer, Node> map = new HashMap<Integer, Node>();

		try {
			// make a reader
			BufferedReader br = new BufferedReader(new FileReader(nodes));
			String line;

			// read in each line of the file
			while ((line = br.readLine()) != null) {
				// tokenise the line by splitting it at the tabs.
				String[] tokens = line.split("[,]+");

				// proces
				String regex = "[0-9]+";

				// Compile the ReGex
				Pattern p = Pattern.compile(regex);

				// Find match between given string
				// and regular expression
				// using Pattern.matcher()
				Matcher m = p.matcher(tokens[0]);
				if(!m.matches()){
					continue;
				}
				// Return if the string
				// matched the ReGex


				int nodeID = Integer.parseInt(tokens[0]);
				String name = tokens[2];
				double lat = Double.parseDouble(tokens[4]);
				double lon = Double.parseDouble(tokens[5]);

				Node node = new Node(nodeID, name, lat, lon);
				map.put(nodeID, node);
			}

			br.close();
		} catch (IOException e) {
			throw new RuntimeException("file reading failed.");
		}

		return map;
	}

	public static Map<String, Route> parseRoutes(File shapes, Graph graph) {
		Map<String, Route> map = new HashMap<>();

		try {
			// make a reader
			BufferedReader br = new BufferedReader(new FileReader(shapes));
			br.readLine();
			String line;

			// read in each line of the file
			while ((line = br.readLine()) != null) {
				// tokenise the line by splitting it at the tabs.
				String[] tokens = line.split("[,]+");

				if (!map.keySet().contains(tokens[0])) {
					map.put(tokens[0], new Route(tokens[0]));
				}
				map.get(tokens[0]).lat.add(Double.parseDouble(tokens[1]));
				map.get(tokens[0]).lon.add(Double.parseDouble(tokens[2]));
			}

			br.close();
		} catch (IOException e) {
			throw new RuntimeException("file reading failed.");
		}

		return map;
	}

	/**
	 * Parses the boundaries of each district in New Zealand and creates new Boundary objects
	 * to store this data.
	 * @param boundaries
	 * @param graph
	 * @return
	 */
	public static Map<Integer,Boundary> parseBoundaries(File boundaries, Graph graph) {
		HashMap<Integer, Boundary> boundariesMap = new HashMap<>();

		Object obj = null;
		try {
			obj = new JSONParser().parse(new FileReader(boundaries));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		JSONObject jo = (JSONObject) obj;

		ArrayList<JSONObject> features = ((ArrayList<JSONObject>)jo.get("features"));

		for(JSONObject j : features){
			HashMap<String, Object> properties = (HashMap<String, Object>) j.get("properties");

			// change back to "ID" for island.json file
			int id = Math.toIntExact((long) properties.get("TARGET_FID"));
			Boundary boundary = new Boundary(id);

			HashMap<String, Object> geometry = (HashMap<String, Object>) j.get("geometry");

			if(geometry.get("type").equals("MultiPolygon")){

				ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> coordinates = (ArrayList<ArrayList<ArrayList<ArrayList<Double>>>>) geometry.get("coordinates");

				// this is only size of 1
				for (ArrayList<ArrayList<ArrayList<Double>>> big : coordinates){
					for (ArrayList<ArrayList<Double>> med : big){
						Boundary smallArea = new Boundary(boundary.id);

						for (int i = 0; i < med.size(); i++) {
	//						if(i % 2 == 0 && i != med.size() -1 && i != 0){	// only parse every 2nd coordinate
							System.out.println(i);
								ArrayList<Double> small = med.get(i);
								double lat = small.get(1);
								double lon = small.get(0);
								smallArea.lat.add(lat);
								smallArea.lon.add(lon);
	//						}
						}

						boundary.smallerAreas.add(smallArea);
					}
				}

			} else {
				ArrayList<ArrayList<ArrayList<Double>>> coordinates = (ArrayList<ArrayList<ArrayList<Double>>>) geometry.get("coordinates");

				// this is only size of 1
				for (ArrayList<ArrayList<Double>> big : coordinates){

					for (ArrayList<Double> med : big){
						double lat = med.get(1);
						double lon = med.get(0);
						boundary.lat.add(lat);
						boundary.lon.add(lon);
					}
				}
			}


			boundariesMap.put(id, boundary);
		}
		return boundariesMap;
	}

	public static Map<Integer, Boundary> parseIslands(File islands, Graph graph) {
		Map<Integer, Boundary> map = new HashMap<>();

		try {
			// make a reader
			BufferedReader br = new BufferedReader(new FileReader(islands));
			br.readLine();

			String line;


			// read in each line of the file
			while ((line = br.readLine()) != null) {
				// tokenise the line by splitting it at the tabs.

				// gets a string of the line that has just the coordinates separated by commas
				String shapeType = line.substring(0, line.indexOf("(")-1);
				if(shapeType.equals("POLYGON")){
					ArrayList<Double> latList = new ArrayList<>();
					ArrayList<Double> lonList = new ArrayList<>();

					String coordinates = line.substring(shapeType.length() + 3, line.substring(shapeType.length() + 2).indexOf(")") + shapeType.length() + 2);

					String restOfLine = line.substring(shapeType.length() + 3 + coordinates.length()+2);

					String[] polygonInformation = restOfLine.split("[,]+");
					String name;
					int id;

					try{
						name = polygonInformation[0];	// often nothing
						id = Integer.parseInt(polygonInformation[1]);
					} catch(NumberFormatException n){
						name = polygonInformation[1];
						id = Integer.parseInt(polygonInformation[2]);
					}

					String[] individualCoordinates = coordinates.split("[,]+");
					for(String s : individualCoordinates){
						String[] latLon = s.split("[ ]+");
						lonList.add(Double.parseDouble(latLon[0]));	// check; lat vs lon
						latList.add(Double.parseDouble(latLon[1]));
					}
					Boundary b = new Boundary(id);
					b.lat = latList;
					b.lon = lonList;
					map.put(id, b);

				} else if (shapeType.equals("MULTIPOLYGON")){
					ArrayList<Double> latList = new ArrayList<>();
					ArrayList<Double> lonList = new ArrayList<>();

					String coordinates = line.substring(shapeType.length() + 4, line.lastIndexOf(")")-2);

					String[] smallerPolygons = coordinates.split("(\\)\\),\\(\\()");
					ArrayList<Boundary> smallerBoundaries = new ArrayList<>();

					String restOfLine = line.substring(line.lastIndexOf(")"));

					String[] polygonInformation = restOfLine.split("[,]+");
					String name;
					int id;

					try{
						name = polygonInformation[0];	// often nothing
						id = Integer.parseInt(polygonInformation[1]);
					} catch(NumberFormatException n){
						name = polygonInformation[1];
						id = Integer.parseInt(polygonInformation[2]);
					}

					Boundary largeBoundary = new Boundary(id);

					for(String small : smallerPolygons){
						Boundary subBoundary = new Boundary(id);
						String[] individualCoordinates = small.split("[,]+");

						for(String coordinate : individualCoordinates){
							String[] latLon = coordinate.split("[ ]+");
							lonList.add(Double.parseDouble(latLon[0]));	// check; lat vs lon
							latList.add(Double.parseDouble(latLon[1]));
						}

						subBoundary.lat = latList;
						subBoundary.lon = lonList;
						largeBoundary.smallerAreas.add(subBoundary);
					}

					map.put(id, largeBoundary);
				} else {
					throw new ParseException(1);
				}

//				String coordinates = line.substring(11, line.substring(1).indexOf("\"")-1);
//				System.out.println(coordinates);
//				String restOfLine = line.substring(line.substring(1).indexOf("\"")+2);
//				System.out.println(restOfLine);
//				String[] polygonInformation = restOfLine.split("[,]+");
//				String name;
//				int id;


//				System.out.println(id);

//				map.put(nodeID, node);
			}

			br.close();
		} catch (IOException e) {
			throw new RuntimeException("file reading failed.");
		} catch (ParseException e) {
			System.out.println("Shape type not recognised");
		}

		return map;
	}

	public static Map<Integer, Road> parseRoads(File roads, Graph graph) {
		HashMap<Integer, Road> roadsMap = new HashMap<>();

		Object obj = null;
		try {
			obj = new JSONParser().parse(new FileReader(roads));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		JSONObject jo = (JSONObject) obj;

		// each "feature" is a road.
		ArrayList<JSONObject> streets = ((ArrayList<JSONObject>)jo.get("features"));

		for(JSONObject j : streets){
			HashMap<String, Object> properties = (HashMap<String, Object>) j.get("properties");

			int id = Math.toIntExact((long) properties.get("OBJECTID"));

			String name = (String) properties.get("name");

			HashMap<String, Object> geometry = (HashMap<String, Object>) j.get("geometry");


			ArrayList coordinates = (ArrayList) geometry.get("coordinates");
			ArrayList segments = (ArrayList) coordinates.get(0);

			Road road = new Road(id, name);
			if (segments.get(0) instanceof ArrayList){
				for(ArrayList<ArrayList<Double>> list : (ArrayList<ArrayList<ArrayList<Double>>>) coordinates){
					Road subRoad = new Road(0, null);

					ArrayList<Double> latList = new ArrayList<>();
					ArrayList<Double> lonList = new ArrayList<>();
					for(ArrayList<Double> coord : list){
						double lon = coord.get(0);
						double lat = coord.get(1);
						latList.add(lat);
						lonList.add(lon);
					}
					subRoad.lat = latList.toArray(new Double[0]);
					subRoad.lon = lonList.toArray(new Double[0]);

					road.subRoads.add(subRoad);
				}
			} else {
				ArrayList<Double> latList = new ArrayList<>();
				ArrayList<Double> lonList = new ArrayList<>();
				// it is doubles, usually means it is a small road.
				for(ArrayList<Double> list : (ArrayList<ArrayList<Double>>) coordinates) {
					int index = coordinates.size();
					while (index > 0){
						if(index % 2 == 0){
							double lat = list.get(1);
							latList.add(lat);
						} else {
							double lon = list.get(0);
							lonList.add(lon);
						}
						index--;
					}
				}
				road.lat = latList.toArray(new Double[0]);
				road.lon = lonList.toArray(new Double[0]);
			}

			roadsMap.put(id, road);
		}

		return roadsMap;
	}
}