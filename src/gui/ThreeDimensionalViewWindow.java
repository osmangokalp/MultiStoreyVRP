/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import buildingmodel.Connection;
import buildingmodel.Node;
import buildingmodel.Storey;
import java.awt.Checkbox;
import java.awt.List;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 *
 * @author gokalp
 */
public class ThreeDimensionalViewWindow extends Frame {

    private SimpleUniverse universe;
    private final float storeyFSize = 10f; //20 unit in Java 3D
    private double storeyWidth = 400;
    private ArrayList<Storey> storeys = null;
    private ArrayList<Connection> connections = null;
    private ArrayList<ArrayList<Node>> solution = null;
    private ArrayList<BranchGroup> branchGroupsArrayOfRoutes = null;
    private BranchGroup routesBranchGroup = null;
    private ArrayList<BranchGroup> branchGroupsArrayOfTexts = null;
    private BranchGroup textsBranchGroup = null;
    private TransformGroup scenegraphTg = null;

    private List listRoutes = null;
    private float[][] colorList = {
        {1.000f, 0.00f, 0.000f}, //red
        {0.000f, 1.000f, 0.000f}, //green
        {0.000f, 0.000f, 1.000f}, //blue
        {1.000f, 1.000f, 0.000f},
        {0.000f, 1.000f, 1.000f},
        {1.000f, 0.000f, 1.000f}};

