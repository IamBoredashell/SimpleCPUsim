import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class CpuSimulatorUI extends Application {


    /* colors yay :) */
    private static final String BG_COLOR = "#0a0e17";
    private static final String PANEL_BG = "#111827";
    private static final String BORDER_COLOR = "#1f2937";
    private static final String TEXT_CYAN = "#22d3ee";
    private static final String TEXT_GREEN = "#4ade80";
    
    // Incase of stepbacks.	
    public static CPU injectedCpu = null;
    private CPU cpu;
    private Registers registers;
    private Memory memory;
    private ControlUnit cu;

    // for rollbacks
    private Stack<CPU> history = new Stack<>();
    private CPU initialState; 



   // Gui panels.
    private ListView<String> memoryView;
    private Label pcValueLbl, irValueLbl, stateValueLbl;
    private Label fetchPill, execPill, haltPill;
    private StackPane pcCard, irCard;
    private VBox rightRegistersBox;
    private Map<String, Label> activeRegisterLabels = new HashMap<>();
    private Timeline clock;
    // for changing type of numbers.
    private int displayMode = 0; 

    private Button playBtn, pauseBtn, hexBtn, decBtn, binBtn;

    @Override
    public void start(Stage primaryStage) {
        initBackend();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + "; -fx-font-family: 'Consolas', monospace;");

        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.CENTER);

        VBox leftPanel = createLeftPanel();
        VBox centerPanel = createCenterPanel();
        VBox rightPanel = createRightPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContent.getChildren().addAll(leftPanel, centerPanel, rightPanel);

        root.setCenter(mainContent);
        root.setBottom(createBottomBar());

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setTitle("Visual CPU Simulator");
        primaryStage.setScene(scene);
        
        updateUI(); 
        primaryStage.show();
    }

    private void initBackend() {
        if (injectedCpu != null) {
            this.cpu = injectedCpu;
            this.memory = injectedCpu.memory;
            this.registers = injectedCpu.reg;
            this.cu = injectedCpu.cu;
        } else {
            registers = new Registers();
            memory = new Memory();
            cu = new ControlUnit();
            cpu = new CPU(memory, registers, cu, Endianness.LITTLE);
            try {
                RegisterLoader.loadRegistersFromYaml("registers.yaml", registers);
                MicroCodeLoader.load("microcode.yaml", cu);
            } catch (Exception e) {
                System.err.println("Load Error: " + e.getMessage());
            }
        }
        initialState = cpu.copy();
    }

    /*
     *   ------------------
     *   MAIN DISPLAY PANELS
     *   ------------------
     */

    private VBox createLeftPanel() {
        VBox panel = createBasePanel("PROGRAM MEMORY");
        
        memoryView = new ListView<>();
        memoryView.setStyle(
            "-fx-control-inner-background: " + PANEL_BG + "; " +
            "-fx-background-color: " + PANEL_BG + "; " +
            "-fx-border-color: #374151; " +
            "-fx-border-radius: 5;"
        );
        VBox.setVgrow(memoryView, Priority.ALWAYS);
        
  
        memoryView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    if (item.startsWith("▶")) {
                        setText(item.substring(1)); 
                        // Q_Q
                        setStyle("-fx-background-color: #1e3a8a; -fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-border-color: #38bdf8; -fx-border-width: 0 0 0 4; -fx-padding: 4 5 4 10;");
                    } else {
                        setText(item);
                        setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-padding: 4 5 4 14;");
                    }
                }
            }
        });
        
        panel.getChildren().add(memoryView);
        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = createBasePanel("CENTRAL PROCESSING UNIT");
        panel.setAlignment(Pos.TOP_CENTER);

        HBox stateBox = new HBox(10);
        stateBox.setAlignment(Pos.CENTER);
        stateBox.setPadding(new Insets(0, 0, 40, 0));
        
        fetchPill = createStyledLabel("FETCH", "#374151", true);
        execPill = createStyledLabel("EXECUTE", "#374151", true);
        haltPill = createStyledLabel("HALT", "#374151", true);
        stateBox.getChildren().addAll(fetchPill, new Label("→"), execPill, new Label("→"), haltPill);

        GridPane cpuGrid = new GridPane();
        cpuGrid.setHgap(40);
        cpuGrid.setVgap(20);
        cpuGrid.setAlignment(Pos.CENTER);

        pcValueLbl = new Label("--");
        irValueLbl = new Label("--");
        
        pcCard = createCPUNode("PC", pcValueLbl);
        irCard = createCPUNode("IR", irValueLbl);
        VBox leftRegs = new VBox(10, pcCard, irCard);

        stateValueLbl = new Label("IDLE");
        VBox centerCore = new VBox(20, createCPUNode("CONTROL UNIT", stateValueLbl), createCPUNode("ALU", new Label("Idle")));

        cpuGrid.add(leftRegs, 0, 0);
        cpuGrid.add(centerCore, 1, 0);

        panel.getChildren().addAll(stateBox, cpuGrid);
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = createBasePanel("REGISTERS & MEMORY");
        Label regTitle = new Label("REGISTER STATE");
        regTitle.setTextFill(Color.web(TEXT_CYAN));
        rightRegistersBox = new VBox(5);
        panel.getChildren().addAll(regTitle, rightRegistersBox);
        return panel;
    }

    private HBox createBottomBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(15, 30, 15, 30));
        bar.setStyle("-fx-background-color: #050810; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 1 0 0 0;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Button resetBtn = createInteractiveButton("↺", "Reset System");
        Button stepBackBtn = createInteractiveButton("⏮", "Step Backward");
        playBtn = createInteractiveButton("▶", "Auto-Play");
        pauseBtn = createInteractiveButton("⏸", "Pause");
        Button stepFwdBtn = createInteractiveButton("⏭", "Step Forward");

        HBox speedControl = new HBox(10);
        speedControl.setAlignment(Pos.CENTER);
        Label speedLabel = new Label("Speed");
        speedLabel.setTextFill(Color.GRAY);
        
        Slider speedSlider = new Slider(10, 1000, 500);
        speedSlider.setCursor(Cursor.HAND);
        Label msLabel = new Label("500ms");
        msLabel.setTextFill(Color.web(TEXT_CYAN));
        msLabel.setPrefWidth(50); 

        speedControl.getChildren().addAll(speedLabel, speedSlider, msLabel);

        HBox baseSelector = new HBox(5);
        baseSelector.setAlignment(Pos.CENTER_RIGHT);
        
        hexBtn = createInteractiveButton("HEX", "Hexadecimal Base");
        decBtn = createInteractiveButton("DEC", "Decimal Base");
        binBtn = createInteractiveButton("BIN", "Binary Base");
        baseSelector.getChildren().addAll(hexBtn, decBtn, binBtn);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(resetBtn, stepBackBtn, playBtn, pauseBtn, stepFwdBtn, speedControl, spacer, baseSelector);
        
        setupExecutionLoop(speedSlider, msLabel);



	// BUTTON WORKING


        resetBtn.setOnAction(e -> {
            cpu.stop(); clock.pause();
            cpu = initialState.copy();
            history.clear();
            registers = cpu.reg; memory = cpu.memory; cu = cpu.cu;
            cpu.setOnStepComplete(() -> Platform.runLater(this::updateUI));
            updateUI();
        });

        stepBackBtn.setOnAction(e -> {
            cpu.stop(); clock.pause();
            if (!history.isEmpty()) {
                cpu = history.pop();
                registers = cpu.reg; memory = cpu.memory; cu = cpu.cu;
                cpu.setOnStepComplete(() -> Platform.runLater(this::updateUI));
                updateUI();
            }
        });

        stepFwdBtn.setOnAction(e -> { 
            if (cu.getState() == CUState.HALT && !history.isEmpty()) return;
            cpu.stop(); clock.pause(); cpu.start(); 
            history.push(cpu.copy());        
            try { cpu.step(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
            cpu.stop(); 
            updateUI();
        });

        playBtn.setOnAction(e -> { cpu.start(); clock.play(); updateUI(); });
        pauseBtn.setOnAction(e -> { cpu.stop(); clock.pause(); updateUI(); });
        
        hexBtn.setOnAction(e -> { displayMode = 0; updateUI(); });
        decBtn.setOnAction(e -> { displayMode = 1; updateUI(); });
        binBtn.setOnAction(e -> { displayMode = 2; updateUI(); });

        return bar;
    }

    /*
     *---------------------------------
     * EXECUTION LOOP AND VISUAL UPDATES
     *---------------------------------
     */
    private void setupExecutionLoop(Slider speedSlider, Label msLabel) {
        cpu.setOnStepComplete(() -> Platform.runLater(this::updateUI));

        clock = new Timeline(new KeyFrame(Duration.millis(speedSlider.getValue()), e -> {
            if (cpu.isRunning()) {
                history.push(cpu.copy());
                if (history.size() > 500) history.remove(0);
                try { cpu.step(); } 
                catch (Exception ex) { clock.pause(); System.err.println(ex.getMessage()); updateUI(); }
            }
        }));
        clock.setCycleCount(Timeline.INDEFINITE);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            msLabel.setText(String.format("%.0fms", newVal.doubleValue()));
            clock.stop();
            clock.getKeyFrames().setAll(new KeyFrame(Duration.millis(newVal.doubleValue()), e -> {
                if (cpu.isRunning()) {
                    history.push(cpu.copy());
                    if (history.size() > 500) history.remove(0);
                    try { cpu.step(); } 
                    catch (Exception ex) { clock.pause(); System.err.println(ex.getMessage()); updateUI(); }
                }
            }));
            if (cpu.isRunning()) clock.play();
        });
    }

    private void animateValueChange(Label lbl, String newValue, StackPane container) {
        if (!lbl.getText().equals(newValue) && !lbl.getText().equals("--")) {
            FadeTransition ft = new FadeTransition(Duration.millis(150), lbl);
            ft.setFromValue(0.1);
            ft.setToValue(1.0);
            ft.play();
            
            if (container != null) {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), container);
                st.setFromX(1.0); st.setFromY(1.0);
                st.setToX(1.05); st.setToY(1.05);
                st.setAutoReverse(true);
                st.setCycleCount(2);
                st.play();
            }
        }
        lbl.setText(newValue);
    }

    private void updateUI() {
        try {
            if (registers.getRegisterNames().contains("PC")) {
                animateValueChange(pcValueLbl, formatBuffer(registers.read("PC")), pcCard);
            }
            if (registers.getRegisterNames().contains("IR")) {
                animateValueChange(irValueLbl, formatBuffer(registers.read("IR")), irCard);
            }
        } catch (Exception e) {}

        for (String name : registers.getRegisterNames()) {
            if (name.equals("PC") || name.equals("IR")) continue;
            try {
                Buffer b = registers.read(name);
                String valStr = formatBuffer(b);
                
                if (!activeRegisterLabels.containsKey(name)) {
                    Label valLbl = new Label(valStr);
                    valLbl.setTextFill(Color.web(TEXT_GREEN));
                    activeRegisterLabels.put(name, valLbl);
                    rightRegistersBox.getChildren().add(createRegisterRow(name, valLbl));
                } else {
                    animateValueChange(activeRegisterLabels.get(name), valStr, null);
                }
            } catch (Exception e) {}
        }

        memoryView.getItems().clear();
        List<Map.Entry<Buffer, Buffer>> memList = new ArrayList<>(memory.getMemoryMap().entrySet());
        memList.sort((e1, e2) -> formatBuffer(e1.getKey()).compareTo(formatBuffer(e2.getKey())));

        String currentPcStr = pcValueLbl.getText();
        int activeIndex = -1;

        for (int i = 0; i < memList.size(); i++) {
            String addr = formatBuffer(memList.get(i).getKey());
            String data = formatBuffer(memList.get(i).getValue());
            
            if (addr.equals(currentPcStr)) {
                memoryView.getItems().add("▶" + addr + "   DATA: " + data);
                activeIndex = i;
            } else {
                memoryView.getItems().add(addr + "   DATA: " + data);
            }
        }

        if (activeIndex != -1) {
            memoryView.getSelectionModel().select(activeIndex);
            memoryView.scrollTo(activeIndex);
        }

        CUState state = cu.getState();
        stateValueLbl.setText(state.toString());
        
        fetchPill.setStyle("-fx-background-color: " + (state == CUState.FETCH ? "#16a34a" : "transparent") + "; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15; -fx-border-color: #374151; -fx-border-radius: 15;");
        fetchPill.setEffect(state == CUState.FETCH ? new DropShadow(15, Color.web("#16a34a")) : null);

        execPill.setStyle("-fx-background-color: " + (state == CUState.EXECUTE ? "#ea580c" : "transparent") + "; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15; -fx-border-color: #374151; -fx-border-radius: 15;");
        execPill.setEffect(state == CUState.EXECUTE ? new DropShadow(15, Color.web("#ea580c")) : null);

        haltPill.setStyle("-fx-background-color: " + (state == CUState.HALT ? "#dc2626" : "transparent") + "; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15; -fx-border-color: #374151; -fx-border-radius: 15;");
        haltPill.setEffect(state == CUState.HALT ? new DropShadow(15, Color.web("#dc2626")) : null);

        boolean isRun = cpu.isRunning();
        playBtn.setStyle("-fx-background-color: " + (isRun ? "#06b6d4" : "transparent") + "; -fx-text-fill: " + (isRun ? "black" : "#9ca3af") + "; -fx-border-color: " + (isRun ? "#06b6d4" : "#374151") + "; -fx-border-radius: 5;");
        pauseBtn.setStyle("-fx-background-color: " + (!isRun && state != CUState.HALT ? "#ea580c" : "transparent") + "; -fx-text-fill: " + (!isRun && state != CUState.HALT ? "white" : "#9ca3af") + "; -fx-border-color: " + (!isRun && state != CUState.HALT ? "#ea580c" : "#374151") + "; -fx-border-radius: 5;");
        
        hexBtn.setStyle("-fx-background-color: " + (displayMode == 0 ? "#1f2937" : "transparent") + "; -fx-text-fill: " + (displayMode == 0 ? TEXT_CYAN : "#9ca3af") + "; -fx-border-color: " + (displayMode == 0 ? TEXT_CYAN : "#374151") + "; -fx-border-radius: 5;");
        decBtn.setStyle("-fx-background-color: " + (displayMode == 1 ? "#1f2937" : "transparent") + "; -fx-text-fill: " + (displayMode == 1 ? TEXT_CYAN : "#9ca3af") + "; -fx-border-color: " + (displayMode == 1 ? TEXT_CYAN : "#374151") + "; -fx-border-radius: 5;");
        binBtn.setStyle("-fx-background-color: " + (displayMode == 2 ? "#1f2937" : "transparent") + "; -fx-text-fill: " + (displayMode == 2 ? TEXT_CYAN : "#9ca3af") + "; -fx-border-color: " + (displayMode == 2 ? TEXT_CYAN : "#374151") + "; -fx-border-radius: 5;");
    }

    private String formatBuffer(Buffer b) {
        if (b == null || b.getSize() == 0) return "[]";
        switch (displayMode) {
            case 0: return b.toHexString();
            case 1: 
                int val = 0;
                byte[] data = b.getData();
                for (int i=0; i<data.length; i++) val = (val << 8) | (data[i] & 0xFF);
                return String.valueOf(val);
            case 2: return b.toBinString();
            default: return b.toHexString();
        }
    }

   // FANCY BUTTON
    private Button createInteractiveButton(String text, String tooltipStr) {
        Button btn = new Button(text);
        btn.setCursor(Cursor.HAND);
        btn.setTooltip(new Tooltip(tooltipStr));
        
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5;");
        
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("black") && !btn.getStyle().contains("#ea580c")) {
                btn.setStyle("-fx-background-color: #1f2937; -fx-text-fill: white; -fx-border-color: " + TEXT_GREEN + "; -fx-border-radius: 5;");
            }
        });
        
        btn.setOnMouseExited(e -> updateUI()); 
        
        btn.setOnMousePressed(e -> {
            btn.setStyle("-fx-background-color: " + TEXT_GREEN + "; -fx-text-fill: black; -fx-border-color: " + TEXT_GREEN + "; -fx-border-radius: 5;");
            btn.setScaleX(0.95);
            btn.setScaleY(0.95);
        });

        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            updateUI(); 
        });
        
        return btn;
    }

    private VBox createBasePanel(String titleText) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: " + PANEL_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        ds.setOffsetY(3);
        ds.setColor(Color.color(0, 0, 0, 0.4));
        panel.setEffect(ds);

        Label title = new Label(titleText);
        title.setTextFill(Color.web(TEXT_CYAN));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        panel.getChildren().add(title);
        return panel;
    }

    private StackPane createCPUNode(String title, Label valLbl) {
        StackPane pane = new StackPane();
        pane.setStyle("-fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #0d131f;");
        pane.setPrefWidth(140);
        pane.setPrefHeight(60);

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(title);
        titleLbl.setTextFill(Color.GRAY);
        titleLbl.setStyle("-fx-font-size: 10px;");
        
        valLbl.setTextFill(Color.WHITE);
        valLbl.setStyle("-fx-font-size: 12px;");

        content.getChildren().addAll(titleLbl, valLbl);
        pane.getChildren().add(content);
        return pane;
    }

    private HBox createRegisterRow(String name, Label valLbl) {
        HBox row = new HBox();
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: #1f2937; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-color: #0d131f;");
        
        Label nameLbl = new Label(name);
        nameLbl.setTextFill(Color.web(TEXT_CYAN));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        row.getChildren().addAll(nameLbl, spacer, valLbl);
        return row;
    }

    private Label createStyledLabel(String text, String bgColor, boolean isPill) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-padding: 5 10 5 10; " + (isPill ? "-fx-background-radius: 15;" : ""));
        return lbl;
    }
}
