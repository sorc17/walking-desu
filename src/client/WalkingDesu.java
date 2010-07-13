package client;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.lang.reflect.InvocationTargetException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.awt.Graphics2D;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class WalkingDesu {

    private static JFrame f;
    private static MyPanel p;
    private static ServerInteractTask sit = null;
    private static String serverIP = null;
    private static int serverPort = 0;
    private static JButton b;
    private static JTextField t;

    public static long serverStartTime;

    public static void main(String[] args) {
        serverStartTime = System.currentTimeMillis();
        if (args.length > 1) {
            serverIP = args[0];
            serverPort = Integer.parseInt(args[1]);
        } else {
            System.out.println("How about server ip and port?");
            System.exit(1);
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            });
        } catch (InvocationTargetException e) {
            System.err.println(e.getMessage());
            return;
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            return;
        }
        CustomDialog d = new CustomDialog("lol");
        d.setSize(new Dimension(400, 200));
        d.setVisible(true);
        p.init();
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(new RedrawTask());
        sit = new ServerInteractTask();
        executor.execute(sit);
        //(new RedrawTask()).execute();
        //sit.execute();
    }

    public static JFrame getFrame() {
        return f;
    }

    private static void createAndShowGUI() {
        p = new MyPanel();
        f = new JFrame("Walking Desu 3");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        b = new JButton("Send");
        t = new JTextField(50);
        p.add(b);
        p.add(t);
        f.add(p);
        f.setSize(800,600);
        f.setVisible(true);
        
    }

    public static void setButtonActionListener(Player p) {
        b.addActionListener(new MyListener(p, t));
    }

    private static class RedrawTask extends SwingWorker<Void, Void> {
        public final long delay = 20; // 1000 ms / 20 ms = 50 fps

        @Override
        protected Void doInBackground() {
            while (true) {
                p.repaint();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                    return null;
                }
            }
        }
    }

    public static void addOutCommand(String c) {
        if (sit != null) {
            sit.addOutCommand(c);
        }
    }

    private static class ServerInteractTask extends SwingWorker<Void, Void> {
        private final ArrayList<String> outCommands = new ArrayList<String>();
        private ArrayList<String> inCommands = new ArrayList<String>();
        private Socket serverSocket = null;
        private PrintWriter out = null;
        private BufferedReader in = null;
        private final Lock lock = new ReentrantLock();

        @Override
        protected Void doInBackground() {
            try {
                ServerReader sreader = new ServerReader();
                serverSocket = new Socket(serverIP, serverPort);
                out = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), "UTF-8"), true);
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), "UTF-8"));

                outCommands.add("(hello)");
                Executor executor = Executors.newCachedThreadPool();
                executor.execute(sreader);
                while (serverSocket.isConnected()) {
                    synchronized(outCommands) {
                        while (outCommands.size() > 0) {
                            out.println(outCommands.get(0));
                            outCommands.remove(0);
                        }
                        try {
                            outCommands.wait();
                        } catch (InterruptedException ignored) {}
                    }
                }

                sreader.cancel(true);
                out.close();
                in.close();
                serverSocket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void addOutCommand(String c) {
            if (!c.startsWith("(")) {
                c = "(" + c + ")";
            }
            outCommands.add(c);
            System.out.println("Command added: " + c);
            synchronized(outCommands) {
                outCommands.notify();
            }
        }

        private class ServerReader extends SwingWorker<Void, Void> {
            private String command;

            @Override
            protected Void doInBackground() {
                String[] pieces;

                try {
                    while (serverSocket.isConnected()) {
                        command = in.readLine();
                        System.out.println("Command received: " + command);
                        if (command.startsWith("(")) {
                            command = command.substring(1, command.length() - 1); // remove ( )
                        }
                        pieces = command.split(" ");
                        if (pieces[0].equals("hello")) {
                            p.selfLogin(Long.parseLong(pieces[1]), Long.parseLong(pieces[2]), Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]));
                        }
                        if (pieces[0].equals("newplayer")) {
                            p.addPlayer(Long.parseLong(pieces[1]), Integer.parseInt(pieces[2]), Integer.parseInt(pieces[3]));
                        }
                        if (pieces[0].equals("move")) {
                            long begTime = Long.parseLong(pieces[2]);
                            serverStartTime = System.currentTimeMillis() - begTime;
                            p.movePlayer(Long.parseLong(pieces[1]), begTime, Integer.parseInt(pieces[3]), Integer.parseInt(pieces[4]));
                        }
                        if (pieces[0].equals("timesync")) {
                            long remoteTimeDiff = Long.parseLong(pieces[1]);
                            System.out.println("Timesync " + Math.abs(System.currentTimeMillis() - serverStartTime) + " -> " + pieces[1]);
                            serverStartTime = System.currentTimeMillis() - remoteTimeDiff;
                        }
                        if (pieces[0].equals("delplayer")) {
                            p.delPlayer(Long.parseLong(pieces[1]));
                        }
                        if (pieces[0].equals("message")) {
                            pieces = command.split(" ", 3);
                            p.setPlayerText(Long.parseLong(pieces[1]), pieces[2].substring(1, pieces[2].length() - 1));
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

}

class MyPanel extends JPanel {

    public Dimension mapOfst; // Смещение 0,0 карты относительно 0,0 панели.

    public Dimension panelDim = null; // Размер этой панели.

    private BufferedImage buffImg = null;
    private Graphics buffGraph = null;
    private Dimension buffDim = null;

    private Player self = null;
    private ArrayList<Player> players;

    public MyPanel() {

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                BufferedImage map = WDMap.getInstance().getMapImg();
                Dimension m = new Dimension(map.getWidth(), map.getHeight());
                int x = e.getX();
                int y = e.getY();

                // Если клик был в пределах карты.
                if (x >= mapOfst.width && x <= m.width + mapOfst.width
                        && y >= mapOfst.height && y <= m.height + mapOfst.height) {
                    /*ArrayList<Point> points = new ArrayList<Point>();
                    points.add(new Point(0, 0));
                    points.add(new Point(0, 200));
                    points.add(new Point(200, 200));
                    points.add(new Point(200, 0));*/
                    //if (!inpoly(points, x - mapOfst.width, y - mapOfst.height)) {
                        self.move(Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime),
                                x - mapOfst.width, y - mapOfst.height);
                        WalkingDesu.addOutCommand("move " + (x - mapOfst.width)
                                + " " + (y - mapOfst.height));
                    //}
                }
            }
        });

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){

                Dimension d = e.getComponent().getSize();

                if (buffImg != null
                        && (buffImg.getWidth() != d.width || buffImg.getHeight() != d.height)) {
                    buffImg = new BufferedImage(d.width, d.height,
                            BufferedImage.TYPE_INT_RGB);
                    buffDim = new Dimension(buffImg.getWidth(),
                            buffImg.getHeight());
                    buffGraph = buffImg.getGraphics();
                }

                if (panelDim != null && d != null && mapOfst != null // TODO Concurency issue
                        && (panelDim.width != d.width || panelDim.height != d.height)) {
                    mapOfst.width += d.width / 2 - panelDim.width / 2;
                    mapOfst.height += d.height / 2 - panelDim.height / 2;
                }

                panelDim = d;
            }
        });
    }

    public void init() {
        // Наш игрок.
        self = new Player(0, 0, 0);

        players = new ArrayList<Player>();

        // Нашего игрока нужно тоже добавить в список всех игроков, для
        // сортировки по Y координате при отрисовке спрайтов.
        players.add(self);

        // Поскольку наш игрок всегда находится в центре экрана и в начале
        // его положение на карте (0, 0) то точка (0, 0) карты должны быть в
        // центре экрана.
        mapOfst = new Dimension(getWidth() / 2, getHeight() / 2);

        // Буфер для рисования (для двойной буферизации).
        buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        buffGraph = buffImg.getGraphics();
        buffDim = new Dimension(buffImg.getWidth(), buffImg.getHeight());

        panelDim = getSize();

        WalkingDesu.setButtonActionListener(self);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (self != null && buffDim != null) { // TODO Concurency issue

            // Поле вне карты цвета фона и по краям черная рамочка в 1 пиксел.
            buffGraph.setColor(getBackground());
            buffGraph.fillRect(0, 0, buffDim.width - 1, buffDim.height - 1);
            buffGraph.setColor(Color.BLACK);
            buffGraph.drawRect(0, 0, buffDim.width - 1, buffDim.height - 1);

            // TODO На самом деле надо рисовать лишь видимую часть карты
            // которую можно получить с помощью метода getSubimage()
            buffGraph.drawImage(WDMap.getInstance().getMapImg(), mapOfst.width,
                    mapOfst.height, null);

            // Вычислим новое смещение карты (временно это делается здесь)
            if (panelDim.width / 2 != mapOfst.width + self.cur.x
                    || panelDim.height / 2 != mapOfst.height + self.cur.y) {
                mapOfst.width += panelDim.width / 2 - (self.cur.x + mapOfst.width);
                mapOfst.height += panelDim.height / 2 - (self.cur.y + mapOfst.height);
            }

            // Да, придётся отсортировать игроков по возрастанию Y координаты
            // чтобы "нижелещаие" спрайты не перекрывали вышележащие.
            // TODO optimization
            Collections.sort(players, new YAligner());
            Player p;
            BufferedImage textCloud;
            for(int i = 0; i < players.size(); i++) {
                p = players.get(i);
                buffGraph.drawImage(p.getSprite(),
                        p.cur.x + mapOfst.width - p.getSprite().getWidth(null) / 2,
                        p.cur.y + mapOfst.height - p.getSprite().getHeight(null),
                        null);
                textCloud = p.getTextCloud();
                if (textCloud != null) {
                    buffGraph.drawImage(textCloud,
                            p.cur.x + mapOfst.width + p.getSprite().getWidth(null) / 2,
                            p.cur.y + mapOfst.height - p.getSprite().getHeight(null),
                            null);
                }
            }

            g.drawImage(buffImg, 0, 0, null);
        }
    }

    public void addPlayer(long id, int x, int y) {
        if (players != null) {
            players.add(new Player(id, x, y));
        }
    }

    public void delPlayer(long id) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.remove(i);
                    return;
                }
            }
        }
    }

    public void setPlayerText(long id, String t) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.get(i).setText(t);
                    players.get(i).shouldUpdateTextCloud();
                    return;
                }
            }
        }
    }

    public void movePlayer(long id, long tstamp, int x, int y) {
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == id) {
                    players.get(i).move(tstamp, x, y);
                    return;
                }
            }
        }
    }

    public void selfLogin(long id, long tstamp, int x, int y) {
        if (self != null) {
            self.id = id;
            WalkingDesu.serverStartTime = System.currentTimeMillis() - tstamp;
            self.cur.move(x, y);
        }
    }

    public Player getSelf() {
        return self;
    }

    private boolean inpoly(ArrayList<Point> points, int xt, int yt) {
        int xnew,ynew;
        int xold,yold;
        int x1,y1;
        int x2,y2;
        int i;
        boolean inside = false;
        int npoints = points.size();

        if (npoints < 3) {
            return false;
        }
        xold=points.get(npoints - 1).x;
        yold=points.get(npoints - 1).y;
        for (i=0 ; i < npoints ; i++) {
            xnew=points.get(i).x;
            ynew=points.get(i).y;
            if (xnew > xold) {
                x1=xold;
                x2=xnew;
                y1=yold;
                y2=ynew;
            }
            else {
                x1=xnew;
                x2=xold;
                y1=ynew;
                y2=yold;
            }
            /* edge "open" at one end */
            if ((xnew < xt) == (xt <= xold) && ((long)yt-(long)y1)*(long)(x2-x1) < ((long)y2-(long)y1)*(long)(xt-x1)) {
                inside = !inside;
            }
            xold=xnew;
            yold=ynew;
        }
        return inside;
    }
}

