import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents the maze world
class MazesWorld extends World {
  ArrayList<Node> nodes;
  ArrayList<Edge> edges;
  ArrayList<Edge> edgesToGoInTree;
  ArrayList<Edge> drawnWalls;
  HashMap<Node, Node> vertices;
  int sizeX;
  int sizeY;
  int vertWeight;
  int horizWeight;
  Random rand;
  HashMap<Node, Edge> cameFromEdge;
  ArrayList<Node> searchedPath;
  int mazeHeight = 50;
  int mazeWidth = 80;
  int cellSize = 10;
  boolean reachedEnd = false;

  // main constructor that randomizes the weights
  MazesWorld(int sizeX, int sizeY, int vertWeight, int horziWeight) {
    if (sizeX > 100) {
      throw new IllegalArgumentException("sizeX cannot be bigger than 100");
    }
    else {
      this.sizeX = sizeX;
    }
    if (sizeY > 60) {
      throw new IllegalArgumentException("sizeY cannot be bigger than 60");
    }
    else {
      this.sizeY = sizeY;
    }
    this.vertWeight = vertWeight;
    this.horizWeight = horziWeight;
    this.rand = new Random();
    this.nodes = new ArrayList<Node>();
    this.edges = new ArrayList<Edge>();
    this.vertices = new HashMap<Node, Node>();
    this.makeGraph(this.sizeX, this.sizeY);
    this.drawnWalls = new ArrayList<Edge>(this.edges);
    this.edgesToGoInTree = new ArrayList<Edge>();
    this.minSpanningTree(this.edges, this.vertices);
    this.cameFromEdge = new HashMap<Node, Edge>();
    this.searchedPath = new ArrayList<Node>();
  }

