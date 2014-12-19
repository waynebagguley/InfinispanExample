package infinispan_example.data_cache;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An example of how to query an Infinispan cache.
 */
public class App {

	public static void main(String[] args) throws ParseException {
		CacheManager manager = new CacheManager();

		SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

		Date from = format.parse("2014 03 03 10:00:00");
		Date to = format.parse("2014 05 05 23:00:00");

		System.out.println("QUERY");
		List<CacheDataDto> results = manager.queryCache(from, to);

		for (CacheDataDto result : results) {
			System.out.println(result.getId() + ", " + format.format(result.getSomeDate()) + ", "
					+ result.getDescription());
		}

		/**
		 * Performing a refresh currently results in lucene exceptions being
		 * thrown due to indexing problems. See the document describing this POC
		 * for details
		 */
		System.out.println("Perform refresh");
		manager.refresh();
		System.out.println("Refresh done");

		results = manager.queryCache(from, to);

		for (CacheDataDto result : results) {
			System.out.println(result.getId() + ", " + format.format(result.getSomeDate()) + ", "
					+ result.getDescription());
		}

		/**
		 * Destroy the caches to ensure that the application will exit
		 */
		manager.destroy();
	}
}