    public ThreeDimensionalViewWindow(ArrayList<Storey> storeys, double storeyWidth, ArrayList<Connection> connections, ArrayList<ArrayList<Node>> solution) {
        this.storeys = storeys;
        this.storeyWidth = storeyWidth;
        this.connections = connections;
        this.solution = solution;

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        GraphicsConfiguration config
                = SimpleUniverse.getPreferredConfiguration();

        Canvas3D canvas = new Canvas3D(config);
        setTitle("3D View");
        add("Center", canvas);
        add("South", new Label(""));

        universe = new SimpleUniverse(canvas);
        TransformGroup cameraTG = universe.getViewingPlatform().getViewPlatformTransform();
        Transform3D cameraTransform = new Transform3D();
        cameraTransform.setTranslation(new Vector3f(0.0f, 5.0f, 30.0f));
        cameraTG.setTransform(cameraTransform);

        Background background = new Background(new Color3f(0.95f, 0.95f, 0.95f));
        BranchGroup backgroundBg = new BranchGroup();
        BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
        background.setApplicationBounds(sphere);
        backgroundBg.addChild(background);
        universe.getViewingPlatform().addChild(backgroundBg);

        Panel westPanel = new Panel();
        westPanel.setLayout(new BorderLayout());

        Panel westNorthPanel = new Panel();
        westNorthPanel.setLayout(new BoxLayout(westNorthPanel, BoxLayout.Y_AXIS));
        westPanel.add(BorderLayout.NORTH, westNorthPanel);
        Panel westCenterPanel = new Panel();
        westPanel.add(BorderLayout.CENTER, westCenterPanel);
        Checkbox textsCheckBox = new Checkbox("Show Labels");
        textsCheckBox.setState(true);
        textsCheckBox.setFont(new Font("", Font.BOLD, 12));
        westNorthPanel.add(textsCheckBox);

        JLabel label = new JLabel();
        label.setText("Routes:");
        westNorthPanel.add(label);

        ScrollPane scrollPaneList = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollPaneList.setSize(100, 400);
        westCenterPanel.add(scrollPaneList);
        if (solution != null) {
            listRoutes = new List(solution.size(), true);
            listRoutes.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent ie) {
                    updateRouteDraws();
                }
            });

            for (int i = 0; i < solution.size(); i++) {
                listRoutes.add("Route " + i);
                listRoutes.select(i);
            }

            scrollPaneList.add(listRoutes);
        }

        add(BorderLayout.WEST, westPanel);

        BranchGroup contents = createSceneGraph();
        universe.addBranchGraph(contents);

        textsCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) { //checked
                    for (BranchGroup bg : branchGroupsArrayOfTexts) {
                        int indexOfChild = textsBranchGroup.indexOfChild(bg);
                        if (indexOfChild == -1) { //it should be drawn
                            textsBranchGroup.addChild(bg);
                        }
                    }
                } else {
                    for (BranchGroup bg : branchGroupsArrayOfTexts) {
                        int indexOfChild = textsBranchGroup.indexOfChild(bg);
                        if (indexOfChild != -1) { //it should be drawn
                            textsBranchGroup.removeChild(bg);
                        }
                    }
                }
            }
        });

        this.setSize(512, 512);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });
    }

    private void updateRouteDraws() {
        int[] selectedRoutes = listRoutes.getSelectedIndexes();

        ArrayList<Integer> unselectedRoutes = new ArrayList<>(solution.size());
        for (int i = 0; i < solution.size(); i++) {
            unselectedRoutes.add(i);
        }
        for (int i : selectedRoutes) {
            unselectedRoutes.remove(new Integer(i));
        }

        //consider selected routes
        for (int selectedRouteNo : selectedRoutes) {
            BranchGroup bg = branchGroupsArrayOfRoutes.get(selectedRouteNo);
            int indexOfChild = routesBranchGroup.indexOfChild(bg);
            if (indexOfChild == -1) { //it should be drawn
                routesBranchGroup.addChild(bg);
            }
        }

        //consider unselected routes
        for (int unSelectedRouteNo : unselectedRoutes) {
            BranchGroup routeBg = branchGroupsArrayOfRoutes.get(unSelectedRouteNo);
            int indexOfChild = routesBranchGroup.indexOfChild(routeBg);
            if (indexOfChild != -1) { //it should be removed
                routesBranchGroup.removeChild(routeBg);
            }
        }
    }

    private BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();

        scenegraphTg = new TransformGroup();
        scenegraphTg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        scenegraphTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(scenegraphTg);

        branchGroupsArrayOfTexts = new ArrayList<>();
        createAndAddTextsBranchGroup(scenegraphTg);

        for (int i = 0; i < storeys.size(); i++) {
            TransformGroup storeyTg = createStorey(i);
            scenegraphTg.addChild(storeyTg);
        }

        putConnections(scenegraphTg);

        for (BranchGroup b : branchGroupsArrayOfTexts) {
            textsBranchGroup.addChild(b);
        }

        if (solution != null) {
            routesBranchGroup = new BranchGroup();
            routesBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            routesBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            branchGroupsArrayOfRoutes = createBranchGroupsOfAllRoutes();

            for (BranchGroup routeBg : branchGroupsArrayOfRoutes) {
                routesBranchGroup.addChild(routeBg);
            }
            scenegraphTg.addChild(routesBranchGroup);
        }

        BoundingSphere bounds
                = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        MouseRotate mRotate = new MouseRotate();
        mRotate.setTransformGroup(scenegraphTg);
        scenegraphTg.addChild(mRotate);
        mRotate.setSchedulingBounds(bounds);

        MouseTranslate mTranslate = new MouseTranslate();
        mTranslate.setTransformGroup(scenegraphTg);
        scenegraphTg.addChild(mTranslate);
        mTranslate.setSchedulingBounds(bounds);

        MouseZoom mZoom = new MouseZoom();
        mZoom.setTransformGroup(scenegraphTg);
        scenegraphTg.addChild(mZoom);
        mZoom.setSchedulingBounds(bounds);

//        // Create a red light that shines for 100m from the origin
//   Color3f light1Color = new Color3f(1.8f, 0.1f, 0.1f);
//   Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
//   DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
//   light1.setInfluencingBounds(bounds);
//   objRoot.addChild(light1);
        objRoot.compile();
        return objRoot;
    }

    private TransformGroup createStorey(int storeyNo) {
        TransformGroup storeyTg = new TransformGroup();

        //create storey surface
        Appearance storeySurfaceAppearance = new Appearance();
        Color3f col = new Color3f(0.0f, 0.0f, 0.0f);
        ColoringAttributes ca = new ColoringAttributes(col, ColoringAttributes.NICEST);
        storeySurfaceAppearance.setColoringAttributes(ca);

        TransparencyAttributes t_attr
                = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.7f);
        storeySurfaceAppearance.setTransparencyAttributes(t_attr);