  // makes the maze but uses a seeded random
  MazesWorld(int sizeX, int sizeY, int vertWeight, int horziWeight, Random rand) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.vertWeight = vertWeight;
    this.horizWeight = horziWeight;
    this.rand = rand;
    this.nodes = new ArrayList<Node>();
    this.edges = new ArrayList<Edge>();
    this.vertices = new HashMap<Node, Node>();
    this.makeGraph(this.sizeX, this.sizeY);
    this.drawnWalls = new ArrayList<Edge>(this.edges);
    this.edgesToGoInTree = new ArrayList<Edge>();
    this.minSpanningTree(this.edges, this.vertices);
    this.cameFromEdge = new HashMap<Node, Edge>();
    this.searchedPath = new ArrayList<Node>();
  }

  // creates and mutates all the nodes and edges to make a basic graph
  void makeGraph(int sizeX, int sizeY) {
    for (int i = 0; i < sizeX; i++) {
      for (int j = 0; j < sizeY; j++) {
        Node newNode = new Node(i, j);
        this.nodes.add(newNode);
        this.vertices.put(newNode, newNode);
      }
    }
    int i = sizeX - 1;
    for (Node n : this.nodes) {
      if ((this.nodes.indexOf(n) < (sizeX * (sizeY - 1)))) {
        Edge newEdgeBottom = new Edge(n, this.nodes.get(this.nodes.indexOf(n) + sizeX),
            this.rand.nextInt(this.vertWeight));
        this.edges.add(newEdgeBottom);
        n.listOfEdges.add(newEdgeBottom);
      }
      if (i > 0) {
        Edge newEdgeRight = new Edge(n, this.nodes.get(this.nodes.indexOf(n) + 1),
            this.rand.nextInt(this.horizWeight));
        this.edges.add(newEdgeRight);
        n.listOfEdges.add(newEdgeRight);
        i--;
      }
      else {
        i = sizeX - 1;
      }
    }
  }

  // takes the sorted edges and makes a min spanning tree with them
  ArrayList<Edge> minSpanningTree(ArrayList<Edge> edges1, HashMap<Node, Node> vertices1) {
    ArrayList<Edge> edges2 = new Utils().sortEdges(edges1);
    ArrayList<Edge> worklist = new ArrayList<Edge>(edges2);
    while (!worklist.isEmpty()) {
      Edge current = worklist.remove(0);
      if (!find(vertices1, current.to).equals(find(vertices1, current.from))) {
        this.edgesToGoInTree.add(current);
        union(vertices1, find(vertices1, current.to), (find(vertices1, current.from)));
      }
      else {
        current.from.listOfEdges.remove(current);
      }
    }
    worklist.removeAll(this.edgesToGoInTree);
    return this.edgesToGoInTree;
  }

  // finds the vertex at the given key
  Node find(HashMap<Node, Node> nodes, Node find) {
    if (nodes.get(find).equals(find)) {
      return nodes.get(find);
    }
    else {
      return find(nodes, nodes.get(find));
    }
  }

  // changes the node to the given node
  void union(HashMap<Node, Node> nodes, Node n1, Node n2) {
    nodes.put(n1, n2); 
  }

  // makes the scene
  public WorldScene makeScene() {
    WorldScene background = new WorldScene(500, 500);
    int cellWidth = 500 / this.sizeX;
    // draws the grid
    for (Node n : this.nodes) {
      background.placeImageXY(new RectangleImage(cellWidth, cellWidth, OutlineMode.SOLID, n.color),
          n.x * cellWidth + (cellWidth / 2 + 1), n.y * cellWidth + (cellWidth / 2 + 1));
    }
    // draws the edges
    this.drawnWalls.removeAll(this.edgesToGoInTree);
    for (Edge e : this.drawnWalls) {
      e.drawWalls(e.to, e.from, background, cellWidth);
    }
    background.placeImageXY(
        new RectangleImage(cellWidth, cellWidth, OutlineMode.SOLID, Color.magenta),
        this.sizeX * cellWidth - (cellWidth / 2), this.sizeY * cellWidth - (cellWidth / 2));
    background.placeImageXY(
        new RectangleImage(cellWidth, cellWidth, OutlineMode.SOLID, Color.green),
        0 + (cellWidth / 2), 0 + (cellWidth / 2));
    if (!this.searchedPath.isEmpty()) {
      Node curNode = this.searchedPath.remove(0);
      curNode.color = new Color(77, 166, 255); // light blue
    }
    else if (this.reachedEnd) {
      this.reconstruct(cameFromEdge, this.nodes.get(this.sizeX * this.sizeY - 1));
    }
    //check if already is not empty, then draw the first cell in the list
    //if list is empty, then call reconstruct all at once on the last cell
    //make cameFromEdge a field, call it for reconstruct
    return background;
  }

  // EFFECT: if r is typed, reset the board and game
  // EFFECT: if b or d is typed, run breadth or depth first search respectively
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      MazesWorld maze = new MazesWorld(this.sizeX, this.sizeY, 100, 100);
      this.vertWeight = maze.vertWeight;
      this.horizWeight = maze.vertWeight;
      this.rand = new Random();
      this.nodes = maze.nodes;
      this.edges = maze.edges;
      this.vertices = maze.vertices;
      this.drawnWalls = maze.drawnWalls;
      this.edgesToGoInTree = maze.edgesToGoInTree;
      this.cameFromEdge = maze.cameFromEdge;
      this.searchedPath = maze.searchedPath;
      this.reachedEnd = false;
    }
    if (key.equals("b")) {
      this.breadthFirst();
    }
    if (key.equals("d")) {
      this.depthFirst();
    }
    // EXTRA CREDIT: Creates a prefernce for more vertical or horizontal mazes by
    // editing the random weights that are given (between 1-30 instead of 1-100)
    if (key.equals("v")) {
      MazesWorld maze = new MazesWorld(this.sizeX, this.sizeY, 100, 30);
      this.vertWeight = maze.vertWeight;
      this.horizWeight = maze.horizWeight;
      this.rand = new Random();
      this.nodes = maze.nodes;
      this.edges = maze.edges;
      this.vertices = maze.vertices;
      this.drawnWalls = maze.drawnWalls;
      this.edgesToGoInTree = maze.edgesToGoInTree;
      this.cameFromEdge = maze.cameFromEdge;
      this.searchedPath = maze.searchedPath;
      this.reachedEnd = false;
    }
    if (key.equals("h")) {
      MazesWorld maze = new MazesWorld(this.sizeX, this.sizeY, 30, 100);
      this.vertWeight = maze.vertWeight;
      this.horizWeight = maze.horizWeight;
      this.rand = new Random();
      this.nodes = maze.nodes;
      this.edges = maze.edges;
      this.vertices = maze.vertices;
      this.drawnWalls = maze.drawnWalls;
      this.edgesToGoInTree = maze.edgesToGoInTree;
      this.cameFromEdge = maze.cameFromEdge;
      this.searchedPath = maze.searchedPath;
      this.reachedEnd = false;
    }
  }

  //solves the maze in a breadth first method
  void breadthFirst() {
    Queue<Node> worklist = new ArrayDeque<Node>();
    worklist.add(this.nodes.get(0));
    // make already seen a field in world. then draw it one by one in the worldscene
    while (!worklist.isEmpty()) {
      Node next = worklist.remove();
      if (searchedPath.contains(next)) {
        // Do nothing
      }
      else if (next.equals(this.nodes.get(this.sizeX * this.sizeY - 1))) {
        // searchedPath.add(next); //maybe dont add this - maybe try and end the
        // sequence?
        worklist.clear();
        this.reachedEnd = true;
      }
      else {
        searchedPath.add(next);
        for (Edge e : next.listOfEdges) {
          if (!searchedPath.contains(e.to)) {
            worklist.add(e.to);
            System.out.println(e.to.x + "," + e.to.y);
            this.cameFromEdge.put(e.to, e);
          }
        }
        // checking if the above or left nodes are not filled in and need to be seen
        // next
        // must also add a check to make sure that there actually is an edge there
        if (this.nodes.indexOf(next) > this.sizeX) {
          Node aboveNode = this.nodes.get(this.nodes.indexOf(next) - this.sizeX);
          if (aboveNode.hasEdgeTo(next) && !searchedPath.contains(aboveNode)) {
            worklist.add(aboveNode);
            this.cameFromEdge.put(aboveNode, aboveNode.getEdgeGoingTo(next));
          }
        }
        // checks if the left node needs to be added also
        if (next.x != 0) {
          Node leftNode = this.nodes.get(this.nodes.indexOf(next) - 1);
          if (leftNode.hasEdgeTo(next) && !searchedPath.contains(leftNode)) {
            worklist.add(leftNode);
            this.cameFromEdge.put(leftNode, leftNode.getEdgeGoingTo(next));
          }
        }
      }
    }
  }

  //solves the maze in a depth first method
  void depthFirst() {
    Stack<Node> worklist = new Stack<Node>();
    worklist.add(this.nodes.get(0));
    // make already seen a field in world. then draw it one by one in the worldscene
    while (!worklist.isEmpty()) {
      Node next = worklist.pop();
      if (searchedPath.contains(next)) {
        // Do nothing
      }
      else if (next.equals(this.nodes.get(this.sizeX * this.sizeY - 1))) {
        // searchedPath.add(next); //maybe dont add this - maybe try and end the
        // sequence?
        worklist.clear();
        this.reachedEnd = true;
      }
      else {
        searchedPath.add(next);
        for (Edge e : next.listOfEdges) {
          if (!searchedPath.contains(e.to)) {
            worklist.add(e.to);
            System.out.println(e.to.x + "," + e.to.y);
            this.cameFromEdge.put(e.to, e);
          }
        }
        // checking if the above or left nodes are not filled in and need to be seen
        // next
        // must also add a check to make sure that there actually is an edge there
        if (this.nodes.indexOf(next) > this.sizeX) {
          Node aboveNode = this.nodes.get(this.nodes.indexOf(next) - this.sizeX);
          if (aboveNode.hasEdgeTo(next) && !searchedPath.contains(aboveNode)) {
            worklist.add(aboveNode);
            this.cameFromEdge.put(aboveNode, aboveNode.getEdgeGoingTo(next));
          }
        }
        // checks if the left node needs to be added also
        if (next.x != 0) {
          Node leftNode = this.nodes.get(this.nodes.indexOf(next) - 1);
          if (leftNode.hasEdgeTo(next) && !searchedPath.contains(leftNode)) {
            worklist.add(leftNode);
            this.cameFromEdge.put(leftNode, leftNode.getEdgeGoingTo(next));
          }
        }
      }
    }
  }

  //retraces the correct maze path, turning it dark blue
  void reconstruct(HashMap<Node, Edge> cameFromEdge, Node n) {
    // if (next.equals(this.nodes.get(0))) {
    // next.color = Color.blue;
    // } else if(cameFromEdge.get(next).from.equals(next)) {
    // Edge cameFrom = cameFromEdge.get(next);
    // next.color = Color.blue;
    // this.reconstruct(cameFromEdge, cameFrom.to);
    // }
    // else {
    // Edge cameFrom = cameFromEdge.get(next);
    // next.color = Color.blue;
    // this.reconstruct(cameFromEdge, cameFrom.from);
    // }
    Node next = n;
    while (!next.equals(this.nodes.get(0))) {
      Edge cameFrom = cameFromEdge.get(next);
      next.color = Color.blue;
      if (cameFrom.from.equals(next)) {
        next = cameFrom.to;
      }
      else {
        next = cameFrom.from;
      }
    }
    next.color = Color.blue;
  }
}

