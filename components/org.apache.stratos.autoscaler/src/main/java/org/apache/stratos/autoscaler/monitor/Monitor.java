/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.autoscaler.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.AutoscalerContext;
import org.apache.stratos.autoscaler.exception.PartitionValidationException;
import org.apache.stratos.autoscaler.exception.PolicyValidationException;
import org.apache.stratos.autoscaler.grouping.DependencyBuilder;
import org.apache.stratos.autoscaler.monitor.cluster.ClusterMonitor;
import org.apache.stratos.autoscaler.monitor.cluster.LbClusterMonitor;
import org.apache.stratos.autoscaler.monitor.group.GroupMonitor;
import org.apache.stratos.autoscaler.util.AutoscalerUtil;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.Group;
import org.apache.stratos.messaging.domain.topology.ParentBehavior;
import org.apache.stratos.messaging.domain.topology.Status;
import org.apache.stratos.messaging.event.Event;

import java.util.*;

/**
 * Monitor is to monitor it's child monitors and
 * control them according to the dependencies respectively.
 */
public abstract class Monitor extends Observable implements Observer {

    private static final Log log = LogFactory.getLog(Monitor.class);

    protected String id;

    protected Map<String, GroupMonitor> groupMonitors;
    protected Map<String, AbstractClusterMonitor> abstractClusterMonitors;

    protected Queue<String> preOrderTraverse = new LinkedList<String>();

    protected ParentBehavior component;

    protected Status status;

    public Monitor(ParentBehavior component) {
        groupMonitors = new HashMap<String, GroupMonitor>();
        abstractClusterMonitors = new HashMap<String, AbstractClusterMonitor>();
        this.component = component;
        preOrderTraverse = DependencyBuilder.getStartupOrder(component);

        //startDependency();
    }

    protected abstract void startDependency();


    public Map<String, GroupMonitor> getGroupMonitors() {
        return groupMonitors;
    }

    public void setGroupMonitors(Map<String, GroupMonitor> groupMonitors) {
        this.groupMonitors = groupMonitors;
    }

    public Map<String, AbstractClusterMonitor> getAbstractClusterMonitors() {
        return abstractClusterMonitors;
    }

    public void addAbstractMonitor(AbstractClusterMonitor monitor) {
       this.abstractClusterMonitors.put(monitor.getClusterId(), monitor);
    }

    public AbstractClusterMonitor getAbstractMonitor(String clusterId) {
        return this.abstractClusterMonitors.get(clusterId);
    }

    public void setAbstractClusterMonitors(Map<String, AbstractClusterMonitor> abstractClusterMonitors) {
        this.abstractClusterMonitors = abstractClusterMonitors;
    }

    public abstract void monitor();

    /**
     * Triggered when an event is received.
     * @param event
     */
    protected abstract void onEvent(Event event);

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    protected synchronized void startClusterMonitor(Monitor parent, Cluster cluster) {
        Thread th = null;
        if (cluster.isLbCluster()
                && !this.abstractClusterMonitors.containsKey(cluster.getClusterId())) {
            th = new Thread(new LBClusterMonitorAdder(
                    cluster));
        } else if (!cluster.isLbCluster() && !this.abstractClusterMonitors.containsKey(cluster.getClusterId())) {
            th = new Thread(
                    new ClusterMonitorAdder(parent, cluster));
            if (log.isDebugEnabled()) {
                log.debug(String
                        .format("Cluster monitor Adder has been added: [cluster] %s ",
                                cluster.getClusterId()));
            }
        }
        if (th != null) {
            th.start();
            /*try {
                th.join();
            } catch (InterruptedException ignore) {
            }*/

                log.info(String
                        .format("Cluster monitor thread has been started successfully: [cluster] %s ",
                                cluster.getClusterId()));
        }
    }

    protected synchronized void startGroupMonitor(Monitor parent, String dependency, ParentBehavior component) {
        Thread th = null;
        if (!this.groupMonitors.containsKey(dependency)) {
            if (log.isDebugEnabled()) {
                log.debug(String
                        .format("Group monitor Adder has been added: [group] %s ",
                                dependency));
            }
            th = new Thread(
                    new GroupMonitorAdder(parent, dependency, component));
        }

        if (th != null) {
            th.start();
            /*try {
                th.join();
            } catch (InterruptedException ignore) {
            }*/

                log.info(String
                        .format("Group monitor thread has been started successfully: [group] %s ",
                                dependency));
        }
    }

    public Status getStatus() {
        return status;
    }

    private class ClusterMonitorAdder implements Runnable {
        private Cluster cluster;
        private Monitor parent;

