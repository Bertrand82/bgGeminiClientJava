package bg.gdelt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class GDELTRussiaRefineryFires {
	
	private static File dirTarget = new File("target");
	private static File dirOut = new File(dirTarget,"OUT_GDELT");
	

	public static void main(String[] args) {
		try {
			System.out.println("Recherche des raffineries en feu en Russie (3 derniers jours)\n");

			// Requête optimisée pour la Russie en CSV
			String query = "Russia AND (\"oil refinery\" OR \"fuel depot\" OR \"oil depot\" OR petroleum) AND (fire OR explosion OR \"drone attack\" OR \"missile strike\")";
			// STARTDATETIME. Specify the precise date/time in YYYYMMDDHHMMSS format to begin the search – only articles published after this date/time stamp will be considered. It must be within the last 3 months. If you do not specify an ENDDATETIME, the API will search from STARTDATETIME through the present date/time.
			// ENDDATETIME. Specify the precise date/time in YYYYMMDDHHMMSS format to end the search – only articles published before this date/time stamp will be considered. It must be within the last 3 months. If you do not specify a STARTDATETIME, the API will search from 3 months ago through the specified ENDDATETIME
			
			for (int week=40;week <= 53;week++) {
				LocalDateTime localDateTime = getLocalDate(2025, week);
				process(query, localDateTime);
				 try {
		                Thread.sleep(10* 1000);  // 10 seconde entre chaque requête
		            } catch (InterruptedException e) {
		                Thread.currentThread().interrupt();
		            }
			}
			

			// Convertir la liste d'objets en JSON et sauvegarder

		} catch (Exception e) {
			System.err.println("❌ Erreur:  " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void process(String query, LocalDateTime localDateTime) throws Exception {

		String urlString = buildUrlWithDates_2_7days(query, localDateTime, 250);
		System.out.println(toString0("date :",20)+localDateTime.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
		System.out.println(toString0(" URL: ",20) + urlString);

		// Exécuter la requête CSV
		String csvResponse = executeRequest(urlString);

		if (csvResponse != null) {
			
			// saveCsvToFile(csvResponse);

			// Parser le CSV en objets
			List<RefineryFireEvent> events = parseCsvToObjects(csvResponse);
			String json = convertToJson(events);
			saveJsonToFile(json, localDateTime);
			System.out.println(toString0("Nombre d'événements: " ,20)+ events.size() + "\n");
		} else {
			System.out.println("❌ Aucune donnée CSV reçue\n");
		}

	}

	public static LocalDateTime getLocalDate(int year, int weekNumber) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Lundi = début de semaine

		LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
		return localDateTime;
	}

	private static final DateTimeFormatter GDELT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private static String buildUrlWithDates_2_7days(String query, LocalDateTime startDate, int maxRows) {
		try {
			LocalDateTime endDate = startDate.plusDays(7);
			String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
			String startDateStr = startDate.format(GDELT_DATE_FORMAT);
			String endDateStr = endDate.format(GDELT_DATE_FORMAT);

			return "https://api.gdeltproject.org/api/v2/geo/geo" + "?query=" + encodedQuery + "&mode=PointChart"
					+ "&format=csv" + "&STARTDATETIME=" + startDateStr + "&ENDDATETIME=" + endDateStr + "&maxrows="
					+ maxRows;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Construit l'URL de la requête GDELT GEO API en CSV
	 */
	private static String buildUrlWithDates_1(String query, String timespan, int maxrows) {
		try {
			String encodedQuery = java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);

			return "https://api.gdeltproject.org/api/v2/geo/geo" + "?query=" + encodedQuery + "&mode=pointdata" + // &mode=PointChart
			// "&mode=pointdata" + //&mode=PointChart
					"&format=csv" + "&timespan=" + timespan + "&maxrows=" + maxrows;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Exécute une requête HTTP GET
	 */
	private static String executeRequest(String urlString) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
			conn.setRequestProperty("Accept", "text/csv, */*");
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);

			int responseCode = conn.getResponseCode();
			String contentType = conn.getContentType();

			

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

				StringBuilder response = new StringBuilder();
				String line;

				while ((line = in.readLine()) != null) {
					response.append(line).append("\n");
				}
				in.close();

				return response.toString();

			} else {
				System.err.println("❌ Erreur HTTP: " + responseCode);
				return null;
			}

		} catch (Exception e) {
			System.err.println("❌ Exception: " + e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * Parse le CSV et crée une liste d'objets RefineryFireEvent
	 */
	private static List<RefineryFireEvent> parseCsvToObjects(String csv) {
		List<RefineryFireEvent> events = new ArrayList<>();

		try {
			String[] lines = csv.split("\n");

			if (lines.length <= 1) {
				System.out.println("⚠️  CSV vide ou sans données");
				return events;
			}

			// Ligne d'en-tête
			String[] headers = lines[0].split(",");

			//System.out.println(toString0("En-têtes CSV:  ") + String.join(", ", headers) );

			// Parser chaque ligne de données
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i].trim();

				if (line.isEmpty()) {
					continue;
				}

				String[] values = parseCsvLine(line);

				// Créer un objet RefineryFireEvent
				RefineryFireEvent event = new RefineryFireEvent();
				event.csv = line;
				// ﻿Location,LocationResultCount,Latitude,Longitude,URL,ImageURL,Title
				// Mapper les colonnes aux propriétés de l'objet
				for (int j = 0; j < Math.min(headers.length, values.length); j++) {
					String header = headers[j].trim().toLowerCase();
					String header2 = header.replaceAll("[^a-zA-Z0-9 ]", "");
					String value = values[j].trim();
					// ﻿Location,LocationResultCount,Latitude,Longitude,URL,ImageURL,Title

					if (header2.equalsIgnoreCase("location")) {
						event.locationName = value;
					} else if (header2.equalsIgnoreCase("locationResultCount")) {
						event.locationResultCount = value;
					} else if (header2.equalsIgnoreCase("url")) {
						event.url = value;
					} else if (header2.equalsIgnoreCase("imageUrl")) {
						event.imageURL = value;
					} else if (header2.equalsIgnoreCase("title")) {
						event.title = value;
					} else if (header2.equalsIgnoreCase("latitude")) {

						try {
							event.latitude = Double.parseDouble(value);
						} catch (NumberFormatException e) {
							event.latitude = 0.0;
						}

					} else if (header2.equalsIgnoreCase("longitude")) {

						try {
							event.longitude = Double.parseDouble(value);
						} catch (NumberFormatException e) {
							event.longitude = 0.0;
						}
					}

				}

				// Générer le lien Google Maps

				events.add(event);
			}

		} catch (Exception e) {
			System.err.println("❌ Erreur parsing CSV: " + e.getMessage());
			e.printStackTrace();
		}

		return events;
	}

	/**
	 * Parse une ligne CSV en gérant les guillemets et virgules
	 */
	public static String[] parseCsvLine(String line) {
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (c == '"') {
				// Toggle état guillemets
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				// Virgule hors guillemets = séparateur
				result.add(current.toString().trim());
				current = new StringBuilder();
			} else {
				// Caractère normal
				current.append(c);
			}
		}

		// Ajouter le dernier élément
		result.add(current.toString().trim());

		// Nettoyer les guillemets
		for (int i = 0; i < result.size(); i++) {
			String value = result.get(i);
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1);
			}
			result.set(i, value);
		}

		return result.toArray(new String[0]);
	}

	/**
	 * Échappe les caractères spéciaux JSON
	 */
	private static String escapeJson(String str) {
		if (str == null)
			return "";
		return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}

	/**
	 * Formate une date GDELT (YYYYMMDD) en format lisible
	 */
	private static String formatDate(String gdeltDate) {
		try {
			if (gdeltDate.length() >= 8) {
				String year = gdeltDate.substring(0, 4);
				String month = gdeltDate.substring(4, 6);
				String day = gdeltDate.substring(6, 8);
				return day + "/" + month + "/" + year;
			}
			return gdeltDate;
		} catch (Exception e) {
			return gdeltDate;
		}
	}

	static {
		dirOut.mkdirs();
	}

	/**
	 * Sauvegarde le CSV dans un fichier
	 */
	private static String saveCsvToFile_(String csv) {
		try {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String filename = "gdelt_russia_refineries_" + timestamp + ".csv";
			File file = new File(dirOut, filename);
			FileWriter writer = new FileWriter(file);
			writer.write(csv);
			writer.close();

			System.out.println("💾 CSV sauvegardé dans: " + filename);
			return filename;
		} catch (Exception e) {
			System.err.println("❌ Erreur sauvegarde CSV: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Sauvegarde le JSON dans un fichier
	 */

	private static String saveJsonToFile(String json, LocalDateTime date) {
		try {
			String timestamp = date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
			String filename = "gdelt_russia_refineries_" + timestamp + ".json";
			File file = new File(dirOut, filename);
			FileWriter writer = new FileWriter(file);
			writer.write(json);
			writer.close();

			System.out.println(toString0("json sauvegardé dans: " )+ filename);
			return filename;
		} catch (Exception e) {
			System.err.println("❌ Erreur sauvegarde CSV: " + e.getMessage());
			return null;
		}
	}

	public static String convertToJson(List<RefineryFireEvent> events) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			// Configuration pour un JSON lisible
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			// Convertir en JSON
			return mapper.writeValueAsString(events);

		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	private static String saveJsonToFile__old(String json) {
		try {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String filename = "gdelt_russia_refineries_" + timestamp + ".json";
			File file = new File(dirOut, filename);
			FileWriter writer = new FileWriter(file);
			writer.write(json);
			writer.close();

			System.out.println("💾 JSON sauvegardé dans: " + filename);
			return filename;
		} catch (Exception e) {
			System.err.println("❌ Erreur sauvegarde JSON:  " + e.getMessage());
			return null;
		}
	}

	/**
	 * Classe représentant un événement d'incendie de raffinerie
	 */
	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	static class RefineryFireEvent {
		String locationName;
		String locationResultCount;
		double latitude = 0.0;
		double longitude = 0.0;
		String date;
		String url;
		String imageURL;
		String title;
		String csv;

		@Override
		public String toString() {
			try {
				String safeCsv = safe(csv);
				String safeLocationName = safe(locationName);
				String safeLocationResultCount = safe(locationResultCount);
				String safeTitle = safe(title);
				String safeUrl = safe(url);

				return "RefineryFireEvent [locationName=" + toString0(safeLocationName, 80) + ", locationResultCount="
						+ safeLocationResultCount + ", latitude=" + String.format("%06.2f", latitude) + ", longitude="
						+ String.format("%06.2f", longitude) + ", date=" + date + ", url=" + safeUrl + ", imageURL="
						+ imageURL + ", title=" + safeTitle + "]                                :::: csv : " + safeCsv;
			} catch (Exception e) {
				e.printStackTrace();
				return "RefineryFireEvent [locationName=" + toString0(locationName, 80) + ", locationResultCount="
						+ locationResultCount + ", latitude=" + latitude + ", longitude=" + longitude + ", date=" + date
						+ ", url=" + url + ", imageURL=" + imageURL + ", title=" + title + "] ::::: csv :::" + csv;
			}
		}

		/**
		 * Échappe les % pour éviter les erreurs de format
		 */
		private static String safe(String s) {
			if (s == null)
				return "null";
			return s.replace("%", "%%");
		}

	}

	static String toString0(Object o) {
		return toString0(o,20);
	}
	static String toString0(Object o, int l) {

		String s = "" + o;
		while (s.length() < l) {
			s += " ";
		}
		return s;
	}
}
