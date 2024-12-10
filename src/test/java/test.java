import org.junit.Test;
import org.gkdis6.Module;

public class test {

    @Test
    public void testOne() {
        final Module module = new Module("https://test.com","content-key");

        Module.ApiResponse response = module.sendBotNotification("BOT_ID", "TEST_USER", "안녕하세요");

        if(response.success) System.out.println("Notification sent successfully" + response.message);
        else System.out.println("Failed to send notification" + response.message);
    }

}
