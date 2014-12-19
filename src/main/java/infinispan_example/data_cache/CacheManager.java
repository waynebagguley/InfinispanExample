package infinispan_example.data_cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

public class CacheManager {
	private BusinessDataService service = new BusinessDataService();

	private volatile CacheEnum currentCache = null;
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	/*
	 * CacheManager is heavyweight so it needs to be retained across service
	 * calls
	 */
	private DefaultCacheManager manager;

	private enum CacheEnum {
		CACHE1("dataCache-1"), CACHE2("dataCache-2");

		private String name;

		CacheEnum(String name) {
			this.name = name;
		}
	}

	public CacheManager() {
		// Create the cache manager
		manager = new DefaultCacheManager(new ConfigurationBuilder().eviction().strategy(EvictionStrategy.NONE)
				.maxEntries(100).indexing().enable().build());

		// Obtain the data from the business service
		// This would be initiated on startup
		List<CacheDataDto> data = obtainCacheData();
		populateCache(CacheEnum.CACHE1, data);
		currentCache = CacheEnum.CACHE1;
	}

	private void populateCache(CacheEnum cache, List<CacheDataDto> data) {
		Cache<Object, Object> cacheObject = manager.getCache(cache.name);

		// Add the data to the cache
		for (CacheDataDto dto : data) {
			cacheObject.put(dto.getId(), dto);
		}
	}

	private void clearCache(CacheEnum cache) {
		Cache<Object, Object> cacheObject = manager.getCache(cache.name);
		cacheObject.clear();
	}

	private CacheEnum nextCache(CacheEnum currentCache) {
		CacheEnum result = (currentCache == CacheEnum.CACHE1) ? CacheEnum.CACHE2 : CacheEnum.CACHE1;
		return result;
	}

	private List<CacheDataDto> obtainCacheData() {
		return service.findDataToCache();
	}

	// Perform query
	public List<CacheDataDto> queryCache(Date from, Date to) {
		List<CacheDataDto> result = Collections.emptyList();

		/*
		 * Obtain the read lock. Many threads can obtain a read-lock
		 * simultaneously as long as there is no write-lock in place. A
		 * write-lock is used when refreshing the cache.
		 */
		lock.readLock().lock();
		try {
			Cache<Object, Object> cache = manager.getCache(currentCache.name);

			SearchManager searchManager = Search.getSearchManager(cache);
			QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(CacheDataDto.class).get();
			Query luceneQuery = queryBuilder.range().onField("someDate").from(from).to(to).createQuery();

			CacheQuery cacheQuery = searchManager.getQuery(luceneQuery, CacheDataDto.class);
			result = new ArrayList<CacheDataDto>();
			for (Object o : cacheQuery.list()) {
				result.add((CacheDataDto) o);
			}

		} finally {
			lock.readLock().unlock();
		}

		return result;
	}

	/*
	 * Reload the data using the service, populate a new cache and empty the old
	 * one
	 */
	public synchronized void refresh() {
		List<CacheDataDto> data = obtainCacheData();
		CacheEnum nextCache = nextCache(currentCache);
		populateCache(nextCache, data);
		CacheEnum oldCache = currentCache;

		/*
		 * Need to protect this using the lock. After this, any new queries will
		 * use the new cache that has just been populated.
		 */
		lock.writeLock().lock();
		try {
			currentCache = nextCache;
		} finally {
			lock.writeLock().unlock();
		}

		/*
		 * The old cache can now be cleared This is an optional step but is a
		 * memory optimisation.
		 */
		clearCache(oldCache);
	}

	public void destroy() {
		manager.removeCache(CacheEnum.CACHE1.name);
		manager.removeCache(CacheEnum.CACHE2.name);
	}

}