//////////////////////////////////////////////////////////////////////
// class for nodes
class Node {
  int x;
  int y;
  ArrayList<Edge> listOfEdges;
  Color color;

  Node(int x, int y) {
    this.x = x;
    this.y = y;
    this.listOfEdges = new ArrayList<Edge>();
    this.color = Color.LIGHT_GRAY;
  }

  public Edge getEdgeGoingTo(Node next) {
    Edge equalsEdge = null;
    for (Edge e : this.listOfEdges) {
      if (e.to.equals(next)) {
        equalsEdge = e;
      }
    }
    return equalsEdge;
  }

  // sees if this node has any that equal it
  boolean hasEdgeTo(Node next) {
    boolean result = false;
    for (Edge e : this.listOfEdges) {
      if (e.to.equals(next)) {
        result = true;
      }
    }
    System.out.println(result);
    return result;
  }

  // is this node equal to the given object?
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Node)) {
      return false;
    }
    else {
      Node that = (Node) other;
      return this.x == that.x && this.y == that.y;
    }
  }

  // produces a hashcode for this node
  @Override
  public int hashCode() {
    return this.x * 5000 + 1000 * this.y;
  }
}

//////////////////////////////////////////////////////////////////////
// class for edges
class Edge implements Comparable<Edge> {
  Node from;
  Node to;
  int weight;

