/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import algorithm.PrinsGRASPxELS.GRASPxELSAlgorithm;
import algorithm.PrinsGRASPxELS.ParameterList;
import algorithm.beasley.Beasley;
import algorithm.floyds.FloydsDistanceMatrixConstructor;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import buildingmodel.Connection;
import problem.EuclideanCoordinate;
import buildingmodel.Node;
import buildingmodel.Storey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.math3.random.HaltonSequenceGenerator;
import org.apache.commons.math3.random.RandomVectorGenerator;
import problem.Problem;
import problem.Solution;
import util.Util;

/**
 *
 * @author gokalp
 */
public class MainWindow extends javax.swing.JFrame {

    public static double storeyWidth = 400;
    public static double mousePosXMultiplier, mousePosYMultiplier;
    private int numOfStoreys = 3;
    private int nodeID = 0;
    //depot holds real IDs since auxilary IDs will be determiden just before the
    //optimization algorithm works
    private Node depot = null;
    private ArrayList<Storey> storeys = new ArrayList<>();
    private ArrayList<Connection> connections = new ArrayList<>();
    //key: auxilary ID, value: real ID
    private HashMap<Integer, Integer> IDHashMap1 = new HashMap<>();
    //key: real ID, value: auxilary ID
    private HashMap<Integer, Integer> IDHashMap2 = new HashMap<>();
    private boolean firstAddNodeClick = true; //it will make first add node click to be depot
    private boolean panel1ConnectionNodeAdded = false;
    private boolean panel2ConnectionNodeAdded = false;

    private DefaultListModel listModelNodes;
    private DefaultListModel listModelConnections;
    private int maxXMLID = 0;

    private final int DEFAULT_CONNECTION_WEIGHT = 20;
    private final int DEFAULT_DEMAND = 5;
    private int totalDemand = 0;
    private int numOfCustomers = 0;
    private ArrayList<ArrayList<Node>> solution = null;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        jButton1.setIcon(new ImageIcon("gfx/building.png"));
        initProblem();

