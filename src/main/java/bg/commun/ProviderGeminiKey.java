package bg.commun;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ProviderGeminiKey {

	private ProviderGeminiKey() {
		try {
			File fileSecret = new File("secret.properties");		
			Properties pSecret = new Properties();
			FileReader fr = new FileReader(fileSecret);
			pSecret.load(fr);
			keyGemini= pSecret.getProperty("keyGemini");
			passwordACLED=pSecret.getProperty("passwordACLED");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static ProviderGeminiKey instance;
	
	private String keyGemini;
	private String passwordACLED;
	
	public static ProviderGeminiKey getInstance() {
		if (instance== null) {
			instance = new ProviderGeminiKey();
		}
		return instance;
	}

	public String getKeyGemini() {
		return keyGemini;
	}

	public String getPassowordACLED() {
		return passwordACLED;
	}


	
}
