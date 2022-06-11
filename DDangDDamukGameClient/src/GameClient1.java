import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class GameClient1 extends JFrame implements ActionListener
{

	private JPanel contentPane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField username_tf;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					GameClient1 frame = new GameClient1();
					frame.setVisible(true);
					
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GameClient1()
	{
		setResizable(false);
		
		setTitle("DDangDDamukGame");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 346, 419);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel ip_lb = new JLabel("IP");
		ip_lb.setForeground(Color.BLACK);
		ip_lb.setFont(new Font("±º∏≤", Font.PLAIN, 18));
		ip_lb.setHorizontalAlignment(SwingConstants.CENTER);
		ip_lb.setBounds(30, 100, 130, 25);
		contentPane.add(ip_lb);
		
		ip_tf = new JTextField();
		ip_tf.setHorizontalAlignment(SwingConstants.CENTER);
		ip_tf.setText("127.0.0.1");
		ip_tf.setEnabled(false);
		ip_tf.setEditable(false);
		ip_tf.setBounds(170, 100, 130, 25);
		contentPane.add(ip_tf);
		ip_tf.setColumns(10);
		
		JLabel port_lb = new JLabel("Port");
		port_lb.setForeground(Color.BLACK);
		port_lb.setHorizontalAlignment(SwingConstants.CENTER);
		port_lb.setFont(new Font("±º∏≤", Font.PLAIN, 18));
		port_lb.setBounds(30, 130, 130, 25);
		contentPane.add(port_lb);
		
		port_tf = new JTextField();
		port_tf.setText("30000");
		port_tf.setHorizontalAlignment(SwingConstants.CENTER);
		port_tf.setEnabled(false);
		port_tf.setEditable(false);
		port_tf.setColumns(10);
		port_tf.setBounds(170, 130, 130, 25);
		contentPane.add(port_tf);
		
		JLabel username_lb = new JLabel("Name");
		username_lb.setForeground(Color.BLACK);
		username_lb.setHorizontalAlignment(SwingConstants.CENTER);
		username_lb.setFont(new Font("±º∏≤", Font.PLAIN, 18));
		username_lb.setBounds(30, 160, 130, 25);
		contentPane.add(username_lb);
		
		username_tf = new JTextField();
		username_tf.setHorizontalAlignment(SwingConstants.CENTER);
		username_tf.setColumns(10);
		username_tf.setBounds(170, 160, 130, 25);
		contentPane.add(username_tf);
		
		JButton start_btn = new JButton("\uAC8C\uC784 \uC2DC\uC791");
		start_btn.setBounds(30, 200, 270, 70);
		start_btn.addActionListener(this);
		contentPane.add(start_btn);
		
		JButton end_btn = new JButton("\uAC8C\uC784 \uC885\uB8CC");
		end_btn.setBounds(30, 280, 270, 70);
		end_btn.addActionListener(this);
		contentPane.add(end_btn);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JButton btn = (JButton)e.getSource();
		
		if(btn.getText().equals("∞‘¿” Ω√¿€"))
		{
			new GameClient2(ip_tf.getText().trim(), port_tf.getText().trim(), username_tf.getText().trim());
			setVisible(false);
		}
		else if(btn.getText().equals("∞‘¿” ¡æ∑·"))
		{
			System.exit(0);
		}
	}
}