        storeyPanelMouseListener mouseListener = new storeyPanelMouseListener();
        jPanel1.addMouseListener(mouseListener);
        jPanel1.addMouseMotionListener(mouseListener);
        jPanel2.addMouseListener(mouseListener);
        jPanel2.addMouseMotionListener(mouseListener);
    }

    private void initProblem() {
        storeys = new ArrayList<>();
        connections = new ArrayList<>();
        IDHashMap1 = new HashMap<>();
        IDHashMap2 = new HashMap<>();
        nodeID = 0;
        panel1ConnectionNodeAdded = false;
        panel2ConnectionNodeAdded = false;
        mousePosXMultiplier = (double) storeyWidth / (double) jPanel1.getWidth();
        mousePosYMultiplier = (double) storeyWidth / (double) jPanel1.getHeight();
        depot = null;
        firstAddNodeClick = true;
        totalDemand = 0;
        numOfCustomers = 0;
        solution = null;

        initStoreys();
        initComboboxes();
        initPanels();
        initRadioButtons();
        initCardlayouts();
        initID();
        initJLabels();
        initJLists();
    }

    private void initJLists() {
        listModelNodes = new DefaultListModel();
        listModelConnections = new DefaultListModel();

        jListNodes.setModel(listModelNodes);
        jListConnections.setModel(listModelConnections);
    }

    private void initCardlayouts() {
        CardLayout card = (CardLayout) jPanelCardLayoutAddRemoveEdit.getLayout();
        card.first(jPanelCardLayoutAddRemoveEdit);
    }

    private void initJLabels() {
        jLabel3.setText("(0, 0)");
        jLabel4.setText("(0, 0)");
        jLabel7.setText("(" + storeyWidth + ", 0)");
        jLabel8.setText("(" + storeyWidth + ", 0)");
        jLabel5.setText("(0, " + storeyWidth + ")");
        jLabel10.setText("(0, " + storeyWidth + ")");
        jLabel6.setText("(" + storeyWidth + ", " + storeyWidth + ")");
        jLabel9.setText("(" + storeyWidth + ", " + storeyWidth + ")");
        jLabelTotalDemand.setText("Total Demand: " + totalDemand);
        jLabelNumOfCustomers.setText("Num. of Customers: " + numOfCustomers);
    }

    private void initID() {
        this.nodeID = 0;
    }

    private void initRadioButtons() {
        buttonGroupNodeOrConnection.add(jRadioButtonNode);
        buttonGroupNodeOrConnection.add(jRadioButtonConnection);
        jRadioButtonNode.setSelected(true);

        buttonGroupAddOrEdit.add(jRadioButtonAdd);
        buttonGroupAddOrEdit.add(jRadioButtonEditRemove);
        jRadioButtonAdd.setSelected(true);

    }

    private void initPanels() {
        ((StoreyPanel) jPanel1).setStorey(storeys.get(0));
        ((StoreyPanel) jPanel2).setStorey(storeys.get(1));
        jPanel1.repaint();
        jPanel2.repaint();
    }

    private void initStoreys() {
        for (int i = 0; i < numOfStoreys; i++) {
            Storey storey = new Storey();
            storey.setNo(i);
            storeys.add(storey);
        }
    }

    private void initComboboxes() {
        jComboBox1.setModel(new DefaultComboBoxModel());
        jComboBox2.setModel(new DefaultComboBoxModel());

        for (int i = 0; i < numOfStoreys; i++) {
            jComboBox1.addItem("Storey " + i);
            jComboBox2.addItem("Storey " + i);
        }

        ItemListener listener = new comboboxItemListener();
        jComboBox1.addItemListener(listener);
        jComboBox2.addItemListener(listener);

        jComboBox1.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(1);
    }

    private void createNewProblem(int numOfStoreys, double storeyWidth) {
        this.numOfStoreys = numOfStoreys;
        this.storeyWidth = storeyWidth;
        initProblem();
    }

    private void comboboxItemSelected(JComboBox combobox) {
        JComboBox otherCombobox;
        int selectedIndex = combobox.getSelectedIndex();

        if (combobox.equals(jComboBox1)) {
            otherCombobox = jComboBox2;
        } else {
            otherCombobox = jComboBox1;
        }

        try {
            if (otherCombobox.getSelectedIndex() == selectedIndex) {

                if (selectedIndex + 1 < numOfStoreys) {
                    otherCombobox.setSelectedIndex(selectedIndex + 1);
                } else if (selectedIndex - 1 >= 0) {
                    otherCombobox.setSelectedIndex(selectedIndex - 1);
                }
            }
        } catch (IllegalArgumentException e) {

        }
    }

    private void changeStoreyPanel(JComboBox cb) {
        if (cb.equals(jComboBox1)) {
            ((StoreyPanel) jPanel1).setStorey(storeys.get(cb.getSelectedIndex()));
        } else if (cb.equals(jComboBox2)) {
            ((StoreyPanel) jPanel2).setStorey(storeys.get(cb.getSelectedIndex()));
        }
    }

    private void addNewNode(Node node) {
        storeys.get(node.getStorey().getNo()).addNode(node);
        numOfCustomers++;
        updateTotalDemand(node.getDemand());
        listModelNodes.addElement(node);
    }

    private void addNewConnectionFromXML(Connection connection) {
        connections.add(connection);

        listModelConnections.addElement(connection);
    }

    private void addNewConnection(Connection connection) {
        connections.add(connection);
        connection.getNode1().getStorey().addConnectionNode(connection.getNode1());
        connection.getNode2().getStorey().addConnectionNode(connection.getNode2());

        listModelConnections.addElement(connection);
    }

    private void removeNode(Node node) {
        Storey storey = node.getStorey();
        storey.removeNode(node);
        numOfCustomers--;
        updateTotalDemand(-1 * node.getDemand());
        listModelNodes.removeElement(node);

        if (depot == node) { //if removed node is depot, then set depot to null
            depot = null;
        }
        repaintStoreyPanels();
    }

    private void removeConnection(Connection connection) {
        connections.remove(connection);
        connection.getNode1().getStorey().removeConnectionNode(connection.getNode1());
        connection.getNode2().getStorey().removeConnectionNode(connection.getNode2());

        listModelConnections.removeElement(connection);
        repaintStoreyPanels();
    }

    private void repaintStoreyPanels() {
        jPanel1.repaint();
        jPanel2.repaint();
    }

    private double[][] constructWeightMatrix() {
        int totalNodeCount = 0;
        int totalConnectionNodeCount = 0;
        int size;
        double[][] weightMatrix;
        int auxilaryIDNode = 1;
        int auxilaryIDConnectionNode;
        IDHashMap1 = new HashMap<>();
        IDHashMap2 = new HashMap<>();

        for (Storey storey : storeys) {
            totalNodeCount += storey.getNodeCount();
            totalConnectionNodeCount += storey.getConnectionNodeCount();
        }

        auxilaryIDConnectionNode = totalNodeCount;

        size = totalNodeCount + totalConnectionNodeCount;
        weightMatrix = new double[size][size];

        //Init matrix with infinity and zeros
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    weightMatrix[i][j] = 0;
                } else {
                    weightMatrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }


        /* construct  allNodesWithConnections array and assign auxilary ID's*/
        //BURADA DEPOT NODU İÇİN 0 REZERVE EDİLEBİLİR; EĞER DEPOYSA AUXILARYID 0, DEĞİLSE 1'DEN BAŞLAYAN COUNTER'DAKİ DEĞER
        for (Storey storey : storeys) {
            ArrayList<Node> nodes = storey.getNodes();
            ArrayList<Node> connectionNodes = storey.getConnectionNodes();
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).isDepot()) {
                    nodes.get(i).setAuxilaryID(0);
                    IDHashMap1.put(0, nodes.get(i).getID());
                    IDHashMap2.put(nodes.get(i).getID(), 0);
                } else {
                    nodes.get(i).setAuxilaryID(auxilaryIDNode);
                    IDHashMap1.put(auxilaryIDNode, nodes.get(i).getID());
                    IDHashMap2.put(nodes.get(i).getID(), auxilaryIDNode);
                    auxilaryIDNode++;
                }
            }
            for (int i = 0; i < connectionNodes.size(); i++) {
                connectionNodes.get(i).setAuxilaryID(auxilaryIDConnectionNode);
                IDHashMap1.put(auxilaryIDConnectionNode, connectionNodes.get(i).getID());
                IDHashMap2.put(connectionNodes.get(i).getID(), auxilaryIDConnectionNode);
                auxilaryIDConnectionNode++;
            }
        }

        /*calculate euclidean distance between the nodes and connections
         that are on the same storey(set 0 to the same nodes)*/
        for (Storey storey : storeys) {
            ArrayList<Node> nodesOnTheSameStorey = new ArrayList<>();
            nodesOnTheSameStorey.addAll(storey.getNodes());
            nodesOnTheSameStorey.addAll(storey.getConnectionNodes());
            for (int i = 0; i < nodesOnTheSameStorey.size(); i++) {
                for (int j = 0; j < nodesOnTheSameStorey.size(); j++) {
                    if (i != j) {
                        Node iNode, jNode;
                        EuclideanCoordinate iCoord, jCoord;
                        iNode = nodesOnTheSameStorey.get(i);
                        jNode = nodesOnTheSameStorey.get(j);
                        iCoord = iNode.getEuclideanCoordinate();
                        jCoord = jNode.getEuclideanCoordinate();
                        double dist = iCoord.distanceWithAnotherCoordinate(jCoord);
                        weightMatrix[iNode.getAuxilaryID()][jNode.getAuxilaryID()] = dist;
                    } else {
                        weightMatrix[i][j] = 0;
                    }
                }
            }
        }

        /*add connection weights to the weight matrix*/
        for (Connection connection : connections) {
            Node node1 = connection.getNode1();
            Node node2 = connection.getNode2();
            double weight = connection.getWeight();
            weightMatrix[node1.getAuxilaryID()][node2.getAuxilaryID()] = weight;
            weightMatrix[node2.getAuxilaryID()][node1.getAuxilaryID()] = weight;
        }

        return weightMatrix;
    }

    private void writeWeightMatrix(double[][] weightMatrix) {
        for (int i = 0; i < weightMatrix.length; i++) {
            for (int j = 0; j < weightMatrix.length; j++) {
                System.out.printf("%.2f", weightMatrix[i][j]);
                System.out.print("\t");
            }
            System.out.println("");
        }
    }

    private void updateTotalDemand(int diffDemand) {
        totalDemand += diffDemand;
        jLabelTotalDemand.setText("Total Demand: " + totalDemand);
        jLabelNumOfCustomers.setText("Num. of Customers: " + (numOfCustomers == 0 || numOfCustomers == 1 ? 0 : numOfCustomers - 1));
    }

    private int calculateNodeCount() {
        int nodeCount = 0;
        for (Storey storey : storeys) {
            nodeCount += storey.getNodeCount();
        }
        return nodeCount;
    }

    private Node findNodeWithAuxilaryID(int auxID) {
        for (Storey storey : storeys) {
            Node node = storey.findNodeWithAuxilaryID(auxID);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    private ArrayList<ArrayList<Node>> constructSolutionByNodes(Solution bestSolution, int depotNode, FloydsDistanceMatrixConstructor dmc) {
        ArrayList<ArrayList<Node>> solutionByNodes = new ArrayList<>();
        ArrayList<ArrayList<Integer>> routes = bestSolution.getRoutesAsArraylists();
        int size = routes.size();

        for (int i = 0; i < size; i++) {
            ArrayList<Node> nodeRoute = new ArrayList<>();
            ArrayList<Integer> route = routes.get(i);
            
            for (int j = 0; j < route.size() - 1; j++) {
                int from = route.get(j);
                int to = route.get(j + 1);
                nodeRoute.add(findNodeWithAuxilaryID(from));
                ArrayList<Integer> path = dmc.getShortestPath(from, to);
                if (path.size() > 2) { //there is at least one connection node
                    for (int k = 1; k < path.size() - 1; k++) {
                        nodeRoute.add(findNodeWithAuxilaryID(path.get(k)));
//                        System.out.println("Intermediate aux: " + path.get(k)
//                                + ", Intermediate real: " + findNodeWithAuxilaryID(path.get(k))
//                                + ", from: " + from + ", to: " + to);
                    }
                }
            }

            //find and add last node
            nodeRoute.add(findNodeWithAuxilaryID(route.get(route.size() - 1)));

            //find intermediate nodes between last node and the depot
            ArrayList<Integer> path = dmc.getShortestPath(route.get(route.size() - 1), depotNode);
            if (path.size() > 2) { //there is at least one connection node
                for (int k = 1; k < path.size() - 1; k++) {
                    nodeRoute.add(findNodeWithAuxilaryID(path.get(k)));
                }
            }

            //find intermediate nodes between depot node and the first node
            path = dmc.getShortestPath(depotNode, route.get(0));
            if (path.size() > 2) { //there is at least one connection node
                for (int k = 1; k < path.size() - 1; k++) {
                    nodeRoute.add(k - 1, findNodeWithAuxilaryID(path.get(k)));
                }
            }

            //add depot node to the start end end
            nodeRoute.add(0, findNodeWithAuxilaryID(depotNode));
            nodeRoute.add(findNodeWithAuxilaryID(depotNode));

            solutionByNodes.add(nodeRoute);
        }

        return solutionByNodes;
    }

    class comboboxItemListener implements ItemListener {

        // This method is called only if a new item has been selected.
        public void itemStateChanged(ItemEvent evt) {
            JComboBox cb = (JComboBox) evt.getSource();

            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED) {
                comboboxItemSelected(cb);
                changeStoreyPanel(cb);

            } else if (evt.getStateChange() == ItemEvent.DESELECTED) {

            }
        }
    }

    class storeyPanelMouseListener implements MouseListener, MouseMotionListener {

        JPanel panel;

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            panel = (JPanel) e.getSource();
            int storeyNo = -1;
            double posX = (double) e.getX() * mousePosXMultiplier;
            double posY = (double) e.getY() * mousePosYMultiplier;
            if (posX < 0 || posY < 0 || posX > storeyWidth || posY > storeyWidth) { //outside of the panel
                return;
            }

            if (panel.equals(jPanel1)) {
                storeyNo = jComboBox1.getSelectedIndex();
            } else if (panel.equals(jPanel2)) {
                storeyNo = jComboBox2.getSelectedIndex();
            }

            if (jRadioButtonNode.isSelected()) { //Add node mode

                Node node = new Node(((StoreyPanel) panel).getStorey(), new EuclideanCoordinate(posX, posY), MainWindow.this.nodeID++, DEFAULT_DEMAND);
                addNewNode(node);
                if (firstAddNodeClick && depot == null) {
                    node.setDepot(true);
                    node.setDemand(0);
                    updateTotalDemand(-1 * DEFAULT_DEMAND);
                    depot = node;
                }
                firstAddNodeClick = false;
                panel.repaint();
            } else { //Add conneciton mode
                //two clicks from two panels are required
                if (panel.equals(jPanel1)) {
                    ((StoreyPanel) panel).setCandidateConnectionNodeCoordinates(new EuclideanCoordinate(posX, posY));
                    panel.repaint();
                    panel1ConnectionNodeAdded = true;
                } else if (panel.equals(jPanel2)) {
                    ((StoreyPanel) panel).setCandidateConnectionNodeCoordinates(new EuclideanCoordinate(posX, posY));
                    panel.repaint();
                    panel2ConnectionNodeAdded = true;
                }

                if (panel1ConnectionNodeAdded && panel2ConnectionNodeAdded) {
                    //try to add connection by getting weight value from user
                    String weight = JOptionPane.showInputDialog("Enter Connection Weight");
                    if (weight != null) {
                        double connectionWeight = Double.valueOf(weight);
                        StoreyPanel storeyPanel1 = (StoreyPanel) jPanel1;
                        StoreyPanel storeyPanel2 = (StoreyPanel) jPanel2;
                        Node node1 = new Node(storeyPanel1.getStorey(), storeyPanel1.getCandidateConnectionNodeCoordinates(), MainWindow.this.nodeID++, -1);
                        Node node2 = new Node(storeyPanel2.getStorey(), storeyPanel2.getCandidateConnectionNodeCoordinates(), MainWindow.this.nodeID++, -1);

                        Connection connection = new Connection(node1, node2, connectionWeight);
                        addNewConnection(connection); //Add connection

                        panel1ConnectionNodeAdded = false;
                        panel2ConnectionNodeAdded = false;
                        storeyPanel1.setCandidateConnectionNodeCoordinates(null);
                        storeyPanel2.setCandidateConnectionNodeCoordinates(null);

                        jPanel1.repaint();
                        jPanel2.repaint();
                    }
                }
            }

        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
            panel = (JPanel) e.getSource();
            ((StoreyPanel) panel).setMousePosX(-1);
            ((StoreyPanel) panel).setMousePosY(-1);
            repaintStoreyPanels();
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            panel = (JPanel) e.getSource();
            ((StoreyPanel) panel).setMousePosX(e.getX());
            ((StoreyPanel) panel).setMousePosY(e.getY());
            repaintStoreyPanels();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupNodeOrConnection = new javax.swing.ButtonGroup();
        buttonGroupAddOrEdit = new javax.swing.ButtonGroup();
        jMenuItem1 = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new StoreyPanel();
        jPanel2 = new StoreyPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jRadioButtonNode = new javax.swing.JRadioButton();
        jRadioButtonConnection = new javax.swing.JRadioButton();
        jSeparator2 = new javax.swing.JSeparator();
        jRadioButtonAdd = new javax.swing.JRadioButton();
        jRadioButtonEditRemove = new javax.swing.JRadioButton();
        jPanelCardLayoutAddRemoveEdit = new javax.swing.JPanel();
        jPanelAddNode = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldAddNodeXPos = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jTextFieldAddNodeYPos = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldAddNodeStoreyNo = new javax.swing.JTextField();
        jButtonAddNodeManuel = new javax.swing.JButton();
        jCheckBoxDepotNode = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldaddNodeDemand = new javax.swing.JTextField();
        jPanelAddConnection = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldAddConnectionX1Pos = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldAddConnectionY1Pos = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldAddConnectionX2Pos = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldAddConnectionY2Pos = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldAddConnectionStorey1 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldAddConnectionStorey2 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldAddConnectionWeight = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jPanelEditRemoveNode = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListNodes = new javax.swing.JList();
        jButtonEditNode = new javax.swing.JButton();
        jButtonRemoveNode = new javax.swing.JButton();
        jPanelEditRemoveConnection = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListConnections = new javax.swing.JList();
        jButtonEditConneciton = new javax.swing.JButton();
        jButtonRemoveConnection = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelSetProblem = new javax.swing.JPanel();
        jLabelTotalDemand = new javax.swing.JLabel();
        jLabelNumOfCustomers = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldVehicleCapacity = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jTextFieldMaxRouteTime = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jTextFieldDropTime = new javax.swing.JTextField();
        jPanelSetAlgorithm = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldNp = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jTextFieldPMin = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jTextFieldNi = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jTextFieldPMax = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jTextFieldNc = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jTextFieldLambda = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jTextFieldBeta = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jCheckBoxBI = new javax.swing.JCheckBox();
        jButtonSolve = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaResultingTours = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNewProblem = new javax.swing.JMenuItem();
        jMenuItemSaveProblem = new javax.swing.JMenuItem();
        jMenuItemLoadProblem = new javax.swing.JMenuItem();
        jMenuItemGenerateProblem = new javax.swing.JMenuItem();
        jMenuItemScenario1 = new javax.swing.JMenuItem();
        jMenuItemScenario2 = new javax.swing.JMenuItem();
        jMenuItemScenario3 = new javax.swing.JMenuItem();
        jMenuItemScenario4 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuAbout = new javax.swing.JMenu();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Multi-storey VRP");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Storey Panel"));

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 400));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 400));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );

        jLabel3.setText("(0, 0)");

        jLabel4.setText("(0, 0)");

        jLabel5.setText("jLabel5");

        jLabel6.setText("jLabel6");

        jLabel7.setText("jLabel7");

        jLabel8.setText("jLabel8");

        jLabel9.setText("jLabel9");

        jLabel10.setText("jLabel10");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Mode"));

        jRadioButtonNode.setText("Customer");
        jRadioButtonNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNodeActionPerformed(evt);
            }
        });

        jRadioButtonConnection.setText("Connection");
        jRadioButtonConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonConnectionActionPerformed(evt);
            }
        });

        jRadioButtonAdd.setText("Add");
        jRadioButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAddActionPerformed(evt);
            }
        });

        jRadioButtonEditRemove.setText("Edit/Remove");
        jRadioButtonEditRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonEditRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jRadioButtonNode)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonConnection)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jSeparator2)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jRadioButtonAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jRadioButtonEditRemove)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonNode)
                    .addComponent(jRadioButtonConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonAdd)
                    .addComponent(jRadioButtonEditRemove)))
        );

        jPanelCardLayoutAddRemoveEdit.setLayout(new java.awt.CardLayout());

        jPanelAddNode.setBorder(javax.swing.BorderFactory.createTitledBorder("Add Customer"));

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("X Pos:");

        jLabel15.setText("Y Pos:");

        jLabel16.setText("Storey No:");

        jButtonAddNodeManuel.setBackground(new java.awt.Color(153, 255, 153));
        jButtonAddNodeManuel.setText("Add");
        jButtonAddNodeManuel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddNodeManuelActionPerformed(evt);
            }
        });

        jCheckBoxDepotNode.setText("Is Depot?");

        jLabel1.setText("Demand:");

        javax.swing.GroupLayout jPanelAddNodeLayout = new javax.swing.GroupLayout(jPanelAddNode);
        jPanelAddNode.setLayout(jPanelAddNodeLayout);
        jPanelAddNodeLayout.setHorizontalGroup(
            jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddNodeLayout.createSequentialGroup()
                .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAddNodeLayout.createSequentialGroup()
                        .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldAddNodeStoreyNo, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(jTextFieldAddNodeXPos)))
                    .addComponent(jCheckBoxDepotNode))
                .addGap(27, 27, 27)
                .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAddNodeLayout.createSequentialGroup()
                        .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldaddNodeDemand, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAddNodeYPos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButtonAddNodeManuel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelAddNodeLayout.setVerticalGroup(
            jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddNodeLayout.createSequentialGroup()
                .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextFieldAddNodeXPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldAddNodeYPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jTextFieldAddNodeStoreyNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldaddNodeDemand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelAddNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxDepotNode)
                    .addComponent(jButtonAddNodeManuel))
                .addGap(61, 61, 61))
        );

        jPanelCardLayoutAddRemoveEdit.add(jPanelAddNode, "card2");

        jPanelAddConnection.setBorder(javax.swing.BorderFactory.createTitledBorder("Add Connection"));

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel11.setText("X1 Pos:");

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel12.setText("Y1 Pos:");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel17.setText("X2 Pos:");

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel13.setText("Y2 Pos:");

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel18.setText("Storey 1:");

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel19.setText("Storey 2:");

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel20.setText("Weight:");

        jButton4.setBackground(new java.awt.Color(153, 255, 153));
        jButton4.setText("Add");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelAddConnectionLayout = new javax.swing.GroupLayout(jPanelAddConnection);
        jPanelAddConnection.setLayout(jPanelAddConnectionLayout);
        jPanelAddConnectionLayout.setHorizontalGroup(
            jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddConnectionLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAddConnectionLayout.createSequentialGroup()
                        .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldAddConnectionX1Pos, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAddConnectionX2Pos, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelAddConnectionLayout.createSequentialGroup()
                        .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldAddConnectionStorey1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAddConnectionWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelAddConnectionLayout.createSequentialGroup()
                        .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldAddConnectionY1Pos, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAddConnectionY2Pos, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAddConnectionStorey2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelAddConnectionLayout.setVerticalGroup(
            jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAddConnectionLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextFieldAddConnectionX1Pos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jTextFieldAddConnectionY1Pos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jTextFieldAddConnectionX2Pos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jTextFieldAddConnectionY2Pos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jTextFieldAddConnectionStorey1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jTextFieldAddConnectionStorey2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAddConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jTextFieldAddConnectionWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addGap(14, 14, 14))
        );

        jPanelCardLayoutAddRemoveEdit.add(jPanelAddConnection, "card3");

        jPanelEditRemoveNode.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit/Remove Customer"));

        jScrollPane1.setViewportView(jListNodes);

        jButtonEditNode.setBackground(new java.awt.Color(153, 255, 153));
        jButtonEditNode.setText("Edit");
        jButtonEditNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditNodeActionPerformed(evt);
            }
        });

        jButtonRemoveNode.setBackground(new java.awt.Color(153, 255, 153));
        jButtonRemoveNode.setText("Remove");
        jButtonRemoveNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveNodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelEditRemoveNodeLayout = new javax.swing.GroupLayout(jPanelEditRemoveNode);
        jPanelEditRemoveNode.setLayout(jPanelEditRemoveNodeLayout);
        jPanelEditRemoveNodeLayout.setHorizontalGroup(
            jPanelEditRemoveNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
            .addGroup(jPanelEditRemoveNodeLayout.createSequentialGroup()
                .addComponent(jButtonEditNode, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonRemoveNode, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelEditRemoveNodeLayout.setVerticalGroup(
            jPanelEditRemoveNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEditRemoveNodeLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEditRemoveNodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonEditNode)
                    .addComponent(jButtonRemoveNode)))
        );

        jPanelCardLayoutAddRemoveEdit.add(jPanelEditRemoveNode, "card4");

        jPanelEditRemoveConnection.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit/Remove Connection"));

        jScrollPane2.setViewportView(jListConnections);

        jButtonEditConneciton.setBackground(new java.awt.Color(153, 255, 153));
        jButtonEditConneciton.setText("Edit");
        jButtonEditConneciton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditConnecitonActionPerformed(evt);
            }
        });

        jButtonRemoveConnection.setBackground(new java.awt.Color(153, 255, 153));
        jButtonRemoveConnection.setText("Remove");
        jButtonRemoveConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveConnectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelEditRemoveConnectionLayout = new javax.swing.GroupLayout(jPanelEditRemoveConnection);
        jPanelEditRemoveConnection.setLayout(jPanelEditRemoveConnectionLayout);
        jPanelEditRemoveConnectionLayout.setHorizontalGroup(
            jPanelEditRemoveConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEditRemoveConnectionLayout.createSequentialGroup()
                .addComponent(jButtonEditConneciton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonRemoveConnection, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
        );
        jPanelEditRemoveConnectionLayout.setVerticalGroup(
            jPanelEditRemoveConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEditRemoveConnectionLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEditRemoveConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonEditConneciton)
                    .addComponent(jButtonRemoveConnection)))
        );

        jPanelCardLayoutAddRemoveEdit.add(jPanelEditRemoveConnection, "card5");

        jButton1.setText("<html>\n<p>3D</p>\n<p>View</p>\n</html>");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanelSetProblem.setLayout(new java.awt.GridLayout(4, 2, 10, 10));

        jLabelTotalDemand.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTotalDemand.setText("Total Demand:");
        jPanelSetProblem.add(jLabelTotalDemand);

        jLabelNumOfCustomers.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelNumOfCustomers.setText("Num. of Customers:");
        jPanelSetProblem.add(jLabelNumOfCustomers);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel21.setText("Vehicle Capacity:");
        jPanelSetProblem.add(jLabel21);

        jTextFieldVehicleCapacity.setText("100");
        jPanelSetProblem.add(jTextFieldVehicleCapacity);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel22.setText("Max Route Time:");
        jPanelSetProblem.add(jLabel22);

        jTextFieldMaxRouteTime.setText("99999999");
        jPanelSetProblem.add(jTextFieldMaxRouteTime);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Drop Time:");
        jPanelSetProblem.add(jLabel23);

        jTextFieldDropTime.setText("0");
        jPanelSetProblem.add(jTextFieldDropTime);

        jTabbedPane1.addTab("Problem Parameters", jPanelSetProblem);

        jPanelSetAlgorithm.setLayout(new java.awt.GridLayout(4, 4, 10, 10));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("np:");
        jPanelSetAlgorithm.add(jLabel2);

        jTextFieldNp.setText("5");
        jPanelSetAlgorithm.add(jTextFieldNp);

        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText("pMin:");
        jPanelSetAlgorithm.add(jLabel28);

        jTextFieldPMin.setText("1");
        jPanelSetAlgorithm.add(jTextFieldPMin);

        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel30.setText("ni:");
        jPanelSetAlgorithm.add(jLabel30);

        jTextFieldNi.setText("100");
        jPanelSetAlgorithm.add(jTextFieldNi);

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel29.setText("pMax:");
        jPanelSetAlgorithm.add(jLabel29);

        jTextFieldPMax.setText("1");
        jPanelSetAlgorithm.add(jTextFieldPMax);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("nc:");
        jPanelSetAlgorithm.add(jLabel27);

        jTextFieldNc.setText("10");
        jPanelSetAlgorithm.add(jTextFieldNc);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("lambda:");
        jPanelSetAlgorithm.add(jLabel24);

        jTextFieldLambda.setText("3");
        jPanelSetAlgorithm.add(jTextFieldLambda);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("beta:");
        jPanelSetAlgorithm.add(jLabel25);

        jTextFieldBeta.setText("0.05");
        jPanelSetAlgorithm.add(jTextFieldBeta);

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("bi:");
        jPanelSetAlgorithm.add(jLabel26);
        jPanelSetAlgorithm.add(jCheckBoxBI);

        jTabbedPane1.addTab("Algorithm Parameters", jPanelSetAlgorithm);

        jButtonSolve.setBackground(new java.awt.Color(153, 255, 153));
        jButtonSolve.setText("Solve");
        jButtonSolve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSolveActionPerformed(evt);
            }
        });

        jTextAreaResultingTours.setEditable(false);
        jTextAreaResultingTours.setColumns(20);
        jTextAreaResultingTours.setForeground(new java.awt.Color(0, 153, 0));
        jTextAreaResultingTours.setRows(5);
        jScrollPane3.setViewportView(jTextAreaResultingTours);

        jMenuFile.setText("File");

        jMenuItemNewProblem.setText("New Problem");
        jMenuItemNewProblem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewProblemActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemNewProblem);

        jMenuItemSaveProblem.setText("Save Problem");
        jMenuItemSaveProblem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveProblemActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveProblem);

        jMenuItemLoadProblem.setText("Load Problem");
        jMenuItemLoadProblem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadProblemActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemLoadProblem);

        jMenuItemGenerateProblem.setText("Generate Problem");
        jMenuItemGenerateProblem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGenerateProblemActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemGenerateProblem);

        jMenuItemScenario1.setText("Scenario 1");
        jMenuItemScenario1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScenario1ActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemScenario1);

        jMenuItemScenario2.setText("Scenario 2");
        jMenuItemScenario2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScenario2ActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemScenario2);

        jMenuItemScenario3.setText("Scenario 3");
        jMenuItemScenario3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScenario3ActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemScenario3);

        jMenuItemScenario4.setText("Scenario 4");
        jMenuItemScenario4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScenario4ActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemScenario4);
        jMenuFile.add(jSeparator1);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar1.add(jMenuFile);

        jMenuAbout.setText("About");
        jMenuBar1.add(jMenuAbout);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonSolve)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jTabbedPane1)
                    .addComponent(jPanelCardLayoutAddRemoveEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanelCardLayoutAddRemoveEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButtonSolve, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        MainWindow.this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jRadioButtonNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNodeActionPerformed
        CardLayout card = (CardLayout) jPanelCardLayoutAddRemoveEdit.getLayout();
        if (jRadioButtonAdd.isSelected()) {
            card.show(jPanelCardLayoutAddRemoveEdit, "card2");
        } else {
            card.show(jPanelCardLayoutAddRemoveEdit, "card4");
        }

        StoreyPanel storeyPanel1 = (StoreyPanel) jPanel1;
        StoreyPanel storeyPanel2 = (StoreyPanel) jPanel2;

        panel1ConnectionNodeAdded = false;
        panel2ConnectionNodeAdded = false;
        storeyPanel1.setCandidateConnectionNodeCoordinates(null);
        storeyPanel2.setCandidateConnectionNodeCoordinates(null);

        jPanel1.repaint();
        jPanel2.repaint();
    }//GEN-LAST:event_jRadioButtonNodeActionPerformed

    private void jMenuItemNewProblemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewProblemActionPerformed
        JTextField textFieldNumberOfStoreys = new JTextField("3", 5);
        JTextField textFieldStoreyWidth = new JTextField("400", 5);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(2, 2, 5, 5));
        myPanel.add(new JLabel("Number of Storeys:"));
        myPanel.add(textFieldNumberOfStoreys);
        myPanel.add(new JLabel("Width of Storeys:"));
        myPanel.add(textFieldStoreyWidth);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Parameters of New Problem", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String nos = textFieldNumberOfStoreys.getText();
            String sw = textFieldStoreyWidth.getText();
            //Check the format of the values with limits and number format exception !!
            createNewProblem(Integer.valueOf(nos), Integer.valueOf(sw));
        }
    }//GEN-LAST:event_jMenuItemNewProblemActionPerformed

    private void jRadioButtonConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonConnectionActionPerformed
        CardLayout card = (CardLayout) jPanelCardLayoutAddRemoveEdit.getLayout();
        if (jRadioButtonAdd.isSelected()) {
            card.show(jPanelCardLayoutAddRemoveEdit, "card3");
        } else {
            card.show(jPanelCardLayoutAddRemoveEdit, "card5");
        }
    }//GEN-LAST:event_jRadioButtonConnectionActionPerformed

    private void jRadioButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAddActionPerformed
        CardLayout card = (CardLayout) jPanelCardLayoutAddRemoveEdit.getLayout();
        if (jRadioButtonNode.isSelected()) {
            card.show(jPanelCardLayoutAddRemoveEdit, "card2");
        } else {
            card.show(jPanelCardLayoutAddRemoveEdit, "card3");
        }
    }//GEN-LAST:event_jRadioButtonAddActionPerformed

    private void jRadioButtonEditRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonEditRemoveActionPerformed
        CardLayout card = (CardLayout) jPanelCardLayoutAddRemoveEdit.getLayout();
        if (jRadioButtonNode.isSelected()) {
            card.show(jPanelCardLayoutAddRemoveEdit, "card4");
        } else {
            card.show(jPanelCardLayoutAddRemoveEdit, "card5");
        }
    }//GEN-LAST:event_jRadioButtonEditRemoveActionPerformed

    private void jButtonRemoveNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveNodeActionPerformed
        Node node = (Node) jListNodes.getSelectedValue();
        if (node != null) {
            removeNode(node);
        }
    }//GEN-LAST:event_jButtonRemoveNodeActionPerformed

    private void jButtonRemoveConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveConnectionActionPerformed
        Connection connection = (Connection) jListConnections.getSelectedValue();
        if (connection != null) {
            removeConnection(connection);
        }
    }//GEN-LAST:event_jButtonRemoveConnectionActionPerformed

    private void jButtonAddNodeManuelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddNodeManuelActionPerformed
        double xPos = Double.valueOf(jTextFieldAddNodeXPos.getText());
        double yPos = Double.valueOf(jTextFieldAddNodeYPos.getText());
        int storeyNo = Integer.valueOf(jTextFieldAddNodeStoreyNo.getText());
        int demand = Integer.valueOf(jTextFieldaddNodeDemand.getText());
        Storey storey = storeys.get(storeyNo);

        Node node = new Node(storey, new EuclideanCoordinate(xPos, yPos), MainWindow.this.nodeID++, demand);
        if (jCheckBoxDepotNode.isSelected()) {
            node.setDepot(true);
            if (depot != null) {
                depot.setDepot(false); //it is not depot from now on
            }
            depot = node; //new depot node is assigned
            jCheckBoxDepotNode.setSelected(false);
        }
        addNewNode(node);
        repaintStoreyPanels();
    }//GEN-LAST:event_jButtonAddNodeManuelActionPerformed

    private void jButtonEditNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditNodeActionPerformed
        Node node = (Node) jListNodes.getSelectedValue();
        if (node == null) {
            return; //There is no selected item
        }

        boolean wasDepotNode = false;

        JTextField jTextFieldXPos = new JTextField(String.valueOf(node.getEuclideanCoordinate().getX()));
        JTextField jTextFieldYPos = new JTextField(String.valueOf(node.getEuclideanCoordinate().getY()));
        JTextField jTextFieldStorey = new JTextField(String.valueOf(node.getStorey().getNo()));
        JTextField jTextFieldDemand = new JTextField(String.valueOf(node.getDemand()));
        JCheckBox jCheckBoxDepot = new JCheckBox("Is Depot?", false);
        if (node.isDepot()) {
            jCheckBoxDepot.setSelected(true);
            wasDepotNode = true;
        }

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(5, 2, 5, 5));
        myPanel.add(new JLabel("X Pos:"));
        myPanel.add(jTextFieldXPos);
        myPanel.add(new JLabel("Y Pos:"));
        myPanel.add(jTextFieldYPos);
        myPanel.add(new JLabel("Demand:"));
        myPanel.add(jTextFieldDemand);
        myPanel.add(new JLabel("Storey:"));
        myPanel.add(jTextFieldStorey);
        myPanel.add(jCheckBoxDepot);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Edit Node", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String xPos = jTextFieldXPos.getText();
            String yPos = jTextFieldYPos.getText();
            String storeyNo = jTextFieldStorey.getText();
            String demand = jTextFieldDemand.getText();
            boolean depotSelected = jCheckBoxDepot.isSelected();
            int diffDemand = Integer.valueOf(demand) - node.getDemand();
            updateTotalDemand(diffDemand);
            //Check the format of the values with limits and number format exception !!
            node.updateNode(storeys.get(Integer.valueOf(storeyNo)), new EuclideanCoordinate(Double.valueOf(xPos), Double.valueOf(yPos)), Integer.valueOf(demand));
            if (depotSelected) {
                if (!wasDepotNode) { //newly selected as depot node
                    node.setDepot(true);
                    if (depot != null) {
                        depot.setDepot(false); //it is not depot from now on
                    }
                    depot = node; //new depot node is assigned
                }
            } else if (wasDepotNode) { //will not be depot anymore
                node.setDepot(false);
                if (depot != null) {
                    depot = null;
                }
            }
        }

        jListNodes.repaint();
        repaintStoreyPanels();
    }//GEN-LAST:event_jButtonEditNodeActionPerformed

    private void jButtonEditConnecitonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditConnecitonActionPerformed
        Connection connection = (Connection) jListConnections.getSelectedValue();
        if (connection == null) {
            return; //There is no selected item
        }

        JTextField jTextFieldX1Pos = new JTextField(String.valueOf(connection.getNode1().getEuclideanCoordinate().getX()));
        JTextField jTextFieldY1Pos = new JTextField(String.valueOf(connection.getNode1().getEuclideanCoordinate().getY()));
        JTextField jTextFieldStorey1 = new JTextField(String.valueOf(connection.getNode1().getStorey().getNo()));
        JTextField jTextFieldX2Pos = new JTextField(String.valueOf(connection.getNode2().getEuclideanCoordinate().getX()));
        JTextField jTextFieldY2Pos = new JTextField(String.valueOf(connection.getNode2().getEuclideanCoordinate().getY()));
        JTextField jTextFieldStorey2 = new JTextField(String.valueOf(connection.getNode2().getStorey().getNo()));
        JTextField jTextFieldweight = new JTextField(String.valueOf(connection.getWeight()));

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(7, 2, 5, 5));
        myPanel.add(new JLabel("X1 Pos:"));
        myPanel.add(jTextFieldX1Pos);
        myPanel.add(new JLabel("Y1 Pos:"));
        myPanel.add(jTextFieldY1Pos);
        myPanel.add(new JLabel("Storey1:"));
        myPanel.add(jTextFieldStorey1);
        myPanel.add(new JLabel("X2 Pos:"));
        myPanel.add(jTextFieldX2Pos);
        myPanel.add(new JLabel("Y2 Pos:"));
        myPanel.add(jTextFieldY2Pos);
        myPanel.add(new JLabel("Storey2:"));
        myPanel.add(jTextFieldStorey2);
        myPanel.add(new JLabel("Weight:"));
        myPanel.add(jTextFieldweight);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Edit Node", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String x1Pos = jTextFieldX1Pos.getText();
            String y1Pos = jTextFieldY1Pos.getText();
            String storeyNo1 = jTextFieldStorey1.getText();
            String x2Pos = jTextFieldX2Pos.getText();
            String y2Pos = jTextFieldY2Pos.getText();
            String storeyNo2 = jTextFieldStorey2.getText();
            String weight = jTextFieldweight.getText();

            //Check the format of the values with limits and number format exception !!
            Node node1 = new Node(storeys.get(Integer.valueOf(storeyNo1)),
                    new EuclideanCoordinate(Double.valueOf(x1Pos), Double.valueOf(y1Pos)),
                    -1, -1); //-1 is used as dummy ID and demand, because this is not a real node
            Node node2 = new Node(storeys.get(Integer.valueOf(storeyNo2)),
                    new EuclideanCoordinate(Double.valueOf(x2Pos), Double.valueOf(y2Pos)),
                    -1, -1);
            Connection newConnection = new Connection(node1, node2, Double.valueOf(weight));

            connection.updateConnection(newConnection);

        }

        jListConnections.repaint();
        repaintStoreyPanels();
    }//GEN-LAST:event_jButtonEditConnecitonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        java.awt.EventQueue.invokeLater(() -> {
            ThreeDimensionalViewWindow tdw = new ThreeDimensionalViewWindow(storeys, storeyWidth, connections, solution);
            tdw.setVisible(true);
        });
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItemSaveProblemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveProblemActionPerformed
        int nodeCount = 0;
        int connectionNodeCount = 0;

        for (Storey storey : storeys) {
            nodeCount += storey.getNodeCount();
            connectionNodeCount += storey.getConnectionNodeCount();
        }

        if (nodeCount + connectionNodeCount > 0) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose or Create a File");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("xml", "xml");
            fileChooser.setFileFilter(filter);

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    File fileToSave = fileChooser.getSelectedFile();
                    writeProblemToXMLFile(fileToSave);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(rootPane, "File Save Error", "File Save Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else {
            JOptionPane.showMessageDialog(rootPane, "You have not add any object or connection yet!", "Empty Problem Instance", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemSaveProblemActionPerformed

    private void jMenuItemLoadProblemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadProblemActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a File to Load Problem Instance");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xml", "xml");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                File fileToLoad = fileChooser.getSelectedFile();
                readProblemFromXMLFile(fileToLoad);
                repaintStoreyPanels();
                firstAddNodeClick = false;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(rootPane, "File Open Error", "File Open Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemLoadProblemActionPerformed

    private void jButtonSolveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSolveActionPerformed
        Random random = new Random(101);
        jTextAreaResultingTours.setText("");
        double[][] weightMatrix = constructWeightMatrix();
        FloydsDistanceMatrixConstructor dmc = new FloydsDistanceMatrixConstructor();
        int nodeCount = calculateNodeCount();
        double[][] distanceMatrix = dmc.constructDistanceMatrix(weightMatrix, nodeCount);
        int[] demands = new int[nodeCount];

        demands[0] = 0; //depot has no demand
        //construct demand array
        for (int i = 1; i < nodeCount; i++) {
            Node node = findNodeWithAuxilaryID(i);
            demands[i] = node.getDemand();
        }

        //construct problem
        Problem problem = new Problem();
        problem.setDemands(demands);
        problem.setDistanceMatrix(distanceMatrix);
        problem.setDropTime(Integer.valueOf(jTextFieldDropTime.getText()));
        problem.setMaxRouteTime(Integer.valueOf(jTextFieldMaxRouteTime.getText()));
        problem.setNumOfCustomers(nodeCount - 1);
        problem.setVehicleCapacity(Integer.valueOf(jTextFieldVehicleCapacity.getText()));
        problem.setDepot(0);

        ParameterList params = new ParameterList();
        params.setBeta(Double.valueOf(jTextFieldBeta.getText()));
        params.setRandom(random);
        params.setBi(jCheckBoxBI.isSelected());
        params.setLambda(Integer.valueOf(jTextFieldLambda.getText()));
        params.setNp(Integer.valueOf(jTextFieldNp.getText()));
        params.setNi(Integer.valueOf(jTextFieldNi.getText()));
        params.setNc(Integer.valueOf(jTextFieldNc.getText()));
        params.setpMax(Integer.valueOf(jTextFieldPMax.getText()));
        params.setpMin(Integer.valueOf(jTextFieldPMin.getText()));

        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum(Integer.valueOf(jTextFieldNp.getText()) * Integer.valueOf(jTextFieldNi.getText()) * Integer.valueOf(jTextFieldNc.getText()));
        jProgressBar1.setValue(0);

        GRASPxELSAlgorithm gels = new GRASPxELSAlgorithm(problem, params, jProgressBar1);
        Thread algorithmThread;
        algorithmThread = new Thread() {
            public void run() {
                //Solution sol = gels.solve();
                Solution sol = gels.solve();
                solution = constructSolutionByNodes(sol, problem.getDepot(), dmc);
                java.awt.EventQueue.invokeLater(() -> {
                    ThreeDimensionalViewWindow tdw = new ThreeDimensionalViewWindow(storeys, storeyWidth, connections, solution);
                    tdw.setVisible(true);
                });
                int size = solution.size();

                ArrayList<Integer> demands = sol.getDemandOfEachRoute();
                ArrayList<Double> times = sol.getTimeOfEachRoute();

                double totalTime = 0;
                String resultingTour = "";
                for (int i = 0; i < size; i++) {
                    resultingTour += "ROUTE " + i + ":\t" + " Time: " + times.get(i) + "\t" + " Demand: " + demands.get(i) + "\t Path: ";
                    int index = 0;
                    for (Node node : solution.get(i)) {
                        if (index == 0) {
                            resultingTour += node.getID();
                        } else {
                            resultingTour += "->" + node.getID();
                        }
                        index++;
                    }
                    resultingTour += "\n";
                    totalTime += times.get(i);
                }

                resultingTour += "\nTOTAL TIME: " + util.Util.applyPrecision(totalTime, 2);
                jTextAreaResultingTours.setText(resultingTour);
            }
        };
        algorithmThread.start();


    }//GEN-LAST:event_jButtonSolveActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        double x1Pos = Double.valueOf(jTextFieldAddConnectionX1Pos.getText());
        double y1Pos = Double.valueOf(jTextFieldAddConnectionY1Pos.getText());
        double x2Pos = Double.valueOf(jTextFieldAddConnectionX2Pos.getText());
        double y2Pos = Double.valueOf(jTextFieldAddConnectionY2Pos.getText());
        double weigth = Double.valueOf(jTextFieldAddConnectionWeight.getText());
        int storeyNo1 = Integer.valueOf(jTextFieldAddConnectionStorey1.getText());
        int storeyNo2 = Integer.valueOf(jTextFieldAddConnectionStorey2.getText());

        Storey storey1 = storeys.get(storeyNo1);
        Storey storey2 = storeys.get(storeyNo2);
        Node node1 = new Node(storey1, new EuclideanCoordinate(x1Pos, y1Pos), MainWindow.this.nodeID++, -1);
        Node node2 = new Node(storey2, new EuclideanCoordinate(x2Pos, y2Pos), MainWindow.this.nodeID++, -1);

        Connection connection = new Connection(node1, node2, weigth);
        addNewConnection(connection);

        repaintStoreyPanels();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jMenuItemGenerateProblemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGenerateProblemActionPerformed
        JTextField textFieldNumberOfStoreys = new JTextField("3", 5);
        JTextField textFieldStoreyWidth = new JTextField("400", 5);
        JTextField textFieldConnectionCount = new JTextField("2", 5);
        JTextField textFieldNumberOfCustomers = new JTextField("20", 5);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(2, 2, 5, 5));
        myPanel.add(new JLabel("Number of Storeys:"));
        myPanel.add(textFieldNumberOfStoreys);
        myPanel.add(new JLabel("Width of Storeys:"));
        myPanel.add(textFieldStoreyWidth);
        myPanel.add(new JLabel("Connection Count:"));
        myPanel.add(textFieldConnectionCount);
        myPanel.add(new JLabel("Number of Customers:"));
        myPanel.add(textFieldNumberOfCustomers);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Generate New Problem", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int nos = Integer.valueOf(textFieldNumberOfStoreys.getText());
            int sw = Integer.valueOf(textFieldStoreyWidth.getText());
            int cc = Integer.valueOf(textFieldConnectionCount.getText());
            int noc = Integer.valueOf(textFieldNumberOfCustomers.getText());

            createNewProblem(nos, sw);

            ArrayList<EuclideanCoordinate> connectionCoordinates = new ArrayList<>();

            for (int i = 0; i < cc; i++) {
                if (i == 0) {
                    connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                }
                if (i == 1) {
                    connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                }
                if (i == 2) {
                    connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                }
                if (i == 3) {
                    connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                }
            }

            //add depot
            Node depot = new Node(storeys.get(0), new EuclideanCoordinate(storeyWidth / 2, storeyWidth / 2), MainWindow.this.nodeID++, 0);
            depot.setDepot(true);
            addNewNode(depot);

            for (int j = 0; j < cc; j++) {
                ArrayList<Node> connectionNodes = new ArrayList<>();
                for (int i = 0; i < nos; i++) {
                    Node cNode = new Node(storeys.get(i), connectionCoordinates.get(j), MainWindow.this.nodeID++, -1);
                    connectionNodes.add(cNode);
                }
                for (int k = 0; k < nos - 1; k++) {
                    Node node1 = connectionNodes.get(k);
                    Node node2 = connectionNodes.get(k + 1);
                    Connection connection = new Connection(node1, node2, DEFAULT_CONNECTION_WEIGHT);
                    addNewConnection(connection);
                }
            }

            RandomVectorGenerator generator = new HaltonSequenceGenerator(2);

            Random random = new Random(100);

            //add nodes to each storey
            for (int i = 0; i < nos; i++) {
                Storey storey = storeys.get(i);
                for (int j = 0; j < noc; j++) {
                    double[] randomVector = generator.nextVector();
                    EuclideanCoordinate coordinate = new EuclideanCoordinate(randomVector[0] * storeyWidth, randomVector[1] * storeyWidth);
                    Node node = new Node(storey, coordinate, MainWindow.this.nodeID++, random.nextInt(10) + 1);
                    addNewNode(node);
                }
            }

        }
    }//GEN-LAST:event_jMenuItemGenerateProblemActionPerformed


    /**
     * Fixed connection count = 2 # of customers = 60, 90, 120 # of storeys = 2,
     * 3, 4, 5, 6
     *
     * @param evt
     */
    private void jMenuItemScenario2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScenario2ActionPerformed
        int not = 30;
        int SEED = 101;
        int cc = 2;
        double sw = 400;

        RandomVectorGenerator generator = new HaltonSequenceGenerator(2);

        for (int nos = 2; nos <= 6; nos++) {
            for (int noc = 60; noc <= 180; noc += 60) {
                for (int t = 0; t < not; t++) {
                    Random random = new Random(SEED++);

                    double randomVector[][] = new double[noc][];
                    for (int i = 0; i < noc; i++) {
                        randomVector[i] = generator.nextVector();
                    }

                    int demandVector[] = new int[noc];
                    for (int i = 0; i < noc; i++) {
                        demandVector[i] = random.nextInt(10) + 1;
                    }

                    createNewProblem(nos, sw);

                    ArrayList<EuclideanCoordinate> connectionCoordinates = new ArrayList<>();
                    switch (cc) {
                        case 1:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            break;
                        case 2:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            break;
                        case 4:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            break;
                        case 8:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(0, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth));
                            break;
                        default:
                            break;
                    }

                    //add depot
                    Node depot = new Node(storeys.get(0), new EuclideanCoordinate(sw / 2, sw / 2), MainWindow.this.nodeID++, 0);
                    depot.setDepot(true);
                    addNewNode(depot);

                    for (int j = 0; j < cc; j++) {
                        ArrayList<Node> connectionNodes = new ArrayList<>();
                        for (int i = 0; i < nos; i++) {
                            Node cNode = new Node(storeys.get(i), connectionCoordinates.get(j), MainWindow.this.nodeID++, -1);
                            connectionNodes.add(cNode);
                        }
                        for (int k = 0; k < nos - 1; k++) {
                            Node node1 = connectionNodes.get(k);
                            Node node2 = connectionNodes.get(k + 1);
                            Connection connection = new Connection(node1, node2, DEFAULT_CONNECTION_WEIGHT);
                            addNewConnection(connection);
                        }
                    }

                    int counter = 0;
                    //add nodes to each storey
                    for (int i = 0; i < nos; i++) {
                        Storey storey = storeys.get(i);
                        for (int j = 0; j < (noc / nos); j++) { //equal number of customers for each storey
                            double[] tempVector = randomVector[counter];
                            EuclideanCoordinate coordinate = new EuclideanCoordinate(tempVector[0] * sw, tempVector[1] * sw);
                            Node node = new Node(storey, coordinate, MainWindow.this.nodeID++, demandVector[counter]);
                            addNewNode(node);
                            counter++;
                        }
                    }

                    //Solve Problem
                    double[][] weightMatrix = constructWeightMatrix();
                    FloydsDistanceMatrixConstructor dmc = new FloydsDistanceMatrixConstructor();
                    int nodeCount = calculateNodeCount();
                    double[][] distanceMatrix = dmc.constructDistanceMatrix(weightMatrix, nodeCount);
                    int[] demands = new int[nodeCount];

                    demands[0] = 0; //depot has no demand
                    //construct demand array
                    for (int i = 1; i < nodeCount; i++) {
                        Node node = findNodeWithAuxilaryID(i);
                        demands[i] = node.getDemand();
                    }

                    //construct problem
                    Problem problem = new Problem();
                    problem.setDemands(demands);
                    problem.setDistanceMatrix(distanceMatrix);
                    problem.setDropTime(Integer.valueOf(jTextFieldDropTime.getText()));
                    problem.setMaxRouteTime(Integer.valueOf(jTextFieldMaxRouteTime.getText()));
                    problem.setNumOfCustomers(nodeCount - 1);
                    problem.setVehicleCapacity(Integer.valueOf(jTextFieldVehicleCapacity.getText()));
                    problem.setDepot(0);

                    ParameterList params = new ParameterList();
                    params.setBeta(Double.valueOf(jTextFieldBeta.getText()));
                    params.setRandom(random);
                    params.setBi(jCheckBoxBI.isSelected());
                    params.setLambda(Integer.valueOf(jTextFieldLambda.getText()));
                    params.setNp(Integer.valueOf(jTextFieldNp.getText()));
                    params.setNi(Integer.valueOf(jTextFieldNi.getText()));
                    params.setNc(Integer.valueOf(jTextFieldNc.getText()));
                    params.setpMax(Integer.valueOf(jTextFieldPMax.getText()));
                    params.setpMin(Integer.valueOf(jTextFieldPMin.getText()));

                    jProgressBar1.setMinimum(0);
                    jProgressBar1.setMaximum(Integer.valueOf(jTextFieldNp.getText()) * Integer.valueOf(jTextFieldNi.getText()) * Integer.valueOf(jTextFieldNc.getText()));
                    jProgressBar1.setValue(0);

                    GRASPxELSAlgorithm gels = new GRASPxELSAlgorithm(problem, params, jProgressBar1);
                    long startTime = System.nanoTime();
                    Solution sol = gels.solve();
                    long estimatedTime = System.nanoTime() - startTime;
                    System.out.println(nos + " " + noc + " " + t + " " + Util.applyPrecision(sol.getFitness(), 2) + " " + estimatedTime / 1000000);
                }
            }
        }
    }//GEN-LAST:event_jMenuItemScenario2ActionPerformed

    /**
     * Fixed storey count = 3 # of customers = 60, 90, 120 # of connections = 1,
     * 2 ,4, 8
     *
     * @param evt
     */
    private void jMenuItemScenario1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScenario1ActionPerformed
        int not = 30;
        int SEED = 101;
        int nos = 3;
        double sw = 400;

        RandomVectorGenerator generator = new HaltonSequenceGenerator(2);

        for (int cc = 1; cc <= 8; cc *= 2) {
            for (int noc = 60; noc <= 180; noc += 60) {
                for (int t = 0; t < not; t++) {
                    Random random = new Random(SEED++);

                    double randomVector[][] = new double[noc][];
                    for (int i = 0; i < noc; i++) {
                        randomVector[i] = generator.nextVector();
                    }

                    int demandVector[] = new int[noc];
                    for (int i = 0; i < noc; i++) {
                        demandVector[i] = random.nextInt(10) + 1;
                    }

                    createNewProblem(nos, sw);

                    ArrayList<EuclideanCoordinate> connectionCoordinates = new ArrayList<>();
                    switch (cc) {
                        case 1:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            break;
                        case 2:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            break;
                        case 4:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            break;
                        case 8:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(0, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth));
                            break;
                        default:
                            break;
                    }

                    //add depot
                    Node depot = new Node(storeys.get(0), new EuclideanCoordinate(sw / 2, sw / 2), MainWindow.this.nodeID++, 0);
                    depot.setDepot(true);
                    addNewNode(depot);

                    for (int j = 0; j < cc; j++) {
                        ArrayList<Node> connectionNodes = new ArrayList<>();
                        for (int i = 0; i < nos; i++) {
                            Node cNode = new Node(storeys.get(i), connectionCoordinates.get(j), MainWindow.this.nodeID++, -1);
                            connectionNodes.add(cNode);
                        }
                        for (int k = 0; k < nos - 1; k++) {
                            Node node1 = connectionNodes.get(k);
                            Node node2 = connectionNodes.get(k + 1);
                            Connection connection = new Connection(node1, node2, DEFAULT_CONNECTION_WEIGHT);
                            addNewConnection(connection);
                        }
                    }

                    int counter = 0;
                    //add nodes to each storey
                    for (int i = 0; i < nos; i++) {
                        Storey storey = storeys.get(i);
                        for (int j = 0; j < (noc / nos); j++) { //equal number of customers for each storey
                            double[] tempVector = randomVector[counter];
                            EuclideanCoordinate coordinate = new EuclideanCoordinate(tempVector[0] * sw, tempVector[1] * sw);
                            Node node = new Node(storey, coordinate, MainWindow.this.nodeID++, demandVector[counter]);
                            addNewNode(node);
                            counter++;
                        }
                    }

                    //Solve Problem
                    double[][] weightMatrix = constructWeightMatrix();
                    FloydsDistanceMatrixConstructor dmc = new FloydsDistanceMatrixConstructor();
                    int nodeCount = calculateNodeCount();
                    double[][] distanceMatrix = dmc.constructDistanceMatrix(weightMatrix, nodeCount);
                    int[] demands = new int[nodeCount];

                    demands[0] = 0; //depot has no demand
                    //construct demand array
                    for (int i = 1; i < nodeCount; i++) {
                        Node node = findNodeWithAuxilaryID(i);
                        demands[i] = node.getDemand();
                    }

                    //construct problem
                    Problem problem = new Problem();
                    problem.setDemands(demands);
                    problem.setDistanceMatrix(distanceMatrix);
                    problem.setDropTime(Integer.valueOf(jTextFieldDropTime.getText()));
                    problem.setMaxRouteTime(Integer.valueOf(jTextFieldMaxRouteTime.getText()));
                    problem.setNumOfCustomers(nodeCount - 1);
                    problem.setVehicleCapacity(Integer.valueOf(jTextFieldVehicleCapacity.getText()));
                    problem.setDepot(0);

                    ParameterList params = new ParameterList();
                    params.setBeta(Double.valueOf(jTextFieldBeta.getText()));
                    params.setRandom(random);
                    params.setBi(jCheckBoxBI.isSelected());
                    params.setLambda(Integer.valueOf(jTextFieldLambda.getText()));
                    params.setNp(Integer.valueOf(jTextFieldNp.getText()));
                    params.setNi(Integer.valueOf(jTextFieldNi.getText()));
                    params.setNc(Integer.valueOf(jTextFieldNc.getText()));
                    params.setpMax(Integer.valueOf(jTextFieldPMax.getText()));
                    params.setpMin(Integer.valueOf(jTextFieldPMin.getText()));

                    jProgressBar1.setMinimum(0);
                    jProgressBar1.setMaximum(Integer.valueOf(jTextFieldNp.getText()) * Integer.valueOf(jTextFieldNi.getText()) * Integer.valueOf(jTextFieldNc.getText()));
                    jProgressBar1.setValue(0);

                    GRASPxELSAlgorithm gels = new GRASPxELSAlgorithm(problem, params, jProgressBar1);
                    long startTime = System.nanoTime();
                    Solution sol = gels.solve();
                    long estimatedTime = System.nanoTime() - startTime;
                    
                    System.out.println(cc + " " + noc + " " + t + " " + Util.applyPrecision(sol.getFitness(), 2) + " " + estimatedTime / 1000000);
                }
            }
        }
    }//GEN-LAST:event_jMenuItemScenario1ActionPerformed

    /**
     * Fixed customer count = 120 # of storeys = 2, 3, 4, 5, 6 # of connections
     * = 1, 2 ,4, 8
     *
     * @param evt
     */
    private void jMenuItemScenario3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScenario3ActionPerformed
        int not = 30;
        int SEED = 101;
        int noc = 120;
        double sw = 400;

        RandomVectorGenerator generator = new HaltonSequenceGenerator(2);

        for (int cc = 1; cc <= 8; cc *= 2) {
            for (int nos = 2; nos <= 6; nos++) {
                for (int t = 0; t < not; t++) {
                    Random random = new Random(SEED++);

                    double randomVector[][] = new double[noc][];
                    for (int i = 0; i < noc; i++) {
                        randomVector[i] = generator.nextVector();
                    }

                    int demandVector[] = new int[noc];
                    for (int i = 0; i < noc; i++) {
                        demandVector[i] = random.nextInt(10) + 1;
                    }

                    createNewProblem(nos, sw);

                    ArrayList<EuclideanCoordinate> connectionCoordinates = new ArrayList<>();
                    switch (cc) {
                        case 1:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            break;
                        case 2:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            break;
                        case 4:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            break;
                        case 8:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(0, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth));
                            break;
                        default:
                            break;
                    }

                    //add depot
                    Node depot = new Node(storeys.get(0), new EuclideanCoordinate(sw / 2, sw / 2), MainWindow.this.nodeID++, 0);
                    depot.setDepot(true);
                    addNewNode(depot);

                    for (int j = 0; j < cc; j++) {
                        ArrayList<Node> connectionNodes = new ArrayList<>();
                        for (int i = 0; i < nos; i++) {
                            Node cNode = new Node(storeys.get(i), connectionCoordinates.get(j), MainWindow.this.nodeID++, -1);
                            connectionNodes.add(cNode);
                        }
                        for (int k = 0; k < nos - 1; k++) {
                            Node node1 = connectionNodes.get(k);
                            Node node2 = connectionNodes.get(k + 1);
                            Connection connection = new Connection(node1, node2, DEFAULT_CONNECTION_WEIGHT);
                            addNewConnection(connection);
                        }
                    }

                    int counter = 0;
                    //add nodes to each storey
                    for (int i = 0; i < nos; i++) {
                        Storey storey = storeys.get(i);
                        for (int j = 0; j < (noc / nos); j++) { //equal number of customers for each storey
                            double[] tempVector = randomVector[counter];
                            EuclideanCoordinate coordinate = new EuclideanCoordinate(tempVector[0] * sw, tempVector[1] * sw);
                            Node node = new Node(storey, coordinate, MainWindow.this.nodeID++, demandVector[counter]);
                            addNewNode(node);
                            counter++;
                        }
                    }

                    //Solve Problem
                    double[][] weightMatrix = constructWeightMatrix();
                    FloydsDistanceMatrixConstructor dmc = new FloydsDistanceMatrixConstructor();
                    int nodeCount = calculateNodeCount();
                    double[][] distanceMatrix = dmc.constructDistanceMatrix(weightMatrix, nodeCount);
                    int[] demands = new int[nodeCount];

                    demands[0] = 0; //depot has no demand
                    //construct demand array
                    for (int i = 1; i < nodeCount; i++) {
                        Node node = findNodeWithAuxilaryID(i);
                        demands[i] = node.getDemand();
                    }

                    //construct problem
                    Problem problem = new Problem();
                    problem.setDemands(demands);
                    problem.setDistanceMatrix(distanceMatrix);
                    problem.setDropTime(Integer.valueOf(jTextFieldDropTime.getText()));
                    problem.setMaxRouteTime(Integer.valueOf(jTextFieldMaxRouteTime.getText()));
                    problem.setNumOfCustomers(nodeCount - 1);
                    problem.setVehicleCapacity(Integer.valueOf(jTextFieldVehicleCapacity.getText()));
                    problem.setDepot(0);

                    ParameterList params = new ParameterList();
                    params.setBeta(Double.valueOf(jTextFieldBeta.getText()));
                    params.setRandom(random);
                    params.setBi(jCheckBoxBI.isSelected());
                    params.setLambda(Integer.valueOf(jTextFieldLambda.getText()));
                    params.setNp(Integer.valueOf(jTextFieldNp.getText()));
                    params.setNi(Integer.valueOf(jTextFieldNi.getText()));
                    params.setNc(Integer.valueOf(jTextFieldNc.getText()));
                    params.setpMax(Integer.valueOf(jTextFieldPMax.getText()));
                    params.setpMin(Integer.valueOf(jTextFieldPMin.getText()));

                    jProgressBar1.setMinimum(0);
                    jProgressBar1.setMaximum(Integer.valueOf(jTextFieldNp.getText()) * Integer.valueOf(jTextFieldNi.getText()) * Integer.valueOf(jTextFieldNc.getText()));
                    jProgressBar1.setValue(0);

                    GRASPxELSAlgorithm gels = new GRASPxELSAlgorithm(problem, params, jProgressBar1);
                    long startTime = System.nanoTime();
                    Solution sol = gels.solve();
                    long estimatedTime = System.nanoTime() - startTime;
                    System.out.println(cc + " " + nos + " " + t + " " + Util.applyPrecision(sol.getFitness(), 2) + " " + estimatedTime / 1000000);
                }
            }
        }
    }//GEN-LAST:event_jMenuItemScenario3ActionPerformed

    /**
     * Fixed total area = 480,000, Fixed number of customers = 120 # of storeys
     * = 2, 3, 4, 5, 6 # of connections = 1, 2 ,4, 8
     *
     * @param evt
     */
    private void jMenuItemScenario4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScenario4ActionPerformed
        int not = 30;
        int SEED = 101;
        int noc = 120;
        double sw;
        int totalArea = 480000;

        RandomVectorGenerator generator = new HaltonSequenceGenerator(2);

        for (int nos = 2; nos <= 6; nos++) {
            for (int cc = 1; cc <= 8; cc*=2) {
                for (int t = 0; t < not; t++) {
                    Random random = new Random(SEED++);

                    double randomVector[][] = new double[noc][];
                    for (int i = 0; i < noc; i++) {
                        randomVector[i] = generator.nextVector();
                    }

                    int demandVector[] = new int[noc];
                    for (int i = 0; i < noc; i++) {
                        demandVector[i] = random.nextInt(10) + 1;
                    }

                    sw = Math.sqrt((totalArea / nos));
                    createNewProblem(nos, sw);

                    ArrayList<EuclideanCoordinate> connectionCoordinates = new ArrayList<>();
                    switch (cc) {
                        case 1:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            break;
                        case 2:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            break;
                        case 4:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            break;
                        case 8:
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth / 2, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth / 2));
                            connectionCoordinates.add(new EuclideanCoordinate(0, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(0, storeyWidth));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, 0));
                            connectionCoordinates.add(new EuclideanCoordinate(storeyWidth, storeyWidth));
                            break;
                        default:
                            break;
                    }

                    //add depot
                    Node depot = new Node(storeys.get(0), new EuclideanCoordinate(sw / 2, sw / 2), MainWindow.this.nodeID++, 0);
                    depot.setDepot(true);
                    addNewNode(depot);

                    for (int j = 0; j < cc; j++) {
                        ArrayList<Node> connectionNodes = new ArrayList<>();
                        for (int i = 0; i < nos; i++) {
                            Node cNode = new Node(storeys.get(i), connectionCoordinates.get(j), MainWindow.this.nodeID++, -1);
                            connectionNodes.add(cNode);
                        }
                        for (int k = 0; k < nos - 1; k++) {
                            Node node1 = connectionNodes.get(k);
                            Node node2 = connectionNodes.get(k + 1);
                            Connection connection = new Connection(node1, node2, DEFAULT_CONNECTION_WEIGHT);
                            addNewConnection(connection);
                        }
                    }

                    int counter = 0;
                    //add nodes to each storey
                    for (int i = 0; i < nos; i++) {
                        Storey storey = storeys.get(i);
                        for (int j = 0; j < (noc / nos); j++) { //equal number of customers for each storey
                            double[] tempVector = randomVector[counter];
                            EuclideanCoordinate coordinate = new EuclideanCoordinate(tempVector[0] * sw, tempVector[1] * sw);
                            Node node = new Node(storey, coordinate, MainWindow.this.nodeID++, demandVector[counter]);
                            addNewNode(node);
                            counter++;
                        }
                    }

                    //Solve Problem
                    double[][] weightMatrix = constructWeightMatrix();
                    FloydsDistanceMatrixConstructor dmc = new FloydsDistanceMatrixConstructor();
                    int nodeCount = calculateNodeCount();
                    double[][] distanceMatrix = dmc.constructDistanceMatrix(weightMatrix, nodeCount);
                    int[] demands = new int[nodeCount];

                    demands[0] = 0; //depot has no demand
                    //construct demand array
                    for (int i = 1; i < nodeCount; i++) {
                        Node node = findNodeWithAuxilaryID(i);
                        demands[i] = node.getDemand();
                    }

                    //construct problem
                    Problem problem = new Problem();
                    problem.setDemands(demands);
                    problem.setDistanceMatrix(distanceMatrix);
                    problem.setDropTime(Integer.valueOf(jTextFieldDropTime.getText()));
                    problem.setMaxRouteTime(Integer.valueOf(jTextFieldMaxRouteTime.getText()));
                    problem.setNumOfCustomers(nodeCount - 1);
                    problem.setVehicleCapacity(Integer.valueOf(jTextFieldVehicleCapacity.getText()));
                    problem.setDepot(0);

                    ParameterList params = new ParameterList();
                    params.setBeta(Double.valueOf(jTextFieldBeta.getText()));
                    params.setRandom(random);
                    params.setBi(jCheckBoxBI.isSelected());
                    params.setLambda(Integer.valueOf(jTextFieldLambda.getText()));
                    params.setNp(Integer.valueOf(jTextFieldNp.getText()));
                    params.setNi(Integer.valueOf(jTextFieldNi.getText()));
                    params.setNc(Integer.valueOf(jTextFieldNc.getText()));
                    params.setpMax(Integer.valueOf(jTextFieldPMax.getText()));
                    params.setpMin(Integer.valueOf(jTextFieldPMin.getText()));

                    jProgressBar1.setMinimum(0);
                    jProgressBar1.setMaximum(Integer.valueOf(jTextFieldNp.getText()) * Integer.valueOf(jTextFieldNi.getText()) * Integer.valueOf(jTextFieldNc.getText()));
                    jProgressBar1.setValue(0);

                    GRASPxELSAlgorithm gels = new GRASPxELSAlgorithm(problem, params, jProgressBar1);
                    long startTime = System.nanoTime();
                    Solution sol = gels.solve();
                    long estimatedTime = System.nanoTime() - startTime;
                    System.out.println(nos + " " + cc + " " + t + " " + Util.applyPrecision(sol.getFitness(), 2) + " " + estimatedTime / 1000000);
                }
            }
        }
    }//GEN-LAST:event_jMenuItemScenario4ActionPerformed

    private Connection createConnectionFromXML(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event;
        Connection connection = null;
        int cpId1 = 0, cpId2 = 0;
        double weight = 0;
        while (eventReader.hasNext()) {
            event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();

                if (startElement.getName().getLocalPart().equals("connection_node_1")) {
                    while (eventReader.hasNext()) {
                        event = eventReader.nextEvent();

                        if (event.isStartElement()) {
                            startElement = event.asStartElement();

                            if (startElement.getName().getLocalPart().equals("id")) {
                                event = eventReader.nextEvent();
                                cpId1 = Integer.parseInt(event.asCharacters().getData());
                                break;
                            }
                        }
                    }

                }

                if (startElement.getName().getLocalPart().equals("connection_node_2")) {
                    while (eventReader.hasNext()) {
                        event = eventReader.nextEvent();

                        if (event.isStartElement()) {
                            startElement = event.asStartElement();

                            if (startElement.getName().getLocalPart().equals("id")) {
                                event = eventReader.nextEvent();
                                cpId2 = Integer.parseInt(event.asCharacters().getData());
                                break;
                            }
                        }
                    }

                }

                if (startElement.getName().getLocalPart().equals("weight")) {
                    event = eventReader.nextEvent();
                    weight = Double.parseDouble(event.asCharacters().getData());
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("connection")) {
                Node connectionNode1 = null, connectionNode2 = null;
                //find first connection point
                for (Storey storey : storeys) {
                    connectionNode1 = storey.findConnectionNode(cpId1);
                    if (connectionNode1 != null) {
                        break;
                    }
                }

                //find second connection point
                for (Storey storey : storeys) {
                    connectionNode2 = storey.findConnectionNode(cpId2);
                    if (connectionNode2 != null) {
                        break;
                    }
                }

                connection = new Connection(connectionNode1, connectionNode2, weight);
                return connection;
            }
        }

        return connection;
    }

    private void readProblemFromXMLFile(File fileToLoad) throws XMLStreamException {
        maxXMLID = 0;
        Storey storey;
        Connection connection;
        int depotNo = -1;

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        // Setup a new eventReader
        InputStream in = null;

        try {
            in = new FileInputStream(fileToLoad);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(rootPane, "File not Found", "File not Found", JOptionPane.ERROR_MESSAGE);
        }
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();

                //read depot
                if (startElement.getName().getLocalPart().equals("depot")) {
                    event = eventReader.nextEvent();
                    depotNo = Integer.parseInt(event.asCharacters().getData());
                }

                //read numberOfStoreys
                if (startElement.getName().getLocalPart().equals("number_of_storeys")) {
                    event = eventReader.nextEvent();
                    numOfStoreys = Integer.parseInt(event.asCharacters().getData());
                }

                //read storey width
                if (startElement.getName().getLocalPart().equals("storey_width")) {
                    event = eventReader.nextEvent();
                    storeyWidth = Integer.parseInt(event.asCharacters().getData());

                    initProblem(); //Now we can init problem with numOfStoreys and storeyWidth
                }

                //read storey
                if (startElement.getName().getLocalPart().equals("storey")) {
                    createStoreyFromXML(eventReader, depotNo);
                    nodeID = maxXMLID + 1;
                }

                //Read Connection
                if (startElement.getName().getLocalPart().equals("connection")) {
                    connection = createConnectionFromXML(eventReader);
                    addNewConnectionFromXML(connection);
                    nodeID = maxXMLID + 1;
                }
            }
        }

    }

    private Node createConnectionNodeFromXML(XMLEventReader eventReader, Storey storey) throws XMLStreamException {
        XMLEvent event;
        Node connectionNode = null;
        int id = 0;
        EuclideanCoordinate coordinates = null;
        while (eventReader.hasNext()) {
            event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();

                if (startElement.getName().getLocalPart().equals("id")) {
                    event = eventReader.nextEvent();
                    id = Integer.parseInt(event.asCharacters().getData());
                }

                if (startElement.getName().getLocalPart().equals("coordinates")) {
                    coordinates = createCoordinatesFromXML(eventReader);
                }

            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("connection_node")) {
                connectionNode = new Node(storey, coordinates, id, -1);
                return connectionNode;
            }

        }
        return connectionNode;
    }

    private EuclideanCoordinate createCoordinatesFromXML(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event;
        EuclideanCoordinate coordinates = null;
        double posX = 0;
        double posY = 0;
        while (eventReader.hasNext()) {
            event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();

                if (startElement.getName().getLocalPart().equals("position_x")) {
                    event = eventReader.nextEvent();

                    posX = Double.parseDouble(event.asCharacters().getData());
                }

                if (startElement.getName().getLocalPart().equals("position_y")) {
                    event = eventReader.nextEvent();
                    posY = Double.parseDouble(event.asCharacters().getData());
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("coordinates")) {
                coordinates = new EuclideanCoordinate(posX, posY);
                return coordinates;
            }
        }

        return coordinates;
    }

    private Node createNodeFromXML(XMLEventReader eventReader, Storey storey) throws XMLStreamException {
        XMLEvent event;
        Node node = null;
        int id = 0;
        EuclideanCoordinate coordinates = null;
        int demand = DEFAULT_DEMAND;
        while (eventReader.hasNext()) {
            event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();

                if (startElement.getName().getLocalPart().equals("id")) {
                    event = eventReader.nextEvent();
                    id = Integer.parseInt(event.asCharacters().getData());
                }

                if (startElement.getName().getLocalPart().equals("demand")) {
                    event = eventReader.nextEvent();
                    demand = Integer.parseInt(event.asCharacters().getData());
                }

                if (startElement.getName().getLocalPart().equals("coordinates")) {
                    coordinates = createCoordinatesFromXML(eventReader);
                }

            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("customer")) {
                node = new Node(storey, coordinates, id, demand);
                return node;
            }

        }
        return node;
    }

    private void createStoreyFromXML(XMLEventReader eventReader, int depotNo) throws XMLStreamException {
        Storey storey = null;
        int storeyNo;
        Node node;
        Node connectionNode;
        XMLEvent event;

        while (eventReader.hasNext()) {
            event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();

                if (startElement.getName().getLocalPart().equals("no")) {
                    event = eventReader.nextEvent();
                    storeyNo = Integer.parseInt(event.asCharacters().getData());
                    storey = storeys.get(storeyNo);
                }

                if (startElement.getName().getLocalPart().equals("customer")) {
                    node = createNodeFromXML(eventReader, storey);
                    if (node.getID() == depotNo) {// if this is a depot node
                        node.setDepot(true);
                        depot = node;
                    }
                    if (node.getID() > maxXMLID) {
                        maxXMLID = node.getID();
                    }
                    addNewNode(node);
                }

                if (startElement.getName().getLocalPart().equals("connection_node")) {
                    connectionNode = createConnectionNodeFromXML(eventReader, storey);
                    if (connectionNode.getID() > maxXMLID) {
                        maxXMLID = connectionNode.getID();
                    }
                    storey.addConnectionNode(connectionNode);
                }
            }

            //storey elementi bittigi zaman
            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("storey")) {
                return;
            }
        }
    }

    private void writeProblemToXMLFile(File fileToSave) throws Exception {

        if (!fileToSave.getAbsolutePath().endsWith(".xml")) {
            fileToSave = new File(fileToSave + ".xml");
        }
//        System.out.println("Save as file: " + fileToSave.getAbsolutePath());

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(fileToSave));

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");
        StartDocument startDocument = eventFactory.createStartDocument();
        eventWriter.add(startDocument);

        StartElement startElement;
        EndElement endElement;
        Characters characters;

        eventWriter.add(end);
        startElement = eventFactory.createStartElement("", "", "problem_instance");
        eventWriter.add(startElement);

        //depot node
        eventWriter.add(end);
        eventWriter.add(tab);
        startElement = eventFactory.createStartElement("", "", "depot");
        eventWriter.add(startElement);
        if (depot != null) {
            characters = eventFactory.createCharacters(String.valueOf(depot.getID()));
        } else {
            characters = eventFactory.createCharacters(String.valueOf(-1));
        }
        eventWriter.add(characters);
        endElement = eventFactory.createEndElement("", "", "depot");
        eventWriter.add(endElement);

        //number of storeys
        eventWriter.add(end);
        eventWriter.add(tab);
        startElement = eventFactory.createStartElement("", "", "number_of_storeys");
        eventWriter.add(startElement);
        characters = eventFactory.createCharacters(String.valueOf(storeys.size()));
        eventWriter.add(characters);
        endElement = eventFactory.createEndElement("", "", "number_of_storeys");
        eventWriter.add(endElement);

        //storey width
        eventWriter.add(end);
        eventWriter.add(tab);
        startElement = eventFactory.createStartElement("", "", "storey_width");
        eventWriter.add(startElement);
        characters = eventFactory.createCharacters(String.valueOf(storeyWidth));
        eventWriter.add(characters);
        endElement = eventFactory.createEndElement("", "", "storey_width");
        eventWriter.add(endElement);

        int storeyIndex = 0;
        for (Storey storey : storeys) {
            eventWriter.add(end);
            startElement = eventFactory.createStartElement("", "", "storey");
            eventWriter.add(tab);
            eventWriter.add(startElement);
            eventWriter.add(end);

            eventWriter.add(tab);
            eventWriter.add(tab);
            startElement = eventFactory.createStartElement("", "", "no");
            eventWriter.add(startElement);
            characters = eventFactory.createCharacters(String.valueOf(storeyIndex++));
            eventWriter.add(characters);
            endElement = eventFactory.createEndElement("", "", "no");
            eventWriter.add(endElement);
            eventWriter.add(end);

            for (Node node : storey.getNodes()) {
                startElement = eventFactory.createStartElement("", "", "customer");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                eventWriter.add(end);

                startElement = eventFactory.createStartElement("", "", "id");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(node.getID()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "id");
                eventWriter.add(endElement);
                eventWriter.add(end);

                startElement = eventFactory.createStartElement("", "", "demand");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(node.getDemand()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "demand");
                eventWriter.add(endElement);
                eventWriter.add(end);

                startElement = eventFactory.createStartElement("", "", "coordinates");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                eventWriter.add(end);
                startElement = eventFactory.createStartElement("", "", "position_x");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(node.getEuclideanCoordinate().getX()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "position_x");
                eventWriter.add(endElement);
                eventWriter.add(end);
                startElement = eventFactory.createStartElement("", "", "position_y");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(node.getEuclideanCoordinate().getY()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "position_y");
                eventWriter.add(endElement);
                eventWriter.add(end);
                endElement = eventFactory.createEndElement("", "", "coordinates");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(endElement);
                eventWriter.add(end);

                endElement = eventFactory.createEndElement("", "", "customer");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(endElement);
                eventWriter.add(end);
            }

            for (Node connectionNode : storey.getConnectionNodes()) {
                startElement = eventFactory.createStartElement("", "", "connection_node");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                eventWriter.add(end);

                startElement = eventFactory.createStartElement("", "", "id");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(connectionNode.getID()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "id");
                eventWriter.add(endElement);
                eventWriter.add(end);

                startElement = eventFactory.createStartElement("", "", "coordinates");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                eventWriter.add(end);
                startElement = eventFactory.createStartElement("", "", "position_x");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(connectionNode.getEuclideanCoordinate().getX()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "position_x");
                eventWriter.add(endElement);
                eventWriter.add(end);
                startElement = eventFactory.createStartElement("", "", "position_y");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(startElement);
                characters = eventFactory.createCharacters(String.valueOf(connectionNode.getEuclideanCoordinate().getY()));
                eventWriter.add(characters);
                endElement = eventFactory.createEndElement("", "", "position_y");
                eventWriter.add(endElement);
                eventWriter.add(end);
                endElement = eventFactory.createEndElement("", "", "coordinates");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(endElement);
                eventWriter.add(end);

                endElement = eventFactory.createEndElement("", "", "connection_node");
                eventWriter.add(tab);
                eventWriter.add(tab);
                eventWriter.add(endElement);
                eventWriter.add(end);
            }

            endElement = eventFactory.createEndElement("", "", "storey");
            eventWriter.add(tab);
            eventWriter.add(endElement);
        }

        for (Connection connection : connections) {
            eventWriter.add(end);
            startElement = eventFactory.createStartElement("", "", "connection");
            eventWriter.add(tab);
            eventWriter.add(startElement);
            eventWriter.add(end);

            startElement = eventFactory.createStartElement("", "", "connection_node_1");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(startElement);
            eventWriter.add(end);
            startElement = eventFactory.createStartElement("", "", "id");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(startElement);
            characters = eventFactory.createCharacters(String.valueOf(connection.getNode1().getID()));
            eventWriter.add(characters);
            endElement = eventFactory.createEndElement("", "", "id");
            eventWriter.add(endElement);
            eventWriter.add(end);
            endElement = eventFactory.createEndElement("", "", "connection_node_1");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(endElement);
            eventWriter.add(end);

            startElement = eventFactory.createStartElement("", "", "connection_node_2");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(startElement);
            eventWriter.add(end);
            startElement = eventFactory.createStartElement("", "", "id");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(startElement);
            characters = eventFactory.createCharacters(String.valueOf(connection.getNode2().getID()));
            eventWriter.add(characters);
            endElement = eventFactory.createEndElement("", "", "id");
            eventWriter.add(endElement);
            eventWriter.add(end);
            endElement = eventFactory.createEndElement("", "", "connection_node_2");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(endElement);
            eventWriter.add(end);

            startElement = eventFactory.createStartElement("", "", "weight");
            eventWriter.add(tab);
            eventWriter.add(tab);
            eventWriter.add(startElement);
            characters = eventFactory.createCharacters(String.valueOf(connection.getWeight()));
            eventWriter.add(characters);
            endElement = eventFactory.createEndElement("", "", "weight");
            eventWriter.add(endElement);
            eventWriter.add(end);

            endElement = eventFactory.createEndElement("", "", "connection");
            eventWriter.add(tab);
            eventWriter.add(endElement);
        }

        eventWriter.add(end);
        endElement = eventFactory.createEndElement("", "", "problem_instance");
        eventWriter.add(endElement);

        eventWriter.add(end);
        eventWriter.add(eventFactory.createEndDocument());
        eventWriter.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupAddOrEdit;
    private javax.swing.ButtonGroup buttonGroupNodeOrConnection;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButtonAddNodeManuel;
    private javax.swing.JButton jButtonEditConneciton;
    private javax.swing.JButton jButtonEditNode;
    private javax.swing.JButton jButtonRemoveConnection;
    private javax.swing.JButton jButtonRemoveNode;
    private javax.swing.JButton jButtonSolve;
    private javax.swing.JCheckBox jCheckBoxBI;
    private javax.swing.JCheckBox jCheckBoxDepotNode;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelNumOfCustomers;
    private javax.swing.JLabel jLabelTotalDemand;
    private javax.swing.JList jListConnections;
    private javax.swing.JList jListNodes;
    private javax.swing.JMenu jMenuAbout;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemGenerateProblem;
    private javax.swing.JMenuItem jMenuItemLoadProblem;
    private javax.swing.JMenuItem jMenuItemNewProblem;
    private javax.swing.JMenuItem jMenuItemSaveProblem;
    private javax.swing.JMenuItem jMenuItemScenario1;
    private javax.swing.JMenuItem jMenuItemScenario2;
    private javax.swing.JMenuItem jMenuItemScenario3;
    private javax.swing.JMenuItem jMenuItemScenario4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelAddConnection;
    private javax.swing.JPanel jPanelAddNode;
    private javax.swing.JPanel jPanelCardLayoutAddRemoveEdit;
    private javax.swing.JPanel jPanelEditRemoveConnection;
    private javax.swing.JPanel jPanelEditRemoveNode;
    private javax.swing.JPanel jPanelSetAlgorithm;
    private javax.swing.JPanel jPanelSetProblem;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JRadioButton jRadioButtonAdd;
    private javax.swing.JRadioButton jRadioButtonConnection;
    private javax.swing.JRadioButton jRadioButtonEditRemove;
    private javax.swing.JRadioButton jRadioButtonNode;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextAreaResultingTours;
    private javax.swing.JTextField jTextFieldAddConnectionStorey1;
    private javax.swing.JTextField jTextFieldAddConnectionStorey2;
    private javax.swing.JTextField jTextFieldAddConnectionWeight;
    private javax.swing.JTextField jTextFieldAddConnectionX1Pos;
    private javax.swing.JTextField jTextFieldAddConnectionX2Pos;
    private javax.swing.JTextField jTextFieldAddConnectionY1Pos;
    private javax.swing.JTextField jTextFieldAddConnectionY2Pos;
    private javax.swing.JTextField jTextFieldAddNodeStoreyNo;
    private javax.swing.JTextField jTextFieldAddNodeXPos;
    private javax.swing.JTextField jTextFieldAddNodeYPos;
    private javax.swing.JTextField jTextFieldBeta;
    private javax.swing.JTextField jTextFieldDropTime;
    private javax.swing.JTextField jTextFieldLambda;
    private javax.swing.JTextField jTextFieldMaxRouteTime;
    private javax.swing.JTextField jTextFieldNc;
    private javax.swing.JTextField jTextFieldNi;
    private javax.swing.JTextField jTextFieldNp;
    private javax.swing.JTextField jTextFieldPMax;
    private javax.swing.JTextField jTextFieldPMin;
    private javax.swing.JTextField jTextFieldVehicleCapacity;
    private javax.swing.JTextField jTextFieldaddNodeDemand;
    // End of variables declaration//GEN-END:variables
}
