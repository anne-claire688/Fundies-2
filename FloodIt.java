import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//abstract class for Cells and MtCells
abstract class ACell {

  // checks if this ACell has an adjacent cells that are flooded
  abstract boolean hasAdjacentFlooded();

  // recurses through the neighbor cells to make sure that if the colors circle
  // around backwards,
  // none miss being marked 'flooded' by the for-loop that goes down the board in
  // order
  abstract void recurseAdjacentFlooding(Color color);

  // now that we know the cell should indeed be flooded=true,
  // recurseAdjacentFloodingHelper
  // changes it to true
  abstract void recurseAdjacentFloodingHelper(Color color);

  abstract void waterfallHelper(Color color);

  abstract void waterfallHelper2(Color color);

  // method to reassign top and bottom in the Cell constructor
  abstract void reassign(String newAssignment, ACell cellToAssign);

  // checks if this ACell has flooded = true
  abstract boolean isFlooded();

}

// Represents a single square of the game area
class Cell extends ACell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  ACell left;
  ACell top;
  ACell right;
  ACell bottom;

  // constructor for cell that takes in information like x, y, color, and adjacent
  // cells,
  // and connects cells together.
  Cell(int x, int y, Color color, ACell left, ACell top, ACell right, ACell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = false;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;

    this.top.reassign("bottom", this);
    this.bottom.reassign("top", this);
    this.left.reassign("right", this);
    this.right.reassign("left", this);

  }

  // checks if this cell has any adjacent cells that are flooded=true
  boolean hasAdjacentFlooded() {
    return (this.top.isFlooded()) || (this.left.isFlooded()) || (this.right.isFlooded())
        || (this.bottom.isFlooded());
  }

  // checks if any adjacent cells should be flooded=true and does so, then
  // recurses on its adjacents
  void recurseAdjacentFlooding(Color color) {
    this.top.recurseAdjacentFloodingHelper(color);
    this.left.recurseAdjacentFloodingHelper(color);

    this.right.recurseAdjacentFloodingHelper(color);

    this.bottom.recurseAdjacentFloodingHelper(color);
  }

  // now that we know the cell should indeed be flooded=true,
  // recurseAdjacentFloodingHelper does so.
  void recurseAdjacentFloodingHelper(Color color) {
    if (this.color == color && !(this.flooded)) {
      this.flooded = true;
      this.recurseAdjacentFlooding(color);
    }
  }

  void waterfallHelper(Color color) {
    this.top.waterfallHelper2(color);
    this.left.waterfallHelper2(color);
    this.bottom.waterfallHelper2(color);
    this.right.waterfallHelper2(color);
  }

  void waterfallHelper2(Color color) {
    if ((this.color != color) && this.flooded) {
      this.color = color;
      this.waterfallHelper(color);
    }
  }

  // method to reassign top and bottom in the Cell constructor
  void reassign(String newAssignment, ACell cellToAssign) {
    if (newAssignment.equals("bottom")) {
      this.bottom = cellToAssign;
    }
    if (newAssignment.equals("top")) {
      this.top = cellToAssign;
    }
    if (newAssignment.equals("right")) {
      this.right = cellToAssign;
    }
    if (newAssignment.equals("left")) {
      this.left = cellToAssign;
    }
  }

  // checks if this ACell has flooded = true
  boolean isFlooded() {
    return this.flooded;
  }

}

class MtCell extends ACell {

  // checks if this ACell has any adjacent flooded cells
  // will always return false for an edge/empty cell because it has no fields to
  // store adjacent cells
  boolean hasAdjacentFlooded() {
    return false;
  }

  // recurses through the neighbor cells to make sure that if the colors circle
  // around backwards,
  // none miss being marked 'flooded' by the for-loop that goes down the board in
  // order.

  void recurseAdjacentFlooding(Color color) {
    // does nothing in MtCell, because mtCell signals reaching the end of the
    // recursion
  }

  // changes this ACell to the desired color.
  void recurseAdjacentFloodingHelper(Color color) {
    // does nothing in MtCell, because mtCell doesn't have any color field to change

  }

  void waterfallHelper(Color color) {
    // does nothing in MtCell,
  }

  void waterfallHelper2(Color color) {
    // does nothing in MtCell,
  }

  // method to reassign top and bottom in the Cell constructor

  void reassign(String newAssignment, ACell cellToAssign) {
    // does nothing in empty cell because mtCell has no fields
  }

