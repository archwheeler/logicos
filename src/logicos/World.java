package logicos;

import logicos.entities.*;
import logicos.exceptions.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class World {
  private Entity[][] map;
  private int size;
  private List<Logico> logicos;
  private int tick;
  private int firstTurn;
  private List<String> history;

  public World(int size, int nLogicos, int maxStrength, double maxInaccuracy) {
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
      map[x][y] = new Logico(r.nextInt(maxStrength) + 1, r.nextDouble() * maxInaccuracy,
                              new Location(x, y), 100); // Generate random strength
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
    world.append("Tick " + tick + ":\n");
    for (int x = 0; x != size; ++x) {
      for (int y = 0; y != size; ++y) {
        if (map[x][y] == null)
          world.append(" - "); // represent empty locations as -
        else if (map[x][y] instanceof Logico)
          world.append(String.format("%3s", ((Logico) map[x][y]).getHealth())); // represent logicos as their health
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
      world = new World(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]));
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      System.out.println("[!] Arguments: [world size] [number of logicos] [maximum strength] [maximum inaccuracy]");
      return;
    }
    while (world.population() > 1) {
      world.advance();
    }

    System.out.println("[*] Simulation finished in " + world.getTick() + " ticks.");
    System.out.println("[*] Type help for a list of commands");
    Scanner input = new Scanner(System.in);
    String command = input.nextLine();
    int output = 0;
    int tick = 0;
    while (!command.equals("quit")) {
      if (command.startsWith("run")) {
        try {
          output = Integer.parseInt(command.split(" ")[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
          System.out.println("[!] Usage: run [x]");
          output = 0;
        }
      } else {
        try {
          tick = Integer.parseInt(command);
          output = 1;
        } catch (NumberFormatException e) {
          switch (command) {
            case ",": // backwards
              --tick;
              output = 1;
              break;
            case ".": // forwards
              ++tick;
              output = 1;
              break;
            case "help":
              System.out.println("Enter an integer to observe the world state at that tick.");
              System.out.println("Enter ',' to observe the previous world state.");
              System.out.println("Enter '.' to observe the next world state.");
              System.out.println("Enter 'run x' to observe the next x world states.");
              System.out.println("Enter 'quit' to end the program.");
              output = 0;
              break;
            default:
              System.out.println("[!] Invalid command, type 'help' for more information.");
              output = 0;
          }
        }
      }
      while (output > 0) {
        try {
          System.out.println(world.getOldWorld(tick));
        } catch (IndexOutOfBoundsException e) {
          System.out.println("[!] No world exists for tick " + tick + ".");
          break;
        }
        ++tick;
        --output;
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          continue;
        }
      }
      command = input.nextLine();
    }
  }
}