        public ClusterMonitorAdder(Monitor parent, Cluster cluster) {
            this.parent = parent;
            this.cluster = cluster;
        }

        public void run() {
            ClusterMonitor monitor = null;
            int retries = 5;
            boolean success = false;
            do {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
                try {
                    if(log.isDebugEnabled()) {
                        log.debug("CLuster monitor is going to be started for [cluster] "
                                + cluster.getClusterId());
                    }
                    monitor = AutoscalerUtil.getClusterMonitor(cluster);
                    monitor.addObserver(parent);
                    success = true;
                    //TODO start the status checker
                } catch (PolicyValidationException e) {
                    String msg = "Cluster monitor creation failed for cluster: " + cluster.getClusterId();
                    log.warn(msg, e);
                    retries--;


                } catch (PartitionValidationException e) {
                    String msg = "Cluster monitor creation failed for cluster: " + cluster.getClusterId();
                    log.warn(msg, e);
                    retries--;

                }

            } while (!success && retries != 0);


                    if (monitor == null) {
                String msg = "Cluster monitor creation failed, even after retrying for 5 times, "
                        + "for cluster: " + cluster.getClusterId();
                log.error(msg);
                //TODO parent.notify();
                throw new RuntimeException(msg);
            }

            Thread th = new Thread(monitor);
            th.start();

            AutoscalerContext.getInstance().addMonitor(monitor);
            abstractClusterMonitors.put(cluster.getClusterId(), monitor);
            if (log.isInfoEnabled()) {
                log.info(String.format("Cluster monitor has been added successfully: [cluster] %s",
                        cluster.getClusterId()));
            }
        }
    }

    private class GroupMonitorAdder implements Runnable {
        private Group group;
        private String dependency;
        private Monitor parent;

        public GroupMonitorAdder(Monitor parent, String dependency, ParentBehavior group) {
            this.group = (Group)group;
            this.dependency = dependency;
            this.parent = parent;
        }

        public void run() {
            GroupMonitor monitor = null;
            int retries = 5;
            boolean success = false;
             do {
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException e1) {
                 }

                try {
                    if(log.isDebugEnabled()) {
                        log.debug("Group monitor is going to be started for [group] "
                                + group.getAlias());
                    }
                    monitor = AutoscalerUtil.getGroupMonitor(group.getGroup(dependency));
                    monitor.addObserver(parent);
                    success = true;

                } catch (Exception e) {
                    String msg = "Group monitor creation failed for group: " + dependency;
                    log.warn(msg, e);
                    retries--;


                }
            } while (!success && retries != 0);

            if (monitor == null) {
                String msg = "Group monitor creation failed, even after retrying for 5 times, "
                        + "for group: " + dependency;
                log.error(msg);
                //TODO parent.notify();
                throw new RuntimeException(msg);
            }

            groupMonitors.put(dependency, monitor);
            parent.addObserver(monitor);

            if (log.isInfoEnabled()) {
                log.info(String.format("Group monitor has been added successfully: [group] %s",
                        dependency));
            }
        }
    }

    private class LBClusterMonitorAdder implements Runnable {
        private Cluster cluster;

        public LBClusterMonitorAdder(Cluster cluster) {
            this.cluster = cluster;
        }

        public void run() {
            LbClusterMonitor monitor = null;
            int retries = 5;
            boolean success = false;
            do {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
                try {
                    monitor = AutoscalerUtil.getLBClusterMonitor(cluster);
                    success = true;

                } catch (PolicyValidationException e) {
                    String msg = "LB Cluster monitor creation failed for cluster: " + cluster.getClusterId();
                    log.warn(msg, e);
                    retries--;

                } catch (PartitionValidationException e) {
                    String msg = "LB Cluster monitor creation failed for cluster: " + cluster.getClusterId();
                    log.warn(msg, e);
                    retries--;
                }
            } while (!success && retries <= 0);

            if (monitor == null) {
                String msg = "LB Cluster monitor creation failed, even after retrying for 5 times, "
                        + "for cluster: " + cluster.getClusterId();
                log.error(msg);
                throw new RuntimeException(msg);
            }

            Thread th = new Thread(monitor);
            th.start();
            AutoscalerContext.getInstance().addLbMonitor(monitor);
            abstractClusterMonitors.put(cluster.getClusterId(), monitor);
            if (log.isInfoEnabled()) {
                log.info(String.format("LB Cluster monitor has been added successfully: [cluster] %s",
                        cluster.getClusterId()));
            }
        }
    }


}
