package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;


public class LedLight extends JComponent{
	private Color ledColor;
	private Color lightColor;
	private Color darkColor;
	private Timer timer;
	private boolean isOn;
	
	public LedLight(Color color){
		this.ledColor=color;
		lightColor=new Color(ledColor.getRed()/3,ledColor.getGreen()/3,ledColor.getBlue()/3);
		darkColor=Color.black;
	}
	
//	@Override
//	public Dimension getPreferredSize() {
//		return new Dimension(20,20);
//	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		Paint oldPaint = g2.getPaint();

		g2.setColor(lightColor);
		g2.fillOval(0,0,getWidth()-1,getHeight()-1);
		
		Paint paint;
		paint=new GradientPaint(0,0,new Color(0.0f,0.0f,0.0f,0.6f),getWidth(),getHeight(),new Color(0.0f,0.0f,0.0f,0.0f));
		g2.setPaint(paint);
		g2.fillOval(0,0,getWidth()-1,getHeight()-1);
		
		paint=new RadialGradientPaint(new Point2D.Double(getWidth()/2.0,getHeight()/2.0),getWidth()/2.0f,new float[]{0.8f,1.0f},new Color[]{lightColor,darkColor});
		g2.setPaint(paint);
		g2.fillOval(0,0,getWidth()-1,getHeight()-1);

		paint=new RadialGradientPaint(new Point2D.Double(getWidth()/2.0,getHeight()/2.0),getWidth()/1.4f,new Point2D.Double(getWidth()/3.2,getHeight()/4.9),
				new float[]{0.0f,0.5f},new Color[]{new Color(1.0f, 1.0f, 1.0f, 0.8f),new Color(1.0f,1.0f,1.0f,0.0f)},RadialGradientPaint.CycleMethod.NO_CYCLE);
		g2.setPaint(paint);
		g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
		g2.setPaint(oldPaint);
	}

	public void on(){
		ledon();
		if(timer!=null){
			timer.stop();
			timer=null;
		}
	}
	
	public void off(){
		ledOff();
		if(timer!=null){
			timer.stop();
			timer=null;
		}
	}
	
	private void ledon(){
		isOn=true;
		lightColor=ledColor;
		darkColor=new Color(ledColor.getRed()/3,ledColor.getGreen()/3,ledColor.getBlue()/3);
		repaint();
	}
	
	private void ledOff(){
		isOn=false;
		lightColor=new Color(ledColor.getRed()/3,ledColor.getGreen()/3,ledColor.getBlue()/3);
		darkColor=Color.black;
		repaint();
	}

	public void blink(int blinkRate){
		blink(blinkRate,0);
	}
	
	public void blink(int blinkRate,int initDelay){
    	timer = new Timer(blinkRate,new TimerListener());
        timer.setInitialDelay(initDelay);
        timer.start();
	}

	private class TimerListener implements ActionListener {

    	public TimerListener() {
    		ledOff();
    	}

    	public void actionPerformed(ActionEvent e) {
    		if (isOn) {
    			ledOff();
    		}
    		else {
    			ledon();
    		}
    	}
    }

	public boolean isOn(){
		return isOn;
	}
	
	public static void main(String... args) {
//    	ll=new LedLight(new Color(30,144,255));
    	final LedLight ll1=new LedLight(Color.red);
    	final LedLight ll2=new LedLight(Color.green);
    	final LedLight ll3=new LedLight(new Color(1411583));
    	final LedLight ll4=new LedLight(new Color(1411583));

    	ll1.setPreferredSize(new Dimension(10,10));
    	ll2.setPreferredSize(new Dimension(30,30));
    	ll3.setPreferredSize(new Dimension(20,20));
    	ll4.setPreferredSize(new Dimension(20,20));
    	
    	JFrame frame=new JFrame();
    	JPanel panel = new JPanel();
        panel.add(ll1);
        panel.add(ll2);
        panel.add(ll3);
        panel.add(ll4);
        frame.add(panel);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        ll1.blink(1000);
        ll2.blink(1000,700);
        ll3.off();
        ll4.on();
    }
}


