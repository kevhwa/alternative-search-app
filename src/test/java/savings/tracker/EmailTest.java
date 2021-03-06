package savings.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;

import savings.tracker.util.SendGridEmailer;

import java.io.IOException;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class EmailTest {
  
  @Test
  @Order(1)
  public void makeEmailTest() {
    Email fromEmail = new Email();
    fromEmail.setName("Savings Tracker Team");
    fromEmail.setEmail("ASE.email.api@gmail.com");
    
    assertEquals(SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00").getFrom(), fromEmail);
  }
  
  @Test
  @Order(2)
  public void sendEmailTest() throws IOException {
    
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertEquals(SendGridEmailer.send(mail, "SG.bxNhOT"), false);
  }
  
  @Test
  @Order(3)
  public void sendEmailInputNoNullTest() throws IOException {
    
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertEquals(SendGridEmailer.send(mail, "SG.bxNhOTUbQM"), false);
  }
  
  @Test
  @Order(4)
  public void sendEmailInputNullTest() throws IOException {
    
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertEquals(SendGridEmailer.send(null, "SG.bxNhOTU"), false);
  }
  
  @Test
  @Order(5)
  public void sendEmailAPIKeyEmptyTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertEquals(SendGridEmailer.send(mail, ""), false);
  }
  
  @Test
  @Order(6)
  public void sendEmailAPIKeyInvalidTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertEquals(SendGridEmailer.send(mail, "123343rgasd"), false);
  }
  
  @Test
  @Order(7)
  public void sendEmailAPIKeyValidTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertEquals(SendGridEmailer.send(mail, "SG.38487ddsaf"), false);
  }
  
  @Test
  @Order(8)
  public void sendEmailAPIKeyLongTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    String longString = "";
    for (int i = 0; i < 51; i++) {
      longString += "a";
    }
    assertEquals(SendGridEmailer.send(mail, longString), false);
  }
  
  @Test
  @Order(9)
  public void buildDynamicTemplateEmailEmptyTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("", "5.00", "6.00");
    assertNull(mail);
  }
  
  @Test
  @Order(10)
  public void buildDynamicTemplateEmailValidTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("ASE.email.api@gmail.com", "5.00", "6.00");
    assertNotNull(mail);
  }
  
  @Test
  @Order(11)
  public void buildDynamicTemplateEmailShortTest() throws IOException {
    Mail mail = SendGridEmailer.buildDynamicTemplate("asdf", "5.00", "6.00");
    assertNull(mail);
  }
  
  @Test
  @Order(12)
  public void buildDynamicTemplateEmailLongTest() throws IOException {
    String longMail = "";
    for (int i = 0; i < 240; i++) {
      longMail += "a";
    }
    longMail += "@mail";
    Mail mail = SendGridEmailer.buildDynamicTemplate(longMail, "5.00", "6.00");
    assertNotNull(mail);
  }
  
  @Test
  @Order(13)
  public void sendDynamicEmailEmailEmptyTest() throws IOException {
    String emptyMail = "";
    assertEquals(SendGridEmailer.sendDynamicEmail(emptyMail,"5.00", "6.00"), false);
  }
  
  @Test
  @Order(14)
  public void sendDynamicEmailEmailValidTest() throws IOException {
    //SendGridEmailer.sendDynamicEmail("123@123.com", "5.00", "6.00");
  }
  
  @Test
  @Order(15)
  public void sendDynamicEmailEmailShortTest() throws IOException {
    String shortMail = "aaaa";
    assertEquals(SendGridEmailer.sendDynamicEmail(shortMail, "5.00", "6.00"), false);
  }
  
  @Test
  @Order(16)
  public void sendDynamicEmailEmailLongTest() throws IOException {
    String longMail = "";
    for (int i = 0; i < 254; i++) {
      longMail += "a";
    }
    assertEquals(SendGridEmailer.sendDynamicEmail(longMail, "5.00", "6.00"), false);
  }
}
