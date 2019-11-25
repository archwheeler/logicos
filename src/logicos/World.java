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
  private List<String> history;

  public World(int size, int nLogicos, int maxStrength) {
    tick = 0;
    firstTurn = 0;
    map = new Entity[size][size];
    this.size = size;
    logicos = new ArrayList<>();
    history = new LinkedList<>();


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
    history.add(this.toString());
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
              surroundings.add(new Location(curRow, curCol));
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
    }
    ++tick;
    firstTurn = (firstTurn + 1) % population();
    history.add(this.toString());
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

  @Override
  public String toString() {
    StringBuilder world = new StringBuilder();
    for (int x = 0; x != size; ++x) {
      for (int y = 0; y != size; ++y) {
        if (map[x][y] == null)
          world.append("- "); // represent empty locations as -
        else if (map[x][y] instanceof Logico)
          world.append(((Logico) map[x][y]).getStrength()).append(" "); // represent logicos as their strength
      }
      world.append("\n");
    }
    return world.toString();
  }

  public String getOldWorld(int tick) {
    if (tick < 0 || tick >= history.size())
      throw new IndexOutOfBoundsException();
    return history.get(tick);
  }

  public static void main(String[] args) {
    World world;
    try {
      world = new World(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      System.out.println("[!] Arguments: [world size] [number of logicos] [maximum strength]");
      return;
    }
    while (world.population() > 1) {
      world.advance();
    }

    System.out.println("[*] Simulation finished in " + world.getTick() + " ticks.");
    System.out.println("[*] Type help for a list of commands");
    Scanner input = new Scanner(System.in);
    String command = input.nextLine();
    boolean output = false;
    int tick = 0;
    while (!command.equals("quit")) {
      try {
        tick = Integer.parseInt(command);
        output = true;
      } catch (NumberFormatException e) {
        switch (command) {
          case ",": // backwards
            --tick;
            output = true;
            break;
          case ".": // forwards
            ++tick;
            output = true;
            break;
          case "help":
            System.out.println("Enter an integer to observe the world at that tick.");
            System.out.println("Enter ',' to observe the previous world state.");
            System.out.println("Enter '.' to observe the next world state.");
            output = false;
            break;
          default:
            System.out.println("[!] Invalid command, type 'help' for more information.");
            output = false;
        }
      }
      if (output)
        try {
          System.out.println("Tick " + tick + ":\n" + world.getOldWorld(tick));
        } catch (IndexOutOfBoundsException e) {
          System.out.println("[!] No world exists for tick " + tick + ".");
        }
      command = input.nextLine();
    }
  }
}
