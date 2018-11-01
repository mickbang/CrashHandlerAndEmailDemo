package com.lq.email;

import java.io.IOException;
import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.os.AsyncTask;

/**
 * 邮件管理类
 * 使用的发送者邮件，必须在邮箱设置允许smtp功能
 */
public class MailManager {

    /**
     * 发送者邮箱密码
     */
    // 发件人的 邮箱 和 密码（替换为自己的邮箱和密码）
    // PS: 某些邮箱服务器为了增加邮箱本身密码的安全性，给 SMTP 客户端设置了独立密码（有的邮箱称为“授权码”）, 
    //     对于开启了独立密码的邮箱, 这里的邮箱密码必需使用这个独立密码（授权码）。
    private static final String MAIL_FROM = "zhanglei349@126.com";
    private static final String MAIL_FROM_PWD = "zhanglei349";
    /**
     * 接收者账户
     */
    private static final String MAIL_TO1 = "xxx@qq.com";
    private static final String MAIL_TO2 = "mickbang_offical@qq.com";

    /**
     * QQ邮箱服务器
     */
    private static final String VALUE_MAIL_HOST_QQ = "smtp.126.com";

    public static MailManager getInstance() {
        return InstanceHolder.instance;
    }

    private MailManager() {
    }

    private static class InstanceHolder {
        private static MailManager instance = new MailManager();
    }

    class MailTask extends AsyncTask {

        private MimeMessage mimeMessage;

        public MailTask(MimeMessage mimeMessage) {
            this.mimeMessage = mimeMessage;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                /*
                 *发送附件时会报异常：
                    javax.activation.UnsupportedDataTypeException: no object DCH for MIME type multipart/related;
                    解决方法是就是在sendmail前，加一段代码：
                    参考 ：http://blog.csdn.net/chinafe/article/details/7183400
                 */
                // add handlers for main MIME types
                MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
                CommandMap.setDefaultCommandMap(mc);


                Transport.send(mimeMessage);
                return Boolean.TRUE;
            } catch (MessagingException e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }
    }

    /**
     * 不带附件的邮件
     *
     * @param title
     * @param content
     */
    public void sendMail(final String title, final String content) {
        MimeMessage mimeMessage = createMessage(title, content);
        MailTask mailTask = new MailTask(mimeMessage);
        mailTask.execute();
    }

    /**
     * 带附件的邮件
     *
     * @param title
     * @param content
     * @param filePath
     */
    public void sendMailWithFile(String title, String content, String filePath) {
        MimeMessage mimeMessage = createMessage(title, content);
        appendFile(mimeMessage, filePath);
        MailTask mailTask = new MailTask(mimeMessage);
        mailTask.execute();
    }

    /**
     * 多个附件
     *
     * @param title
     * @param content
     * @param pathList
     */
    public void sendMailWithMultiFile(String title, String content, List pathList) {
        MimeMessage mimeMessage = createMessage(title, content);
        appendMultiFile(mimeMessage, pathList);
        MailTask mailTask = new MailTask(mimeMessage);
        mailTask.execute();
    }

    private Authenticator getAuthenticator() {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_FROM, MAIL_FROM_PWD);
            }
        };
    }

    private MimeMessage createMessage(String title, String content) {
        //设置发送的属性
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");//向SMTP服务器提交用户认证  
        properties.put("mail.transport.protocol", "smtp");//指定发送邮件协议  
        properties.put("mail.host", VALUE_MAIL_HOST_QQ);//SMTP服务器主机地址

        // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
        //     如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
        //     打开下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。

        // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
        //                  需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
        //                  QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)

        //获取Session
        Session session = Session.getDefaultInstance(properties, getAuthenticator());
        session.setDebug(true);   // 设置为debug模式, 可以查看详细的发送 log
        //创建消息
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            //设置发送者
            mimeMessage.setFrom(new InternetAddress(MAIL_FROM));
            //设置接收者（可以增加多个收件人、抄送、密送）
            InternetAddress[] addresses = new InternetAddress[]{
//                    new InternetAddress(MAIL_TO1),
                    new InternetAddress(MAIL_TO2)};
            mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
            //设置邮件的主题
            mimeMessage.setSubject(title);
            //设置邮件的内容
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(content, "text/html");
            textPart.setText(content, "UTF-8");
            Multipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(textPart);
            mimeMessage.setContent(multipart);
            //设置发送时间
            mimeMessage.setSentDate(new Date());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return mimeMessage;
    }

    /**
     * 添加单个附件
     *
     * @param message
     * @param filePath
     */
    private void appendFile(MimeMessage message, String filePath) {
        try {
            Multipart multipart = (Multipart) message.getContent();
            MimeBodyPart filePart = new MimeBodyPart();
            filePart.attachFile(filePath);
            multipart.addBodyPart(filePart);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加多个附件
     *
     * @param message
     * @param pathList
     */
    private void appendMultiFile(MimeMessage message, List<String> pathList) {
        try {
            Multipart multipart = (Multipart) message.getContent();
            for (String path : pathList) {
                MimeBodyPart filePart = new MimeBodyPart();
                filePart.attachFile(path);
                multipart.addBodyPart(filePart);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}


//private void sendEMail() {
//		/*
//		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "HuyuLog";
//		ArrayList res = new ArrayList();
//		File dir = new File(path);
//		if(dir.exists()){
//			res.add(dir);
//		}
//		if(!res.isEmpty()){
//			String zipFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
//					+ "HuyuLog.zip";
//			File zipFile = new File(zipFilePath);
//			if(zipFile.exists())
//				zipFile.delete();
//
//			ZipUtils.zipFiles(res, zipFile, new ZipUtils.ZipListener() {
//
//				@Override
//				public void zipProgress(int zipProgress) {
//
//				}
//			});
//			MailManager.getInstance().sendMailWithFile("错误日志", "看附件", zipFilePath);
//			return;
//		}
//		*/
//        MailManager.getInstance().sendMail("标题：错误日志", "正文内容：没有附件:" + new Date());
//    }