  Edge(Node from, Node to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // draws the walls
  WorldScene drawWalls(Node node1, Node node2, WorldScene background, int cellWidth) {
    if (node1.x == node2.x) {
      background.placeImageXY(new RectangleImage(cellWidth, 2, OutlineMode.SOLID, Color.black),
          (node2.x + 1) * cellWidth - cellWidth / 2, (node2.y + 1) * cellWidth);
    }
    else if (node1.y == node2.y) {
      background.placeImageXY(new RectangleImage(2, cellWidth, OutlineMode.SOLID, Color.black),
          (node2.x + 1) * cellWidth, (node2.y + 1) * cellWidth - cellWidth / 2);
    }
    return background;
  }

  // compares edges
  public int compareTo(Edge other) {
    if (this.weight < other.weight) {
      return -1;
    }
    if (this.weight > other.weight) {
      return 1;
    }
    else {
      return 0;
    }
  }

  // is this edge equal to the given object?
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Edge)) {
      return false;
    }
    else {
      Edge that = (Edge) other;
      return ((this.from.equals(that.from) && this.to.equals(that.to))
          || (this.from.equals(that.to) && this.to.equals(that.from))
          && this.weight == that.weight);
    }
  }

  // produces a hashcode for this edge
  @Override
  public int hashCode() {
    return this.from.hashCode() + this.to.hashCode() + this.weight * 1000;
  }
}

