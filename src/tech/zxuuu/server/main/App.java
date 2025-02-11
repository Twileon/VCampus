package tech.zxuuu.server.main;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import tech.zxuuu.dao.IStudentMapper;
import tech.zxuuu.entity.Student;
import tech.zxuuu.net.RequestListener;
import tech.zxuuu.server.messageQueue.RequestHandler;
import tech.zxuuu.server.messageQueue.RequestQueue;
import tech.zxuuu.util.ServerUtils;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;

/**
 * 服务器端全局App对象
 * 
 * @author z0gSh1u
 */
public class App extends JFrame {

	private JPanel contentPane;

	private RequestListener requestListener; // 请求监听器
	public static RequestQueue requestQueue; // 服务器端全局请求消息队列
	public static RequestHandler requestHandler; // 请求处理器
	public static SqlSessionFactory sqlSessionFactory; // MyBatis连接工厂

	public static JTextPane paneLog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					App frame = new App();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the frame.
	 */
	public App() {
		setResizable(false);
		setTitle("服务器端 - VCampus");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 718, 493);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(null);

		JLabel label = new JLabel("服务器日志");
		label.setBounds(302, 13, 75, 18);
		panel_1.add(label);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(14, 38, 662, 385);
		panel_1.add(scrollPane);
		App.paneLog = new JTextPane();
		App.paneLog.setEditable(false);
		scrollPane.setViewportView(paneLog);
		App.paneLog.setFont(new Font("宋体", Font.PLAIN, 14));
		/**
		 * 新增部分
		 */
		// 初始化全局消息队列
		App.requestQueue = RequestQueue.getInstance();
		// 初始化MyBatis的SqlSession工厂
		String resource = "resources/mybatis-config.xml";
		InputStream inputStream;
		try {
			inputStream = Resources.getResourceAsStream(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			App.paneLog.setText("数据库配置读取成功！");
		} catch (IOException e) {
			App.paneLog.setText("严重错误！数据库配置读取失败！" + e.toString());
			e.printStackTrace();
		}
		// 尝试连接数据库
		try {
			SqlSession sqlSession = App.sqlSessionFactory.openSession();
			IStudentMapper studentMapper = sqlSession.getMapper(IStudentMapper.class);
			Boolean verifyResult = studentMapper.verifyStudent(new Student("0", null, "0", "0"));
			sqlSession.commit();
			App.paneLog.setText("数据库连接成功！");
		} catch (Exception e) {
			App.paneLog.setText("严重错误！数据库连接失败！请检查有关配置！");
			e.printStackTrace();
		}
		// 启动服务器端侦听
		requestListener = new RequestListener(Integer.parseInt(ServerUtils.getMainPort()));
		requestListener.start();
		// 启动请求处理器
		App.requestHandler = new RequestHandler();
		App.requestHandler.start();
		App.paneLog.setText(
				paneLog.getText() + (paneLog.getText().equals("") ? "" : "\n") + "开始服务器端侦听...端口=" + ServerUtils.getMainPort());
		;

	}

	public static void appendLog(String msg) {
		App.paneLog.setText(App.paneLog.getText() + "\n" + msg);
	}

}
