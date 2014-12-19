package infinispan_example.data_cache;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This represents a business service which is used to obtain the data which will reside in the cache
 * 
 */
public class BusinessDataService {
	public List<CacheDataDto> findDataToCache() {
		List<CacheDataDto> result = new ArrayList<CacheDataDto>();

		DateFormat f = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

		try {
			result.add(new CacheDataDto(1, f.parse("2014 01 01 10:11:12"), "Cache data 1"));
			result.add(new CacheDataDto(2, f.parse("2014 02 02 10:11:12"), "Cache data 2"));
			result.add(new CacheDataDto(3, f.parse("2014 03 03 10:11:12"), "Cache data 3"));
			result.add(new CacheDataDto(4, f.parse("2014 04 04 10:11:12"), "Cache data 4"));
			result.add(new CacheDataDto(5, f.parse("2014 05 05 10:11:12"), "Cache data 5"));
			result.add(new CacheDataDto(6, f.parse("2014 06 06 10:11:12"), "Cache data 6"));
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		return result;
	}
}
