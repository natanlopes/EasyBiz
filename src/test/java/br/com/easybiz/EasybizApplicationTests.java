package br.com.easybiz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
@SpringBootTest
class EasybizApplicationTests {
	@SuppressWarnings("unused")
	@MockitoBean
	private JavaMailSender javaMailSender;
	@Test
	void contextLoads() {
	}

}
