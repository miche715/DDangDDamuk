import java.util.*;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class GameClient2 extends JFrame implements ActionListener, KeyListener
{
	private static final  int BUF_LEN = 128;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Timer timer;
	
	int player_stop_time;
	
	private JPanel contentPane;
	private JTextField chatting_tf;
	private JScrollPane userchat_sp;
	private JTextArea userchat_ta;
	private JLabel userlist_lb;
	private JLabel wall_lb;
	private JButton start_btn;
	private JTextField user1name_tf;
	private JTextField user2name_tf;
	private JTextField user3name_tf;
	private JTextField user4name_tf;
	private JTextField user1score_tf;
	private JTextField user2score_tf;
	private JTextField user3score_tf;
	private JTextField user4score_tf;
	private JTextField timer_tf;
	private JTextField time_tf;
	private JLabel background_lb;
	
	JPanel itemp;
	
	ImageIcon updatespeedupIcon;
	ImageIcon updatespeeddownIcon;
	ImageIcon updategethorizontalIcon;
	ImageIcon updategetverticalIcon;
	
	private int usercnt = 0;
	private int[][] map = new int[720][660]; //캐릭터들의 위치 좌표 정보 및 점령(임시) 상태
	//0 = 벽
	//1 = PLAYER 1
	//2 = PLAYER 2
	//3 = PLAYER 3
	//4 = PLAYER 4
	//5 = 맨 땅
	//6 = 아이템 1
	//7 = 아이템 2
	//8 = 아이템 2
	//9 = 아이템 2
	private JLabel[][] outland = new JLabel[15][15]; //실제로 점령되어 밖에서 보여지는 땅
	private int[][] inland = new int[15][15]; //유저들이 점령한 땅을 관리
	//0 = 점령되지 않은 땅
	//1 = PLAYER 1이 점령한 땅
	//2 = PLAYER 2가 점령한 땅
	//3 = PLAYER 3이 점령한 땅
	//4 = PLAYER 4가 점령한 땅
	
	private int oldx;
	private int oldy;
	private int newx;
	private int newy;
	
	private ArrayList<String> users = new ArrayList<>();
	private ArrayList<JLabel> items = new ArrayList<>();
	
	private String username;
	private int pid = 0;
	
	private JLabel player1_lb = new JLabel();
	private JLabel player2_lb = new JLabel();
	private JLabel player3_lb = new JLabel();
	private JLabel player4_lb = new JLabel();
	private int p1speed;
	private int p2speed;
	private int p3speed;
	private int p4speed;
	private	int p1pos;
	private int p2pos;
	private int p3pos;
	private int p4pos;
	private int p1score;
	private int p2score;
	private int p3score;
	private int p4score;
	private Color[] pcolor = new Color[5];
	
	public GameClient2(String ip, String port, String username)
	{
		int i;
		int j;
		
		this.username = username;
		pcolor[1] = Color.ORANGE;
		pcolor[2] = Color.CYAN;
		pcolor[3] = Color.GREEN;
		pcolor[4] = Color.MAGENTA;
		
		setTitle("DDangDDamukGame");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		userchat_ta = new JTextArea();
		userchat_ta.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userchat_ta.setLineWrap(true);
		userchat_ta.setBackground(Color.WHITE);
		userchat_ta.setEditable(false);
		userchat_sp = new JScrollPane(userchat_ta);
		userchat_sp.setSize(263, 375);
		userchat_sp.setLocation(721, 193);
		contentPane.add(userchat_sp);
		
		chatting_tf = new JTextField();
		chatting_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		chatting_tf.setBackground(Color.WHITE);
		chatting_tf.setBounds(721, 568, 263, 41);
		chatting_tf.setColumns(10);
		contentPane.add(chatting_tf);
		chatting_tf.addActionListener(this);
		chatting_tf.requestFocus();
		
		userlist_lb = new JLabel("S C O R E");
		userlist_lb.setBackground(SystemColor.activeCaption);
		userlist_lb.setOpaque(true);
		userlist_lb.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		userlist_lb.setHorizontalAlignment(SwingConstants.CENTER);
		userlist_lb.setForeground(Color.BLACK);
		userlist_lb.setBounds(721, 0, 263, 18);
		contentPane.add(userlist_lb);
		
		player1_lb.setText("1P");
		player1_lb.setHorizontalAlignment(SwingConstants.CENTER);
		player1_lb.setBackground(Color.ORANGE);
		player1_lb.setOpaque(true);
		player1_lb.setBounds(15, 15, 30, 30);
		contentPane.add(player1_lb);
		
		player2_lb.setText("2P");
		player2_lb.setHorizontalAlignment(SwingConstants.CENTER);
		player2_lb.setBackground(Color.CYAN);
		player2_lb.setOpaque(true);
		player2_lb.setBounds(675, 15, 30, 30);
		contentPane.add(player2_lb);
		
		player3_lb.setText("3P");
		player3_lb.setHorizontalAlignment(SwingConstants.CENTER);
		player3_lb.setBackground(Color.GREEN);
		player3_lb.setOpaque(true);
		player3_lb.setBounds(15, 615, 30, 30);
		contentPane.add(player3_lb);
		
		player4_lb.setText("4P");
		player4_lb.setHorizontalAlignment(SwingConstants.CENTER);
		player4_lb.setBackground(Color.MAGENTA);
		player4_lb.setOpaque(true);
		player4_lb.setBounds(675, 615, 30, 30);
		contentPane.add(player4_lb);
		
		itemp = new JPanel();
		itemp.setLayout(null);
		itemp.setBounds(0, 0, 720, 660);
		itemp.setBackground(new Color(0, 0, 0, 0));
		contentPane.add(itemp);
		
		LineBorder lineborder = new LineBorder(new Color(0, 0, 0, 150), 1, false);
		for(i = 0; i < 15; i++)
		{
			for(j = 0; j < 15; j++)
			{
				outland[i][j] = new JLabel();
				outland[i][j].setBounds(15 + 46 * i, 15 + 42 * j, 46, 42);
				outland[i][j].setBorder(lineborder);
				//outland[i][j].setOpaque(true);
				contentPane.add(outland[i][j]);
			}
		}
		
		start_btn = new JButton("Game Start");
		start_btn.setBounds(721, 609, 263, 51);
		start_btn.addActionListener(this);
		start_btn.setEnabled(false);
		contentPane.add(start_btn);
		
		user1name_tf = new JTextField();
		user1name_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user1name_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user1name_tf.setBackground(Color.ORANGE);
		user1name_tf.setEditable(false);
		user1name_tf.setOpaque(true);
		user1name_tf.setBounds(721, 19, 131, 36);
		contentPane.add(user1name_tf);
		user1name_tf.setColumns(10);
		
		user2name_tf = new JTextField();
		user2name_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user2name_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user2name_tf.setBackground(Color.CYAN);
		user2name_tf.setEditable(false);
		user2name_tf.setColumns(10);
		user2name_tf.setOpaque(true);
		user2name_tf.setBounds(721, 56, 131, 36);
		contentPane.add(user2name_tf);
		
		user3name_tf = new JTextField();
		user3name_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user3name_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user3name_tf.setBackground(Color.GREEN);
		user3name_tf.setEditable(false);
		user3name_tf.setColumns(10);
		user3name_tf.setOpaque(true);
		user3name_tf.setBounds(721, 93, 131, 36);
		contentPane.add(user3name_tf);
		
		user4name_tf = new JTextField();
		user4name_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user4name_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user4name_tf.setBackground(Color.MAGENTA);
		user4name_tf.setEditable(false);
		user4name_tf.setColumns(10);
		user4name_tf.setOpaque(true);
		user4name_tf.setBounds(721, 130, 131, 36);
		contentPane.add(user4name_tf);
		
		user1score_tf = new JTextField();
		user1score_tf.setBackground(Color.ORANGE);
		user1score_tf.setForeground(Color.BLACK);
		user1score_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user1score_tf.setEditable(false);
		user1score_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user1score_tf.setText("0");
		user1score_tf.setColumns(10);
		user1score_tf.setOpaque(true);
		user1score_tf.setBounds(853, 19, 131, 36);
		contentPane.add(user1score_tf);
		
		user2score_tf = new JTextField();
		user2score_tf.setBackground(Color.CYAN);
		user2score_tf.setForeground(Color.BLACK);
		user2score_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user2score_tf.setEditable(false);
		user2score_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user2score_tf.setText("0");
		user2score_tf.setColumns(10);
		user2score_tf.setOpaque(true);
		user2score_tf.setBounds(853, 56, 131, 36);
		contentPane.add(user2score_tf);
		
		user3score_tf = new JTextField();
		user3score_tf.setBackground(Color.GREEN);
		user3score_tf.setForeground(Color.BLACK);
		user3score_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user3score_tf.setEditable(false);
		user3score_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user3score_tf.setText("0");
		user3score_tf.setColumns(10);
		user3score_tf.setOpaque(true);
		user3score_tf.setBounds(853, 93, 131, 36);
		contentPane.add(user3score_tf);
		
		user4score_tf = new JTextField();
		user4score_tf.setBackground(Color.MAGENTA);
		user4score_tf.setForeground(Color.BLACK);
		user4score_tf.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		user4score_tf.setEditable(false);
		user4score_tf.setHorizontalAlignment(SwingConstants.CENTER);
		user4score_tf.setText("0");
		user4score_tf.setColumns(10);
		user4score_tf.setOpaque(true);
		user4score_tf.setBounds(853, 130, 131, 36);
		contentPane.add(user4score_tf);
		
		time_tf = new JTextField();
		time_tf.setFont(new Font("돋움", Font.BOLD, 13));
		time_tf.setEditable(false);
		time_tf.setBackground(Color.BLACK);
		time_tf.setForeground(new Color(0, 128, 0));
		time_tf.setHorizontalAlignment(SwingConstants.CENTER);
		time_tf.setText("T I M E");
		time_tf.setColumns(10);
		time_tf.setOpaque(true);
		time_tf.setBounds(721, 167, 99, 25);
		contentPane.add(time_tf);
		
		timer_tf = new JTextField();
		timer_tf.setText("0 1 : 0 0");
		timer_tf.setHorizontalAlignment(SwingConstants.CENTER);
		timer_tf.setFont(new Font("돋움", Font.PLAIN, 14));
		timer_tf.setEditable(false);
		timer_tf.setBackground(Color.BLACK);
		timer_tf.setForeground(new Color(0, 128, 0));
		timer_tf.setBounds(821, 167, 163, 25);
		timer_tf.setOpaque(true);
		timer_tf.setColumns(10);
		contentPane.add(timer_tf);
		
		ImageIcon icon = new ImageIcon(GameClient2.class.getResource("/images/flat-sand-beach-textured-backdrop.png"));
		Image img = icon.getImage();
		Image updateImg = img.getScaledInstance(690, 630, Image.SCALE_SMOOTH);
		ImageIcon updateIcon = new ImageIcon(updateImg);
		background_lb = new JLabel(updateIcon);
		background_lb.setBounds(15, 15, 690, 630);
		contentPane.add(background_lb);
		
		ImageIcon icon1 = new ImageIcon(GameClient2.class.getResource("/images/wall.png"));
		Image img1 = icon1.getImage();
		Image updateImg1 = img1.getScaledInstance(720, 660, Image.SCALE_SMOOTH);
		ImageIcon updateIcon1 = new ImageIcon(updateImg1);
		wall_lb = new JLabel(updateIcon1);
		wall_lb.setBounds(0, 0, 720, 660);
		contentPane.add(wall_lb);
		
		
		ImageIcon speedupicon = new ImageIcon(GameClient2.class.getResource("/images/speed_up.png"));
		Image speedupimg = speedupicon.getImage();
		Image updatespeedupImg = speedupimg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		updatespeedupIcon = new ImageIcon(updatespeedupImg);
		
		ImageIcon speeddownicon = new ImageIcon(GameClient2.class.getResource("/images/speed_down.png"));
		Image speeddownimg = speeddownicon.getImage();
		Image updatespeeddownImg = speeddownimg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		updatespeeddownIcon = new ImageIcon(updatespeeddownImg);
		
		ImageIcon gethorizontalicon = new ImageIcon(GameClient2.class.getResource("/images/get_horizontal.png"));
		Image gethorizontalimg = gethorizontalicon.getImage();
		Image updategethorizontalImg = gethorizontalimg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		updategethorizontalIcon = new ImageIcon(updategethorizontalImg);
		
		ImageIcon getverticalicon = new ImageIcon(GameClient2.class.getResource("/images/get_vertical.png"));
		Image getverticalimg = getverticalicon.getImage();
		Image updategetverticalImg = getverticalimg.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		updategetverticalIcon = new ImageIcon(updategetverticalImg);
		
		setVisible(true);
		
		background_lb.addKeyListener(this);
		background_lb.requestFocus();
		//--------------------------------------------------------------------------------------------------
		//                                    * * * 서버 연결 시도 시작 * * *
		//--------------------------------------------------------------------------------------------------
		try
		{
			socket = new Socket(ip, Integer.parseInt(port));
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			sendMessage("100" + " " + username);
			ListenNetwork net = new ListenNetwork();
			net.start();
		}
		catch (Exception e)
		{
			System.out.println("연결 실패(" + e.toString() + ").");
		}
		//--------------------------------------------------------------------------------------------------
		//                                    * * * 서버 연결 시도 종료 * * *
		//--------------------------------------------------------------------------------------------------
	}

	class ListenNetwork extends Thread
	{	
		public void run()
		{
			while (true)
			{
				try
				{
					
					byte[] b = new byte[BUF_LEN];
					int ret;
					ret = dis.read(b);
					if (ret < 0)
					{
						try
						{
							dos.close();
							dis.close();
							socket.close();
							break;
						}
						catch (Exception ee)
						{
							break;
						}// catch문 끝
					}
					
					//--------------------------------------------------------------------------------------------------
					//                                   * * * 서버 메세지 수신 시작 * * *
					//--------------------------------------------------------------------------------------------------
					String msg = new String(b, "euc-kr");
					msg = msg.trim();
					
					if(!msg.equals(""))
					{
						String[] msgarr = msg.split(" ", 2);
						int prtc = Integer.parseInt(msgarr[0]); //프로토콜
						String str = msgarr.length >= 2 ? msgarr[1] : ""; //프로토콜을 제외한 내용
						
						if(prtc == 001) //게임 시작
						{
							gameInit();
							gameStart();
							start_btn.setEnabled(false);
						}
						else if(prtc == 002) //게임 종료
						{
							gameOver();
						}
						else if(prtc == 100) //pid 할당
						{
							int _pid = Integer.parseInt(str);
							
							pid = _pid;
						}		
						else if(prtc == 101) //로그인
						{	
							String[] cmd = str.split(" ");
							String _username = cmd[0];
							int _pid = Integer.parseInt(cmd[1]);

							if(_pid == 1)
							{
								user1name_tf.setText(_username);
							}
							else if(_pid == 2)
							{
								user2name_tf.setText(_username);
							}
							else if(_pid == 3)
							{
								user3name_tf.setText(_username);
							}
							else if(_pid == 4)
							{
								user4name_tf.setText(_username);
							}
							
							users.add(_pid - 1, _username);
							usercnt++;
							
							if(usercnt == 4 && pid == 1)
							{
								start_btn.setEnabled(true);
							}
						}
						else if(prtc == 102) //로그아웃
						{
							String[] cmd = str.split(" ");
							String _username = cmd[0];
							int _pid = Integer.parseInt(cmd[1]);
							
							if(_pid == 1)
							{
								user1name_tf.setText("");
							}
							else if(_pid == 2)
							{
								user2name_tf.setText("");
							}
							else if(_pid == 3)
							{
								user3name_tf.setText("");
							}
							else if(_pid == 4)
							{
								user4name_tf.setText("");
							}
						}
						else if(prtc == 200) //채팅 메세지
						{
							userchat_ta.append(str + "\n");
						}
						else if(prtc == 400) //진행 방향
						{
							String[] cmd = str.split(" ");
							int _pid = Integer.parseInt(cmd[0]);
							int _pos = Integer.parseInt(cmd[1]);
							
							if(_pid == 1)
							{
								p1pos = _pos;
							}
							else if(_pid == 2)
							{
								p2pos = _pos;
							}
							else if(_pid == 3)
							{
								p3pos = _pos;
							}
							else if(_pid == 4)
							{
								p4pos = _pos;
							}
						}
						else if(prtc == 500) //x y 좌표값
						{
							String[] cmd = str.split(" ");
							int _oldx = Integer.parseInt(cmd[0]);
							int _oldy = Integer.parseInt(cmd[1]);
							int _newx = Integer.parseInt(cmd[2]);
							int _newy = Integer.parseInt(cmd[3]);
							int _pid = Integer.parseInt(cmd[4]);
							
							if(_pid == 1)
							{
								player1_lb.setLocation(_newx, _newy);
							}
							else if(_pid == 2)
							{
								player2_lb.setLocation(_newx, _newy);
							}
							else if(_pid == 3)
							{
								player3_lb.setLocation(_newx, _newy);
							}
							else if(_pid == 4)
							{
								player4_lb.setLocation(_newx, _newy);
							}
							
							if(map[_newx][_newy] >= 60 && pid == _pid) //60이상이면 바뀐 좌표에 아이템이 있다는 뜻
							{
								int _item_code = map[_newx][_newy] / 10;
								int _item_index = map[_newx][_newy] % 10;
								int _x = items.get(_item_index).getLocation().x;
								int _y = items.get(_item_index).getLocation().y;
								
								sendMessage("802" + " " + _item_code + " " + _item_index + " " + _x + " " + _y + " " +_pid + " " + username);
							}
						}
						else if(prtc == 600) //땅 획득
						{					
							String[] cmd = str.split(" ");
							int _x = Integer.parseInt(cmd[0]);
							int _y = Integer.parseInt(cmd[1]);
							int _pid = Integer.parseInt(cmd[2]);
							
							inland[_x][_y] = _pid;
							outland[_x][_y].setOpaque(true);
							outland[_x][_y].setBackground(pcolor[_pid]);
							calcScore();
							repaint();
						}
						else if(prtc == 700) //게임 시간 수신
						{
							timer_tf.setText(str);
						}
						else if(prtc == 801) //아이템 생성
						{
							String[] cmd = str.split(" ");
							int _x = Integer.parseInt(cmd[0]);
							int _y = Integer.parseInt(cmd[1]);
							int _item = Integer.parseInt(cmd[2]);
							items.add(new JLabel());
							JLabel _item_lb = items.get(items.size() - 1);
							
							if(_item == 6) //속도 증가
							{						 
								_item_lb.setIcon(updatespeedupIcon);
							}
							else if(_item == 7) //속도 감소
							{
								_item_lb.setIcon(updatespeeddownIcon);
							}
							else if(_item == 8) //x축 1줄 점령
							{
								_item_lb.setIcon(updategethorizontalIcon);
							}
							else if(_item == 9) //y축 1줄 점령
							{
								_item_lb.setIcon(updategetverticalIcon);
							}
							_item_lb.setText(_item + "" + (items.size() - 1) + ""); //60 아이템 코드는 6이고, 인덱스는 0
							_item_lb.setOpaque(true);
							_item_lb.setBounds(_x, _y, 30, 30);
							_item_lb.setForeground(new Color(0, 0, 0, 0));
							itemp.add(_item_lb);
							repaint();
							
							for(int _i = _x; _i <= _x + 30; _i++)
							{
								for(int _j = _y; _j <= _y + 30; _j++)
								{
									map[_i][_j] = Integer.parseInt(_item_lb.getText()); //60 ~ 93
								}
							}
						}
						else if(prtc == 802) //아이템 획득
						{
							String[] cmd = str.split(" ");
							int _item_code = Integer.parseInt(cmd[0]);
							int _item_index = Integer.parseInt(cmd[1]);
							int _x = Integer.parseInt(cmd[2]);
							int _y = Integer.parseInt(cmd[3]);
							int _pid = Integer.parseInt(cmd[4]);
							
							if(_item_code == 6) //속도 증가
							{
								itemSpeedUp(_pid);
							}
							else if(_item_code == 7) //다른 유저 정지
							{
								itemSpeedDown(_pid);
							}
							else if(_item_code == 8) //x축 1줄 점령
							{
								itemGetHorizontal( _y, _pid);
							}
							else if(_item_code == 9) //y축 1줄 점령
							{
								itemGetVertical(_x, _pid);
							}
							
							items.get(_item_index).setVisible(false);
							itemp.remove(items.get(_item_index));
							repaint();
							for(int _i = _x; _i <= _x + 30; _i++)
							{
								for(int _j = _y; _j <= _y + 30; _j++)
								{
									map[_i][_j] = 5; //아이템이 채워져 있던 자리를 다시 맨 땅으로 만듬.
								}
							}
						}
					}
					//--------------------------------------------------------------------------------------------------
					//                                   * * * 서버 메세지 수신 종료 * * *
					//--------------------------------------------------------------------------------------------------		
				}
				catch (IOException e)
				{
					try
					{
						dos.close();
						dis.close();
						socket.close();
						
						break;
					}
					catch (Exception ee)
					{
						break;
					}
				}	
			}
		}
	}
	
	public void gameInit()
	{
		int i;
		int j;
		
		oldx = 0;
		oldy = 0;
		newx = 0;
		newy = 0;
		
		p1score = 0;
		p2score = 0;
		p3score = 0;
		p4score = 0;
		
		p1speed = 4;
		p2speed = 4;
		p3speed = 4;
		p4speed = 4;
		
		p1pos = 39;
		p2pos = 40;
		p3pos = 38;
		p4pos = 37;
		
		player1_lb.setLocation(15, 15);
		player2_lb.setLocation(675, 15);
		player3_lb.setLocation(15, 615);
		player4_lb.setLocation(675, 615);
		
		for(i = 0; i < 720; i++)
		{
			for(j = 0; j < 660; j++)
			{
				map[i][j] = 0; //전체를 벽으로 채움.
			}
		}
		for(i = 15; i <= 705; i++)
		{
			for(j = 15; j <= 645; j++)
			{
				map[i][j] = 5; //움직일 수 있는 공간을 다시 맨 땅으로 채움.
			}
		}
		
		for(i = 0; i < 15; i++)
		{
			for(j = 0; j < 15; j++)
			{
				inland[i][j] = 0;
			}
		}
		
		for(i = 15; i <= 45; i++)
		{
			for(j = 15; j <= 45; j++)
			{
				map[i][j] = 1;
			}
		}
		for(i = 675; i <= 705; i++)
		{
			for(j = 15; j <= 45; j++)
			{
				map[i][j] = 2;
			}
		}
		for(i = 15; i <= 45; i++)
		{
			for(j = 615; j <= 645; j++)
			{
				map[i][j] = 3;
			}
		}
		for(i = 675; i <= 705; i++)
		{
			for(j = 615; j <= 645; j++)
			{
				map[i][j] = 4;
			}
		}
	}
	
	public void gameOver()
	{
		int max_score = -1;
		int max_user = 0;
		int[] score = {p1score, p2score, p3score, p4score}; 
		int i;
		int j;
		
		timer.cancel();
		
		for(i = 0; i < 4; i++)
		{
			if(score[i] >= max_score)
			{
				max_score = score[i];
				max_user = i;
			}
		}
		
		//userchat_ta.append("---------------------------------------------\n");
		//userchat_ta.append("[system] 승자: " + users.get(max_user) + " " + max_score + "점.\n");
		//userchat_ta.append("---------------------------------------------\n");
		
		JOptionPane.showMessageDialog(null, "승자: " + users.get(max_user) + "\n" + "점수: " + max_score, "결과", JOptionPane.PLAIN_MESSAGE);
	}
	
	public void gameStart()
	{
		timer = new Timer();
		
		TimerTask move_charcter = new TimerTask()
		{
			@Override
			public void run()
			{
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 1 시작 * * *
				//--------------------------------------------------------------------------------------------------
				if(pid == 1)
				{
					oldx = player1_lb.getLocation().x;
					oldy = player1_lb.getLocation().y;
					newx = player1_lb.getLocation().x;
					newy = player1_lb.getLocation().y;
					if(p1pos == 37)
					{
						if(map[oldx - p1speed][oldy] == 0)
						{
							newx = 15;
						}
						else
						{
							newx = oldx - p1speed;
						}	
					}
					else if(p1pos == 39)
					{
						if(map[oldx + p1speed + 30][oldy] == 0)
						{
							newx = 675;
						}
						else
						{
							newx = oldx + p1speed;
						}
					}
					else if(p1pos == 38)
					{
						if(map[oldx][oldy - p1speed] == 0)
						{
							newy = 15;
						}
						else
						{
							newy = oldy - p1speed;
						}
					}
					else if(p1pos == 40)
					{
						if(map[oldx][oldy + p1speed + 30] == 0)
						{
							newy = 615;
						}
						else
						{
							newy = oldy + p1speed;
						}
					}
					sendMessage("500"  + " " + oldx  + " " + oldy + " " + newx + " " + newy + " " + pid);
				}
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 1 종료 * * *
				//--------------------------------------------------------------------------------------------------	
				
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 2 시작 * * *
				//--------------------------------------------------------------------------------------------------
				if(pid == 2)
				{
					oldx = player2_lb.getLocation().x;
					oldy = player2_lb.getLocation().y;
					newx = player2_lb.getLocation().x;
					newy = player2_lb.getLocation().y;
					if(p2pos == 37)
					{
						if(map[oldx - p2speed][oldy] == 0)
						{
							newx = 15;
						}
						else
						{
							newx = oldx - p2speed;
						}
					}
					else if(p2pos == 39)
					{
						if(map[oldx + p2speed + 30][oldy] == 0)
						{
							newx = 675;
						}
						else
						{
							newx = oldx + p2speed;
						}
					}
					else if(p2pos == 38)
					{
						if(map[oldx][oldy - p2speed] == 0)
						{
							newy = 15;
						}
						else
						{
							newy = oldy - p2speed;
						}
					}
					else if(p2pos == 40)
					{
						if(map[oldx][oldy + p2speed + 30] == 0)
						{
							newy = 615;
						}
						else
						{
							newy = oldy + p2speed;
						}
					}
					sendMessage("500"  + " " + oldx  + " " + oldy + " " + newx + " " + newy + " " + pid);
				}
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 2 종료 * * *
				//--------------------------------------------------------------------------------------------------	
				
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 3 시작 * * *
				//--------------------------------------------------------------------------------------------------
				if(pid == 3)
				{
					oldx = player3_lb.getLocation().x;
					oldy = player3_lb.getLocation().y;
					newx = player3_lb.getLocation().x;
					newy = player3_lb.getLocation().y;
					if(p3pos == 37)
					{
						if(map[oldx - p3speed][oldy] == 0)
						{
							newx = 15;
						}
						else
						{
							newx = oldx - p3speed;
						}
					}
					else if(p3pos == 39)
					{
						if(map[oldx + p3speed + 30][oldy] == 0)
						{
							newx = 675;
						}
						else
						{
							newx = oldx + p3speed;
						}
					}
					else if(p3pos == 38)
					{
						if(map[oldx][oldy - p3speed] == 0)
						{
							newy = 15;
						}
						else
						{
							newy = oldy - p3speed;
						}
					}
					else if(p3pos == 40)
					{
						if(map[oldx][oldy + p3speed + 30] == 0)
						{
							newy = 615;
						}
						else
						{
							newy = oldy + p3speed;
						}
					}
					sendMessage("500"  + " " + oldx  + " " + oldy + " " + newx + " " + newy + " " + pid);
				}
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 3 종료 * * *
				//--------------------------------------------------------------------------------------------------	
				
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 4 시작 * * *
				//--------------------------------------------------------------------------------------------------
				if(pid == 4)
				{
					oldx = player4_lb.getLocation().x;
					oldy = player4_lb.getLocation().y;
					newx = player4_lb.getLocation().x;
					newy = player4_lb.getLocation().y;
					if(p4pos == 37)
					{
						if(map[oldx - p4speed][oldy] == 0)
						{
							newx = 15;
						}
						else
						{
							newx = oldx - p4speed;
						}
					}
					else if(p4pos == 39)
					{
						if(map[oldx + p4speed + 30][oldy] == 0)
						{
							newx = 675;
						}
						else
						{
							newx = oldx + p4speed;
						}
					}
					else if(p4pos == 38)
					{
						if(map[oldx][oldy - p4speed] == 0)
						{
							newy = 15;
						}
						else
						{
							newy = oldy - p4speed;
						}
					}
					else if(p4pos == 40)
					{
						if(map[oldx][oldy + p4speed + 30] == 0)
						{
							newy = 615;
						}
						else
						{
							newy = oldy + p4speed;
						}
					}
					sendMessage("500"  + " " + oldx  + " " + oldy + " " + newx + " " + newy + " " + pid);
				}
				//--------------------------------------------------------------------------------------------------
				//                                    * * * PLAYER 4 종료 * * *
				//--------------------------------------------------------------------------------------------------
			}
		};
		timer.schedule(move_charcter, 0, 10);
	}

	public void calcScore()
	{
		int i;
		int j;
		int p1land = 0;
		int p2land = 0;
		int p3land = 0;
		int p4land = 0;
		
		for(i = 0; i < 15; i++)
		{
			for(j = 0; j < 15; j++)
			{
				if(inland[i][j] == 1)
				{
					p1land++;
				}
				else if(inland[i][j] == 2)
				{
					p2land++;
				}
				else if(inland[i][j] == 3)
				{
					p3land++;
				}
				else if(inland[i][j] == 4)
				{
					p4land++;
				}
			}
		}
		
		p1score = p1land;
		p2score = p2land;
		p3score = p3land;
		p4score = p4land;
		
		user1score_tf.setText(p1score + "");
		user2score_tf.setText(p2score + "");
		user3score_tf.setText(p3score + "");
		user4score_tf.setText(p4score + "");
	}
	
	public void itemSpeedUp(int pid)
	{
		if(pid == 1)
		{
			p1speed = p1speed < 8 ? p1speed + 1 : 8;
		}
		else if(pid == 2)
		{
			p2speed = p2speed < 8 ? p2speed + 1 : 8;
		}
		else if(pid == 3)
		{
			p3speed = p3speed < 8 ? p3speed + 1 : 8;
		}
		else if(pid == 4)
		{
			p4speed = p4speed < 8 ? p4speed + 1 : 8;
		}
	}
	
	public void itemSpeedDown(int pid)
	{
		if(pid == 1)
		{
			p2speed = p2speed > 1 ? p2speed - 1 : 1;
			p3speed = p3speed > 1 ? p3speed - 1 : 1;
			p4speed = p4speed > 1 ? p4speed - 1 : 1;
		}
		else if(pid == 2)
		{
			p1speed = p1speed > 1 ? p1speed - 1 : 1;
			p3speed = p3speed > 1 ? p3speed - 1 : 1;
			p4speed = p4speed > 1 ? p4speed - 1 : 1;
		}
		else if(pid == 3)
		{
			p1speed = p1speed > 1 ? p1speed - 1 : 1;
			p2speed = p2speed > 1 ? p2speed - 1 : 1;
			p4speed = p4speed > 1 ? p4speed - 1 : 1;
		}
		else if(pid == 4)
		{
			p1speed = p1speed > 1 ? p1speed - 1 : 1;
			p2speed = p2speed > 1 ? p2speed - 1 : 1;
			p3speed = p3speed > 1 ? p3speed - 1 : 1;
		}
	}
	
	public void itemGetHorizontal(int y, int pid)
	{
		int _y = (y >= 15 ? y - 15 : 0) / 42;
		
		sendMessage("8021" + " " + _y + " " + pid);
	}
	
	public void itemGetVertical(int x, int pid)
	{
		int _x = (x >= 15 ? x - 15 : 0) / 46;
		
		sendMessage("8022" + " " + _x + " " + pid);
	}
	
	
	public byte[] makePacket(String msg)
	{
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try
		{
			bb = msg.getBytes("euc-kr");
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	public void sendMessage(String msg)
	{
		try
		{
			byte[] bb;
			bb = makePacket(msg);
			dos.write(bb, 0, bb.length);
		}
		catch (IOException e)
		{
			try
			{
				dos.close();
				dis.close();
				socket.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == chatting_tf)
		{
			if(!chatting_tf.getText().trim().equals(""))
			{
				sendMessage("200" + " [" + username + "] " + chatting_tf.getText() + "\n");
			}
			chatting_tf.setText("");
		}
		else if(e.getSource() == start_btn)
		{
			sendMessage("001");
		}
		background_lb.requestFocus();
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
	

	@Override
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		
		if(key == 10) //엔터
		{
			chatting_tf.requestFocus();
		}
		else if(key == 37 || key == 38 || key == 39 || key == 40) //← ↑ → ↓
		{
			sendMessage("400" + " " + pid + " " + key);
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{

	}
}