  // checks if this ACell has flooded = true, which will always return false for
  // an edge/empty cell
  boolean isFlooded() {
    return false;
  }

}

//////////////////////////////////////////////////////////////////////////////////////////

//represents a game of Flood It
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<ArrayList<Cell>> board;
  boolean flooding;
  boolean gameEnded;
  boolean gameEndedWin;
  Random rand;
  int clickNum;
  int numColors;
  Color colorClicked;
  int waterfallIndex;
  double timeFromStart;

  // constructor that only takes in a size and number of colors, for playing the
  // game
  FloodItWorld(int size, int numColors) {
    if ((numColors > 8) || (numColors < 3)) {
      throw new IllegalArgumentException("must input 3-8 for the number of colors");
    }
    else if ((size > 26) || (size < 2)) {
      throw new IllegalArgumentException("must input 2-26 for the size of the board");
    }
    else {
      this.board = new Utils().makeBoard2(size, numColors, new Random());
      this.flooding = false;
      this.gameEnded = false;
      this.rand = new Random();
      this.clickNum = 0;
      this.numColors = numColors;
      this.colorClicked = null;
      this.waterfallIndex = 0;
      this.timeFromStart = -0.5;
    }
  }

  // constructor that takes in a random with a seed, for testing purposes
  FloodItWorld(int size, int numColors, Random rand) {
    // add an exception if numColors is > 8
    this.board = new Utils().makeBoard2(size, numColors, rand);
    this.flooding = false;
    this.gameEnded = false;
    this.rand = rand;
    this.clickNum = 0;
    this.numColors = numColors;
    this.colorClicked = null;
    this.waterfallIndex = 0;
  }

  // worldScene method, which makes the scene to be depicted
  public WorldScene makeScene() {
    int allCell = ((this.board.size() * this.board.size()) / 5) + (this.numColors - 3) + 2;
    if (gameEnded) {
      return lastScene("You Lose!");
    }
    else if (gameEndedWin) {
      return lastScene("You Win :)");
    }

    else {

      // drawing the normal board
      WorldScene worldScene = new WorldScene(this.board.size() * 50,
          (this.board.size() * 50) + 100);
      for (ArrayList<Cell> list : this.board) {
        for (Cell cell : list) {
          worldScene.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, cell.color),
              cell.x * 50 + 25, cell.y * 50 + 25);
        }
      }
      worldScene.placeImageXY(
          new TextImage(Integer.toString(this.clickNum) + "/" + Integer.toString(allCell), 20,
              Color.black),
          30, (this.board.size() * 50) + 20);
      worldScene.placeImageXY(new TextImage(
          "Seconds elapsed: " + Integer.toString(new Double(this.timeFromStart).intValue()), 20,
          Color.black), 100, (this.board.size() * 50) + 50);

      return worldScene;
    }
  }

  // on mouse click method, for determining which color the player clicked and
  // making the appropriate changes
  // EFFECT: Changes this.flooding to true and starts the flooding
  public void onMouseClicked(Posn pos) {
    if (this.clickNum < (((this.board.size() * this.board.size()) / 5) + (this.numColors - 3)
        + 2)) {
      Color colorClicked = this.board.get(pos.x / 50).get(pos.y / 50).color;
      if (this.board.get(0).get(0).color != colorClicked) {
        this.clickNum = this.clickNum + 1;
      }
      new Utils().changeToFlooded(colorClicked, this.board);
      this.colorClicked = colorClicked;
      this.flooding = true;
      this.gameEndedWin = true;
      for (ArrayList<Cell> list : board) {
        for (Cell cell : list) {
          if (!(cell.flooded)) {
            this.gameEndedWin = false;
          }
        }
      }

    }
    else {
      this.gameEnded = true;
    }
  }

  // onTick method, which handles waterfalling and updating time elapsed
  public void onTick() {
    // this 0.2 is based on our tick rate when we run big bang
    this.timeFromStart = this.timeFromStart + 0.2;

    if (this.waterfallIndex >= (this.board.size() * 2)) {
      this.flooding = false;
      this.waterfallIndex = 0;
    }
    if (this.flooding) {
      int jHolder = this.waterfallIndex;
      for (int i = 0; i <= this.waterfallIndex; i++) {
        for (int j = jHolder; j >= 0; j = j - 1) {
          int tempi = i;
          int tempj = j;
          if (i >= this.board.size()) {
            tempi = this.board.size() - 1;
          }
          if (j >= this.board.size()) {
            tempj = this.board.size() - 1;
          }
          if (this.board.get(tempi).get(tempj).flooded) {
            this.board.get(tempi).get(tempj).color = this.colorClicked;
          }
        }
        jHolder--;
      }
      this.waterfallIndex = this.waterfallIndex + 1;
    }
  }

  // represents the endscene of the game, whether win or lose
  public WorldScene lastScene(String msg) {
    WorldScene background = new WorldScene(200, 200);
    background.placeImageXY(new TextImage(msg, 20, Color.black), 100, 100);
    return background;
  }

  // if r is typed, reset the board and game
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = new Utils().makeBoard2(this.board.size(), this.numColors, this.rand);
      this.clickNum = 0;
      this.gameEnded = false;
    }
  }

}

