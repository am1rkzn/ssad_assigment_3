import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        simulation();
    }

    /**
     * Simulation function receives input and creates board and initial figures
     * Then it reads commands and uses processCommand function
     */
    static void simulation() {
        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt(); // board dimension
        Board board = new Board(N);

        //creating initial green figure
        int x_green = sc.nextInt();
        int y_green = sc.nextInt();
        GreenFigure greenFigure = new GreenFigure(x_green,y_green, Color.GREEN.toString(), board );
        board.addFigure(x_green, y_green, greenFigure);

        // creating initial red figure
        int x_red = sc.nextInt();
        int y_red = sc.nextInt();
        RedFigure redFigure = new RedFigure(x_red,y_red, Color.RED.toString(), board );
        board.addFigure(x_red, y_red, redFigure);

        // reading coins and adding them to the board
        int m_coins = sc.nextInt();
        for (int i = 0; i < m_coins; i++) {
            int x_coin  = sc.nextInt();
            int y_coin  = sc.nextInt();
            int value = sc.nextInt();
            board.addCoin(x_coin, y_coin, value);
        }

        int n_commands = sc.nextInt(); // number of commands
        sc.nextLine();

        // reading commands
        for (int i = 0; i < n_commands; i++) {
            String line = sc.nextLine();
            String[] parts = line.split(" ");
            String figureName = parts[0];
            String command = parts[1];

            processCommand(figureName, command, board);
        }

        //The rest part is getting and printing game score
        int green_core = board.getGreen_score();
        int red_core = board.getRed_score();

        if (green_core > red_core) {
            System.out.println("GREEN TEAM WINS. SCORE " + green_core + " " + red_core);
        } else if (green_core < red_core) {
            System.out.println("RED TEAM WINS. SCORE " + green_core + " " + red_core);
        } else {
            System.out.println("TIE. SCORE " + green_core + " " + red_core);
        }
    }

    /**
     *
     * @param figureName figure for which we need to process command
     * @param board
     * This function reads command and does corresponding function
     */
    static void processCommand(String figureName, String command, Board board) {
        Figure figure = board.getFigure(figureName);
        if (figure == null) {
            System.out.println("INVALID ACTION");
            return;
        }

        //I used proxy pattern for processing commands and checking several conditions
        FigureProxy proxy = new FigureProxy(figure , board);


        // For move action I used strategy pattern. setStrategy is the part of the pattern
        // This is application of strategies
        
        if (command.equals("UP")) {
            figure.setStrategy(new upMoveStrategy());
            proxy.move(board);
        } else if (command.equals("DOWN")) {
            figure.setStrategy(new downMoveStrategy());
            proxy.move(board);
        } else if (command.equals("LEFT")) {
            figure.setStrategy(new leftMoveStrategy());
            proxy.move(board);
        } else if (command.equals("RIGHT")) {
            figure.setStrategy(new rightMoveStrategy());
            proxy.move(board);
        } else if (command.equals("COPY")) {
            proxy.clone();
        } else if (command.equals("STYLE")) {
            proxy.changeStyle();
        } else {
            System.out.println("INVALID ACTION");
        }
    }
}

/**
 * Enumerators for Color, GameStyle and Direction when moving
 */
enum Color{
    GREEN , RED
}
enum Game_style{
    NORMAL, ATTACKING
}


/**
 * This interface is common interface for move directions -> main part of strategy pattern
 */

interface moveStrategy {
    void move(Figure figure, Board board);
}

/**
 * This class is used for checking all conditions
 * and printing result for move function
 * Protected gives access only to strategies
 */
abstract class BaseMoveStrategy implements moveStrategy {
    // for concrete strategies
    protected abstract void calculateNewPosition(Figure figure, int[] result);


    /**
     * Implementation of move action
     * It depends on the concrete strategies
     */
    @Override
    public void move(Figure figure, Board board) {
        int[] newPos = new int[2];
        calculateNewPosition(figure, newPos);
        int newX = newPos[0];
        int newY = newPos[1];

        if (!isValidMove(newX, newY, board)) {
            System.out.println("INVALID ACTION");
            return;
        }

        executeMove(figure, board, newX, newY);
    }
    //checking borders
    protected boolean isValidMove(int newX, int newY, Board board) {
        return newX >= 1 && newX <= board.getDimension() &&
                newY >= 1 && newY <= board.getDimension();
    }

