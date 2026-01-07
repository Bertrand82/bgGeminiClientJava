package bg.gemini.model;

public class MainProviderList {

	public static void main(String[] args) {
		ProviderListGeminiModels pms = ProviderListGeminiModels.getInstance();
		pms.listModel.forEach(m -> {
			System.out.println(m);
			System.out.println("--------------------");
		});
	}
}