//////////////////////////////////////////////////////////////////////
class Utils {
  // Sorts the edges
  ArrayList<Edge> sortEdges(ArrayList<Edge> edge) {
    Collections.sort(edge);
    return edge;
  }
}

//////////////////////////////////////////////////////////////////////
class ExamplesMazes {
  WorldScene backgroundWall;
  WorldScene backgroundWallV2;
  WorldScene leftWall;
  MazesWorld mazeWorld;
  MazesWorld mazeWorldSeeded;
  Node A;
  Node B;
  Node C;
  Node D;
  Node E;
  Node F;
  Edge AB;
  Edge AE;
  Edge BC;
  Edge BE;
  Edge BF;
  Edge CD;
  Edge EC;
  Edge FD;
  ArrayList<Edge> unSorted;
  ArrayList<Edge> sorted;
  HashMap<Node, Node> nodesHashMap;
  Node G;
  Node H;
  Node I;
  Node J;
  Edge GH;
  Edge GI;
  Edge HJ;
  Edge IJ;
  ArrayList<Edge> seededList;
  ArrayList<Node> seededNodes;

  // initializes the data
  void initData() {
    backgroundWall = new WorldScene(4, 4);
    backgroundWallV2 = new WorldScene(4, 4);
    mazeWorld = new MazesWorld(5, 5, 100, 100, new Random());
    A = new Node(0, 0);
    B = new Node(0, 1);
    C = new Node(0, 2);
    D = new Node(1, 0);
    E = new Node(1, 1);
    F = new Node(1, 2);
    AB = new Edge(A, B, 30);
    AE = new Edge(A, E, 50);
    BC = new Edge(B, C, 40);
    BE = new Edge(B, E, 35);
    BF = new Edge(B, F, 50);
    CD = new Edge(C, D, 25);
    EC = new Edge(E, C, 15);
    FD = new Edge(F, D, 50);
    this.A.listOfEdges.add(this.AB);
    this.A.listOfEdges.add(this.AE);
    this.B.listOfEdges.add(this.BC);
    this.B.listOfEdges.add(this.BE);
    this.B.listOfEdges.add(this.BF);
    this.B.listOfEdges.add(this.CD);
    this.E.listOfEdges.add(this.EC);
    this.F.listOfEdges.add(this.FD);
    unSorted = new ArrayList<Edge>();
    unSorted.add(AB);
    unSorted.add(AE);
    unSorted.add(BC);
    unSorted.add(BE);
    unSorted.add(BF);
    unSorted.add(CD);
    unSorted.add(EC);
    unSorted.add(FD);
    sorted = new ArrayList<Edge>();
    sorted.add(EC);
    sorted.add(CD);
    sorted.add(AB);
    sorted.add(BE);
    sorted.add(BC);
    sorted.add(AE);
    sorted.add(BF);
    sorted.add(FD);
    nodesHashMap = new HashMap<Node, Node>();
    nodesHashMap.put(A, F);
    nodesHashMap.put(B, A);
    nodesHashMap.put(C, F);
    nodesHashMap.put(D, E);
    nodesHashMap.put(E, E);
    nodesHashMap.put(F, D);
    mazeWorldSeeded = new MazesWorld(2, 2, 100, 100, new Random(2));
    // G(0,0) I(1,0)
    // H(0,1) J(1,1)
    G = new Node(0, 0);
    H = new Node(0, 1);
    I = new Node(1, 0);
    J = new Node(1, 1);
    GH = new Edge(G, H, 72);
    GI = new Edge(G, I, 8);
    IJ = new Edge(I, J, 67);
    HJ = new Edge(H, J, 40);
    G.listOfEdges.add(GH);
    G.listOfEdges.add(GI);
    H.listOfEdges.add(HJ);
    I.listOfEdges.add(IJ);
    seededList = new ArrayList<Edge>();
    seededList.add(GH);
    seededList.add(HJ);
    seededList.add(IJ);
  }