//////////////////////////////////////////////////////////////////////////////////////////

//utils class for classless functions
class Utils {

  // makes the initial board
  ArrayList<ArrayList<Cell>> makeBoard2(int size, int numColors, Random rand) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < size; i++) {
      board.add(new ArrayList<Cell>());
    }
    for (int i = 0; i < size; i++) {
      for (int k = 0; k < size; k++) {
        if (i == 0 && k == 0) {
          board.get(i).add(new Cell(i, k, new Utils().getColor(rand.nextInt(numColors)),
              new MtCell(), new MtCell(), new MtCell(), new MtCell()));
        }
        else if (i == 0 && k != 0) {
          board.get(i).add(new Cell(i, k, new Utils().getColor(rand.nextInt(numColors)),
              new MtCell(), board.get(i).get(k - 1), new MtCell(), new MtCell()));
        }
        else if (i != 0 && k == 0) {
          board.get(i).add(new Cell(i, k, new Utils().getColor(rand.nextInt(numColors)),
              board.get(i - 1).get(k), new MtCell(), new MtCell(), new MtCell()));
        }
        else if (i != 0 && k != 0) {
          board.get(i).add(new Cell(i, k, new Utils().getColor(rand.nextInt(numColors)),
              board.get(i - 1).get(k), board.get(i).get(k - 1), new MtCell(), new MtCell()));
        }
      }
    }
    board.get(0).get(0).flooded = true;
    new Utils().changeToFlooded(board.get(0).get(0).color, board);
    return board;
  }

  // returns the correct color based on the random number provided
  Color getColor(int num) {
    if (num == 0) {
      return Color.red;
    }
    else if (num == 1) {
      return Color.blue;
    }
    else if (num == 2) {
      return Color.green;
    }
    else if (num == 3) {
      return Color.yellow;
    }
    else if (num == 4) {
      return Color.PINK;
    }
    else if (num == 5) {
      return Color.orange;
    }
    else if (num == 6) {
      return Color.magenta;
    }
    else {
      return Color.cyan;
    }
  }

  // changes the correct squares to flooded=true,
  // if they are the right color and have an adjacent square flooded
  void changeToFlooded(Color color, ArrayList<ArrayList<Cell>> board) {
    for (ArrayList<Cell> list : board) {
      for (Cell cell : list) {
        if ((cell.color == color) && cell.hasAdjacentFlooded()) {
          cell.flooded = true;
          cell.recurseAdjacentFlooding(color);
        }
      }
    }
  }

  // changes the colors of the flooded=true squares in a waterfall pattern
  void waterfall(ArrayList<ArrayList<Cell>> board, Color color) {
    board.get(0).get(0).color = color;
    board.get(0).get(0).waterfallHelper(color);
  }

}

//////////////////////////////////////////////////////////////////////////////////////////

//examples class for Flood It 
class ExamplesFloodIt {

  MtCell mtCell = new MtCell();

