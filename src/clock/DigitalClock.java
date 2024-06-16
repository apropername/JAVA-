package clock;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DigitalClock extends JFrame {
public DigitalClock() {
        setTitle("Digital Clock with Alarm");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1));
ClockPanel clockPanel = new ClockPanel();
        AlarmPanel alarmPanel = new AlarmPanel(clockPanel);
add(clockPanel);
        add(alarmPanel);
new Thread(clockPanel).start();
        new Thread(alarmPanel).start();
    }
public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DigitalClock frame = new DigitalClock();
            frame.setVisible(true);
        });
    }
}

class ClockPanel extends JPanel implements Runnable {
    private String time;
    private final Lock lock;
    private final Condition condition;
    private boolean suspended = false;
    public ClockPanel() {
        this.time = "";
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString(time, 50, 100);
    }
    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                while (suspended) {
                    condition.await();
                }
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                repaint();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
public void suspendClock() {
        lock.lock();
        try {
            suspended = true;
        } finally {
            lock.unlock();
        }
    }
public void resumeClock() {
        lock.lock();
        try {
            suspended = false;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}
class AlarmPanel extends JPanel implements Runnable {
private final ClockPanel clockPanel;
public AlarmPanel(ClockPanel clockPanel) {
        this.clockPanel = clockPanel;
    }
@Override
    public void run() {
        while (true) {
            try {
                LocalTime now = LocalTime.now();
                if (now.getMinute() == 0 && now.getSecond() == 0) {
                    clockPanel.suspendClock();
                    for (int i = 0; i < 5; i++) {
                        repaint();
                        Thread.sleep(1000);
                    }
                    clockPanel.resumeClock();
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        LocalTime now = LocalTime.now();
        g.setFont(new Font("Arial", Font.BOLD, 20));
        if (now.getMinute() == 0 && now.getSecond() <= 5) {
            g.drawString("Alarm: " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")), 50, 50);
        }
    }
}
/*1. ClockPanel类:
    - 继承`JPanel`并实现`Runnable`接口，用于显示当前时间。
    - `run`方法每秒获取一次系统时间，并调用`repaint`方法更新面板显示。
    - `suspendClock`和`resumeClock`方法用于挂起和恢复线程。
2. AlarmPanel类:
    - 继承`JPanel`并实现`Runnable`接口，用于整点报时。
    - `run`方法中检查当前时间是否为整点，如果是则挂起`ClockPanel`线程，并每秒输出一次整点时间，共输出5次后恢复`ClockPanel`线程。
3. DigitalClock类:
    - 继承`JFrame`，设置布局管理器并添加`ClockPanel`和`AlarmPanel`。
    - 在`main`方法中启动程序。
 */
