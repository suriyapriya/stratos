/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.docker.compute.options;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.scriptbuilder.domain.Statement;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains options supported in the {@code ComputeService#runNode} operation on the
 * "docker" provider. <h2>Usage</h2> The recommended way to instantiate a
 * DockerTemplateOptions object is to statically import DockerTemplateOptions.* and invoke a static
 * creation method followed by an instance mutator (if needed):
 * <p/>
 * <code>
 * import static org.jclouds.docker.compute.options.DockerTemplateOptions.Builder.*;
 * <p/>
 * ComputeService api = // get connection
 * templateBuilder.options(inboundPorts(22, 80, 8080, 443));
 * Set<? extends NodeMetadata> set = api.createNodesInGroup(tag, 2, templateBuilder.build());
 * <code>
 */
public class DockerTemplateOptions extends TemplateOptions implements Cloneable {

   protected Optional<String> dns = Optional.absent();
   protected Optional<String> hostname = Optional.absent();
   protected Optional<Integer> memory = Optional.absent();
   protected Optional<Integer> cpuShares = Optional.absent();
   protected Optional<List<String>> commands = Optional.absent();
   protected Optional<Map<String, String>> volumes = Optional.absent();

   @Override
   public DockerTemplateOptions clone() {
      DockerTemplateOptions options = new DockerTemplateOptions();
      copyTo(options);
      return options;
   }

   @Override
   public void copyTo(TemplateOptions to) {
      super.copyTo(to);
      if (to instanceof DockerTemplateOptions) {
         DockerTemplateOptions eTo = DockerTemplateOptions.class.cast(to);
         if (volumes.isPresent()) {
            eTo.volumes(getVolumes().get());
         }
         if (hostname.isPresent()) {
            eTo.hostname(hostname.get());
         }
         if (dns.isPresent()) {
            eTo.dns(dns.get());
         }
         if (memory.isPresent()) {
            eTo.memory(memory.get());
         }
         if (commands.isPresent()) {
            eTo.commands(commands.get());
         }
         if (cpuShares.isPresent()) {
            eTo.cpuShares(cpuShares.get());
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      DockerTemplateOptions that = DockerTemplateOptions.class.cast(o);
      return super.equals(that) && equal(this.volumes, that.volumes) &&
              equal(this.hostname, that.hostname) &&
              equal(this.dns, that.dns) &&
              equal(this.memory, that.memory) &&
              equal(this.commands, that.commands) &&
              equal(this.cpuShares, that.cpuShares);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(super.hashCode(), volumes, hostname, dns, memory, commands, cpuShares);
   }

   @Override
   public String toString() {
      return Objects.toStringHelper(this)
              .add("dns", dns)
              .add("hostname", hostname)
              .add("memory", memory)
              .add("cpuShares", cpuShares)
              .add("commands", commands)
              .add("volumes", volumes)
              .toString();
   }

   public static final DockerTemplateOptions NONE = new DockerTemplateOptions();

   public DockerTemplateOptions volumes(Map<String, String> volumes) {
      this.volumes = Optional.<Map<String, String>> of(ImmutableMap.copyOf(volumes));
      return this;
   }

   public TemplateOptions dns(@Nullable String dns) {
      this.dns = Optional.fromNullable(dns);
      return this;
   }

   public TemplateOptions hostname(@Nullable String hostname) {
      this.hostname = Optional.fromNullable(hostname);
      return this;
   }

   public TemplateOptions memory(@Nullable Integer memory) {
      this.memory = Optional.fromNullable(memory);
      return this;
   }

   public TemplateOptions commands(Iterable<String> commands) {
      for (String command : checkNotNull(commands, "commands"))
         checkNotNull(command, "all commands must be non-empty");
      this.commands = Optional.<List<String>> of(ImmutableList.copyOf(commands));
      return this;
   }

   public TemplateOptions commands(String... commands) {
      return commands(ImmutableList.copyOf(checkNotNull(commands, "commands")));
   }

   public TemplateOptions cpuShares(@Nullable Integer cpuShares) {
      this.cpuShares = Optional.fromNullable(cpuShares);
      return this;
   }

   public Optional<Map<String, String>> getVolumes() {
      return volumes;
   }

   public Optional<String> getDns() { return dns; }

   public Optional<String> getHostname() { return hostname; }

   public Optional<Integer> getMemory() { return memory; }

   public Optional<List<String>> getCommands() {
      return commands;
   }

   public Optional<Integer> getCpuShares() { return cpuShares; }

   public static class Builder {

      /**
       * @see DockerTemplateOptions#volumes(java.util.Map)
       */
      public static DockerTemplateOptions volumes(Map<String, String> volumes) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.volumes(volumes));
      }

      /**
       * @see DockerTemplateOptions#dns(String)
       */
      public static DockerTemplateOptions dns(String dns) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.dns(dns));
      }

      /**
       * @see DockerTemplateOptions#hostname(String)
       */
      public static DockerTemplateOptions hostname(String hostname) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.hostname(hostname));
      }

      /**
       * @see DockerTemplateOptions#memory(int)
       */
      public static DockerTemplateOptions memory(int memory) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.memory(memory));
      }

      /**
       * @see DockerTemplateOptions#commands(Iterable)
       */
      public static DockerTemplateOptions commands(String... commands) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.commands(commands));
      }

      public static DockerTemplateOptions commands(Iterable<String> commands) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.commands(commands));
      }

      /**
       * @see DockerTemplateOptions#cpuShares(int)
       */
      public static DockerTemplateOptions cpuShares(int cpuShares) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.cpuShares(cpuShares));
      }

      // methods that only facilitate returning the correct object type

      /**
       * @see TemplateOptions#inboundPorts
       */
      public static DockerTemplateOptions inboundPorts(int... ports) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.inboundPorts(ports));
      }

      /**
       * @see TemplateOptions#port
       */
      public static DockerTemplateOptions blockOnPort(int port, int seconds) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.blockOnPort(port, seconds));
      }

      /**
       * @see TemplateOptions#installPrivateKey
       */
      public static DockerTemplateOptions installPrivateKey(String rsaKey) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.installPrivateKey(rsaKey));
      }

      /**
       * @see TemplateOptions#authorizePublicKey
       */
      public static DockerTemplateOptions authorizePublicKey(String rsaKey) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.authorizePublicKey(rsaKey));
      }

      /**
       * @see TemplateOptions#userMetadata
       */
      public static DockerTemplateOptions userMetadata(Map<String, String> userMetadata) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.userMetadata(userMetadata));
      }

      /**
       * @see TemplateOptions#nodeNames(Iterable)
       */
      public static DockerTemplateOptions nodeNames(Iterable<String> nodeNames) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.nodeNames(nodeNames));
      }

      /**
       * @see TemplateOptions#networks(Iterable)
       */
      public static DockerTemplateOptions networks(Iterable<String> networks) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return DockerTemplateOptions.class.cast(options.networks(networks));
      }

      /**
       * @see TemplateOptions#overrideLoginUser
       */
      public static DockerTemplateOptions overrideLoginUser(String user) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return options.overrideLoginUser(user);
      }

      /**
       * @see TemplateOptions#overrideLoginPassword
       */
      public static DockerTemplateOptions overrideLoginPassword(String password) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return options.overrideLoginPassword(password);
      }

      /**
       * @see TemplateOptions#overrideLoginPrivateKey
       */
      public static DockerTemplateOptions overrideLoginPrivateKey(String privateKey) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return options.overrideLoginPrivateKey(privateKey);
      }

      /**
       * @see TemplateOptions#overrideAuthenticateSudo
       */
      public static DockerTemplateOptions overrideAuthenticateSudo(boolean authenticateSudo) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return options.overrideAuthenticateSudo(authenticateSudo);
      }

      /**
       * @see TemplateOptions#overrideLoginCredentials
       */
      public static DockerTemplateOptions overrideLoginCredentials(LoginCredentials credentials) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return options.overrideLoginCredentials(credentials);
      }

      /**
       * @see TemplateOptions#blockUntilRunning
       */
      public static DockerTemplateOptions blockUntilRunning(boolean blockUntilRunning) {
         DockerTemplateOptions options = new DockerTemplateOptions();
         return options.blockUntilRunning(blockUntilRunning);
      }

   }

   // methods that only facilitate returning the correct object type

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions blockOnPort(int port, int seconds) {
      return DockerTemplateOptions.class.cast(super.blockOnPort(port, seconds));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions inboundPorts(int... ports) {
      return DockerTemplateOptions.class.cast(super.inboundPorts(ports));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions authorizePublicKey(String publicKey) {
      return DockerTemplateOptions.class.cast(super.authorizePublicKey(publicKey));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions installPrivateKey(String privateKey) {
      return DockerTemplateOptions.class.cast(super.installPrivateKey(privateKey));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions blockUntilRunning(boolean blockUntilRunning) {
      return DockerTemplateOptions.class.cast(super.blockUntilRunning(blockUntilRunning));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions dontAuthorizePublicKey() {
      return DockerTemplateOptions.class.cast(super.dontAuthorizePublicKey());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions nameTask(String name) {
      return DockerTemplateOptions.class.cast(super.nameTask(name));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions runAsRoot(boolean runAsRoot) {
      return DockerTemplateOptions.class.cast(super.runAsRoot(runAsRoot));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions runScript(Statement script) {
      return DockerTemplateOptions.class.cast(super.runScript(script));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions overrideLoginCredentials(LoginCredentials overridingCredentials) {
      return DockerTemplateOptions.class.cast(super.overrideLoginCredentials(overridingCredentials));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions overrideLoginPassword(String password) {
      return DockerTemplateOptions.class.cast(super.overrideLoginPassword(password));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions overrideLoginPrivateKey(String privateKey) {
      return DockerTemplateOptions.class.cast(super.overrideLoginPrivateKey(privateKey));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions overrideLoginUser(String loginUser) {
      return DockerTemplateOptions.class.cast(super.overrideLoginUser(loginUser));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions overrideAuthenticateSudo(boolean authenticateSudo) {
      return DockerTemplateOptions.class.cast(super.overrideAuthenticateSudo(authenticateSudo));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions userMetadata(Map<String, String> userMetadata) {
      return DockerTemplateOptions.class.cast(super.userMetadata(userMetadata));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions userMetadata(String key, String value) {
      return DockerTemplateOptions.class.cast(super.userMetadata(key, value));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions nodeNames(Iterable<String> nodeNames) {
      return DockerTemplateOptions.class.cast(super.nodeNames(nodeNames));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DockerTemplateOptions networks(Iterable<String> networks) {
      return DockerTemplateOptions.class.cast(super.networks(networks));
   }

}
