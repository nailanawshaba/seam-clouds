package org.jboss.seam.infinispan;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.infinispan.config.Configuration;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.seam.infinispan.InfinispanExtension.ConfigurationHolder;
import org.jboss.seam.infinispan.event.cachemanager.CacheManagerEventBridge;

/**
 * <p>
 * Manages the CacheContainer, by default creating a {@link DefaultCacheManager}
 * using configuration defaults.
 * </p>
 * 
 * <p>
 * If you want to use a different {@link CacheContainer} implementation or
 * configuration with your caches, then you can specialize this class.
 * {@link #defineScannedConfigurations()} should be called if you want scanned
 * configurations and Infinispan notification to be bridged to the CDI event
 * bus.
 * </p>
 * 
 * <p>
 * If you want to use a different {@link CacheContainer} for an individual
 * cache, provide a bean that has the type {@link CacheContainer} and the
 * qualifiers of the cache.
 * </p>
 * 
 * @author Pete Muir
 * 
 */
@ApplicationScoped
public class CacheContainerManager
{

   /**
    * Registers scanned configurations (if not already in existence) with
    * Infinispan
    * 
    * @param cacheContainer the {@link EmbeddedCacheManager} with which to
    *           register the configurations
    * @param extension the {@link InfinispanExtension} instance for this module
    * @param beanManager the beanManager for this module
    */
   protected static EmbeddedCacheManager defineScannedConfigurations(EmbeddedCacheManager cacheContainer, InfinispanExtension extension, BeanManager beanManager)
   {
      CreationalContext<Configuration> ctx = beanManager.createCreationalContext(null);
      for (ConfigurationHolder configurationHolder : extension.getConfigurations())
      {
         Configuration configuration = configurationHolder.getProducer().produce(ctx);
         if (!cacheContainer.getCacheNames().contains(configurationHolder.getName()))
         {
            cacheContainer.defineConfiguration(configurationHolder.getName(), configuration);
         }
      }
      return cacheContainer;
   }

   /**
    * Sets up Infinispan notification to CDI Event Bus bridging.
    * 
    * @param cacheContainer the {@link EmbeddedCacheManager} with which to
    *           register the observers
    * @param extension the {@link InfinispanExtension} instance for this module
    * @param eventBridge the {@link CacheManagerEventBridge} instance for this
    *           module
    */
   protected static EmbeddedCacheManager registerObservers(EmbeddedCacheManager cacheContainer, InfinispanExtension extension, CacheManagerEventBridge eventBridge)
   {
      for (ConfigurationHolder configurationHolder : extension.getConfigurations())
      {
         // Register any listeners
         eventBridge.registerObservers(configurationHolder.getQualifiers(), configurationHolder.getName(), cacheContainer);
      }
      return cacheContainer;
   }

   private final CacheContainer cacheContainer;

   /**
    *  Constructor for proxies
    */
   protected CacheContainerManager()
   {
      this.cacheContainer = null;
   }

   /**
    * Instantiate a new {@link CacheContainerManager} instance. Normally called
    * by the CDI container or a specializing class.
    * 
    * @param extension the {@link InfinispanExtension}
    * @param beanManager the {@link BeanManager}
    * @param eventBridge the {@link CacheManagerEventBridge}
    */
   @Inject
   public CacheContainerManager(InfinispanExtension extension, BeanManager beanManager, CacheManagerEventBridge eventBridge)
   {
      this(registerObservers(defineScannedConfigurations(new DefaultCacheManager(), extension, beanManager), extension, eventBridge));
   }

   /**
    * Instantiate a new cache container.
    * 
    * @param cacheContainer the cache container to expose.
    */
   protected CacheContainerManager(CacheContainer cacheContainer)
   {
      this.cacheContainer = cacheContainer;
   }

   @Produces
   public CacheContainer getCacheContainer()
   {
      return cacheContainer;
   }

   @PreDestroy
   void cleanup()
   {
      cacheContainer.stop();
   }

}
