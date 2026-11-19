package com.yisy.modules.doramcu.shipdesign;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A windowed Swing user interface for building FullThrust ships.
 * <p>
 * Provides visual controls for all ShipBuilder properties, including size
 * selection, numeric fields, weapon management, design point tracking,
 * template loading, and ship creation.
 * </p>
 */
public class ShipBuilderUI extends JFrame {

    // --- Input fields ---
    private JTextField nameField;
    private JComboBox<Ship.Size> sizeCombo;
    private JSpinner speedSpinner;
    private JSpinner armorSpinner;
    private JSpinner hitPointsSpinner;
    private JSpinner sensorRangeSpinner;
    private JSpinner crewCapacitySpinner;
    private JSpinner maxFuelSpinner;

    // --- Weapons table ---
    private DefaultTableModel weaponsTableModel;
    private JTable weaponsTable;

    // --- Design points ---
    private JLabel designPointsLabel;
    private JProgressBar designPointsBar;
    private JTextArea warningsArea;

    // --- Status ---
    private JLabel statusLabel;

    /**
     * Creates and shows the ShipBuilder UI window.
     */
    public ShipBuilderUI() {
        setTitle("FullThrust Ship Builder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        buildUI();
    }

    // --- UI Construction ---

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(20, 20, 30));

        // Top section: templates and actions
        mainPanel.add(buildTopBar(), BorderLayout.NORTH);

        // Center: two-column layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);

        // Left panel: ship properties
        splitPane.setLeftComponent(buildPropertiesPanel());

