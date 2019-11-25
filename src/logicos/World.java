package logicos;

import logicos.entities.*;
import logicos.exceptions.*;

import java.util.*;

public class World {
  private Entity[][] map;
  private int size;
  private List<Logico> logicos;
  private int tick;
  private int firstTurn;

  public World(int size, int nLogicos, int maxStrength) {
    tick = 0;
    firstTurn = 0;
    map = new Entity[size][size];
    this.size = size;
    logicos = new ArrayList<>();


    // Place [nLogicos] amount of Logicos randomly around the map
    Random r = new Random();
    int x = r.nextInt(size), y = r.nextInt(size);
    while (nLogicos > 0) {
      while (map[x][y] != null) {
        x = r.nextInt(size);
        y = r.nextInt(size);
      }
      map[x][y] = new Logico(r.nextInt(maxStrength) + 1, new Location(x, y)); // Generate random strength
      logicos.add((Logico) map[x][y]);
      --nLogicos;
    }
  }

  public void setLocation(Location location, Entity entity) {
    map[location.getX()][location.getY()] = entity;
    if (entity instanceof Logico)
      ((Logico) entity).setLocation(location); // TODO: can we avoid redundant storing of location ?
  }

  public List<Entity> getSurroundings(Logico logico) {
    List<Entity> surroundings = new LinkedList<>();
    Location location = logico.getLocation();
    int xLoc = location.getX();
    int yLoc = location.getY();
    int curRow, curCol;
    for (int rowOffset = -1; rowOffset <= 1; ++rowOffset) {
      curRow = xLoc + rowOffset;
      if (curRow >= 0 && curRow < size) // i.e we are not looking out of the map (past top / bottom)
        for (int colOffset = -1; colOffset <= 1; ++colOffset) {
          curCol = yLoc + colOffset;
          // if (we're not looking at the logico) && (we're not looking out of the map, past left / right)
          if (!(rowOffset == 0 && colOffset == 0) && (curCol >= 0 && curCol < size)) {
            if (map[curRow][curCol] == null) // if we're looking at an empty tile
              surroundings.add(new Location(curRow, curCol)); // TODO: should we represent empty tiles as null or an entity?
            else
              surroundings.add(map[curRow][curCol]);
          }
        }
    }
    return surroundings;
  }

  public void advance() {
    int turn = firstTurn;
    Logico logico;
    for (int processed = 0; processed < population(); ++processed) {
      logico = logicos.get(turn);
      logico.advance(getSurroundings(logico)).perform(this);
      turn = (turn + 1) % population();
      ++tick;
      //display();
    }
    firstTurn = (firstTurn + 1) % population();
  }

  public int population() {
    return logicos.size();
  }

  public int getTick() {
    return tick;
  }

  public void removeEntity(Location location) throws EntityNotPresent, OutOfWorld {
    if (getEntity(location) == null)
      throw new EntityNotPresent();
    int x = location.getX();
    int y = location.getY();
    if (map[x][y] instanceof Logico)
      logicos.remove(map[x][y]);
    map[x][y] = null;
  }

  public Entity getEntity(Location location) throws OutOfWorld {
    int x = location.getX();
    int y = location.getY();
    if (x >= size || x < 0 || y >= size || y < 0)
      throw new OutOfWorld();
    return map[x][y];
  }

  public void display() {
    System.out.println(tick);
    for (int x = 0; x != size; ++x) {
      for (int y = 0; y != size; ++y) {
        if (map[x][y] == null)
          System.out.printf("- "); // represent empty locations as -
        else if (map[x][y] instanceof Logico)
          System.out.printf(((Logico) map[x][y]).getStrength() + " "); // represent logicos as their strength
      }
      System.out.printf("\n");
    }
  }

  public static void main(String[] args) {
    World world = new World(10, 5, 9); // Default world constants
    while (world.population() > 1) {
      world.advance();
    }
    System.out.println("[*] Simulation finished in " + world.getTick() + " ticks.");
  }
}
