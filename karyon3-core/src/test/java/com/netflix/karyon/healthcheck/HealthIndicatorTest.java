package com.netflix.karyon.healthcheck;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.netflix.governator.Governator;
import com.netflix.karyon.HealthCheck;
import com.netflix.karyon.HealthCheckStatus;
import com.netflix.karyon.LifecycleState;

public class HealthIndicatorTest {
    @Test
    public void failureOnNoDefinedHealthCheck() {
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(LifecycleState.Running, hc.check().join().getState());
        Assert.assertEquals(0, status.getIndicators().size());
    }
    
    @Test
    public void successWithSingleHealthCheck() {
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(LifecycleState.Running, hc.check().join().getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }
    
    @Test
    public void successWithSingleAndNamedHealthCheck() {
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(LifecycleState.Failed, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
    }
    
    @Test
    public void successDefaultCompositeWithSingleNamedHealthCheck() {
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
            }
        });
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(LifecycleState.Failed, status.getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }
    
    @Test
    public void successDefaultCompositeWithMultipleNamedHealthCheck() {
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("bar")).toInstance(HealthIndicators.alwaysUnhealthy("bar"));
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(LifecycleState.Failed, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
    }
}