    // exact implementation of moving
    protected void executeMove(Figure figure, Board board, int newX, int newY) {
        int currentX = figure.getX();
        int currentY = figure.getY();

        // Checking all cases for moving

        // if cell is already occupied by teammate
        if (board.hasSameColorFigure(newX, newY, figure)) {
            System.out.println("INVALID ACTION");
            return;
        }

        boolean killOccurs = board.hasOppositeFigure(newX, newY, figure);
        boolean coinPresent = board.hasCoin(newX, newY);

        // if cell is occupied by enemy
        if (killOccurs) {
            System.out.println(figure.getColor() + " MOVED TO " + newX + " " + newY + " AND KILLED " +
                    board.getFigure(newX, newY).getColor());
            board.killFigure(newX, newY);
            board.moveFigure(currentX, currentY, newX, newY);
            figure.setPosition(newX, newY);
        }
        // if cell occupied by coin
        else if (coinPresent) {
            int coinValue = board.getCoin(newX, newY);
            System.out.println(figure.getColor() + " MOVED TO " + newX + " " + newY + " AND COLLECTED " + coinValue);
            board.collectCoin(newX, newY, figure.getColor());
            board.moveFigure(currentX, currentY, newX, newY);
            figure.setPosition(newX, newY);
        } else {
            System.out.println(figure.getColor() + " MOVED TO " + newX + " " + newY);
            board.moveFigure(currentX, currentY, newX, newY);
            figure.setPosition(newX, newY);
        }

    }
}

// The following concrete strategies calculate new coordinates according to task

class upMoveStrategy extends BaseMoveStrategy {
    @Override
    protected void calculateNewPosition(Figure figure, int[] result) {
        int step = 1;
        if (figure.getGame_style().equals(Game_style.ATTACKING.toString())) {
            step = 2;
        }
        result[0] = figure.getX() - step;
        result[1] = figure.getY();
    }
}

class downMoveStrategy extends BaseMoveStrategy {
    @Override
    protected void calculateNewPosition(Figure figure, int[] result) {
        int step = 1;
        if (figure.getGame_style().equals(Game_style.ATTACKING.toString())){
            step = 2;
        }
        result[0] = figure.getX() + step;
        result[1] = figure.getY();
    }
}

class leftMoveStrategy extends BaseMoveStrategy {
    @Override
    protected void calculateNewPosition(Figure figure, int[] result) {
        int step = 1;
        if (figure.getGame_style().equals(Game_style.ATTACKING.toString())) {
            step = 2;
        }
        result[0] = figure.getX();
        result[1] = figure.getY() - step;
    }
}

class rightMoveStrategy extends BaseMoveStrategy {
    @Override
    protected void calculateNewPosition(Figure figure, int[] result) {
        int step = 1;
        if (figure.getGame_style().equals(Game_style.ATTACKING.toString())) {
            step = 2;
        }
        result[0] = figure.getX();
        result[1] = figure.getY() + step;
    }
}
/**
 * common interface with figures functionalities
 */
interface FigureAction{
    Figure clone(); // clone() function is used for prototype pattern
    void changeStyle(); // Style action
    void move(Board board); // Move action
}

/**
 * main figure class. it describes all characteristics of figures
 */
class Figure implements FigureAction{
    // coordinates
    private int x;
    private int y;

    private String color;//color (GREEN, RED, GREENCLONE, REDCLONE)
    private String game_style = Game_style.NORMAL.toString(); // game style (ATTACKING, NORMAL)
    private boolean isAlive = true;
    private boolean clonable; // for controlling clone process
    public moveStrategy strategy; // for move action. part of strategy pattern
    private Board board;

    // constructor
    Figure(int x, int y, String color, Board board ,boolean clonable){
        this.x = x;
        this.y = y;
        this.color = color;
        this.board = board;
        this.clonable = clonable;
    }

    // getters
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public String getColor() {
        return color;
    }
    public String getGame_style() {
        return game_style;
    }
    public moveStrategy getStrategy() {
        return strategy;
    }

    //setters
    public void setPosition(int x, int y) { // for changing coordinates of figure when moving
        this.x = x;
        this.y = y;
    }
    // setter for strategy pattern
    public void setStrategy(moveStrategy strategy){
        this.strategy = strategy;
    }

    // checkers
    public boolean isAlive() {
        return isAlive;
    }
    public boolean isClonable() {
        return clonable;
    }

    // Copy action. Part of prototype pattern
    @Override
    public Figure clone() {
        clonable = false; // change boolean because figure cannot be copied twice
        int new_x = y;
        int new_y = x;
        String new_color;
        if (this.color.startsWith(Color.GREEN.toString())) {
            new_color = Color.GREEN + "CLONE";
        } else {
            new_color = Color.RED + "CLONE";
        }
        System.out.println(this.color + " CLONED TO " + new_x + " " + new_y);

        //Creating clone with new coordinates. Moreover,we cannot copy clone
        return new Figure(new_x, new_y, new_color, board,false);
    }

    //Style action. Just changing the game_style
    @Override
    public void changeStyle() {
        if (game_style.equals(Game_style.ATTACKING.toString())){
            game_style = Game_style.NORMAL.toString();
        } else {
            game_style = Game_style.ATTACKING.toString();
        }
        System.out.println(color + " CHANGED STYLE TO " + game_style);
    }


    // move is empty because proxy makes call to the function
    @Override
    public void move(Board board) {
        if (strategy == null) {
            System.out.println("INVALID ACTION");
        }
    }

    // for killing figures
    public void die() {
        isAlive = false;
    }

}

// Proxy pattern
class FigureProxy extends Figure {
    private Figure proxy; // object of main class according to pattern
    private Board board;

    // constructor
    FigureProxy(Figure figure, Board board) {
        super(figure.getX(), figure.getY(), figure.getColor(), board, figure.isClonable());
        this.proxy = figure;
        this.board = board;
    }

