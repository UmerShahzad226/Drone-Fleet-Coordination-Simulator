package form_GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import Backend.*;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class GUI_Project {

    protected Shell shell;
    private Simulator simulator;
    private Thread simThread;
    private volatile boolean running = false;
    private int stepCount = 0;
    private int simDelay = 50; // Dynamic simulation speed

    // --- Camera State ---
    private double camZoom = 1.0;
    private double camYaw = Math.PI / 4; // 45 degrees
    private double camPitch = Math.PI / 6; // 30 degrees
    private int camScrollX = 0;
    private int camScrollY = 0;
    private int lastMouseX, lastMouseY;
    private boolean isDragging = false;

    private Canvas canvas;
    private Text txtDroneCount, txtX, txtY, txtZ;
    private Text txtSimSpeed, txtCollisionDist; // Advanced inputs
    private Combo comboSelectDrone;
    private Label statusLabel, timeLabel;
    private Image droneImage;
    private LocalResourceManager localResourceManager;
    private Label lblCollisions;
    private Logger logger;

    public static void main(String[] args) {
        try {
            GUI_Project window = new GUI_Project();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        Display display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (droneImage != null)
            droneImage.dispose();
    }

    protected void createContents() {
        shell = new Shell();
        createResourceManager();
        shell.addDisposeListener(e -> {
            if (logger != null)
                logger.close();
        });
        shell.setText("Autonomous Drone Fleet Simulator By UMER-MUZAMMIL-ANEEQ");
        shell.setSize(1300, 800);
        // Use FormLayout for the main shell to split Canvas (Left) and Controls (Right)
        shell.setLayout(new FormLayout());

        // Load drone image
        droneImage = new Image(shell.getDisplay(), "C:\\Users\\SC\\Desktop\\OOP\\JAVA_Project\\src\\Images\\DRONE.png");

        // --- Right Panel (Dashboard) ---
        Composite rightPanel = new Composite(shell, SWT.NONE);
        // Dark Charcoal Background
        Color darkBg = new Color(shell.getDisplay(), 45, 45, 48);
        Color lightText = new Color(shell.getDisplay(), 220, 220, 220);
        rightPanel.setBackground(darkBg);
        // Layout Data for Right Panel
        FormData fd_rightPanel = new FormData();
        fd_rightPanel.top = new FormAttachment(0);
        fd_rightPanel.bottom = new FormAttachment(100);
        fd_rightPanel.right = new FormAttachment(100);
        fd_rightPanel.width = 280; // Fixed width for sidebar
        rightPanel.setLayoutData(fd_rightPanel);

        // Use GridLayout for the panel components
        GridLayout gl_rightPanel = new GridLayout(1, false);
        gl_rightPanel.marginWidth = 10;
        gl_rightPanel.marginHeight = 10;
        gl_rightPanel.verticalSpacing = 15;
        rightPanel.setLayout(gl_rightPanel);

        // --- 0. Camera View (TOP) ---
        Group grpCamera = new Group(rightPanel, SWT.NONE);
        grpCamera.setText("Camera View");
        grpCamera.setForeground(lightText);
        grpCamera.setFont(localResourceManager.create(FontDescriptor.createFrom("Book Antiqua", 10, SWT.BOLD)));
        grpCamera.setBackground(darkBg);
        grpCamera.setLayout(new GridLayout(4, true)); // 4 columns for buttons
        grpCamera.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Rotations
        createCamBtn(grpCamera, "\u27F2", () -> updateCamera(0, -0.1, 0)); // Yaw Right
        createCamBtn(grpCamera, "\u25B2", () -> updateCamera(0, 0, -0.05)); // Pitch Up
        createCamBtn(grpCamera, "\u25BC", () -> updateCamera(0, 0, 0.05)); // Pitch Down
        createCamBtn(grpCamera, "\u27F3", () -> updateCamera(0, 0.1, 0)); // Yaw Left

        // Zoom
        createCamBtn(grpCamera, "-", () -> updateCamera(-0.1, 0, 0)); // Zoom Out
        createCamBtn(grpCamera, "\u2302", () -> resetCamera()); // Reset
        createCamBtn(grpCamera, "+", () -> updateCamera(0.1, 0, 0)); // Zoom In
        // Spacer for 4th column in 2nd row
        new Label(grpCamera, SWT.NONE).setBackground(darkBg);

        // --- 1. Status Group ---
        Group grpStatus = new Group(rightPanel, SWT.NONE);
        grpStatus.setText("Simulation Status");
        grpStatus.setForeground(lightText);
        grpStatus.setFont(localResourceManager.create(FontDescriptor.createFrom("Book Antiqua", 10, SWT.BOLD)));
        grpStatus.setBackground(darkBg);
        grpStatus.setLayout(new GridLayout(1, false));
        grpStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        statusLabel = new Label(grpStatus, SWT.NONE);
        statusLabel.setText("STATUS: READY");
        statusLabel.setForeground(lightText);
        statusLabel.setFont(localResourceManager.create(FontDescriptor.createFrom("Times New Roman", 12, SWT.BOLD)));
        statusLabel.setBackground(rightPanel.getBackground());
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        timeLabel = new Label(grpStatus, SWT.NONE);
        timeLabel.setText("TIME: 0.0s");
        timeLabel.setForeground(lightText);
        timeLabel.setFont(localResourceManager.create(FontDescriptor.createFrom("Times New Roman", 12, SWT.NORMAL)));
        timeLabel.setBackground(rightPanel.getBackground());
        timeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        lblCollisions = new Label(grpStatus, SWT.NONE);
        lblCollisions.setText("COLLISION: 0");
        lblCollisions.setForeground(lightText);
        lblCollisions
                .setFont(localResourceManager.create(FontDescriptor.createFrom("Times New Roman", 12, SWT.NORMAL)));
        lblCollisions.setBackground(rightPanel.getBackground());
        lblCollisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // --- 2. Configuration Group ---
        Group grpConfig = new Group(rightPanel, SWT.NONE);
        grpConfig.setText("Fleet Configuration");
        grpConfig.setForeground(lightText);
        grpConfig.setFont(localResourceManager.create(FontDescriptor.createFrom("Book Antiqua", 10, SWT.BOLD)));
        grpConfig.setBackground(darkBg);
        grpConfig.setLayout(new GridLayout(2, false));
        grpConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label lblDroneCount = new Label(grpConfig, SWT.NONE);
        lblDroneCount.setText("Drone Count:");
        lblDroneCount.setForeground(lightText);
        lblDroneCount.setBackground(rightPanel.getBackground());

        txtDroneCount = new Text(grpConfig, SWT.BORDER);
        txtDroneCount.setText("5");
        txtDroneCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnRestart = new Button(grpConfig, SWT.PUSH);
        btnRestart.setText("Initialize / Restart");
        btnRestart.setFont(localResourceManager.create(FontDescriptor.createFrom("Times New Roman", 10, SWT.BOLD)));
        GridData gd_btnRestart = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_btnRestart.heightHint = 30;
        btnRestart.setLayoutData(gd_btnRestart);
        // Events
        btnRestart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                restartSimulation();
            }
        });

        // --- 3. Individual Control Group ---
        Group grpControl = new Group(rightPanel, SWT.NONE);
        grpControl.setText("Manual Control");
        grpControl.setForeground(lightText);
        grpControl.setFont(localResourceManager.create(FontDescriptor.createFrom("Book Antiqua", 10, SWT.BOLD)));
        grpControl.setBackground(darkBg);
        grpControl.setLayout(new GridLayout(2, false));
        grpControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label lblSelect = new Label(grpControl, SWT.NONE);
        lblSelect.setText("Select:");
        lblSelect.setForeground(lightText);
        lblSelect.setBackground(rightPanel.getBackground());

        comboSelectDrone = new Combo(grpControl, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboSelectDrone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label lblX = new Label(grpControl, SWT.NONE);
        lblX.setText("X:");
        lblX.setForeground(lightText);
        lblX.setBackground(rightPanel.getBackground());
        txtX = new Text(grpControl, SWT.BORDER);
        txtX.setText("400");
        txtX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label lblY = new Label(grpControl, SWT.NONE);
        lblY.setText("Y:");
        lblY.setForeground(lightText);
        lblY.setBackground(rightPanel.getBackground());
        txtY = new Text(grpControl, SWT.BORDER);
        txtY.setText("350");
        txtY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false)); // Updated default to be safe

        Label lblZ = new Label(grpControl, SWT.NONE);
        lblZ.setText("Z:");
        lblZ.setForeground(lightText);
        lblZ.setBackground(rightPanel.getBackground());
        txtZ = new Text(grpControl, SWT.BORDER);
        txtZ.setText("200");
        txtZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnSetTarget = new Button(grpControl, SWT.PUSH);
        btnSetTarget.setText("Set Target");
        GridData gd_btnSet = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_btnSet.heightHint = 30;
        btnSetTarget.setLayoutData(gd_btnSet);
        btnSetTarget.addListener(SWT.Selection, e -> setDroneTarget());

        // --- 4. Advanced Settings Group (NEW) ---
        Group grpAdvanced = new Group(rightPanel, SWT.NONE);
        grpAdvanced.setText("Advanced Settings");
        grpAdvanced.setForeground(lightText);
        grpAdvanced.setFont(localResourceManager.create(FontDescriptor.createFrom("Book Antiqua", 10, SWT.BOLD)));
        grpAdvanced.setBackground(darkBg);
        grpAdvanced.setLayout(new GridLayout(2, false));
        grpAdvanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label lblSpeed = new Label(grpAdvanced, SWT.NONE);
        lblSpeed.setText("Sim Speed (ms):");
        lblSpeed.setForeground(lightText);
        lblSpeed.setBackground(darkBg);
        txtSimSpeed = new Text(grpAdvanced, SWT.BORDER);
        txtSimSpeed.setText("50");
        txtSimSpeed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label lblColDist = new Label(grpAdvanced, SWT.NONE);
        lblColDist.setText("Collision Dist:");
        lblColDist.setForeground(lightText);
        lblColDist.setBackground(darkBg);
        txtCollisionDist = new Text(grpAdvanced, SWT.BORDER);
        txtCollisionDist.setText("30");
        txtCollisionDist.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnApply = new Button(grpAdvanced, SWT.PUSH);
        btnApply.setText("Apply Settings");
        btnApply.setFont(localResourceManager.create(FontDescriptor.createFrom("Times New Roman", 10, SWT.BOLD)));
        GridData gd_btnApply = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_btnApply.heightHint = 25;
        btnApply.setLayoutData(gd_btnApply);
        btnApply.addListener(SWT.Selection, e -> applyAdvancedSettings());

        // --- 5. Simulation Control ---
        Group grpActions = new Group(rightPanel, SWT.NONE);
        grpActions.setText("Global Actions");
        grpActions.setForeground(lightText);
        grpActions.setFont(localResourceManager.create(FontDescriptor.createFrom("Book Antiqua", 10, SWT.BOLD)));
        grpActions.setBackground(darkBg);
        grpActions.setLayout(new GridLayout(2, true)); // Equal width columns
        grpActions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnStart = new Button(grpActions, SWT.PUSH);
        btnStart.setText("START");
        btnStart.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_GREEN));
        GridData gd_btnStart = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_btnStart.heightHint = 40;
        btnStart.setLayoutData(gd_btnStart);
        btnStart.addListener(SWT.Selection, e -> startSimulation());

        Button btnStop = new Button(grpActions, SWT.PUSH);
        btnStop.setText("STOP");
        btnStop.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
        GridData gd_btnStop = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_btnStop.heightHint = 40;
        btnStop.setLayoutData(gd_btnStop);
        btnStop.addListener(SWT.Selection, e -> stopSimulation());

        // --- Left Canvas ---
        canvas = new Canvas(shell, SWT.BORDER | SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
        FormData fd_canvas = new FormData();
        fd_canvas.top = new FormAttachment(0);
        fd_canvas.bottom = new FormAttachment(100);
        fd_canvas.left = new FormAttachment(0);
        fd_canvas.right = new FormAttachment(rightPanel); // Attach to left side of right panel
        canvas.setLayoutData(fd_canvas);

        canvas.addPaintListener(e -> drawGridAndDrones(e.gc));

        // Mouse Drag Logic
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (e.button == 1) { // Left click
                    isDragging = true;
                    lastMouseX = e.x;
                    lastMouseY = e.y;
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (e.button == 1) {
                    isDragging = false;
                }
            }
        });

        canvas.addMouseMoveListener(e -> {
            if (isDragging) {
                int dx = e.x - lastMouseX;
                int dy = e.y - lastMouseY;

                // Orbit Logic (Rotate View)
                // dx controls Yaw (horizontal rotation)
                // dy controls Pitch (vertical rotation)
                double sensitivity = 0.01;
                updateCamera(0, dx * sensitivity, dy * sensitivity);

                lastMouseX = e.x;
                lastMouseY = e.y;
                canvas.redraw();
            }
        });

        initializeSimulation(5, false); // Start with file check allowed
    }

    private void createResourceManager() {
        localResourceManager = new LocalResourceManager(JFaceResources.getResources(), shell);
    }

    // Helper to create camera buttons
    private void createCamBtn(Composite parent, String text, Runnable action) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(text);
        b.setFont(localResourceManager.create(FontDescriptor.createFrom("Segoe UI Symbol", 10, SWT.BOLD)));
        b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        b.addListener(SWT.Selection, e -> {
            action.run();
            canvas.redraw();
        });
    }

    private void updateCamera(double dZoom, double dYaw, double dPitch) {
        camZoom += dZoom;
        if (camZoom < 0.1)
            camZoom = 0.1;
        if (camZoom > 5.0)
            camZoom = 5.0;

        camYaw += dYaw;
        camPitch += dPitch;
        // Clamp Pitch to avoid flipping upside down too much
        if (camPitch < 0)
            camPitch = 0;
        if (camPitch > Math.PI / 2)
            camPitch = Math.PI / 2;
    }

    private void resetCamera() {
        camZoom = 1.0;
        camYaw = Math.PI / 4;
        camPitch = Math.PI / 6;
        canvas.redraw();
    }

    private Point projectIso(double x, double y, double z) {
        // Full Orbit Camera Projection

        // 1. Center the world
        double wx = x - 500;
        double wy = y - 500;
        double wz = z; // Ground is Z=0

        // 2. Yaw Rotation (Around Z-axis)
        double cosY = Math.cos(camYaw);
        double sinY = Math.sin(camYaw);
        double x1 = wx * cosY - wy * sinY;
        double y1 = wx * sinY + wy * cosY;

        // 3. Pitch Rotation (Screen X-axis tilt)
        double cosP = Math.cos(camPitch);
        double sinP = Math.sin(camPitch);

        // Flattened 2D coords
        double projX = x1;
        double projY = y1 * sinP - wz * cosP; // Y is depth scaled by pitch, Z is height

        // 4. Zoom & Offset
        int screenX = (int) (projX * camZoom + 500 + 150 - camScrollX); // Center Screen X (approx 650)
        int screenY = (int) (projY * camZoom + 350 + 100 - camScrollY); // Center Screen Y (approx 450)

        return new Point(screenX, screenY);
    }

    private void drawGridAndDrones(GC g) {
        // Clear background manually because SWT.NO_BACKGROUND is set
        g.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        g.fillRectangle(canvas.getClientArea());

        g.setAdvanced(true);
        g.setAntialias(SWT.ON);
        g.setInterpolation(SWT.HIGH); // High quality scaling

        // 1. Draw 3D Grid (Floor at Z=0)
        g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_GRAY));

        // Draw constant X lines (traverse Y)
        for (int x = 0; x <= 1000; x += 100) {
            Point p1 = projectIso(x, 0, 0);
            Point p2 = projectIso(x, 1000, 0);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw constant Y lines (traverse X)
        for (int y = 0; y <= 1000; y += 100) {
            Point p1 = projectIso(0, y, 0);
            Point p2 = projectIso(1000, y, 0);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        if (simulator == null)
            return;

        var positions = simulator.getDronePositions();
        var drones = simulator.getDrones();

        // 1.5 Draw Axes (Rh-system: X=Red, Y=Green, Z=Blue)
        g.setLineWidth(3);
        Point pOrigin = projectIso(0, 0, 0);
        Point pX = projectIso(200, 0, 0);
        Point pY = projectIso(0, 200, 0);
        Point pZ = projectIso(0, 0, 200);

        // X-Axis (Red)
        g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_RED));
        g.drawLine(pOrigin.x, pOrigin.y, pX.x, pX.y);
        g.drawString("X", pX.x, pX.y, true);

        // Y-Axis (Green)
        g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_GREEN));
        g.drawLine(pOrigin.x, pOrigin.y, pY.x, pY.y);
        g.drawString("Y", pY.x, pY.y, true);

        // Z-Axis (Blue)
        g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_BLUE));
        g.drawLine(pOrigin.x, pOrigin.y, pZ.x, pZ.y);
        g.drawString("Z", pZ.x, pZ.y, true);

        // 2. Draw Targets
        g.setLineWidth(2);
        g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_RED));
        for (Drone d : drones) {
            Vector3D target = d.getTarget();
            if (target != null) {
                Point p3d = projectIso(target.x, target.y, target.z);
                int size = 8;
                g.drawLine(p3d.x - size, p3d.y - size, p3d.x + size, p3d.y + size);
                g.drawLine(p3d.x - size, p3d.y + size, p3d.x + size, p3d.y - size);

                // Optional: Draw line to floor for target context
                g.setLineStyle(SWT.LINE_DOT);
                Point pFloor = projectIso(target.x, target.y, 0);
                g.drawLine(p3d.x, p3d.y, pFloor.x, pFloor.y);
                g.setLineStyle(SWT.LINE_SOLID);
            }
        }

        // 3. Draw Drones and Shadows
        for (int i = 0; i < positions.size(); i++) {
            Vector3D p = positions.get(i);

            Point p3d = projectIso(p.x, p.y, p.z);
            Point pFloor = projectIso(p.x, p.y, 0);

            // Draw Shadow
            int shadowSize = 30;
            g.setAlpha(100);
            g.setBackground(g.getDevice().getSystemColor(SWT.COLOR_BLACK));
            g.fillOval(pFloor.x - shadowSize / 2, pFloor.y - shadowSize / 4, shadowSize, shadowSize / 2); // Flattened
                                                                                                          // oval
            g.setAlpha(255);

            // Draw Connector
            g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
            g.setLineStyle(SWT.LINE_DOT);
            g.setLineWidth(1);
            g.drawLine(p3d.x, p3d.y, pFloor.x, pFloor.y);
            g.setLineStyle(SWT.LINE_SOLID);

            // Draw Drone
            int dW = 60;
            int dH = 60;
            g.drawImage(droneImage, 0, 0, droneImage.getBounds().width, droneImage.getBounds().height,
                    p3d.x - dW / 2, p3d.y - dH / 2, dW, dH);

            // Draw ID
            g.setForeground(g.getDevice().getSystemColor(SWT.COLOR_WHITE));
            g.drawString("D" + i, p3d.x - 10, p3d.y - 40, true);
        }
    }

    private void initializeSimulation(int droneCount, boolean forceManual) {
        Drone[] drones;
        String configPath = "initial_input.txt";
        java.io.File f = new java.io.File(configPath);

        // Use file ONLY if it exists AND we are not forcing manual override (Restart
        // button)
        if (!forceManual && f.exists() && f.isFile()) {
            drones = ConfigLoader.loadDrones(configPath);
            // Update the UI count to match the file
            final int count = drones.length;
            Display.getDefault().asyncExec(() -> {
                txtDroneCount.setText(String.valueOf(count));
                statusLabel.setText("STATUS: LOADED FROM FILE");
            });
        } else {
            drones = ConfigLoader.createDrones(droneCount);
            Display.getDefault().asyncExec(() -> {
                statusLabel.setText("STATUS: READY");
            });
        }

        Environment env = new Environment(1000, 1000, 600);
        FormationManager fm = new FormationManager();
        CollisionAvoidance ca = new CollisionAvoidance();
        CommunicationModule comm = new CommunicationModule();
        logger = new Logger("positions.csv", "metrics.txt");
        simulator = new Simulator(drones, env, fm, ca, comm, logger);
        stepCount = 0;
        timeLabel.setText("TIME: 0.0s");

        comboSelectDrone.removeAll();
        for (int i = 0; i < drones.length; i++) {
            comboSelectDrone.add("Drone " + i);
        }
        if (drones.length > 0)
            comboSelectDrone.select(0);

        canvas.redraw();
    }

    private void startSimulation() {
        if (running)
            return;
        running = true;
        statusLabel.setText("STATUS: RUNNING");

        simThread = new Thread(() -> {
            while (running) {
                simulator.step();

                // Check if all drones reached target
                boolean allReached = true;
                double threshold = 5.0; // Acceptable distance to target
                for (Drone d : simulator.getDrones()) {
                    if (d.getTarget() != null) {
                        double dist = d.getPosition().subtract(d.getTarget()).length();
                        if (dist > threshold) {
                            allReached = false;
                            break;
                        }
                    }
                }

                if (allReached && simulator.getDrones().length > 0) {
                    running = false;
                    Display.getDefault().asyncExec(() -> {
                        statusLabel.setText("STATUS: TARGET REACHED");
                        canvas.redraw(); // Final redraw
                    });
                } else {
                    Display.getDefault().asyncExec(() -> {
                        canvas.redraw();
                        stepCount++;
                        timeLabel.setText(String.format("TIME: %.1fs", stepCount * 0.1));
                        if (simulator != null) {
                            lblCollisions.setText("COLLISION: " + simulator.getCollisionCount());
                        }
                    });
                }

                try {
                    Thread.sleep(simDelay); // Use dynamic delay
                } catch (InterruptedException ignored) {
                }
            }
        });
        simThread.start();
    }

    private void stopSimulation() {
        running = false;
        if (simThread != null && simThread.isAlive()) {
            try {
                // Wait for the thread to actually finish to prevent multiple threads running
                simThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        statusLabel.setText("STATUS: PAUSED");
        if (logger != null && simulator != null) {
            logger.writeSummary(simulator.getDrones(), simulator.getMinCollisionDist());
        }
    }

    private void drawTexturedPlane(GC g, Image img, Point p0, Point px, Point py) {
        Transform t = new Transform(g.getDevice());
        Rectangle imgBounds = img.getBounds();
        float w = imgBounds.width;
        float h = imgBounds.height;

        // m11/m12 = Vector(px-p0) / w
        float m11 = (px.x - p0.x) / w;
        float m12 = (px.y - p0.y) / w;

        // m21/m22 = Vector(py-p0) / h
        float m21 = (py.x - p0.x) / h;
        float m22 = (py.y - p0.y) / h;

        float dx = p0.x;
        float dy = p0.y;

        t.setElements(m11, m12, m21, m22, dx, dy);
        g.setTransform(t);

        g.drawImage(img, 0, 0);

        t.dispose();
        g.setTransform(null);
    }

    private void restartSimulation() {
        stopSimulation();
        if (logger != null)
            logger.close();
        try {
            int count = Integer.parseInt(txtDroneCount.getText());
            if (count <= 0)
                count = 5;
            initializeSimulation(count, true); // Force manual override on restart
            statusLabel.setText("Status: READY");
        } catch (NumberFormatException e) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setMessage("Invalid drone count!");
            mb.open();
        }
    }

    private void setDroneTarget() {
        if (simulator == null)
            return;
        try {
            int x = Integer.parseInt(txtX.getText());
            int y = Integer.parseInt(txtY.getText());
            int z = Integer.parseInt(txtZ.getText());
            int index = comboSelectDrone.getSelectionIndex();
            if (index >= 0) {
                simulator.getDrones()[index].setTarget(new Vector3D(x, y, z));
            }
        } catch (NumberFormatException e) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setMessage("Invalid target coordinates!");
            mb.open();
        }
    }

    private void applyAdvancedSettings() {
        try {
            int speed = Integer.parseInt(txtSimSpeed.getText());
            double dist = Double.parseDouble(txtCollisionDist.getText());

            if (speed < 5)
                speed = 5; // Safety lower bound
            this.simDelay = speed;

            if (simulator != null) {
                simulator.setMinCollisionDist(dist);
            }

            statusLabel.setText("STATUS: SETTINGS APPLIED");
        } catch (NumberFormatException e) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setMessage("Invalid numbers for settings!");
            mb.open();
        }
    }
}