//        TextureLoader storeySurfaceTexture = new TextureLoader("gfx/storeySurfaceTexture.jpg", "RGB", this);
//        storeySurfaceAppearance.setTexture(storeySurfaceTexture.getTexture());
//        TextureAttributes storeySurfaceTextureAttributes = new TextureAttributes();
//        storeySurfaceTextureAttributes.setTextureMode(TextureAttributes.MODULATE);
//        storeySurfaceAppearance.setTextureAttributes(storeySurfaceTextureAttributes);
        Box storeySurface = new Box(storeyFSize / 2, storeyFSize / 100, storeyFSize / 2, Box.GENERATE_TEXTURE_COORDS | Box.GENERATE_NORMALS, storeySurfaceAppearance);

        Transform3D storeyTgTransform = new Transform3D();
        storeyTgTransform.setTranslation(new Vector3f(0f, (float) storeyNo * 5f, 0f));
        storeyTg.setTransform(storeyTgTransform);
        storeyTg.addChild(storeySurface);

        putNodes(storeySurface, storeyNo);

        return storeyTg;

    }

    private void putNodes(Box storeySurface, int storeyNo) {
        BranchGroup tbg = new BranchGroup();
        tbg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        tbg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        tbg.setCapability(BranchGroup.ALLOW_DETACH);

        Appearance blueAppearence = new Appearance();
        Color3f col = new Color3f(0.0f, 0.0f, 1.0f);
        ColoringAttributes blueCa = new ColoringAttributes(col, ColoringAttributes.NICEST);
        blueAppearence.setColoringAttributes(blueCa);

        Appearance greenAppearence = new Appearance();
        col = new Color3f(0.0f, 1.0f, 0.0f);
        ColoringAttributes greenCa = new ColoringAttributes(col, ColoringAttributes.NICEST);
        greenAppearence.setColoringAttributes(greenCa);
//        TextureLoader nodeTexture = new TextureLoader("gfx/nodeTexture.png", "RGB", this);
//        nodeAppearance.setTexture(nodeTexture.getTexture());
//        TextureAttributes nodeTextureAttributes = new TextureAttributes();
//        nodeTextureAttributes.setTextureMode(TextureAttributes.MODULATE);
//        nodeAppearance.setTextureAttributes(nodeTextureAttributes);

        ArrayList<Node> nodes = storeys.get(storeyNo).getNodes();
        for (Node node : nodes) {
            Cylinder node3D;
            if (node.isDepot()) {
                node3D = new Cylinder(storeyFSize / 100, storeyFSize / 80, Cylinder.GENERATE_TEXTURE_COORDS | Cylinder.GENERATE_NORMALS, greenAppearence);
            } else {
                node3D = new Cylinder(storeyFSize / 100, storeyFSize / 80, Cylinder.GENERATE_TEXTURE_COORDS | Cylinder.GENERATE_NORMALS, blueAppearence);
            }
            float node1XNew = convertPositionTo3DCoordinates(node.getEuclideanCoordinate().getX());
            float node1YNew = convertPositionTo3DCoordinates(node.getEuclideanCoordinate().getY());
            Transform3D nodeTransform = new Transform3D();
            nodeTransform.setTranslation(new Vector3f(node1XNew, storeyFSize / 80, node1YNew));
            TransformGroup nodeTg = new TransformGroup();
            nodeTg.setTransform(nodeTransform);
            nodeTg.addChild(node3D);

//            //put ID text
//            Appearance app = new Appearance();
//            ColoringAttributes ca = new ColoringAttributes(new Color3f(1.0f, 0.0f, 0.0f), ColoringAttributes.SHADE_FLAT);
//            app.setColoringAttributes(ca);
//            
//            //put ID1 text
//            Sphere sphere = new Sphere(0.05f);
//            sphere.setAppearance(app);
//            TransformGroup sphereTg = new TransformGroup();
//            sphereTg.addChild(sphere);
//            Transform3D sphereTransform = new Transform3D();
//            sphereTransform.setTranslation(new Vector3f(node1XNew, (float) storeyNo * 5f + storeyFSize / 50, node1YNew + 0.2f));
//            sphereTg.setTransform(sphereTransform);
//            textsBranchGroup.addChild(sphereTg);
//            
//            String ID = String.valueOf(node.getID());
//            Font3D font = new Font3D(new Font("nodeFont", Font.PLAIN, 1), new FontExtrusion());
//            Text3D text = new Text3D(font, ID, new Point3f(0.0f, storeyFSize / 80, 0.0f));
//            Shape3D textShape = new Shape3D(text);
//            textShape.setAppearance(app);
//            TransformGroup textTg = new TransformGroup();
//            Transform3D textScale = new Transform3D();
//            textScale.setScale(0.4);
//            textTg.setTransform(textScale);
//            textTg.addChild(textShape);
//            sphereTg.addChild(textTg);
            //put ID text
            Appearance app = new Appearance();
            ColoringAttributes ca = new ColoringAttributes(new Color3f(1.0f, 0.0f, 0.0f), ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);

            //put ID1 text
            String ID = String.valueOf(node.getID());
            Font3D font = new Font3D(new Font("nodeFont", Font.PLAIN, 1), new FontExtrusion());
            Text3D text = new Text3D(font, ID, new Point3f(0.0f, storeyFSize / 80, 0.0f));
            Shape3D textShape = new Shape3D(text);
            textShape.setAppearance(app);
            TransformGroup textTg = new TransformGroup();
            textTg.addChild(textShape);
            Transform3D translateTransform = new Transform3D();
            translateTransform.setTranslation(new Vector3f(node1XNew, (float) storeyNo * 5f + storeyFSize / 50, node1YNew + 0.2f));
            Transform3D scaleTransform = new Transform3D();
            scaleTransform.setScale(0.3);
            translateTransform.mul(scaleTransform);
            textTg.setTransform(translateTransform);
            tbg.addChild(textTg);

            storeySurface.addChild(nodeTg);
        }

        branchGroupsArrayOfTexts.add(tbg);

    }

    private float convertPositionTo3DCoordinates(double pos) {
        float pos3D;

        pos3D = ((float) pos - ((float) storeyWidth / 2f)) * (storeyFSize / (float) storeyWidth);

        return pos3D;
    }

    private void putConnections(TransformGroup tg) {
        BranchGroup tbg = new BranchGroup();
        tbg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        tbg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        tbg.setCapability(BranchGroup.ALLOW_DETACH);

        for (Connection connection : connections) {
            Appearance app = new Appearance();
            ColoringAttributes ca = new ColoringAttributes(new Color3f(0.0f, 0.0f, 0.0f), ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);
            LineAttributes la = new LineAttributes(7, LineAttributes.PATTERN_SOLID, true);
            app.setLineAttributes(la);
            TransparencyAttributes t_attr
                    = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.7f);
            app.setTransparencyAttributes(t_attr);

            Node node1 = connection.getNode1();
            Node node2 = connection.getNode2();

            float node1XNew = convertPositionTo3DCoordinates(node1.getEuclideanCoordinate().getX());
            float node1YNew = convertPositionTo3DCoordinates(node1.getEuclideanCoordinate().getY());
            float node2XNew = convertPositionTo3DCoordinates(node2.getEuclideanCoordinate().getX());
            float node2YNew = convertPositionTo3DCoordinates(node2.getEuclideanCoordinate().getY());
            int storey1No = node1.getStorey().getNo();
            int storey2No = node2.getStorey().getNo();

            LineArray line = new LineArray(2, LineArray.COORDINATES);
            line.setCoordinate(0, new Point3f(node1XNew, (float) storey1No * 5f, node1YNew));
            line.setCoordinate(1, new Point3f(node2XNew, (float) storey2No * 5f, node2YNew));
            Shape3D plShape = new Shape3D(line, app);
            tg.addChild(plShape);

            app = new Appearance();
            ca = new ColoringAttributes(new Color3f(0.0f, 0.0f, 0.0f), ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);

            //put ID1 text
            String ID = String.valueOf(node1.getID());
            Font font = new Font("nodeFont", Font.PLAIN, 1);
            Font3D font3D = new Font3D(font, new FontExtrusion());
            Text3D text3D = new Text3D(font3D, ID);
            Shape3D textShape = new Shape3D(text3D);
            textShape.setAppearance(app);
            TransformGroup textTg = new TransformGroup();
            textTg.addChild(textShape);
            Transform3D translateTransform = new Transform3D();
            translateTransform.setTranslation(new Vector3f(node1XNew, (float) storey1No * 5f + storeyFSize / 50, node1YNew + 0.2f));
            Transform3D scaleTransform = new Transform3D();
            scaleTransform.setScale(0.3);
            translateTransform.mul(scaleTransform);
            textTg.setTransform(translateTransform);
            tbg.addChild(textTg);

            //put ID2 text
            ID = String.valueOf(node2.getID());
            font3D = new Font3D(font, new FontExtrusion());
            text3D = new Text3D(font3D, ID);
            textShape = new Shape3D(text3D);
            textShape.setAppearance(app);
            textTg = new TransformGroup();
            textTg.addChild(textShape);
            translateTransform = new Transform3D();
            translateTransform.setTranslation(new Vector3f(node2XNew, (float) storey2No * 5f + storeyFSize / 50, node2YNew + 0.2f));
            scaleTransform = new Transform3D();
            scaleTransform.setScale(0.3);
            translateTransform.mul(scaleTransform);
            textTg.setTransform(translateTransform);
            tbg.addChild(textTg);

        }

        branchGroupsArrayOfTexts.add(tbg);
    }

    private ArrayList<BranchGroup> createBranchGroupsOfAllRoutes() {
        ArrayList<BranchGroup> bgs = new ArrayList<>(solution.size());

        Appearance app;
        ColoringAttributes ca;
        LineAttributes la = new LineAttributes(2, LineAttributes.PATTERN_SOLID, true);

        int index = 0;
        for (ArrayList<Node> route : solution) {

            BranchGroup bg = new BranchGroup();
            bg.setCapability(BranchGroup.ALLOW_DETACH);

            app = new Appearance();
            if (index == colorList.length) {
                index = 0;
            }
            ca = new ColoringAttributes(new Color3f(colorList[index][0], colorList[index][1], colorList[index][2]), ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);
            app.setLineAttributes(la);

            float node1XNew, node1YNew, node2XNew, node2YNew;
            int storeyNo1, storeyNo2;
            for (int i = 0; i < route.size() - 1; i++) {
                Node node1 = route.get(i);
                Node node2 = route.get(i + 1);
                Point3f[] plaPts = new Point3f[2];
                node1XNew = convertPositionTo3DCoordinates(node1.getEuclideanCoordinate().getX());
                node1YNew = convertPositionTo3DCoordinates(node1.getEuclideanCoordinate().getY());
                node2XNew = convertPositionTo3DCoordinates(node2.getEuclideanCoordinate().getX());
                node2YNew = convertPositionTo3DCoordinates(node2.getEuclideanCoordinate().getY());
                storeyNo1 = node1.getStorey().getNo();
                storeyNo2 = node2.getStorey().getNo();
                plaPts[0] = new Point3f(node1XNew, (float) storeyNo1 * 5f + 0.2f, node1YNew);
                plaPts[1] = new Point3f(node2XNew, (float) storeyNo2 * 5f + 0.2f, node2YNew);

                LineArray pla = new LineArray(2, LineArray.COORDINATES);
                pla.setCoordinates(0, plaPts);

                Shape3D plShape = new Shape3D(pla, app);
                bg.addChild(plShape);
            }
            bgs.add(bg);
            index++;
        }
        return bgs;
    }

    private void createAndAddTextsBranchGroup(TransformGroup scenegraphTg) {
        textsBranchGroup = new BranchGroup();
        textsBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        textsBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        scenegraphTg.addChild(textsBranchGroup);
    }

}
