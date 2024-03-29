
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Dockerfile for installing the necessary dependencies for building Hadoop.
# See BUILDING.txt.


FROM ubuntu:trusty

WORKDIR /root

ENV DEBIAN_FRONTEND noninteractive
ENV DEBCONF_TERSE true

######
# Install common dependencies from packages
#
# WARNING: DO NOT PUT JAVA APPS HERE! Otherwise they will install default
# Ubuntu Java.  See Java section below!
######
RUN apt-get -q update && apt-get -q install --no-install-recommends -y \
    build-essential \
    bzip2 \
    cmake \
    curl \
    doxygen \
    fuse \
    g++ \
    gcc \
    git \
    gnupg-agent \
    make \
    libbz2-dev \
    libcurl4-openssl-dev \
    libfuse-dev \
    libperl-critic-perl \
    libprotobuf-dev \
    libprotoc-dev \
    libsnappy-dev \
    libssl-dev \
    libtool \
    pinentry-curses \
    pkg-config \
    protobuf-compiler \
    protobuf-c-compiler \
    python \
    python2.7 \
    python-pip \
    rsync \
    snappy \
    xz-utils \
    zlib1g-dev \
    wget

####
# Apps that require Java.
# Maven and ant depend on ubuntu trusty's headless jdk7. The install of
# maven and ant will pull down this jdk even though we don't want it.
# Do the maven and ant install here rather than later where the jdk7
# will overwrite the jdk7 we actually want to use. See next section on jdks.
###
RUN apt-get -q update && apt-get -q install --no-install-recommends -y \
    ant \
    maven

#######
# Install jdk7 and jdk8.
#######
# The jdks in ubuntu trusty don't work. HDFS hangs on openjdk-7 151.
# See HBASE-19204. So, we use the azul jdks because they are available, and
# later versions of openjdk (openjdk-7 161). Below we add the azul repo and
# then install its jdks. We then move aside the headless jdk7 added above
# when we added maven and ant
RUN echo "dot_style = mega" > "/root/.wgetrc"
RUN echo "quiet = on" >> "/root/.wgetrc"
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0x219BD9C9
RUN apt-get -q update && apt-get -q install --no-install-recommends -y software-properties-common python-software-properties
RUN apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main'
RUN apt-get -q update
RUN apt-get -q install --no-install-recommends -y zulu-8 zulu-7
RUN update-alternatives --config java
RUN update-alternatives --config javac
RUN mv /usr/lib/jvm/java-7-openjdk-amd64 /usr/lib/jvm/moved.java-7-openjdk-amd64
ENV JAVA_HOME /usr/lib/jvm/zulu-7-amd64

# Fixing the Apache commons / Maven dependency problem under Ubuntu:
# See http://wiki.apache.org/commons/VfsProblems
RUN cd /usr/share/maven/lib && ln -s ../../java/commons-lang.jar .

######
# Install findbugs
######
RUN mkdir -p /opt/findbugs && \
    curl -L -s -S \
         https://sourceforge.net/projects/findbugs/files/findbugs/3.0.1/findbugs-noUpdateChecks-3.0.1.tar.gz/download \
         -o /opt/findbugs.tar.gz && \
    tar xzf /opt/findbugs.tar.gz --strip-components 1 -C /opt/findbugs
ENV FINDBUGS_HOME /opt/findbugs

####
# Install pylint
####
RUN pip install pylint==1.9.2

####
# Install dateutil.parser
####
RUN pip install python-dateutil

####
# Install Ruby 2, based on Yetus 0.4.0 dockerfile
###
RUN echo 'gem: --no-rdoc --no-ri' >> /root/.gemrc
RUN apt-add-repository ppa:brightbox/ruby-ng
RUN apt-get -q update

RUN apt-get -q install --no-install-recommends -y ruby2.3 ruby2.3-dev ruby-switch
RUN ruby-switch --set ruby2.3

####
# Install rubocop
###
RUN gem install rake
RUN gem install rubocop:'<= 0.81'

####
# Install ruby-lint
###
RUN gem install ruby-lint

####
# Install shellcheck
#
# Include workaround for static linking bug
# https://github.com/koalaman/shellcheck/issues/1053
###
RUN mkdir -p /opt/shellcheck && \
    curl -L -s -S \
        https://storage.googleapis.com/shellcheck/shellcheck-stable.linux.x86_64.tar.xz \
        -o /opt/shellcheck.tar.xz && \
    tar xJf /opt/shellcheck.tar.xz --strip-components 1 -C /opt/shellcheck && \
    touch /tmp/libc.so.6 && \
    echo '#!/bin/bash\n\
LD_LIBRARY_PATH=/tmp /opt/shellcheck/shellcheck $@'\
> /usr/bin/shellcheck && \
    chmod +x /usr/bin/shellcheck && \
    rm -f /opt/shellcheck.tar.xz

###
# Avoid out of memory errors in builds
###
ENV MAVEN_OPTS -Xmx3g

###
# Everything past this point is either not needed for testing or breaks Yetus.
# So tell Yetus not to read the rest of the file:
# YETUS CUT HERE
###
