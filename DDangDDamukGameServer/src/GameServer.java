import java.util.*;
import java.util.Timer;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class GameServer extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextArea textArea;
	private JTextField txtPortNumber;
	
	private static final int BUF_LEN = 128;
	private ServerSocket socket;
	private Socket client_socket;
	private Vector users = new Vector();

	int usercnt = 0;
	int time = 60;
	String timestr;
	
	int i;
	int j;
	int x;
	int y;
	
	int p1pos = 39;
	int p2pos = 40;
	int p3pos = 38;
	int p4pos = 37;
	
	private int[][] map = new int[720][660]; //캐릭터들의 위치 좌표 정보 및 점령(임시) 상태
	private int[][] inland = new int[15][15]; //유저들이 점령한 땅을 관리
	
	private int[] item = {6, 7, 8, 9};
	//6 = 속도 증가
	//7 = 다른 유저 정지
	//8 = x축 1줄 점령
	//9 = y축 1줄 점령
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					GameServer frame = new GameServer();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public GameServer()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 386);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 244);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(12, 264, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(111, 264, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				}
				catch (NumberFormatException | IOException e1)
				{
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false);
				txtPortNumber.setEnabled(false);
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 300, 300, 35);
		contentPane.add(btnServerStart);
	}

	public void AppendText(String str)
	{
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
	}
	
	
	class AcceptServer extends Thread
	{
		@SuppressWarnings("unchecked")
		public void run()
		{
			while (true)
			{
				try
				{
					AppendText("Waiting clients ...");
					client_socket = socket.accept();
					AppendText("새로운 참가자 from " + client_socket);

					UserService new_user = new UserService(client_socket);
					users.add(new_user);
					AppendText("사용자 입장. 현재 참가자 수 " + users.size());
					new_user.start();
					
					usercnt = users.size();
					
					new_user.login();
				}
				catch (IOException e)
				{
					AppendText("!!!! accept 에러 발생... !!!!");
				}
			}
		}
	}

	//--------------------------------------------------------------------------------------------------
	//                                    * * * 유저 서비스 시작 * * *
	//--------------------------------------------------------------------------------------------------
	class UserService extends Thread
	{
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket client_socket;
		private Vector user_vc;
		private String UserName = "";
		private int pid;

		public UserService(Socket client_socket)
		{
			this.client_socket = client_socket;
			this.user_vc = users;
			try
			{
				is = client_socket.getInputStream();
				dis = new DataInputStream(is);
				os = client_socket.getOutputStream();
				dos = new DataOutputStream(os);
				byte[] b = new byte[BUF_LEN];
				dis.read(b);
				String line1 = new String(b);
				String[] msg = line1.split(" ");
				UserName = msg[1].trim();
				
				pid = ++usercnt;
				WriteOne("100" + " " + pid);
			}
			catch (Exception e)
			{
				AppendText("userService error");
			}
		}
		
		public void run()
		{
			while (true)
			{
				try
				{
					//--------------------------------------------------------------------------------------------------
					//                                * * * 클라이언트 메세지 수신 시작 * * *
					//--------------------------------------------------------------------------------------------------
					byte[] b = new byte[BUF_LEN];
					int ret;
					ret = dis.read(b);
					if (ret < 0)
					{
						logout();
						break;
					}
					
					String msg = new String(b, "euc-kr");
					msg = msg.trim();
					
					if(!msg.equals(""))
					{
						String[] msgarr = msg.split(" ", 2);
						int prtc = Integer.parseInt(msgarr[0]); //프로토콜
						String str = msgarr.length >= 2 ? msgarr[1] : ""; //프로토콜을 제외한 내용
						
						if(prtc == 001) //게임 시작
						{
							WriteAll("001");
							gameTimer();
						}
						else if(prtc == 200) //채팅 메세지
						{
							WriteAll("200" + " " + str + "\n"); // Write All
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
							
							WriteAll("400" + " " + _pid + " " + _pos);
						}
						else if(prtc == 500) //x y 좌표값
						{			
							String[] cmd = str.split(" ");
							int _oldx = Integer.parseInt(cmd[0]);
							int _oldy = Integer.parseInt(cmd[1]);
							int _newx = Integer.parseInt(cmd[2]);
							int _newy = Integer.parseInt(cmd[3]);
							int _pid = Integer.parseInt(cmd[4]);
							
							WriteAll("500" + " " + _oldx + " " + _oldy + " " + _newx + " " + _newy + " " + _pid);
							
							if(_pid == 1)
							{
								getMap(p1pos, _oldx, _oldy, _newx, _newy, _pid);
							}
							else if(_pid == 2)
							{
								getMap(p2pos, _oldx, _oldy, _newx, _newy, _pid);
							}
							else if(_pid == 3)
							{
								getMap(p3pos, _oldx, _oldy, _newx, _newy, _pid);
							}
							else if(_pid == 4)
							{
								getMap(p4pos, _oldx, _oldy, _newx, _newy, _pid);
							}
							
							getLand(_pid);
						}
						else if(prtc == 802) //아이템 획득
						{
							String[] cmd = str.split(" ");
							int _item_code = Integer.parseInt(cmd[0]);
							int _item_index = Integer.parseInt(cmd[1]);
							int _x = Integer.parseInt(cmd[2]);
							int _y = Integer.parseInt(cmd[3]);
							int _pid = Integer.parseInt(cmd[4]);
							String _username = cmd[5];
							String _itemname = "";
							
							AppendText("802" + " " + _item_code + " " + _item_index + " " + _x + " " + _y + " " +_pid);
							WriteAll("802" + " " + _item_code + " " + _item_index + " " + _x + " " + _y + " " +_pid);
							
							if(_item_code == 6)
							{
								_itemname = "속도 증가";
							}
							else if(_item_code == 7)
							{
								_itemname = "속도 감소";
							}
							else if(_item_code == 8)
							{
								_itemname = "가로축 1줄 점령";
							}
							else if(_item_code == 9)
							{
								_itemname = "세로축 1줄 점령";
							}
							WriteAll("200" + " " + "[system] " + _username + "님이 " + _itemname + " 아이템 획득!" + "\n");
						}
						else if(prtc == 8021) //x축 1줄 점령 획득
						{
							String[] cmd = str.split(" ");
							int _x = 0;
							int _y = Integer.parseInt(cmd[0]);
							int _pid = Integer.parseInt(cmd[1]);
							
							for(int _i = 15; _i <= 705; _i++)
							{
								for(int _j = _y * 42 + 15; _j <= _y * 42 + 15 + 42; _j++)
								{
									map[_i][_j] = _pid;
								}
							}
							
							for(_x = 0; _x < 15; _x++)
							{
								WriteAll("600" + " " + _x + " " + _y + " " + _pid);
								inland[_x][_y] = _pid;
							}
						}
						else if(prtc == 8022) //y축 1줄 점령 획득
						{
							String[] cmd = str.split(" ");
							int _x = Integer.parseInt(cmd[0]);
							int _y = 0;
							int _pid = Integer.parseInt(cmd[1]);
							
							for(int _i = _x * 46 + 15; _i <= _x * 46 + 15 + 46; _i++)
							{
								for(int _j = 15; _j <= 645; _j++)
								{
									map[_i][_j] = _pid;
								}
							}
							
							for(_y = 0; _y < 15; _y++)
							{
								WriteAll("600" + " " + _x + " " + _y + " " + _pid);
								inland[_x][_y] = _pid;
							}
						}
					}
					//--------------------------------------------------------------------------------------------------
					//                                * * * 클라이언트 메세지 수신 종료 * * *
					//--------------------------------------------------------------------------------------------------
				}
				catch (IOException e)
				{
					logout();
					break;
				}
			}
		}
		
		public void gameTimer()
		{		
			Timer timer = new Timer();
			
			TimerTask game_timer = new TimerTask()
			{
				@Override
				public void run()
				{
					time--;
					timestr = time + "";
					
					if(time >= 10)
					{
						WriteAll("700" + " " + "0 0 : " + timestr.charAt(0) + " " + timestr.charAt(1));
					}
					else
					{
						WriteAll("700" + " " + "0 0 : 0 " + timestr.charAt(0));
					}
										
					if(time == 0)
					{
						WriteAll("002");
						
						timer.cancel();
					}
					
					if(time % 10 == 0 && time > 0) //50, 40, 30, 20, 10초에 아이템 생성
					{
						createItem(time);
					}
				}
			};
			timer.schedule(game_timer, 0, 1000);
		}
		
		public void createItem(int time)
		{
			int _x = (int)(Math.random() * 659 + 16); //16 ~ 674
			int _y = (int)(Math.random() * 559 + 16); //16 ~ 614
			int _item = (int)(Math.random() * 4 + 6); //6 ~ 9
			
			WriteAll("801" + " " + _x + " " + _y + " " + _item);
		}
		
		public synchronized void getMap(int pos, int oldx, int oldy, int newx, int newy, int pid)
		{
			if(pos == 37)
			{
				for(i = newx; i <= newx + 30; i++)
				{
					for(j = newy; j <= newy + 30; j++)
					{
						map[i][j] = pid;
					}
				}
			}
			if(pos == 39)
			{
				for(i = newx; i <= newx + 30; i++)
				{
					for(j = newy; j <= newy + 30; j++)
					{
						map[i][j] = pid;
					}
				}
			}
			if(pos == 38)
			{
				for(i = newx; i <= newx + 30; i++)
				{
					for(j = newy; j <= newy + 30; j++)
					{
						map[i][j] = pid;
					}
				}
			}
			if(pos == 40)
			{
				for(i = newx; i <= newx + 30; i++)
				{
					for(j = newy; j <= newy + 30; j++)
					{
						map[i][j] = pid;
					}
				}
			}
		}
		
		public synchronized void getLand(int pid)
		{
			int x; //0 <= x <= 14
			int y; //0 <= y <= 14
			int tmppid = 0;
			Boolean flag = true;
			int i;
			int j;
			
			for(x = 0; x <= 14; x++)
			{
				for(y = 0; y <= 14; y++)
				{
					for(i = 0; i <= 46; i++)
					{
						for(j = 0; j <= 42; j++)
						{
							if(i == 0 && j == 0)
							{
								tmppid = map[i + 15 + x * 46][j + 15 + y * 42];
							}
							else
							{
								if(tmppid != map[i + 15 + x * 46][j + 15 + y * 42] || tmppid == 5 || tmppid == 0)
								{
									flag = false;
								}
							}
							
							if(!flag)
							{
								break;
							}
						}
						if(!flag)
						{
							break;
						}
					}
					if(flag)
					{
						if(tmppid != inland[x][y] && tmppid == pid)
						{
							WriteAll("600" + " " + x + " " + y + " " + tmppid);
							inland[x][y] = tmppid;
						}
					}
					
					flag = true;
				}
			}
		}
		
		public byte[] MakePacket(String msg)
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
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		public void WriteOne(String msg)
		{
			try
			{
				byte[] bb;
				bb = MakePacket(msg);
				
				dos.write(bb, 0, bb.length);
			}
			catch (IOException e)
			{
				AppendText("dos.write() error");
				try
				{
					dos.close();
					dis.close();
					client_socket.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				users.removeElement(this);
				AppendText("사용자 퇴장. 현재 참가자 수 " + users.size());
			}
		}
		
		public void WriteAll(String str)
		{
			for (int i = 0; i < user_vc.size(); i++)
			{
				UserService user = (UserService)user_vc.elementAt(i);
				user.WriteOne(str);
			}
		}
		
		public void login()
		{
			AppendText("새로운 참가자 " + UserName + " 입장.");
			
			WriteAll("200" + " " + "[system] " + UserName + "님이 입장하셨습니다.\n");
			
			for (int i = 0; i < users.size(); i++)
			{
				UserService user = (UserService) users.elementAt(i);
				
				WriteAll("101" + " " + user.UserName + " " + user.pid);
			}
		}
		
		public void logout()
		{
			WriteAll("9999 "); //이걸 왜 넣어놨는지 기억이 잘 안나는데, 아무튼 이걸 안넣으면 뭔가 잘 안됏던 것 같다.
			WriteAll("200" + " " + "[system] " + UserName + "님이 퇴장하셨습니다.\n");
			
			for (int i = 0; i < users.size(); i++)
			{
				UserService user = (UserService) users.elementAt(i);
				
				WriteAll("102" + " " + user.UserName + " " + (user.pid + 1));
			}
			usercnt--;
			
			AppendText("dis.read() error");
			try
			{
				dos.close();
				dis.close();
				client_socket.close();
				users.removeElement(this);
				AppendText("사용자 퇴장. 남은 참가자 수 " + users.size());
			}
			catch (Exception ee)
			{
				ee.printStackTrace();
			}
		}
	}
	//--------------------------------------------------------------------------------------------------
	//                                    * * * 유저 서비스 종료 * * *
	//--------------------------------------------------------------------------------------------------
}