        // Right panel: weapons and design points
        splitPane.setRightComponent(buildRightPanel());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom: status bar
        mainPanel.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(30, 30, 45));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel templateLabel = new JLabel("Template:");
        templateLabel.setForeground(Color.WHITE);
        panel.add(templateLabel);

        JComboBox<String> templateCombo = new JComboBox<>();
        templateCombo.addItem("-- Custom --");
        templateCombo.addItem("Scout");
        templateCombo.addItem("Fighter");
        templateCombo.addItem("Cruiser");
        templateCombo.addItem("Battleship");
        templateCombo.addItem("Transport");
        templateCombo.addActionListener(e -> loadTemplate((String) templateCombo.getSelectedItem()));
        panel.add(templateCombo);

        panel.add(Box.createHorizontalStrut(20));

        JButton buildButton = new JButton("Build Ship");
        buildButton.addActionListener(e -> buildAndShowShip());
        panel.add(buildButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetForm());
        panel.add(resetButton);

        return panel;
    }

    private JPanel buildPropertiesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 38));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                "Ship Properties",
                0, 0,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(180, 200, 255)));

        // Name
        panel.add(buildFieldRow("Name:", createTextField()));
        panel.add(Box.createVerticalStrut(5));

        // Size
        panel.add(buildEnumRow("Size:", createSizeCombo()));
        panel.add(Box.createVerticalStrut(5));

        // Numeric fields
        panel.add(buildFieldRow("Speed:", createSpinner(0, 20, 6)));
        panel.add(Box.createVerticalStrut(3));
        panel.add(buildFieldRow("Armor:", createSpinner(0, 10, 2)));
        panel.add(Box.createVerticalStrut(3));
        panel.add(buildFieldRow("Hit Points:", createSpinner(1, 100, 6)));
        panel.add(Box.createVerticalStrut(3));
        panel.add(buildFieldRow("Sensor Range:", createSpinner(1, 20, 3)));
        panel.add(Box.createVerticalStrut(3));
        panel.add(buildFieldRow("Crew Capacity:", createSpinner(1, 100, 6)));
        panel.add(Box.createVerticalStrut(3));
        panel.add(buildFieldRow("Max Fuel:", createSpinner(1, 100, 6)));

        // Hex footprint info
        panel.add(Box.createVerticalStrut(10));
        JLabel hexLabel = new JLabel("Hex Footprint: 1 hex");
        hexLabel.setForeground(new Color(150, 170, 220));
        hexLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hexLabel.setName("hexFootprintLabel");
        panel.add(hexLabel);

        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 38));

        // Weapons section
        panel.add(buildWeaponsPanel());
        panel.add(Box.createVerticalStrut(10));

        // Design points section
        panel.add(buildDesignPointsPanel());

        return panel;
    }

    private JPanel buildWeaponsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(25, 25, 38));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                "Weapons",
                0, 0,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(180, 200, 255)));

        // Weapons table
        String[] columns = {"Type", "Name", "Strength", "Range", "Arcs"};
        weaponsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        weaponsTable = new JTable(weaponsTableModel);
        weaponsTable.setBackground(new Color(35, 35, 50));
        weaponsTable.setForeground(Color.WHITE);
        weaponsTable.setGridColor(new Color(60, 60, 80));
        JScrollPane scrollPane = new JScrollPane(weaponsTable);

        // Add weapon buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonPanel.setBackground(new Color(25, 25, 38));

        JButton addBeam = new JButton("+ Beam");
        addBeam.addActionListener(e -> addWeaponDialog(Ship.WeaponType.BEAM));
        buttonPanel.add(addBeam);

        JButton addMissile = new JButton("+ Missile");
        addMissile.addActionListener(e -> addWeaponDialog(Ship.WeaponType.MISSILE));
        buttonPanel.add(addMissile);

        JButton addTorpedo = new JButton("+ Torpedo");
        addTorpedo.addActionListener(e -> addWeaponDialog(Ship.WeaponType.TORPEDO));
        buttonPanel.add(addTorpedo);

        JButton removeWeapon = new JButton("- Remove");
        removeWeapon.addActionListener(e -> removeSelectedWeapon());
        buttonPanel.add(removeWeapon);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildDesignPointsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(25, 25, 38));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                "Design Points",
                0, 0,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(180, 200, 255)));

        // Points summary
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 3));
        infoPanel.setBackground(new Color(25, 25, 38));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        designPointsLabel = new JLabel("0 / 0 points");
        designPointsLabel.setForeground(Color.WHITE);
        infoPanel.add(new JLabel("Total:"));
        infoPanel.add(designPointsLabel);

        // Progress bar
        designPointsBar = new JProgressBar(0, 100);
        designPointsBar.setStringPainted(true);
        designPointsBar.setString("0%");
        designPointsBar.setForeground(new Color(80, 180, 80));
        designPointsBar.setBackground(new Color(40, 40, 55));
        infoPanel.add(new JLabel("Budget:"));
        infoPanel.add(designPointsBar);

        // Warnings
        warningsArea = new JTextArea();
        warningsArea.setEditable(false);
        warningsArea.setBackground(new Color(35, 35, 50));
        warningsArea.setForeground(new Color(255, 200, 100));
        warningsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane warnScroll = new JScrollPane(warningsArea);
        warnScroll.setPreferredSize(new Dimension(0, 60));

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(warnScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(30, 30, 45));
        panel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(150, 170, 220));
        panel.add(statusLabel);

        return panel;
    }

    // --- Component factories ---

    private JTextField createTextField() {
        nameField = new JTextField(15);
        nameField.setName("nameField");
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                updateDesignPoints();
            }
        });
        return nameField;
    }

    private JComboBox<Ship.Size> createSizeCombo() {
        sizeCombo = new JComboBox<>(Ship.Size.values());
        sizeCombo.setName("sizeCombo");
        sizeCombo.addActionListener(e -> onSizeChanged());
        return sizeCombo;
    }

    private JSpinner createSpinner(int min, int max, int defaultValue) {
        SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.addChangeListener(e -> updateDesignPoints());

        // Assign to the appropriate field based on current state
        if (speedSpinner == null) {
            speedSpinner = spinner;
        } else if (armorSpinner == null) {
            armorSpinner = spinner;
        } else if (hitPointsSpinner == null) {
            hitPointsSpinner = spinner;
        } else if (sensorRangeSpinner == null) {
            sensorRangeSpinner = spinner;
        } else if (crewCapacitySpinner == null) {
            crewCapacitySpinner = spinner;
        } else if (maxFuelSpinner == null) {
            maxFuelSpinner = spinner;
        }

        return spinner;
    }

    private JPanel buildFieldRow(String label, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setBackground(new Color(25, 25, 38));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 200, 255));
        lbl.setPreferredSize(new Dimension(110, 25));
        row.add(lbl);
        row.add(field);

        return row;
    }

    private JPanel buildEnumRow(String label, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setBackground(new Color(25, 25, 38));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(180, 200, 255));
        lbl.setPreferredSize(new Dimension(110, 25));
        row.add(lbl);
        row.add(field);

        return row;
    }

    // --- Event handlers ---

    private void onSizeChanged() {
        Ship.Size size = (Ship.Size) sizeCombo.getSelectedItem();
        if (size == null) return;

        // Apply defaults
        ShipBuilder builder = new ShipBuilder().size(size);

        // Temporarily build to get defaults
        Ship tempShip = builder.build();

        // Update fields with defaults (preserve name)
        String currentName = nameField.getText();
        speedSpinner.setValue(tempShip.getSpeed());
        armorSpinner.setValue(tempShip.getArmor());
        hitPointsSpinner.setValue(tempShip.getMaxHitPoints());
        sensorRangeSpinner.setValue(tempShip.getSensorRange());
        crewCapacitySpinner.setValue(tempShip.getCrewCapacity());
        maxFuelSpinner.setValue(tempShip.getMaxFuel());

        // Update hex footprint
        JLabel hexLabel = (JLabel) findComponentByName("hexFootprintLabel");
        if (hexLabel != null) {
            hexLabel.setText("Hex Footprint: " + size.getHexes() + " hex" + (size.getHexes() > 1 ? "es" : ""));
        }

        nameField.setText(currentName.isEmpty() ? "" : currentName);
        updateDesignPoints();
    }

    private void updateDesignPoints() {
        Ship.Size size = (Ship.Size) sizeCombo.getSelectedItem();
        if (size == null) {
            designPointsLabel.setText("0 / 0 points");
            designPointsBar.setValue(0);
            designPointsBar.setString("0%");
            warningsArea.setText("Select a ship size first");
            return;
        }

        DesignPoints dp = new DesignPoints(size);
        dp.calculateSpeedPoints((Integer) speedSpinner.getValue());
        dp.calculateArmorPoints((Integer) armorSpinner.getValue());
        dp.calculateHitPointsPoints((Integer) hitPointsSpinner.getValue());
        dp.calculateSensorPoints((Integer) sensorRangeSpinner.getValue());
        dp.calculateCrewPoints((Integer) crewCapacitySpinner.getValue());
        dp.calculateFuelPoints((Integer) maxFuelSpinner.getValue());

        // Calculate weapon points from table
        for (int i = 0; i < weaponsTableModel.getRowCount(); i++) {
            String typeStr = (String) weaponsTableModel.getValueAt(i, 0);
            String name = (String) weaponsTableModel.getValueAt(i, 1);
            int strength = (Integer) weaponsTableModel.getValueAt(i, 2);
            int range = (Integer) weaponsTableModel.getValueAt(i, 3);
            int arcs = (Integer) weaponsTableModel.getValueAt(i, 4);

            Ship.WeaponType type = Ship.WeaponType.valueOf(typeStr);
            Ship.Weapon weapon = new Ship.Weapon(type, name, strength, range, arcs);
            dp.calculateWeaponPoints(weapon);
        }

        int total = dp.getTotalPoints();
        int max = dp.getMaxPoints();
        int remaining = max - total;

        designPointsLabel.setText(total + " / " + max + " points" +
                (remaining < 0 ? " (OVER BUDGET!)" : " (" + remaining + " remaining)"));

        int percentage = max > 0 ? (total * 100) / max : 0;
        designPointsBar.setMaximum(100);
        designPointsBar.setValue(Math.min(percentage, 100));
        designPointsBar.setString(percentage + "%");

        if (percentage > 100) {
            designPointsBar.setForeground(new Color(220, 60, 60));
        } else if (percentage > 85) {
            designPointsBar.setForeground(new Color(220, 180, 60));
        } else {
            designPointsBar.setForeground(new Color(80, 180, 80));
        }

        // Validate and show warnings
        dp.validate();
        List<String> warnings = dp.getWarnings();
        if (warnings.isEmpty()) {
            warningsArea.setText("Design is valid");
            warningsArea.setForeground(new Color(100, 220, 100));
        } else {
            warningsArea.setText(String.join("\n", warnings));
            warningsArea.setForeground(new Color(255, 200, 100));
        }

        statusLabel.setText("Design points: " + total + "/" + max);
    }

    // --- Template loading ---

    private void loadTemplate(String templateName) {
        if (templateName == null || templateName.equals("-- Custom --")) {
            return;
        }

        ShipTemplate template = null;
        switch (templateName) {
            case "Scout": template = ShipTemplate.scout(); break;
            case "Fighter": template = ShipTemplate.fighter(); break;
            case "Cruiser": template = ShipTemplate.cruiser(); break;
            case "Battleship": template = ShipTemplate.battleship(); break;
            case "Transport": template = ShipTemplate.transport(); break;
        }

        if (template == null) return;

        // Populate form
        nameField.setText("");
        sizeCombo.setSelectedItem(template.getSize());
        speedSpinner.setValue(template.getSpeed());
        armorSpinner.setValue(template.getArmor());
        hitPointsSpinner.setValue(template.getHitPoints());
        sensorRangeSpinner.setValue(template.getSensorRange());
        crewCapacitySpinner.setValue(template.getCrewCapacity());
        maxFuelSpinner.setValue(template.getMaxFuel());

        // Clear and reload weapons
        weaponsTableModel.setRowCount(0);
        for (Ship.Weapon weapon : template.getWeapons()) {
            weaponsTableModel.addRow(new Object[]{
                    weapon.getType().toString(),
                    weapon.getName(),
                    weapon.getStrength(),
                    weapon.getRange(),
                    weapon.getArcs()
            });
        }

        // Update hex footprint
        JLabel hexLabel = (JLabel) findComponentByName("hexFootprintLabel");
        if (hexLabel != null) {
            hexLabel.setText("Hex Footprint: " + template.getSize().getHexes() + " hex" +
                    (template.getSize().getHexes() > 1 ? "es" : ""));
        }

        updateDesignPoints();
        statusLabel.setText("Loaded template: " + templateName + " - " + template.getDescription());
    }

    // --- Weapon management ---

    private void addWeaponDialog(Ship.WeaponType type) {
        // Default values based on type
        int defaultStrength, defaultRange;
        switch (type) {
            case BEAM: defaultStrength = 2; defaultRange = 5; break;
            case MISSILE: defaultStrength = 3; defaultRange = 8; break;
            case TORPEDO: defaultStrength = 5; defaultRange = 10; break;
            default: defaultStrength = 1; defaultRange = 5;
        }

        // Build dialog
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Name:"));
        JTextField nameInput = new JTextField(type.name() + " Weapon");
        panel.add(nameInput);

        panel.add(new JLabel("Strength:"));
        JSpinner strengthInput = new JSpinner(new SpinnerNumberModel(defaultStrength, 1, 20, 1));
        panel.add(strengthInput);

        panel.add(new JLabel("Range:"));
        JSpinner rangeInput = new JSpinner(new SpinnerNumberModel(defaultRange, 1, 20, 1));
        panel.add(rangeInput);

        panel.add(new JLabel("Arcs:"));
        JSpinner arcsInput = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        panel.add(arcsInput);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add " + type + " Weapon", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameInput.getText().trim();
            if (name.isEmpty()) name = type.name() + " Weapon";

            weaponsTableModel.addRow(new Object[]{
                    type.toString(),
                    name,
                    strengthInput.getValue(),
                    rangeInput.getValue(),
                    arcsInput.getValue()
            });

            updateDesignPoints();
            statusLabel.setText("Added " + type + ": " + name);
        }
    }

    private void removeSelectedWeapon() {
        int selectedRow = weaponsTable.getSelectedRow();
        if (selectedRow >= 0) {
            weaponsTableModel.removeRow(selectedRow);
            updateDesignPoints();
            statusLabel.setText("Weapon removed");
        }
    }

    // --- Build and reset ---

    private void buildAndShowShip() {
        Ship.Size size = (Ship.Size) sizeCombo.getSelectedItem();
        if (size == null) {
            JOptionPane.showMessageDialog(this, "Please select a ship size.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ShipBuilder builder = new ShipBuilder();

        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            builder.name(name);
        }

        builder.size(size)
                .speed((Integer) speedSpinner.getValue())
                .armor((Integer) armorSpinner.getValue())
                .hitPoints((Integer) hitPointsSpinner.getValue())
                .sensorRange((Integer) sensorRangeSpinner.getValue())
                .crewCapacity((Integer) crewCapacitySpinner.getValue())
                .maxFuel((Integer) maxFuelSpinner.getValue());

        // Add weapons from table
        for (int i = 0; i < weaponsTableModel.getRowCount(); i++) {
            String typeStr = (String) weaponsTableModel.getValueAt(i, 0);
            String wName = (String) weaponsTableModel.getValueAt(i, 1);
            int strength = (Integer) weaponsTableModel.getValueAt(i, 2);
            int range = (Integer) weaponsTableModel.getValueAt(i, 3);
            int arcs = (Integer) weaponsTableModel.getValueAt(i, 4);

            Ship.WeaponType type = Ship.WeaponType.valueOf(typeStr);
            builder.addWeapon(new Ship.Weapon(type, wName, strength, range, arcs));
        }

        try {
            Ship ship = builder.build();

            // Show result
            String details = buildShipDetails(ship);
            JOptionPane.showMessageDialog(this, details,
                    "Ship Built: " + ship.getName(), JOptionPane.INFORMATION_MESSAGE);

            statusLabel.setText("Built ship: " + ship.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error building ship: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildShipDetails(Ship ship) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: monospace; font-size: 12px;'>");
        sb.append("<b>").append(ship.getName()).append("</b><br>");
        sb.append("Size: ").append(ship.getSize()).append(" (").append(ship.getHexSize()).append(" hexes)<br>");
        sb.append("Speed: ").append(ship.getSpeed()).append("<br>");
        sb.append("Armor: ").append(ship.getArmor()).append("<br>");
        sb.append("Hit Points: ").append(ship.getMaxHitPoints()).append("<br>");
        sb.append("Sensors: ").append(ship.getSensorRange()).append("<br>");
        sb.append("Crew: ").append(ship.getCrewCapacity()).append("<br>");
        sb.append("Fuel: ").append(ship.getMaxFuel()).append("<br>");
        sb.append("<br><b>Weapons (").append(ship.getWeapons().size()).append("):</b><br>");

        for (Ship.Weapon w : ship.getWeapons()) {
            sb.append("  ").append(w.getType()).append(" - ")
                    .append(w.getName()).append(" (Str:")
                    .append(w.getStrength()).append(" Range:")
                    .append(w.getRange()).append(" Arcs:")
                    .append(w.getArcs()).append(")<br>");
        }

        // Design points
        DesignPoints dp = DesignPoints.evaluate(ship);
        sb.append("<br><b>Design Points:</b> ").append(dp.getTotalPoints())
                .append("/").append(dp.getMaxPoints());
        if (dp.getTotalPoints() > dp.getMaxPoints()) {
            sb.append(" <span style='color:red'>(OVER BUDGET)</span>");
        }
        sb.append("</body></html>");

        return sb.toString();
    }

    private void resetForm() {
        nameField.setText("");
        sizeCombo.setSelectedIndex(0);
        speedSpinner.setValue(6);
        armorSpinner.setValue(2);
        hitPointsSpinner.setValue(6);
        sensorRangeSpinner.setValue(3);
        crewCapacitySpinner.setValue(6);
        maxFuelSpinner.setValue(6);
        weaponsTableModel.setRowCount(0);

        JLabel hexLabel = (JLabel) findComponentByName("hexFootprintLabel");
        if (hexLabel != null) {
            hexLabel.setText("Hex Footprint: 1 hex");
        }

        designPointsLabel.setText("0 / 0 points");
        designPointsBar.setValue(0);
        designPointsBar.setString("0%");
        warningsArea.setText("Select a ship size first");

        statusLabel.setText("Form reset");
    }

    // --- Utility ---

    private Component findComponentByName(String name) {
        // Search recursively through the component tree
        return findComponentInTree(getContentPane(), name);
    }

    private Component findComponentInTree(Component parent, String name) {
        if (parent.getName() != null && parent.getName().equals(name)) {
            return parent;
        }
        if (parent instanceof Container) {
            for (Component child : ((Container) parent).getComponents()) {
                Component found = findComponentInTree(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    // --- Main entry point ---

    /**
     * Launches the ShipBuilder UI.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            ShipBuilderUI ui = new ShipBuilderUI();
            ui.setVisible(true);
        });
    }
}
