package it.unisa.studenti.porcelli.j.sudoku.board;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;

public class SudokuPanel extends JPanel {
    private static final int CLUSTER = 3;
    private static final int MAX_ROWS = 9;
    private static final float FIELD_PTS = 32f;
    private static final int GAP = 3;
    private static final Color BG = Color.BLACK;
    private static final Color SOLVED_BG = Color.LIGHT_GRAY;
    public static final int TIMER_DELAY = 2 * 1000;
    private JTextField[][] fieldGrid = new JTextField[MAX_ROWS][MAX_ROWS];

    public SudokuPanel(Integer[][] matrix) {
        JPanel mainPanel = new JPanel(new GridLayout(CLUSTER, CLUSTER));	// External grid to contain the subgrids.
        mainPanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        mainPanel.setBackground(BG);
        JPanel[][] panels = new JPanel[CLUSTER][CLUSTER];
        for (int i = 0; i < panels.length; i++) {
            for (int j = 0; j < panels[i].length; j++) {
                panels[i][j] = new JPanel(new GridLayout(CLUSTER, CLUSTER, 1, 1));	// Subgrids 3x3;
                panels[i][j].setBackground(BG);
                panels[i][j].setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
                mainPanel.add(panels[i][j]);
            }
        }

        for (int row = 0; row < fieldGrid.length; row++) {
            for (int col = 0; col < fieldGrid[row].length; col++) {
                fieldGrid[row][col] = createField(matrix[row][col], row, col);
                int i = row / 3;
                int j = col / 3;
                panels[i][j].add(fieldGrid[row][col]);
            }
        }

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        // add(new JButton(new SolveAction("Solve")), BorderLayout.PAGE_END);
    }

    private JTextField createField(int value, int row, int col) {
        JTextField field = new JTextField(2);
        if(value != 0)
        	field.setText("" + value);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(field.getFont().deriveFont(Font.BOLD, FIELD_PTS));

        return field;
    }
    
    public void placeValue(int i, int j, int number) {
    	fieldGrid[i][j].setText(number+"");
    }
    
    

    public static SudokuPanel createAndShowGui(Integer[][] matrix) {
        SudokuPanel mainPanel = new SudokuPanel(matrix);

        JFrame frame = new JFrame("Sudoku");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        
        return mainPanel;
    }
}