package org.jboss.seam.infinispan.event.cachemanager;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.infinispan.notifications.Listenable;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStartedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStoppedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.Event;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.jboss.seam.infinispan.event.AbstractEventBridge;

public class CacheManagerEventBridge extends AbstractEventBridge<Event>
{

   public void registerObservers(Set<Annotation> qualifierSet, String cacheName, Listenable listenable)
   {
      Annotation[] qualifiers = qualifierSet.toArray(new Annotation[qualifierSet.size()]);
      if (hasObservers(CacheStartedAdapter.EMTPTY, qualifiers))
      {
         listenable.addListener(new CacheStartedAdapter(getBaseEvent().select(CacheStartedEvent.class, qualifiers), cacheName));
      }
      if (hasObservers(CacheStoppedAdapter.EMTPTY, qualifiers))
      {
         listenable.addListener(new CacheStoppedAdapter(getBaseEvent().select(CacheStoppedEvent.class, qualifiers), cacheName));
      }
      if (hasObservers(ViewChangedAdapter.EMTPTY, qualifiers))
      {
         listenable.addListener(new ViewChangedAdapter(getBaseEvent().select(ViewChangedEvent.class, qualifiers)));
      }
   }

}
