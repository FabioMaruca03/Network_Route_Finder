package com.marufeb;

/**
 * A controller for the West Midlands Railway Route Finder system.
 * This controller includes the features that the intended
 * prototype West Midlands Railway Route Finder system 
 * is expected to have.
 * 
 * @author Sylvia Wong
 * @version 08-11-2020
 */
public interface Controller {
	/**
	 * Lists all termini of a specified railway line.
	 * @param line The number of the required line as shown in the com.marufeb.TUI.
	 * @return the name of all stations that are the end point of the specified lines in the network. 
	 */
	String listTermini(String line);
	
	/**
	 * Lists the stations in their respective order in the specified railway line.
	 * @param line	a specified line in the railway network
	 * @return	a String representation of all stations in the specified line.
	 */
	String listStationsInLine(String line);
	
	/**
	 * Lists all railway lines and the total travel time along all stations 
	 * between two termini on each line
	 * @return	a String representation of all railway lines and their total travel time
	 */
	String listAllLines();
	
	/**
	 * Shows one accessible path for a wheelchair user between the specified stations.
	 * @param fromStation	the intended start station
	 * @param toStation	the intended destination station
	 * @return	a String representation of a sequence of train stations showing an accessible path between the specified stations
	 */
	String showAccessiblePath(String fromStation, String toStation);
	
	/**
	 * Shows all paths between two specified stations and the number of changes
	 * required in each path.
	 * @param fromStation	the start station
	 * @param toStation	the destination station
	 * @return	all paths between two specified stations
	 */
	String showAllPaths(String fromStation, String toStation);
	
	/**
	 * Shows the shortest path between two specified stations and its travel time.
	 * @param fromStation	the start station
	 * @param toStation	the destination station
	 * @return	the shortest path between two specified stations
	 */
	String showShortestPath(String fromStation, String toStation);
}
