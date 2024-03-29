/*	VIDIS is a simulation and visualisation framework for distributed systems.
	Copyright (C) 2009 Jesus M. Salvo, Ralf Vandenhouten
	This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
	You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>. */
package vidis.util.graphs.graph.algorithm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import vidis.util.graphs.graph.Graph;

/**
 * Abstract class for an algorithm implementing the coloring of a graph.
 * Concrete implementations of this class must never modify the Graph itself.
 *
 * @author Ralf Vandenhouten
 * @version 2002/09/29
 */

public abstract class GraphColoring implements Serializable {

  /**
   * The Graph on which graph coloring will be performed.
   */
  protected Graph   graph;

  /**
   * The HashMap for mapping the vertices of the graph to their colors
   */
  protected HashMap colorMap;

  public GraphColoring( Graph graph ) {
    this.graph = graph;
    this.colorMap = new HashMap();
  }

  /**
   * Abstract coloring method to be implemented by subclasses.
   *
   * @param maxNumOfColors The maximum number of colors to be used for coloring.
   *          If that is not enough, a NotEnoughColorsException is thrown.
   *
   * @return The HashMap containing the color mapping of the vertices.
   */
  public abstract Map coloring( int maxNumOfColors )
  throws NotEnoughColorsException;

  /**
   * Coloring method to be implemented by subclasses. The default behaviour
   * is that it simply calls this.coloring(n) where n is the number of
   * vertices of the graph.
   *
   * @return The HashMap containing the color mapping of the vertices.
   */
  public Map coloring() {
    try {
      return coloring( graph.getVerticesCount() );
    } catch (NotEnoughColorsException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns a HashMap that maps each vertex to its color.
   *
   * @return The HashMap that maps each vertex to each color.
   */
  public Map getColorMap() {
    return colorMap;
  }
}