  // initializes the world
  void testMazesWorld(Tester t) {
    World starterWorld = new MazesWorld(20, 20, 100, 100);
    starterWorld.bigBang(600, 600, 0.05);
  }

  // tests the sortEdges method in the Utils class
  void testSortEdges(Tester t) {
    this.initData();
    t.checkExpect(new Utils().sortEdges(this.unSorted), this.sorted);
    t.checkExpect(new Utils().sortEdges(this.unSorted), this.sorted);
  }

  // test the minSpanningTree method in the MazeWorld class
  void testMinSpanningTree(Tester t) {
    this.initData();
    t.checkExpect(mazeWorld.edgesToGoInTree.size(), mazeWorld.sizeX * mazeWorld.sizeY - 1);
    t.checkExpect(mazeWorldSeeded.edgesToGoInTree.get(0).equals(GI), true);
    t.checkExpect(mazeWorldSeeded.edgesToGoInTree.get(1).equals(HJ), true);
    t.checkExpect(mazeWorldSeeded.edgesToGoInTree.get(2).equals(IJ), true);
  }

  // test the minSpanningTree method in the MazeWorld class
  void testMakeGraph(Tester t) {
    this.initData();
    System.out.println("Weight" + mazeWorldSeeded.edges.get(1).weight + " = " + HJ.weight);
    t.checkExpect(mazeWorldSeeded.nodes.get(0).equals(G), true);
    t.checkExpect(mazeWorldSeeded.nodes.get(1).equals(H), true);
    t.checkExpect(mazeWorldSeeded.nodes.get(2).equals(I), true);
    t.checkExpect(mazeWorldSeeded.nodes.get(3).equals(J), true);
    t.checkExpect(mazeWorldSeeded.edges.size(), 4);
    t.checkExpect(mazeWorldSeeded.edges.get(0).equals(GI), true);
    t.checkExpect(mazeWorldSeeded.edges.get(1).equals(HJ), true);
    t.checkExpect(mazeWorldSeeded.edges.get(2).equals(IJ), true);
    t.checkExpect(mazeWorldSeeded.edges.get(3).equals(GH), true);
  }

