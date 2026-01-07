package bg.gemini.applis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import com.google.gson.annotations.SerializedName;

import bg.gemini.client.GeminiClient;
import bg.gemini.client.GeminiResponse;
import bg.gemini.client.JsonSchema;

public class MainGeminiClientStrikesRequest {

	static File dirResults = new File("strikes");

	// Classe pour mapper chaque attaque dans la réponse JSON
	static class Strike {
		@SerializedName("date")
		private String date;

		@SerializedName("site_name")
		private String siteName;

		@SerializedName("site_type")
		private String siteType;

		@SerializedName("city")
		private String city;

		@SerializedName("region")
		private String region;

		@SerializedName("country")
		private String country;

		@SerializedName("latitude")
		private double latitude;

		@SerializedName("longitude")
		private double longitude;

		@SerializedName("description")
		private String description;

		@SerializedName("sources")
		private String sources;

		// Getters
		public String getDate() {
			return date;
		}

		public String getSiteName() {
			return siteName;
		}

		public String getSiteType() {
			return siteType;
		}

		public String getCity() {
			return city;
		}

		public String getRegion() {
			return region;
		}

		public String getCountry() {
			return country;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public String getDescription() {
			return description;
		}

		public String getSources() {
			return sources;
		}

		@Override
		public String toString() {
			return String.format(
					"Strike{date='%s', site_name='%s', site_type='%s', city='%s', region='%s', country='%s', lat=%.6f, lon=%.6f, description='%s', sources='%s'}",
					date, siteName, siteType, city, region, country, latitude, longitude, description, sources);
		}
	}

	public static void main(String[] args) {
		try {
			// Initialiser le client avec votre clé API
			GeminiClient client = new GeminiClient();

			// Définir le schéma JSON pour un tableau d'attaques
			JsonSchema strikeSchema = new JsonSchema("object")
					.addProperty("date",
							new JsonSchema.PropertySchema("string")
									.setDescription("Date of the strike in ISO format (YYYY-MM-DD)"))
					.addProperty("site_name",
							new JsonSchema.PropertySchema("string").setDescription("Name of the targeted site"))
					.addProperty("site_type",
							new JsonSchema.PropertySchema("string")
									.setDescription("Type of infrastructure (refinery, oil depot, terminal, pipeline)"))
					.addProperty("city",
							new JsonSchema.PropertySchema("string").setDescription("City where the site is located"))
					.addProperty("region", new JsonSchema.PropertySchema("string").setDescription("Region or oblast"))
					.addProperty("country", new JsonSchema.PropertySchema("string").setDescription("Country (Russia)"))
					.addProperty("latitude",
							new JsonSchema.PropertySchema("number").setDescription("Latitude in WGS84 decimal format"))
					.addProperty("longitude",
							new JsonSchema.PropertySchema("number").setDescription("Longitude in WGS84 decimal format"))
					.addProperty("description",
							new JsonSchema.PropertySchema("string")
									.setDescription("Brief description of the strike and damage"))
					.addProperty("sources",
							new JsonSchema.PropertySchema("string").setDescription("Source references for the strike"))
					.setRequired("date", "site_name", "site_type", "city", "region", "country", "latitude", "longitude",
							"description", "sources");

			// Schéma principal : un tableau d'objets Strike
			JsonSchema schema = new JsonSchema("array").setItemsSchema(strikeSchema)
					.setDescription("Array of Ukrainian strikes against Russian oil infrastructure");

			for (Month month : Month.values()) {
				int year = 2025;

				// Créer le prompt
				String prompt = "Provide a comprehensive chronological list of confirmed Ukrainian strikes against Russian oil infrastructure (refineries, oil depots, terminals, pipelines) during the month  "
						+ month + " " + year
						+ ". The data will be used for a spatiotemporal map with a time slider.\n\n"
						+ "Return the result as a valid JSON array. Each object must include:\n"
						+ "date, site_name, site_type, city, region, country, latitude, longitude, description, sources.\n\n"
						+ "Use WGS84 GPS coordinates.\n" + "Do not include any explanatory text outside the JSON.";

				// Envoyer la requête
				System.out.println("Envoi de la requête à Gemini...");
				GeminiResponse response = client.sendRequest(prompt, schema);

				// Afficher la réponse brute
				System.out.println("\n=== Réponse JSON ===");
				System.out.println(response.getText());
				String filename = String.format("strikes_%d_%02d", year, month.getValue());
				saveInFile(response.getText(), filename);

				// Parser la réponse en tableau d'objets Java
				Strike[] strikes = response.parseJson(Strike[].class);
				System.out.println("\n=== Nombre d'attaques trouvées: " + strikes.length + " ===\n");

			}

		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void saveInFile(String textJson, String datesStr) {
		try {
			dirResults.mkdirs();
			File file = new File(dirResults, "strikes_" + datesStr + ".json");
			Files.writeString(file.toPath(), textJson);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
