import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.web.util.UriUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StockRetriever {

	public static void main(String[] args) throws IOException {
		String symbol = "GOOGL";
		String start = "2013-01-01";
		String end = "2014-01-01";
		new StockRetriever().retrieveStockData(symbol, start, end);
	}

	private void retrieveStockData(String symbol, String start, String end) throws IOException {
		URL url = buildUrl(symbol, start, end);

		InputStream inputStream = (InputStream) url.getContent();

		String content = IOUtils.toString(inputStream);

		JsonParser parser = new JsonParser();

		JsonObject root = parser.parse(content).getAsJsonObject();
		JsonObject queryJson = root.getAsJsonObject("query");
		int count = queryJson.getAsJsonPrimitive("count").getAsInt();

		if (count > 0) {
			JsonObject resultsJson = queryJson.getAsJsonObject("results");
			JsonArray rowJson = resultsJson.getAsJsonArray("quote");

			System.out.print("[");
			for (int i = 1; i < count; i++) {
				JsonObject row = rowJson.get(i).getAsJsonObject();
				printRow(row, i, count - 1);
			}
			System.out.print("]");
		} else {
			System.out.println("zero results :(");
		}
	}

	private URL buildUrl(String symbol, String start, String end) throws UnsupportedEncodingException, MalformedURLException {
		// @formatter:off
		String queryParam = "select * from yahoo.finance.historicaldata  " +
							"where symbol='" + symbol + "' " + 
							"and startDate = '"+start+"' " +
							"and endDate= '"+end+"'" ;

		String query = UriUtils.encodeQuery(queryParam, "UTF-8");

		String stringUrl = 	"https://query.yahooapis.com/v1/public/yql" +
							"?q=" + query + "" +
							"&format=json" +
							"&diagnostics=true" +
							"&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys" +
							"&callback=";
		// @formatter:on
		return new URL(stringUrl);
	}

	private void printRow(JsonObject row, Integer index, Integer max) {
		Double high = row.get("High").getAsDouble();
		Double low = row.get("Low").getAsDouble();
		Double open = row.get("Open").getAsDouble();
		Double close = row.get("Close").getAsDouble();
		String date = row.get("Date").getAsString();
		System.out.println("{");
		System.out.println("\t\"remainingDays\": \"" + (max - index) + "\",");
		System.out.println("\t\"open\": \"" + open + "\",");
		System.out.println("\t\"close\": \"" + close + "\",");
		System.out.println("\t\"high\": \"" + high + "\",");
		System.out.println("\t\"low\": \"" + low + "\",");
		System.out.println("\t\"day\": \"" + index + "\",");
		System.out.println("\t\"date\": \"" + date + "\"");
		System.out.print("}");
		if (index < max) {
			System.out.println(",");
		}
	}
}
