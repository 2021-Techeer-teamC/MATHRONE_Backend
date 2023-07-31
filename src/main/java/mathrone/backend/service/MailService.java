package mathrone.backend.service;

import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import mathrone.backend.domain.UserInfo;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service

@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendId(UserInfo user) {
        String subject = "Mathrone 아이디 찾기";
        String content = "당신의 아이디는? " + user.getAccountId();
        String to = user.getEmail();
        String id = user.getAccountId();

        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper mailHelper = new MimeMessageHelper(mail, true, "UTF-8");

            // 1. 메일 수신자 설정
            mailHelper.setTo(to);
            mailHelper.setSubject(subject);  // 메일 제목 설정
            mailHelper.setText(content, true);   // 메일 내용 설정

            // 4. 메일 전송
            javaMailSender.send(mail);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            MimeMessageHelper mailHelper = new MimeMessageHelper(mail, true, "UTF-8");

            // 1. 메일 수신자 설정
            mailHelper.setTo(to);
            mailHelper.setSubject(subject);  // 메일 제목 설정
            mailHelper.setText(content, true);   // 메일 내용 설정

            // 4. 메일 전송
            javaMailSender.send(mail);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