    // clone implementation with checking conditions
    @Override
    public Figure clone() {
        if (!proxy.isAlive()) {
            System.out.println("INVALID ACTION");
            return null;
        }
        if (!proxy.isClonable()) {
            System.out.println("INVALID ACTION");
            return null;
        }
        if (proxy.getX() == proxy.getY()) {
            System.out.println("INVALID ACTION");
            return null;
        }

        int cloneX = proxy.getY();
        int cloneY = proxy.getX();

        if (cloneX < 1 || cloneX > board.getDimension() || cloneY < 1 || cloneY > board.getDimension()) {
            System.out.println("INVALID ACTION");
            return null;
        }
        if (board.hasFigure(cloneX, cloneY)) {
            System.out.println("INVALID ACTION");
            return null;
        }
        if (board.hasCoin(cloneX, cloneY)) {
            System.out.println("INVALID ACTION");
            return null;
        }

        Figure clone = proxy.clone();
        board.addFigure(cloneX, cloneY, clone);
        return clone;
    }

    // Style action with checking conditions
    @Override
    public void changeStyle() {
        if (!proxy.isAlive()) {
            System.out.println("INVALID ACTION");
            return;
        }
        proxy.changeStyle();
    }


    // move implementation with strategy pattern
    @Override
    public void move(Board board) {
        if (!proxy.isAlive()) {
            System.out.println("INVALID ACTION");
            return;
        }
        proxy.getStrategy().move(proxy, board);
    }
}


// concrete figures

//GREEN
class GreenFigure extends Figure{
    GreenFigure(int x, int y, String color, Board board){
        super(x,y,Color.GREEN.toString(),board,true);
    }
}

//RED
class RedFigure extends Figure{
    RedFigure(int x, int y, String color,Board board){
        super(x,y,Color.RED.toString(),board,true);
    }
}


/**
 * Board class for implementation of all actions on the board
 */
class Board{
    private int dimension; // dimension of the board
    private Map<String, Figure> figures = new HashMap<>(); // map for figures -> easier to address to them.
    private Map<String, Integer> coins = new HashMap<>(); // the same for coins

    //scores of both teams
    private int green_score = 0;
    private int red_score = 0;

    //constructor
    Board(int dimension){
        this.dimension = dimension;
    }
    // this getter is useful for checking out-of-bound conditions
    int getDimension(){
        return dimension;
    }

    // for simplifying code
    private String coordKey(int x, int y) {
        return x + "," + y;
    }


    // adding figures and coins
    public void addFigure(int x, int y, Figure figure) {
        if (figure != null) {
            figures.put(coordKey(x, y), figure);
        }
    }
    public void addCoin(int x, int y, int value){
        coins.put(coordKey(x, y), value);
    }


    // getters
    public Figure getFigure(String color) {
        for (Map.Entry<String, Figure> entry : figures.entrySet()) {
            if (entry.getValue().getColor().equals(color)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Figure getFigure(int x, int y){
        return figures.get(coordKey(x, y));
    }
    public int getCoin(int x, int y) {
        return coins.get(coordKey(x, y));
    }

   // checking conditions for moving -> whether cell is occupied by figure or coin
   public boolean hasSameColorFigure(int x, int y, Figure figure) {
       Figure f = figures.get(x + "," + y);
       if (f == null){
           return false;
       } else if (f.getColor().startsWith(figure.getColor())){
           return true;
       }
       return false;
   }

    public boolean hasOppositeFigure(int x, int y, Figure figure) {
        Figure f = figures.get(x + "," + y);
        if (f == null) {
            return false;
        } else if (!f.getColor().startsWith(figure.getColor())) {
            return true;
        }
        return false;
    }
    public boolean hasFigure(int x, int y) {
        return figures.containsKey(coordKey(x, y));
    }

    public boolean hasCoin(int x, int y) {
        return coins.containsKey(coordKey(x, y));
    }

    
    // removing figure in case when enemy moves to the occupied by another figure
    public void killFigure(int x, int y) {
        String key = coordKey(x, y);
        Figure figure = figures.get(key);
        if (figure != null) {
            figure.die();
            figures.remove(key);
        }
    }
    
    //implementation of moving on the board
    public void moveFigure(int fromX, int fromY, int toX, int toY) {
        String oldKey = coordKey(fromX, fromY);
        String newKey = coordKey(toX, toY);
        Figure figure = figures.remove(oldKey);
        if (figure != null) {
            figures.put(newKey, figure);
        }
    }

    // increase score and remove coin from the board
    public void collectCoin(int x, int y, String collectorColor) {
        String key = coordKey(x, y);
        Integer value = coins.remove(key);
        if (value != null) {
            if (collectorColor.startsWith(Color.GREEN.toString())){
                green_score += value;
            } else if (collectorColor.startsWith(Color.RED.toString())){
                red_score += value;
            }
        }
    }

    // getters for the result
    int getGreen_score(){
        return green_score;
    }
    int getRed_score(){
        return red_score;
    }
}