  // test the minSpanningTree method in the MazeWorld class
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene background = new WorldScene(500, 500);
    // draws the grid
    background.placeImageXY(new RectangleImage(250, 250, OutlineMode.SOLID, Color.lightGray),
        0 * 250 + (250 / 2 + 1), 0 * 250 + (250 / 2 + 1));
    background.placeImageXY(new RectangleImage(250, 250, OutlineMode.SOLID, Color.lightGray),
        0 * 250 + (250 / 2 + 1), 1 * 250 + (250 / 2 + 1));
    background.placeImageXY(new RectangleImage(250, 250, OutlineMode.SOLID, Color.lightGray),
        1 * 250 + (250 / 2 + 1), 0 * 250 + (250 / 2 + 1));
    background.placeImageXY(new RectangleImage(250, 250, OutlineMode.SOLID, Color.lightGray),
        1 * 250 + (250 / 2 + 1), 1 * 250 + (250 / 2 + 1));
    // draws the edges
    GH.drawWalls(GH.to, GH.from, background, 250);
    background.placeImageXY(new RectangleImage(250, 250, OutlineMode.SOLID, Color.magenta),
        2 * 250 - (250 / 2), 2 * 250 - (250 / 2));
    background.placeImageXY(new RectangleImage(250, 250, OutlineMode.SOLID, Color.green),
        0 + (250 / 2), 0 + (250 / 2));
    // mazeWorldSeeded.bigBang(600, 600, 1.0);
    t.checkExpect(mazeWorldSeeded.makeScene(), background);
    // testing the statement, if searchedPath isn't empty
    // so, drawing the searching
    mazeWorldSeeded.breadthFirst();
    mazeWorldSeeded.makeScene();
    Color lightBlue = new Color(77, 166, 255); // light blue
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, lightBlue);
    mazeWorldSeeded.makeScene();
    t.checkExpect(mazeWorldSeeded.nodes.get(2).color, lightBlue);
    mazeWorldSeeded.reachedEnd = true;
    mazeWorldSeeded.makeScene();
    // testing drawing the solution
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, Color.blue);
    t.checkExpect(mazeWorldSeeded.nodes.get(2).color, Color.blue);
    t.checkExpect(mazeWorldSeeded.nodes.get(3).color, Color.blue);
    mazeWorldSeeded.reachedEnd = false;
  }

  // test the find method in the MazeWorld class
  void testFind(Tester t) {
    this.initData();
    t.checkExpect(this.mazeWorld.find(nodesHashMap, D), E);
    t.checkExpect(this.mazeWorld.find(nodesHashMap, F), E);
    t.checkExpect(this.mazeWorld.find(nodesHashMap, B), E);
  }

  // test the union method in the MazeWorld class
  void testUnion(Tester t) {
    this.initData();
    this.mazeWorld.union(nodesHashMap, F, E);
    t.checkExpect(this.nodesHashMap.get(this.F), this.E);
  }

  // test the drawWalls method in the Edge class
  void testDrawWalls(Tester t) {
    // testing horizontal walls
    this.initData();
    this.backgroundWall.placeImageXY(new RectangleImage(4, 2, OutlineMode.SOLID, Color.black),
        ((1) * 4 - (4 / 2)), ((2) * 4));
    t.checkExpect(AB.drawWalls(A, B, new WorldScene(4, 4), 4), backgroundWall);
    // testing vertical walls
    this.backgroundWallV2.placeImageXY(new RectangleImage(2, 4, OutlineMode.SOLID, Color.black),
        ((1) * 4 / 2 + (4 / 2)), ((2) * 4 / 2 + (4 / 2)));
    t.checkExpect(BE.drawWalls(B, E, new WorldScene(4, 4), 4), backgroundWallV2);
  }

  // test the compareTo method in the Maze class
  void testCompareTo(Tester t) {
    this.initData();
    t.checkExpect(this.AE.compareTo(this.AB), 1);
    t.checkExpect(this.AB.compareTo(this.AE), -1);
    t.checkExpect(this.BF.compareTo(this.FD), 0);
  }

  // test the Equals method in the Node/Edge class
  void testEquals(Tester t) {
    this.initData();
    t.checkExpect(this.A.equals(this.A), true);
    t.checkExpect(this.B.equals(this.A), false);
    t.checkExpect(this.AB.equals(this.AB), true);
    t.checkExpect(this.AB.equals(this.CD), false);
  }

  // test the HashCode method in the Node/Edge class
  void testHashCode(Tester t) {
    this.initData();
    Object oN = new Node(0, 0);
    Object oE = new Edge(A, B, 30);
    t.checkExpect(this.A.equals(oN), true);
    t.checkExpect(this.B.equals(oN), false);
    t.checkExpect(this.AB.equals(oE), true);
    t.checkExpect(this.CD.equals(oE), false);
  }

  // test hadEdgeTo
  void testHasEdgeTo(Tester t) {
    this.initData();
    t.checkExpect(G.hasEdgeTo(I), true);
    t.checkExpect(G.hasEdgeTo(H), true);
    t.checkExpect(H.hasEdgeTo(J), true);
    t.checkExpect(J.hasEdgeTo(H), false);
    t.checkExpect(G.hasEdgeTo(J), false);
  }

  // test get edge going to method
  void testGetEdgeTo(Tester t) {
    this.initData();
    t.checkExpect(G.getEdgeGoingTo(I), GI);
    t.checkExpect(G.getEdgeGoingTo(H), GH);
    t.checkExpect(H.getEdgeGoingTo(J), HJ);
    t.checkExpect(I.getEdgeGoingTo(J), IJ);
  }

  // tests the reconstruct method for MazeWorld
  void testReconstruct(Tester t) {
    this.initData();
    mazeWorldSeeded.breadthFirst();
    mazeWorldSeeded.reconstruct(mazeWorldSeeded.cameFromEdge, mazeWorldSeeded.nodes.get(3));
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, Color.blue);
    t.checkExpect(mazeWorldSeeded.nodes.get(2).color, Color.blue);
    t.checkExpect(mazeWorldSeeded.nodes.get(3).color, Color.blue);
  }

  // test on key event, for r, b, d, v, and h.
  void testOnKeyEvent(Tester t) {
    this.initData();
    ArrayList<Edge> tempEdges = new ArrayList<Edge>(mazeWorldSeeded.edges);
    mazeWorldSeeded.onKeyEvent("r");
    t.checkExpect(mazeWorldSeeded.edges != tempEdges, true); // restarts the Maze
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, Color.lightGray);
    mazeWorldSeeded.onKeyEvent("b");
    mazeWorldSeeded.makeScene();
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, new Color(77, 166, 255)); // does breadth
    // first search
    mazeWorldSeeded.onKeyEvent("r");
    mazeWorldSeeded.onKeyEvent("d");
    mazeWorldSeeded.makeScene();
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, new Color(77, 166, 255)); // does depth first
    // search
    // mazes with a bias in a vertical direction - testing edge weights
    // only testing 2 because should be 2 vertical
    mazeWorldSeeded.onKeyEvent("v");
    t.checkExpect(mazeWorldSeeded.edges.get(0).weight <= 30, true);
    t.checkExpect(mazeWorldSeeded.edges.get(1).weight <= 30, true);
    // mazes with a bias in a horizontal direction
    // only testing 2 because should be 2 horizontal
    mazeWorldSeeded.onKeyEvent("h");
    t.checkExpect(mazeWorldSeeded.edges.get(0).weight <= 30, true);
    t.checkExpect(mazeWorldSeeded.edges.get(1).weight <= 30, true);
  }

  // test the breadthfirst search method
  void testBreadthFirst(Tester t) {
    this.initData();
    mazeWorldSeeded.breadthFirst();
    mazeWorldSeeded.makeScene();
    t.checkExpect(this.mazeWorldSeeded.searchedPath.isEmpty(), false);
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, new Color(77, 166, 255));
    t.checkExpect(this.mazeWorldSeeded.reachedEnd, true);
  }

  // test the depthfirst search method
  void testDepthFirst(Tester t) {
    this.initData();
    mazeWorldSeeded.depthFirst();
    mazeWorldSeeded.makeScene();
    t.checkExpect(this.mazeWorldSeeded.searchedPath.isEmpty(), false);
    t.checkExpect(mazeWorldSeeded.nodes.get(0).color, new Color(77, 166, 255));
    t.checkExpect(this.mazeWorldSeeded.reachedEnd, true);
  }

  // test exceptions in the constructor
  void testExceptions(Tester t) {
    this.initData();
    t.checkConstructorException(new IllegalArgumentException("sizeX cannot be bigger than 100"),
        "MazesWorld", 101, 20, 100, 100);
    t.checkConstructorException(new IllegalArgumentException("sizeY cannot be bigger than 60"),
        "MazesWorld", 20, 61, 100, 100);
  }
}
