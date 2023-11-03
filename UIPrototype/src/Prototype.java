import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Prototype extends JFrame {
    // For AB testing use; for A tests, set the bool to true, B tests set to false
    private boolean ABTestSwitch = false;


    private final int WIDTH = 640;
    private final int HEIGHT = 480;
    private JLabel titleText;
    private JPanel mainPanel;
    private JLabel modelDesTxt;
    private JTextField textField1;
    private JTextField textField2;
    private JLabel textureDesTxt;
    private JLabel status;
    private JLabel processTxt;
    private JButton modelGenBtn;
    private JLabel timeWaitedTxt;
    private JProgressBar progressBar;
    private JButton texRegenBtn;
    private JButton cancelGenerationBtn;
    private JLabel timeWaited;
    private JLabel genStatTxt;

    private long startTime;
    private int userCount;



    public Prototype(){
        setSize(WIDTH, HEIGHT);
        setTitle("AI Wonderland");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int)(screenSize.getWidth() - WIDTH) / 2, (int)(screenSize.getHeight() - HEIGHT) / 2);

        componentSetup();
        setVisible(true);
    }

    private void componentSetup() {
        this.add(mainPanel);
        cancelGenerationBtn.setEnabled(false);

        status.setVisible(false);
        timeWaitedTxt.setVisible(ABTestSwitch);

        modelGenBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                status.setVisible(true);
                modelGenBtn.setEnabled(false);
                cancelGenerationBtn.setEnabled(true);
                timeWaited.setVisible(ABTestSwitch);
                startTime = System.currentTimeMillis();
                progressBar.setValue(5);
                userCount++;
                startTimer();
            }
        });

        cancelGenerationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long endTime = System.currentTimeMillis();
                long elapsedTime = (endTime - startTime) / 1000; // in seconds

                int minutes = (int) (elapsedTime / 60);
                int seconds = (int) (elapsedTime % 60);

                String message = "User " + userCount + "(" + (ABTestSwitch? "A" : "B") + " Test): \nEntered obj prompt: " + textField1.getText()+
                        "\nEntered texture prompt: " + textField2.getText()+ "\nWaited " + minutes + " minutes " + seconds + " seconds\n\n";

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true));
                    writer.write(message);
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }


                status.setVisible(false);
                cancelGenerationBtn.setEnabled(false);
                modelGenBtn.setEnabled(true);
                progressBar.setValue(0);
                timeWaited.setVisible(false);
                textField1.setText("");
                textField2.setText("");
            }
        });

    }

    private void startTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = (currentTime - startTime) / 1000; // in seconds

                int minutes = (int) (elapsedTime / 60);
                int seconds = (int) (elapsedTime % 60);

                String timeString = String.format("%02d:%02d", minutes, seconds);
                timeWaited.setText(timeString);
            }
        }, 0, 1000);
    }

    public static void main(String[] args) {
        Prototype frame = new Prototype();
    }

}