class Player {

    long id;

    public Point cur; // Точка на карте, где находится игрок.
    double speed = 0.07;
    private SpriteSet set;

    // Movment
    boolean isMove;
    long begTime;
    Point beg;
    Point end;

    // Temp for animation
    boolean resetStandAnimationTimer = false;

    // For text cloud
    BufferedImage textCloud = null;
    String text = "";
    boolean updateTextCloud = false;

    public void setText(String t) {
        text = t;
    }

    public void shouldUpdateTextCloud() {
        updateTextCloud = true;
    }

    public BufferedImage getTextCloud() {
        if (!updateTextCloud) {
            return textCloud;
        }
        if (text.equals("")) {
            return null;
        } else {
            textCloud = new BufferedImage(150, 100, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = textCloud.getGraphics();

            LineBreakMeasurer lineMeasurer;
            int paragraphStart;
            int paragraphEnd;
            float breakWidth = 149 - 2;
            float drawPosY = 0;
            Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(new Color((float)0.1, (float)1.0, (float)0.3, (float)0.7));
            g2d.fillRoundRect(1, 1, 148, 98, 10, 10);
            g2d.setColor(Color.BLACK);
            AttributedCharacterIterator paragraph = (new AttributedString(text)).getIterator();
            FontRenderContext frc;

            map.put(TextAttribute.FAMILY, "Serif");
            map.put(TextAttribute.SIZE, new Float(18.0));
            paragraphStart = paragraph.getBeginIndex();
            paragraphEnd = paragraph.getEndIndex();
            frc = g2d.getFontRenderContext();
            lineMeasurer = new LineBreakMeasurer(paragraph, frc);

            lineMeasurer.setPosition(paragraphStart);
            while (lineMeasurer.getPosition() < paragraphEnd) {
                TextLayout layout = lineMeasurer.nextLayout(breakWidth);
                float drawPosX = layout.isLeftToRight()
                        ? 2 : breakWidth - layout.getAdvance();
                drawPosY += layout.getAscent();
                layout.draw(g2d, drawPosX, drawPosY);
                drawPosY += layout.getDescent() + layout.getLeading();
            }
            updateTextCloud = false;
            return textCloud;
        }
    }

    public Player(int x, int y) {
        beg = new Point(0, 0);
        end = new Point(1, 1);
        set = new SpriteSet();
        cur = new Point(x, y);
        isMove = false;
    }

    public Player(long _id, int x, int y) {
        id = _id;
        beg = new Point(0, 0);
        end = new Point(1, 1);
        set = new SpriteSet();
        cur = new Point(x, y);
        isMove = false;
    }

    public BufferedImage getSprite() {
        if (isMove()) {
            resetStandAnimationTimer = true;
            return set.getMovement().getDirection(beg, end).getMoveSpr(beg.distance(cur));
        } else {
            BufferedImage tmp = set.getMovement().getDirection(beg, end).getStandSpr(resetStandAnimationTimer,
                    Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime));
            resetStandAnimationTimer = false;
            return tmp;
        }
    }