  // constructing an example of a 2x2 board:
  // blue(0,0) green(1,0)
  // red(0,1) blue(1,1)
  Cell cell1 = new Cell(0, 0, Color.blue, new MtCell(), new MtCell(), new MtCell(), new MtCell());
  Cell cell2 = new Cell(1, 0, Color.green, this.cell1, this.mtCell, new MtCell(), new MtCell());
  Cell cell3 = new Cell(0, 1, Color.red, this.mtCell, this.cell1, new MtCell(), this.mtCell);
  Cell cell4 = new Cell(1, 1, Color.blue, this.cell3, this.cell2, this.mtCell, this.mtCell);
  ArrayList<Cell> list1 = new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell3));
  ArrayList<Cell> list2 = new ArrayList<Cell>(Arrays.asList(this.cell2, this.cell4));
  ArrayList<ArrayList<Cell>> board1 = new ArrayList<ArrayList<Cell>>(
      Arrays.asList(this.list1, this.list2));
  Random randWithSeed = new Random(2);
  Random randWithSeed2 = new Random(5);

  // constructing an example of a 3x3 board:
  // red(0,0) red(1,0) green (2,0)
  // red(0,1) blue(1,1) green (2,1)
  // red(0,2) red(1,2) blue (2,2)

  Cell cell11 = new Cell(0, 0, Color.red, new MtCell(), new MtCell(), new MtCell(), new MtCell());
  Cell cell22 = new Cell(1, 0, Color.red, this.cell11, new MtCell(), new MtCell(), new MtCell());
  Cell cell33 = new Cell(2, 0, Color.green, this.cell22, new MtCell(), new MtCell(), new MtCell());
  Cell cell44 = new Cell(0, 1, Color.red, new MtCell(), this.cell11, new MtCell(), new MtCell());
  Cell cell55 = new Cell(1, 1, Color.blue, this.cell44, this.cell22, new MtCell(), new MtCell());
  Cell cell66 = new Cell(2, 1, Color.green, this.cell55, this.cell33, new MtCell(), new MtCell());
  Cell cell77 = new Cell(0, 2, Color.red, new MtCell(), this.cell44, new MtCell(), new MtCell());
  Cell cell88 = new Cell(1, 2, Color.red, this.cell77, this.cell55, new MtCell(), new MtCell());
  Cell cell99 = new Cell(2, 2, Color.blue, this.cell88, this.cell66, new MtCell(), new MtCell());
  ArrayList<Cell> list11 = new ArrayList<Cell>(
      Arrays.asList(this.cell11, this.cell44, this.cell77));
  ArrayList<Cell> list22 = new ArrayList<Cell>(
      Arrays.asList(this.cell22, this.cell55, this.cell88));
  ArrayList<Cell> list33 = new ArrayList<Cell>(
      Arrays.asList(this.cell33, this.cell66, this.cell99));
  ArrayList<ArrayList<Cell>> board2 = new ArrayList<ArrayList<Cell>>(
      Arrays.asList(this.list11, this.list22, this.list33));

  // drawing a 2x2 board
  FloodItWorld worldWithSeed = new FloodItWorld(2, 3, this.randWithSeed2);
  WorldScene scene1 = new WorldScene(100, 200);
  // drawing a 3x3 board
  FloodItWorld worldWithSeed2 = new FloodItWorld(3, 3, this.randWithSeed2);
  WorldScene scene2 = new WorldScene(150, 250);

  // re-initializes our example data's colors and .flooded to reset it for
  // different tests
  void initData() {
    this.cell1.color = Color.blue;
    this.cell1.flooded = true;
    this.cell2.color = Color.green;
    this.cell2.flooded = false;
    this.cell3.color = Color.red;
    this.cell3.flooded = false;
    this.cell4.color = Color.blue;
    this.cell4.flooded = false;

    this.cell11.color = Color.red;
    this.cell22.color = Color.red;
    this.cell33.color = Color.green;
    this.cell44.color = Color.red;
    this.cell55.color = Color.blue;
    this.cell66.color = Color.green;
    this.cell77.color = Color.red;
    this.cell88.color = Color.red;
    this.cell99.color = Color.blue;

    this.cell11.flooded = true;
    this.cell22.flooded = true;
    this.cell44.flooded = true;
    this.cell77.flooded = true;
    this.cell88.flooded = true;
    this.cell33.flooded = false;
    this.cell55.flooded = false;
    this.cell66.flooded = false;
    this.cell99.flooded = false;

  }

  // initializes the world
  void testFloodItWorld(Tester t) {
    this.initData();
    FloodItWorld starterWorld = new FloodItWorld(7, 5);
    starterWorld.bigBang(starterWorld.board.size() * 50, starterWorld.board.size() * 50 + 100, 0.2);
  }

  // tests the method makeScene for the class FloodItWorld
  void testMakeScene(Tester t) {
    this.scene1.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 25, 25);
    this.scene1.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.blue), 25, 75);
    this.scene1.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 75, 25);
    this.scene1.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 75, 75);
    this.scene1.placeImageXY(new TextImage(0 + "/" + 2, 20, Color.black), 30, (2 * 50) + 20);
    this.scene1.placeImageXY(
        new TextImage("Seconds elapsed: " + Integer.toString(0), 20, Color.black), 100,
        (2 * 50) + 50);
    t.checkExpect(this.worldWithSeed.makeScene(), this.scene1);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.red), 25, 25);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 25, 75);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.blue), 25, 125);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 75, 25);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 75, 75);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.red), 75, 125);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.green), 125, 25);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.red), 125, 75);
    this.scene2.placeImageXY(new RectangleImage(50, 50, OutlineMode.SOLID, Color.blue), 125, 125);
    this.scene2.placeImageXY(new TextImage("0" + "/" + Integer.toString(3), 20, Color.black), 30,
        (3 * 50) + 20);
    this.scene2.placeImageXY(
        new TextImage("Seconds elapsed: " + Integer.toString(0), 20, Color.black), 100,
        (3 * 50) + 50);
    t.checkExpect(this.worldWithSeed2.makeScene(), this.scene2);
  }

  // tests the method makeBoard2 for the class Utils
  void testMakeBoard2(Tester t) {
    this.initData();
    t.checkExpect(new Utils().makeBoard2(2, 3, randWithSeed), board1);
    t.checkExpect(new Utils().makeBoard2(3, 3, randWithSeed), board2);

  }

  // tests the method getColor for the class Utils
  void testGetColor(Tester t) {
    this.initData();
    t.checkExpect(new Utils().getColor(0), Color.red);
    t.checkExpect(new Utils().getColor(1), Color.blue);
    t.checkExpect(new Utils().getColor(2), Color.green);
    t.checkExpect(new Utils().getColor(3), Color.yellow);
    t.checkExpect(new Utils().getColor(4), Color.PINK);
    t.checkExpect(new Utils().getColor(5), Color.orange);
    t.checkExpect(new Utils().getColor(6), Color.magenta);
    t.checkExpect(new Utils().getColor(7), Color.cyan);
    t.checkExpect(new Utils().getColor(12), Color.cyan);

  }

  // test exceptions in the constructor
  void testExceptions(Tester t) {
    this.initData();
    t.checkConstructorException(
        new IllegalArgumentException("must input 3-8 for the number of colors"), "FloodItWorld", 2,
        10);
    t.checkConstructorException(
        new IllegalArgumentException("must input 3-8 for the number of colors"), "FloodItWorld", 2,
        0);
    t.checkConstructorException(
        new IllegalArgumentException("must input 3-8 for the number of colors"), "FloodItWorld", 2,
        2);
    t.checkConstructorException(
        new IllegalArgumentException("must input 2-26 for the size of the board"), "FloodItWorld",
        1, 5);
    t.checkConstructorException(
        new IllegalArgumentException("must input 2-26 for the size of the board"), "FloodItWorld",
        30, 5);
    t.checkConstructorException(
        new IllegalArgumentException("must input 2-26 for the size of the board"), "FloodItWorld",
        0, 5);
  }

  // test the method hasAdjacentFlooded in ACell
  void testHasAdjacentFlooded(Tester t) {
    this.initData();
    t.checkExpect(this.cell55.hasAdjacentFlooded(), true);
    t.checkExpect(this.cell44.hasAdjacentFlooded(), true);
    t.checkExpect(this.cell66.hasAdjacentFlooded(), false);
    t.checkExpect(this.mtCell.hasAdjacentFlooded(), false);
  }

  // test the method recurseAdjacentFlooding in the ACell classes
  void testRecurseAdjacentFlooding(Tester t) {
    this.initData();
    this.cell66.recurseAdjacentFlooding(Color.green);
    t.checkExpect(this.cell66.flooded, true);
    t.checkExpect(this.cell33.flooded, true);
    this.cell4.recurseAdjacentFlooding(Color.red);
    t.checkExpect(this.cell4.flooded, false);
    t.checkExpect(this.cell3.flooded, true);
  }

  // test the method recurseAdjacentFloodingHelper in the ACell classes
  void testRecurseAdjacentFloodingHelper(Tester t) {
    this.initData();
    this.cell4.recurseAdjacentFloodingHelper(Color.green);
    t.checkExpect(this.cell4.flooded, false);
    this.cell66.recurseAdjacentFloodingHelper(Color.green);
    t.checkExpect(this.cell66.flooded, true);
    t.checkExpect(this.cell33.flooded, true);
  }

  // test the method reassign in the ACell classes
  void testReassign(Tester t) {
    this.initData();
    t.checkExpect(this.cell99.top, this.cell66);
    t.checkExpect(this.cell99.left, this.cell88);
    t.checkExpect(this.cell99.bottom, this.mtCell);
    this.cell99.reassign("top", this.cell88);
    this.cell99.reassign("left", this.mtCell);
    this.cell99.reassign("bottom", this.cell66);
    t.checkExpect(this.cell99.top, this.cell88);
    t.checkExpect(this.cell99.left, this.mtCell);
    t.checkExpect(this.cell99.bottom, this.cell66);
    this.cell99.reassign("top", this.cell66);
    this.cell99.reassign("left", this.cell88);
    this.cell99.reassign("bottom", this.mtCell);
    t.checkExpect(this.cell99.top, this.cell66);
    t.checkExpect(this.cell99.left, this.cell88);
    t.checkExpect(this.cell99.bottom, this.mtCell);

    // resetting fields back to start conditions so other tests pass
    this.worldWithSeed.timeFromStart = 0;
    this.worldWithSeed.flooding = false;
    this.worldWithSeed.colorClicked = null;
    this.worldWithSeed.board.get(0).get(0).flooded = true;
    this.worldWithSeed.board.get(0).get(1).flooded = false;
    this.worldWithSeed.board.get(1).get(0).flooded = true;
    this.worldWithSeed.board.get(1).get(1).flooded = true;
    this.worldWithSeed.board.get(0).get(0).color = Color.green;
    this.worldWithSeed.board.get(0).get(1).color = Color.blue;
    this.worldWithSeed.board.get(1).get(0).color = Color.green;
    this.worldWithSeed.board.get(1).get(1).color = Color.green;

  }

  // test the method isFlooded in the ACell classes
  void testIsFlooded(Tester t) {
    this.initData();
    t.checkExpect(this.cell1.isFlooded(), true);
    t.checkExpect(this.cell11.isFlooded(), true);
    t.checkExpect(this.cell99.isFlooded(), false);
    t.checkExpect(this.mtCell.isFlooded(), false);

  }

  // test the method changeToFlooded in the Utils class
  void testChangeToFlooded(Tester t) {
    this.initData();
    new Utils().changeToFlooded(Color.green, this.board1);
    t.checkExpect(this.cell1.flooded, true);
    t.checkExpect(this.cell2.flooded, true);
    t.checkExpect(this.cell3.flooded, false);
    t.checkExpect(this.cell4.flooded, false);

    new Utils().changeToFlooded(Color.green, this.board2);
    t.checkExpect(this.cell33.flooded, true);
    t.checkExpect(this.cell66.flooded, true);
    t.checkExpect(this.cell55.flooded, false);
    t.checkExpect(this.cell99.flooded, false);

  }

  // test the method onTick in the FloodItWorld class
  void testOnTick(Tester t) {
    this.initData();
    this.worldWithSeed.onTick();
    t.checkExpect(this.worldWithSeed.timeFromStart, 0.2);

    this.worldWithSeed.flooding = true;
    this.worldWithSeed.colorClicked = Color.blue;
    this.worldWithSeed.onTick();
    t.checkExpect(this.worldWithSeed.board.get(1).get(0).flooded, true);
    t.checkExpect(this.worldWithSeed.board.get(0).get(0).color, Color.blue);
    this.worldWithSeed.onTick();
    t.checkExpect(this.worldWithSeed.board.get(1).get(0).color, Color.blue);
    t.checkExpect(this.worldWithSeed.board.get(0).get(1).color, Color.blue);
    this.worldWithSeed.onTick();
    t.checkExpect(this.worldWithSeed.board.get(1).get(1).color, Color.blue);

    // resetting fields back to start conditions so other tests pass
    this.worldWithSeed.gameEnded = false;
    this.worldWithSeed.gameEndedWin = false;
    this.worldWithSeed.timeFromStart = 0;
    this.worldWithSeed.clickNum = 0;
    this.worldWithSeed.flooding = false;
    this.worldWithSeed.colorClicked = null;
    this.worldWithSeed.board.get(0).get(0).flooded = true;
    this.worldWithSeed.board.get(0).get(1).flooded = false;
    this.worldWithSeed.board.get(1).get(0).flooded = true;
    this.worldWithSeed.board.get(1).get(1).flooded = true;
    this.worldWithSeed.board.get(0).get(0).color = Color.green;
    this.worldWithSeed.board.get(0).get(1).color = Color.blue;
    this.worldWithSeed.board.get(1).get(0).color = Color.green;
    this.worldWithSeed.board.get(1).get(1).color = Color.green;

  }

  // test the method onMouseClick in the FloodItWorld class
  void testOnMouseClick(Tester t) {
    this.initData();
    t.checkExpect(worldWithSeed.board.get(0).get(1).flooded, false);
    this.worldWithSeed.flooding = false;
    worldWithSeed.onMouseClicked(new Posn(10, 10));
    t.checkExpect(worldWithSeed.board.get(0).get(1).flooded, false);
    t.checkExpect(worldWithSeed.board.get(1).get(1).flooded, true);
    this.worldWithSeed.flooding = false;
    worldWithSeed.onMouseClicked(new Posn(10, 60));
    t.checkExpect(worldWithSeed.board.get(0).get(1).flooded, true);
    t.checkExpect(worldWithSeed.board.get(1).get(1).flooded, true);

    // resetting fields back to start conditions so other tests pass
    this.worldWithSeed.gameEnded = false;
    this.worldWithSeed.gameEndedWin = false;
    this.worldWithSeed.timeFromStart = 0;
    this.worldWithSeed.clickNum = 0;
    this.worldWithSeed.flooding = false;
    this.worldWithSeed.colorClicked = null;
    this.worldWithSeed.board.get(0).get(0).flooded = true;
    this.worldWithSeed.board.get(0).get(1).flooded = false;
    this.worldWithSeed.board.get(1).get(0).flooded = true;
    this.worldWithSeed.board.get(1).get(1).flooded = true;
    this.worldWithSeed.board.get(0).get(0).color = Color.green;
    this.worldWithSeed.board.get(0).get(1).color = Color.blue;
    this.worldWithSeed.board.get(1).get(0).color = Color.green;
    this.worldWithSeed.board.get(1).get(1).color = Color.green;

  }

  // test the method onKeyReleased in the FloodItWorld class
  void testOnKeyReleased(Tester t) {
    this.initData();
    t.checkExpect(worldWithSeed.board.get(0).get(0).color, Color.green);
    worldWithSeed.onKeyEvent("r");
    t.checkExpect(worldWithSeed.board.get(0).get(0).color, Color.red);
    worldWithSeed.onKeyEvent("h");
    t.checkExpect(worldWithSeed.board.get(0).get(0).color, Color.red);
    worldWithSeed.onKeyEvent("r");
    t.checkExpect(worldWithSeed.board.get(0).get(0).color, Color.blue);

    // resetting fields back to start conditions so other tests pass
    this.worldWithSeed.gameEnded = false;
    this.worldWithSeed.gameEndedWin = false;
    this.worldWithSeed.timeFromStart = 0;
    this.worldWithSeed.clickNum = 0;
    this.worldWithSeed.flooding = false;
    this.worldWithSeed.colorClicked = null;
    this.worldWithSeed.board.get(0).get(0).flooded = true;
    this.worldWithSeed.board.get(0).get(1).flooded = false;
    this.worldWithSeed.board.get(1).get(0).flooded = true;
    this.worldWithSeed.board.get(1).get(1).flooded = true;
    this.worldWithSeed.board.get(0).get(0).color = Color.green;
    this.worldWithSeed.board.get(0).get(1).color = Color.blue;
    this.worldWithSeed.board.get(1).get(0).color = Color.green;
    this.worldWithSeed.board.get(1).get(1).color = Color.green;
  }

  // test the method lastScene in the FloodItWorld class
  void testLastScene(Tester t) {
    this.initData();
    WorldScene testLoseScene = new WorldScene(200, 200);
    testLoseScene.placeImageXY(new TextImage("You Lose!", 20, Color.black), 100, 100);
    WorldScene testWinScene = new WorldScene(200, 200);
    testWinScene.placeImageXY(new TextImage("You win :)", 20, Color.black), 100, 100);
    t.checkExpect(this.worldWithSeed.lastScene("You Lose!"), testLoseScene);
    t.checkExpect(this.worldWithSeed.lastScene("You win :)"), testWinScene);

    // resetting the world state so other tests can pass
    this.worldWithSeed.gameEnded = false;
    this.worldWithSeed.gameEndedWin = false;
    this.worldWithSeed.timeFromStart = 0;
    this.worldWithSeed.clickNum = 0;
  }

}