    private boolean isMove() {
        if (isMove) {
            long curTime = Math.abs(System.currentTimeMillis() - WalkingDesu.serverStartTime);

            if (curTime > begTime) {
                //System.out.print("(" + cur.x + ", " + cur.y + ") -> ");
                cur.x = (int) (beg.x + ((end.x - beg.x) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
                cur.y = (int) (beg.y + ((end.y - beg.y) / Math.sqrt(Math.pow(Math.abs(end.x - beg.x), 2) + Math.pow(Math.abs(end.y - beg.y), 2))) * speed * Math.abs(curTime - begTime));
                //System.out.println("(" + cur.x + ", " + cur.y + ")");

                if (beg.x > end.x && end.x > cur.x
                        || beg.x < end.x && end.x < cur.x
                        || beg.y > end.y && end.y > cur.y
                        || beg.y < end.y && end.y < cur.y) {
                    //System.out.print("Oops: " + "(" + cur.x + ", " + cur.y + ") fixed to ");
                    cur.x = end.x;
                    cur.y = end.y;
                    //System.out.println("(" + cur.x + ", " + cur.y + ")");
                    isMove = false;
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void move(long _begTime, int x, int y) {
        isMove = true;
        begTime = _begTime;
        beg.move(cur.x, cur.y);
        end.move(x, y);
    }

}

class WDMap {

    private static WDMap map = null;
    private BufferedImage mapImg;

    private WDMap(int w, int h) {
        Graphics g;

		mapImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		g = mapImg.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(1, 1, w - 2, h - 2);
    }

    public static WDMap getInstance() {
        if (map == null) {
            map = new WDMap(1024, 768);
        }
        return map;
    }

    public BufferedImage getMapImg() {
        return mapImg;
    }

}

class YAligner implements Comparator {
    public final int compare(Object a, Object b) {
        return ((Player) a).cur.y > ((Player) b).cur.y ? 1 : 0;
    }
}

class MyListener implements ActionListener {
    Player self;
    JTextField msgField;

    public MyListener(Player p, JTextField tf) {
        self = p;
        msgField = tf;
    }

    public void actionPerformed(ActionEvent e) {
        if (msgField.getText() != null && !self.text.equals(msgField.getText())) {
            self.text = msgField.getText().length() > 100 ? msgField.getText().substring(0, 99) : msgField.getText();
            self.shouldUpdateTextCloud();
            WalkingDesu.addOutCommand("message \"" + self.text + "\"");
        }
    }
}

class CustomDialog extends JDialog implements ActionListener, PropertyChangeListener {
    private String typedText = null;
    private JTextField textField;

    private String magicWord;
    private JOptionPane optionPane;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";

    public String getValidatedText() {
        return typedText;
    }

    public CustomDialog(String aWord) {
        super(WalkingDesu.getFrame(), true);

        magicWord = aWord.toUpperCase();
        setTitle("Quiz");

        textField = new JTextField(10);

        String msgString1 = "What was Dr. SEUSS's real last name?";
        String msgString2 = "(The answer is \"" + magicWord + "\".)";
        Object[] array = {msgString1, msgString2, textField};

        Object[] options = {btnString1, btnString2};

        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[0]);

        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener(this);

        optionPane.addPropertyChangeListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }

            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                typedText = textField.getText();
                String ucText = typedText.toUpperCase();
                if (magicWord.equals(ucText)) {
                    clearAndHide();
                } else {
                    textField.selectAll();
                    JOptionPane.showMessageDialog(CustomDialog.this,
                            "Sorry, \"" + typedText + "\" "
                            + "isn't a valid response.\n"
                            + "Please enter " + magicWord + ".",
                            "Try again",
                            JOptionPane.ERROR_MESSAGE);
                    typedText = null;
                    textField.requestFocusInWindow();
                }
            } else {
                typedText = null;
                clearAndHide();
            }
        }
    }

    public void clearAndHide() {
    textField.setText(null);
    setVisible(false);
    }
}
